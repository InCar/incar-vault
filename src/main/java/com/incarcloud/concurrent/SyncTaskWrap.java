package com.incarcloud.concurrent;

import com.incarcloud.lang.Action;

// 确保同步任务执行完成后,结束动作一定能被调用
class SyncTaskWrap implements Runnable{
    private final Runnable _task;
    private final Action<Runnable> _actTaskFinished;

    SyncTaskWrap(Runnable task, Action<Runnable> actTaskFinished){
        _task = task;
        _actTaskFinished = actTaskFinished;
    }

    @Override
    public void run() {
        try {
            _task.run();
        }
        finally {
            _actTaskFinished.run(_task);
        }
    }
}
