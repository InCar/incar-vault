package com.incarcloud.concurrent;

import com.incarcloud.lang.Action;

import java.util.concurrent.ExecutorService;

/**
 * 限制最大并发数的同步排队任务,仅仅只提交任务参数，而共享同一个任务处理器
 * @param <T> 任务的参数类型
 */
public class LimitedSyncArgTask<T> extends LimitedSyncTask {

    // 任务处理对象
    private final Action<T> _taskAction;

    /**
     * 构造器
     * @param taskAction 共享的任务处理器
     */
    public LimitedSyncArgTask(Action<T> taskAction){
        super();
        _taskAction = taskAction;
    }

    /**
     * 构造器
     * @param taskAction 共享的任务处理器
     * @param execSrv 外部传入的线程池
     */
    public LimitedSyncArgTask(Action<T> taskAction, ExecutorService execSrv){
        super(execSrv);
        _taskAction = taskAction;
    }

    /**
     * 提交同步任务
     * @param taskArg 任务参数
     */
    public void submit(T taskArg){
        SyncArgTaskTracking<T> tracking = new SyncArgTaskTracking<>(_taskAction, taskArg, this::finishTask);
        queueTask(tracking);
    }
}
