package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import com.incarcloud.lang.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 确保同步任务执行完成后,结束动作一定能被调用
class SyncTaskTracking extends TaskTracking<Runnable>{
    private static final Logger s_logger = LoggerFactory.getLogger(SyncTaskTracking.class);

    SyncTaskTracking(Runnable task, Action<TaskTracking> actTaskFinished){
        super(task, actTaskFinished);
    }

    @Override
    public void run() {
        try {
            markExecTM();
            _task.run();
        }
        catch (Exception ex){
            s_logger.error(Helper.printStackTrace(ex));
        }
        finally {
            markFinished();
        }
    }
}
