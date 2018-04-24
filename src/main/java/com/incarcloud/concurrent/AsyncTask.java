package com.incarcloud.concurrent;

/**
 * 异步任务
 */
public interface AsyncTask {
    /**
     * 异步任务
     * @param onFinished 在异步任务结束（无论正常结束或异常结束）时必须调用此方法，
     *                   否则并发数会被一直占用
     */
    void run(Runnable onFinished);
}
