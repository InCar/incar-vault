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
public abstract class LimitedTask {
    private Logger s_logger = LoggerFactory.getLogger(LimitedTask.class);

    // 最大并发数,可以在运行时动态调整
    private int _max = 2;
    // 停止标志
    private boolean _bCanStop = false;
    // 禁止提交任务标志
    private boolean _bDisableSubmit = false;
    // 任务完成事件,仅在停止信号发出后才触发
    private final Object _objTaskFin = new Object();
    // 需要释放线程池
    private final boolean _bNeedShutdown;
    // 当前并发数
    private final AtomicInteger _atomOnWorking = new AtomicInteger();
    // 任务队列
    private final ConcurrentLinkedQueue<TaskTracking> _queueTask = new ConcurrentLinkedQueue<>();
    // 任务队列计数，派生类的任务队列由Queue构成,size()会遍历整个队列，性能较低
    private final AtomicInteger _queueTaskCount = new AtomicInteger();
    // 任务跟踪
    private final ConcurrentHashMap<TaskTracking, TaskTracking> _mapOnWorking = new ConcurrentHashMap<>();
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
     * 立即停止。
     * 已经开始执行的任务会继续执行，尚处于排队中的任务不会再被执行
     * 此方法立刻返回，不等待正在执行中的任务执行完成。
     * @return 尚没开始执行的任务
     */
    public List<Object> stopASAP(){
        _bCanStop = true;
        _bDisableSubmit = true;

        if(_bNeedShutdown){
            _execSrv.shutdown();
        }

        List<Object> listNotStart = new ArrayList<>(getWaiting());
        while(true){
            TaskTracking tracking = _queueTask.poll();
            if(tracking != null){
                _queueTaskCount.decrementAndGet();
                listNotStart.add(tracking.getTask());
            }
            else
                break;
        }
        return listNotStart;
    }

    /**
     * 停止。
     * 一直等到所有提交的任务都已经执行完毕才返回
     */
    public void stop(){
        // 禁止提交任务
        _bDisableSubmit = true;

        // 等待任务完成
        try {
            while (true) {
                synchronized (_objTaskFin) {
                    _objTaskFin.wait(1000);
                }
                if(getWaiting() == 0 && getRunning() == 0) break;
            }
        }
        catch (InterruptedException ex){
            s_logger.warn(Helper.printStackTrace(ex));
        }

        stopASAP();
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
     * 越高，但精确性越差；反之则瞬时性差而精确性高
     * @return 每秒执行多少个任务(Hz)
     */
    public float queryPerf(){
        return _perf.calcPerfAndReset();
    }

    /**
     * 已经执行完毕的任务统计
     * @return 已经执行任务总数，等待时间(最小最大平均值)，执行时间(最小最大平均值)
     */
    public PerfMetric<Long> queryPerfMetric(){ return _perf.calcPerfMetric(); }

    // 排队任务
    protected void queueTask(TaskTracking tracking){
        if(_bDisableSubmit) throw new IllegalStateException("已经调用了stop()方法,禁止提交任务");

        _queueTask.add(tracking);
        _queueTaskCount.incrementAndGet();
        dispatch();
    }

    // 结束任务
    protected void finishTask(TaskTracking tracking){
        // 从正在执行的任务里移除
        if(_mapOnWorking.remove(tracking) == null){
            s_logger.warn("可能重复调用了onFinished.run()方法 {}", tracking.getTask());
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

            // 如果已经触发停止,让退出例程检测是否可以停止
            if(_bDisableSubmit){
                synchronized (_objTaskFin) {
                    _objTaskFin.notify();
                }
            }
        }
    }

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
                    TaskTracking tracking = _queueTask.poll();
                    if(tracking != null) {
                        // 扣减排队任务数量
                        _queueTaskCount.decrementAndGet();
                        // 任务跟踪
                        _mapOnWorking.put(tracking, tracking);
                        // 执行任务
                        _execSrv.submit(tracking::run);
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
}
