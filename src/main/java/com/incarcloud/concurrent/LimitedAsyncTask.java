package com.incarcloud.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * 限制最大并发数的异步排队任务。
 * 目的是保护相关联的部分不至于过载，软件系统往往存在最在容量/性能限制，如果
 * 超过限制，发生过载，性能或可靠性往往会急剧下降。
 */
public class LimitedAsyncTask extends LimitedTask{
    // 任务队列
    private final ConcurrentLinkedQueue<AsyncTask> _queueTask = new ConcurrentLinkedQueue<>();

    /**
     * 构造器
     */
    public LimitedAsyncTask(){
        super();
    }

    /**
     * 构造器
     * @param execSrv 外部传入的线程池
     */
    public LimitedAsyncTask(ExecutorService execSrv){
        super(execSrv);
    }

    /**
     * 提交异步任务
     * @param task 异步任务
     */
    public void submit(AsyncTask task){
        if(task == null) throw new NullPointerException("task");

        _queueTask.add(task);
        _queueTaskCount.incrementAndGet();
        dispatch();
    }


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
                    AsyncTask task = _queueTask.poll();
                    if(task != null) {
                        _queueTaskCount.decrementAndGet();
                        _mapOnWorking.put(task, new TaskTracking(task));
                        _execSrv.submit(new AsyncTaskWrap(task, this::taskFinished));
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
    private void taskFinished(AsyncTask task){
        // 从正在执行的任务里移除
        _mapOnWorking.remove(task);
        // 归还并发容量
        _atomOnWorking.decrementAndGet();
        // 由于返还了并发数，可以再次分配新任务
        dispatch();
    }
}
