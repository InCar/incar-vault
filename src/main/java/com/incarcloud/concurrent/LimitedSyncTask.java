package com.incarcloud.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * 限制最大并发数的同步排队任务。
 * 目的是保护相关联的部分不至于过载，软件系统往往存在最在容量/性能限制，如果
 * 超过限制，性能往往会急剧下降。
 */
public class LimitedSyncTask extends LimitedTask {
    // 任务队列
    private final ConcurrentLinkedQueue<Runnable> _queueTask = new ConcurrentLinkedQueue<>();

    /**
     * 构造器
     */
    public LimitedSyncTask(){
        super();
    }

    /**
     * 构造器
     * @param execSrv 外部传入的线程池
     */
    public LimitedSyncTask(ExecutorService execSrv){
        super(execSrv);
    }


    /**
     * 提交同步任务
     * @param task 同步任务
     */
    public void submit(Runnable task){
        if(task == null) throw new NullPointerException("task");

        _queueTask.add(task);
        _queueTaskCount.incrementAndGet();
        dispatch();
    }

    // 如果没有超过最大并发限制,把任务调度到一个线程上执行，返回当前并发数
    @Override
    protected int dispatch() {
        int nRetry = 0;
        int nOnWorking = _atomOnWorking.get();

        // 已经指示退出,不要再提交新任务了
        if(_bCanStop) return nOnWorking;

        while(nOnWorking < _max){
            if(_atomOnWorking.compareAndSet(nOnWorking, nOnWorking+1)){
                // 成功保留了并发容量,尝试启动任务
                try {
                    Runnable task = _queueTask.poll();
                    if(task != null) {
                        _queueTaskCount.decrementAndGet();
                        _mapOnWorking.put(task, new TaskTracking(task));
                        _execSrv.submit(new SyncTaskWrap(task, this::taskFinished));
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

                }
                break;
            }
            else{
                nRetry++;
                // 保留并发容量失败，重试
                nOnWorking = _atomOnWorking.get();
                // 极端意外情况保护
                if(nRetry > 1000*1000){
                    throw new IllegalStateException("无法保留并发容量");
                }
            }
        }

        return nOnWorking;
    }

    // 任务结束后的清理动作
    private void taskFinished(Runnable task){
        // 从正在执行的任务里移除
        _mapOnWorking.remove(task);
        // 归还并发容量
        _atomOnWorking.decrementAndGet();
        // 由于返还了并发数，可以再次分配新任务
        dispatch();
    }
}
