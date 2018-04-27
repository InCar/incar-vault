package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import com.incarcloud.lang.Action;
import com.incarcloud.lang.Action2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

class AsyncArgTaskTracking<T> extends TaskTracking<T> {
    private static final Logger s_logger = LoggerFactory.getLogger(SyncArgTaskTracking.class);

    // 已经调用结束标记
    private final AtomicBoolean _atomFinished = new AtomicBoolean(false);
    // 共享的任务处理器
    private final Action2<T, Runnable> _taskAction;

    AsyncArgTaskTracking(Action2<T, Runnable> taskAction, T taskArg, Action<TaskTracking> actTaskFinished){
        super(taskArg, actTaskFinished);
        _taskAction = taskAction;
    }

    @Override
    public void run() {
        try {
            markExecTM();
            _taskAction.run(_task, this::markFinished);
        }
        catch(Exception ex){
            markFinished();
            s_logger.error(Helper.printStackTrace(ex));
        }
    }

    @Override
    protected void markFinished(){
        // 确保只调用1次
        if(_atomFinished.compareAndSet(false, true)){
            super.markFinished();
        }
        else{
            s_logger.warn("重复调用了onFinished {}", _task);
        }
    }
}
