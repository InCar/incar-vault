package com.incarcloud.concurrent;

// 执行中的任务状况跟踪
public class TaskTracking {
    // 任务
    final Object task;
    // 任务开始执行时的时间戳
    final long tmBegin;

    TaskTracking(Object task){
        tmBegin = System.currentTimeMillis();
        this.task = task;
    }
}
