package com.incarcloud.concurrent;

/**
 * 执行中任务的跟踪信息
 */
public class TaskTracking {
    // 任务
    private final Object _task;
    // 任务创建时的时间戳
    private final long _tmCreated;
    // 任务执行时的时间戳
    private long _tmExec;

    /**
     * 构造器
     * @param task 被跟踪任务
     */
    TaskTracking(Object task){
        _tmCreated = System.currentTimeMillis();
        _task = task;
    }

    /**
     * 标记任务开始执行的时间戳
     */
    void markExecTM(){
        _tmExec = System.currentTimeMillis();
    }

    /**
     * 任务创建的时间戳
     * @return 从进入排队开始计算
     */
    public long getCreatedTM(){ return _tmCreated; }

    /**
     * 任务的开始执行时间戳
     * @return 以实际被CPU调度开始计算，进入排队，但没有被CPU调度不纳入计算
     */
    public long getExecTM(){ return _tmExec; }

    /**
     * 获取被跟踪的任务
     * @return 可能是2种情况之一
     *         一种是同步任务, 一个实现了Runnable接口的对象
     *         一种是异步任务，一个实一邮AsyncTask接口的对象
     */
    public Object getTask(){ return _task; }
}
