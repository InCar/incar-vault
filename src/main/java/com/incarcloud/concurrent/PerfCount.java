package com.incarcloud.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

// 性能计数器
class PerfCount {
    // 性能计数器
    private AtomicInteger _atomPerf = new AtomicInteger();
    // 测量时间戳
    private long _tmMark = System.currentTimeMillis();

    // 等待时间
    private PerfMMSum<Long> _mmsWaiting = new PerfMMSum<>();
    // 运行时间
    private PerfMMSum<Long> _mmsRunning = new PerfMMSum<>();
    // 累积计数器
    private final AtomicInteger _atomCount = new AtomicInteger();

    PerfCount(){
        _mmsWaiting.init(0L, Long.MAX_VALUE);
        _mmsRunning.init(0L, Long.MAX_VALUE);
    }

    // 性能计数器数值增加1
    void increasePerfCount(){
        _atomPerf.incrementAndGet();
    }

    // 等待时间/运行时间
    void put(long msWaiting, long msRunning){
        synchronized (_atomCount) {
            _atomCount.incrementAndGet();
            _mmsWaiting.put(msWaiting);
            _mmsRunning.put(msRunning);
        }
    }

    // 计算性能,并重置计数器
    float calcPerfAndReset(){
        long tmNow = System.currentTimeMillis();
        float fPerf = 1000.0f * _atomPerf.getAndSet(0) / (tmNow - _tmMark);
        _tmMark = tmNow;
        return fPerf;
    }

    // 计算任务等待时长和运行时长
    PerfMetric<Long> calcPerfMetric(){
        PerfMetric<Long> metric = new PerfMetric<>();

        synchronized (_atomCount) {
            metric.setTotalTask(_atomCount.get());
            metric.getPerfWaiting().put(
                    _mmsWaiting.getMin(), _mmsWaiting.getMax(),
                    metric.getFinishedTask(), _mmsWaiting.getSum());
            metric.getPerfRunning().put(
                    _mmsRunning.getMin(), _mmsRunning.getMax(),
                    metric.getFinishedTask(), _mmsRunning.getSum());
        }
        return metric;
    }
}
