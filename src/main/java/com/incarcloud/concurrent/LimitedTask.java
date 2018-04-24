package com.incarcloud.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限制最大并发数的排队任务。
 * 目的是保护相关联的部分不至于过载，软件系统往往存在最在容量/性能限制，如果
 * 超过限制，性能往往会急剧下降。
 */
abstract class LimitedTask {
    // 最大并发数,可以在运行时动态调整
    protected int _max = 2;
    // 停止标志
    protected boolean _bCanStop = false;

    // 当前并发数
    protected final AtomicInteger _atomOnWorking = new AtomicInteger();
    // 任务队列计数，派生类的任务队列由Queue构成,size()会遍历整个队列，性能较低
    protected final AtomicInteger _queueTaskCount = new AtomicInteger();
    // 任务跟踪
    protected final ConcurrentHashMap<Object, TaskTracking> _mapOnWorking = new ConcurrentHashMap<>();
    // 线程池
    protected final ExecutorService _execSrv;

    /**
     * 构造器
     */
    public LimitedTask(){
        _execSrv = Executors.newCachedThreadPool();
    }

    /**
     * 构造器
     * @param execSrv 外部传入的线程池
     */
    public LimitedTask(ExecutorService execSrv){
        _execSrv = execSrv;
    }

    /**
     * 当前最大并发数
     * @return 当前最大并发数
     */
    public int getMax(){ return _max; }

    /**
     * 设置当前最大并发数,可以在运行时动态调整,
     * 若设置为0,将没有任务会开始执行
     * @param val 最大并发数
     */
    public void setMax(int val){
        _max = val;
        dispatch();
    }

    /**
     * 停止。
     * 已经开始执行的任务会继续执行，尚处于排队中的任务不会再被执行
     * 此方法立刻返回，不等待正在执行中的任务执行完成。
     */
    public void stop(){
        _bCanStop = true;
    }

    /**
     * 正在执行中的任务数量
     * @return 正在执行中的任务数量
     */
    public int getRunning(){ return _atomOnWorking.get(); }

    /**
     * 正在等待执行任务的数量
     * @return 正在等待执行任务的数量
     */
    public int getWaiting(){ return _queueTaskCount.get(); }

    /**
     * 扫描长时间运行的任务
     * 不宜过于频繁扫描
     * @param msMax 毫秒，执行时间超过的任务会被扫描出来
     * @return 有可能返回null
     */
    public List<TaskTracking> scanForLongTimeTask(long msMax){
        List<TaskTracking> listTracking = null;
        long tmNow = System.currentTimeMillis();

        for(TaskTracking tracking:_mapOnWorking.values()){
            if(tmNow - tracking.tmBegin > msMax){
                if(listTracking == null) listTracking = new ArrayList<>();
                listTracking.add(tracking);
            }
        }

        return listTracking;
    }

    // 分配任务到线程，返回当前的并发数
    protected abstract int dispatch();
}
