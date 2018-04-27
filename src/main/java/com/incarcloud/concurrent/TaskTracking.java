package com.incarcloud.concurrent;

import com.incarcloud.lang.Action;

/**
 * 执行中任务的跟踪信息
 * @param <T> 被跟踪的任务类型
 */
public abstract class TaskTracking<T>{
    // 任务创建时的时间戳
    private final long _tmCreated;
    // 任务执行时的时间戳
    private long _tmExec;
    // 任务结束时调用此方法
    private final Action<TaskTracking> _actTaskFinished;

    // 被跟踪的任务
    protected final T _task;

    /**
     * 构造器
     */
    TaskTracking(T task, Action<TaskTracking> actTaskFinished){
        _task = task;
        _actTaskFinished = actTaskFinished;
        _tmCreated = System.currentTimeMillis();
    }

    /**
     * 获取被跟踪的任务
     * @return 被跟踪的任务
     */
    public T getTask(){ return _task; }

    /**
     * 任务创建的时间戳
     * @return 从进入排队开始计算
     */
    public long getCreatedTM(){ return _tmCreated; }

    /**
     * 任务的开始执行时间戳
     * @return 以实际被CPU调度开始计算，进入排队但没有被CPU调度不纳入计算
     */
    public long getExecTM(){ return _tmExec; }

    // 标记任务开始执行的时间
    protected void markExecTM(){
        _tmExec = System.currentTimeMillis();
    }

    // 表明任务结束了
    protected void markFinished(){
        _actTaskFinished.run(this);
    }

    // 派生类重载，执行具体的任务
    abstract void run();
}
