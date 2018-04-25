package com.incarcloud.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * 限制最大并发数的同步排队任务。
 * 目的是保护相关联的部分不至于过载，软件系统往往存在最在容量/性能限制，如果
 * 超过限制，性能往往会急剧下降。
 */
public class LimitedSyncTask extends LimitedTask {
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
        super.submit(task);
    }

    // 分配任务给线程池
    @Override
    protected void assignTask(Object task){
        Runnable syncTask = (Runnable)task;
        super.assignTask(new SyncTaskWrap(syncTask, super::taskFinished));
    }
}
