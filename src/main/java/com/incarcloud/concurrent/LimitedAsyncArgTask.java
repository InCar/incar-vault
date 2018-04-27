package com.incarcloud.concurrent;

import com.incarcloud.lang.Action2;

import java.util.concurrent.ExecutorService;

/**
 * 限制最大并发数的异步排队任务,仅仅只提交任务参数，而共享同一个任务处理器
 * @param <T> 任务的参数类型
 */
public class LimitedAsyncArgTask<T> extends LimitedAsyncTask {

    // 任务处理对象
    private final Action2<T, Runnable> _taskAction;

    /**
     * 构造器
     * @param taskAction 共享的任务处理器
     */
    public LimitedAsyncArgTask(Action2<T, Runnable> taskAction){
        super();
        _taskAction = taskAction;
    }

    /**
     * 构造器
     * @param taskAction 共享的任务处理器
     * @param execSrv 外部传入的线程池
     */
    public LimitedAsyncArgTask(Action2<T, Runnable> taskAction, ExecutorService execSrv){
        super(execSrv);
        _taskAction = taskAction;
    }

    /**
     * 提交同步任务
     * @param taskArg 任务参数
     */
    public void submit(T taskArg){
        AsyncArgTaskTracking<T> tracking = new AsyncArgTaskTracking<>(_taskAction, taskArg, this::finishTask);
        queueTask(tracking);
    }
}
