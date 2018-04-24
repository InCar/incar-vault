package com.incarcloud.concurrent;

import com.incarcloud.auxiliary.Helper;
import com.incarcloud.lang.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 异步任务包装，确保异常时能释放并发容量
class AsyncTaskWrap implements Runnable {
    private static final Logger s_logger = LoggerFactory.getLogger(AsyncTaskWrap.class);

    private final AsyncTask _task;
    private final Action<AsyncTask> _actTaskFinished;

    AsyncTaskWrap(AsyncTask task, Action<AsyncTask> actTaskFinished){
        _task = task;
        _actTaskFinished = actTaskFinished;
    }


    @Override
    public void run() {
        try {
            _task.run(this::taskFinished);
        }
        catch(Exception ex){
            taskFinished();
            s_logger.error(Helper.printStackTrace(ex));
        }
    }

    private void taskFinished(){
        _actTaskFinished.run(_task);
    }
}
