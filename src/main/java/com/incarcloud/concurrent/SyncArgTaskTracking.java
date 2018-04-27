package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import com.incarcloud.lang.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 确保同步任务执行完成后,结束动作一定能被调用
class SyncArgTaskTracking<T> extends TaskTracking<T> {
    private static final Logger s_logger = LoggerFactory.getLogger(SyncArgTaskTracking.class);

    // 共享的任务处理器
    private final Action<T> _taskAction;

    SyncArgTaskTracking(Action<T> taskAction, T taskArg, Action<TaskTracking> actTaskFinished){
        super(taskArg, actTaskFinished);
        _taskAction = taskAction;
    }

    @Override
    public void run() {
        try {
            markExecTM();
            _taskAction.run(_task);
        }
        catch (Exception ex){
            s_logger.error(Helper.printStackTrace(ex));
        }
        finally {
            markFinished();
        }
    }
}
