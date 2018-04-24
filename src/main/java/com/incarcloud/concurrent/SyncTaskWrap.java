package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import com.incarcloud.lang.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 确保同步任务执行完成后,结束动作一定能被调用
class SyncTaskWrap implements Runnable{
    private static final Logger s_logger = LoggerFactory.getLogger(SyncTaskWrap.class);

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
        catch (Exception ex){
            s_logger.error(Helper.printStackTrace(ex));
        }
        finally {
            _actTaskFinished.run(_task);
        }
    }
}
