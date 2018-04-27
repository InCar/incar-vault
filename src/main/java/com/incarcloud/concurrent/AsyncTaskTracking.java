package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import com.incarcloud.lang.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

class AsyncTaskTracking extends TaskTracking<AsyncTask>{
    private static final Logger s_logger = LoggerFactory.getLogger(AsyncTaskTracking.class);

    // 已经调用结束标记
    private final AtomicBoolean _atomFinished = new AtomicBoolean(false);

    AsyncTaskTracking(AsyncTask task, Action<TaskTracking> actTaskFinished){
        super(task, actTaskFinished);
    }

    @Override
    public void run() {
        try {
            markExecTM();
            _task.run(this::markFinished);
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
