package com.incarcloud.concurrent;

/**
 * 性能测量结果 任务等待时间 任务执行时间
 */
public class PerfMetric<T extends Comparable<T>> {
    // 任务总数
    private long _totalTask;
    // 任务等待时间
    private PerfMetricMMAvg<T> _perfWaiting = new PerfMetricMMAvg<>();
    // 任务执行时间
    private PerfMetricMMAvg<T> _perfRunning = new PerfMetricMMAvg<>();

    void setTotalTask(long val){ _totalTask = val; }

    /**
     * 任务总数
     * @return 任务总数
     */
    public long getFinishedTask(){ return _totalTask; }

    /**
     * 任务等待时间
     * @return 任务等待时间
     */
    public PerfMetricMMAvg<T> getPerfWaiting(){ return _perfWaiting; }

    /**
     * 任务执行时间
     * @return 任务执行时间
     */
    public PerfMetricMMAvg<T> getPerfRunning(){ return _perfRunning; }
}
