package com.incarcloud.concurrent;

import com.incarcloud.lang.Action;

class AsyncTaskWrap implements Runnable {
    private final AsyncTask _task;
    private final Action<AsyncTask> _actTaskFinished;

    AsyncTaskWrap(AsyncTask task, Action<AsyncTask> actTaskFinished){
        _task = task;
        _actTaskFinished = actTaskFinished;
    }


    @Override
    public void run() {
        _task.run(this::taskFinished);
    }

    private void taskFinished(){
        _actTaskFinished.run(_task);
    }
}
