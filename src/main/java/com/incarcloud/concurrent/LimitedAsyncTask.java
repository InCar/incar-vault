package com.incarcloud.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * 限制最大并发数的异步排队任务。
 * 目的是保护相关联的部分不至于过载，软件系统往往存在最在容量/性能限制，如果
 * 超过限制，性能往往会急剧下降。
 */
public class LimitedAsyncTask extends LimitedTask{
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
        AsyncTaskTracking tracking = new AsyncTaskTracking(task, this::finishTask);
        queueTask(tracking);
    }
}
