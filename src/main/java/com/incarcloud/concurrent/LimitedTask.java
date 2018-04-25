package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限制最大并发数的排队任务。
 * 目的是保护相关联的部分不至于过载，软件系统往往存在最在容量/性能限制，如果
 * 超过限制，性能往往会急剧下降。
 */
abstract class LimitedTask {
    private Logger s_logger = LoggerFactory.getLogger(LimitedTask.class);

    // 最大并发数,可以在运行时动态调整
    private int _max = 2;
    // 停止标志
    private boolean _bCanStop = false;
    // 需要释放线程池
    private final boolean _bNeedShutdown;

    // 当前并发数
    private final AtomicInteger _atomOnWorking = new AtomicInteger();

    // 任务队列
    private final ConcurrentLinkedQueue<Object> _queueTask = new ConcurrentLinkedQueue<>();
    // 任务队列计数，派生类的任务队列由Queue构成,size()会遍历整个队列，性能较低
    private final AtomicInteger _queueTaskCount = new AtomicInteger();
    // 任务跟踪
    private final ConcurrentHashMap<Object, TaskTracking> _mapOnWaiting = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, TaskTracking> _mapOnWorking = new ConcurrentHashMap<>();
    // 线程池
    private final ExecutorService _execSrv;
    // 性能计数器
    private final PerfCount _perf = new PerfCount();

    /**
     * 构造器
     */
    protected LimitedTask(){
        _execSrv = Executors.newCachedThreadPool();
        _bNeedShutdown = true;
    }

    /**
     * 构造器
     * @param execSrv 外部传入的线程池
     */
    protected LimitedTask(ExecutorService execSrv){
        _execSrv = execSrv;
        _bNeedShutdown = false;
    }

    /**
     * 当前最大并发数
     * @return 当前最大并发数
     */
    public int getMax(){ return _max; }

    /**
     * 设置当前最大并发数,可以在运行时动态调整,
     * 若设置为0,将没有任务会开始执行
     * @param val 最大并发数
     */
    public void setMax(int val){
        _max = val;
        dispatch();
    }

    /**
     * 停止。
     * 已经开始执行的任务会继续执行，尚处于排队中的任务不会再被执行
     * 此方法立刻返回，不等待正在执行中的任务执行完成。
     */
    public void stop(){
        _bCanStop = true;
        if(_bNeedShutdown){
            _execSrv.shutdown();
        }
    }

    // 提交任务
    protected void submit(Object task){
        if(task == null) throw new NullPointerException("task");

        // 一定要先准备跟踪对象
        _mapOnWaiting.put(task, new TaskTracking(task));
        _queueTask.add(task);
        _queueTaskCount.incrementAndGet();
        dispatch();
    }

    /**
     * 正在执行中的任务数量
     * @return 正在执行中的任务数量
     */
    public int getRunning(){ return _atomOnWorking.get(); }

    /**
     * 正在等待执行任务的数量
     * @return 正在等待执行任务的数量
     */
    public int getWaiting(){ return _queueTaskCount.get(); }

    /**
     * 扫描长时间运行的任务
     * 不宜过于频繁扫描
     * @param msMax 毫秒，执行时间超过的任务会被扫描出来
     * @return 有可能返回null
     */
    public List<TaskTracking> scanForLongTimeTask(long msMax){
        List<TaskTracking> listTracking = null;
        long tmNow = System.currentTimeMillis();

        for(TaskTracking tracking:_mapOnWorking.values()){
            if(tmNow - tracking.getExecTM() > msMax){
                if(listTracking == null) listTracking = new ArrayList<>();
                listTracking.add(tracking);
            }
        }

        return listTracking;
    }

    /**
     * 查询性能.两次调用之间的数据进行累积求平均，调用越频繁，瞬时性
     * 越高，但精确性越差；调用越缓慢，瞬时性越差，但精确性越高
     * @return 每秒执行多少个任务(Hz)
     */
    public float queryPerf(){
        return _perf.calcPerfAndReset();
    }
    public PerfMetric<Long> queryPerfMetric(){ return _perf.calcPerfMetric(); }

    // 分配任务到线程，返回当前的并发数
    private int dispatch() {
        int nRetry = 0;
        int nOnWorking = _atomOnWorking.get();

        // 已经指示退出,不要再提交新任务了
        if(_bCanStop) return nOnWorking;

        while(nOnWorking < _max){
            if(_atomOnWorking.compareAndSet(nOnWorking, nOnWorking+1)){
                // 成功保留了并发容量,尝试启动任务
                try {
                    Object task = _queueTask.poll();
                    if(task != null) {
                        // 扣减排队任务数量
                        _queueTaskCount.decrementAndGet();
                        // 任务跟踪
                        TaskTracking tracking = _mapOnWaiting.remove(task);
                        tracking.markExecTM();
                        _mapOnWorking.put(task, tracking);
                        // 执行任务
                        assignTask(task);
                        // 性能计数
                        _perf.increasePerfCount();
                        nOnWorking++;
                    }
                    else {
                        // 队列中没有任务了,返还并发容量
                        _atomOnWorking.decrementAndGet();
                    }
                }
                catch (Exception ex){
                    // 启动任务失败,释放并发容量
                    _atomOnWorking.decrementAndGet();
                    s_logger.error(Helper.printStackTrace(ex));
                }
                break;
            }
            else{
                nRetry++;
                // 保留并发容量失败，重试
                nOnWorking = _atomOnWorking.get();
                // 极端意外情况保护
                if(nRetry > 1000*100){
                    if(nRetry % (1000*10) == 0)
                        s_logger.warn("保留并发容量失败,已尝试{}次", nRetry);
                    if(nRetry > 1000*1000){
                        throw new IllegalStateException("无法保留并发容量");
                    }
                }
            }
        }

        return nOnWorking;
    }

    // 分配任务给线程池
    protected void assignTask(Object task){
        Runnable taskWrap = (Runnable)task;
        _execSrv.submit(taskWrap);
    }

    // 任务结束后的清理动作
    protected void taskFinished(Object task){
        // 从正在执行的任务里移除
        TaskTracking tracking = _mapOnWorking.remove(task);
        if(tracking == null){
            s_logger.warn("可能重复调用了onFinished.run()方法 {}", task);
        }else {
            // 归还并发容量
            int nOnWorking = _atomOnWorking.decrementAndGet();
            if(nOnWorking < 0){
                s_logger.warn("并发数异常: {}", nOnWorking);
            }
            // 由于返还了并发数，可以再次分配新任务
            dispatch();
            // 性能计数
            long tmNow = System.currentTimeMillis();
            _perf.put(tracking.getExecTM() - tracking.getCreatedTM(), tmNow - tracking.getExecTM());
        }
    }
}
