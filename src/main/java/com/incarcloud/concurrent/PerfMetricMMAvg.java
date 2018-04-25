package com.incarcloud.concurrent;

/**
 * 性能测量结果 最小值 最大值 平均值
 */
public class PerfMetricMMAvg<T extends Comparable<T>> extends PerfMM<T> {
    private float _fAvg;

    // 设置数值
    void put(T min, T max, long count, float fSum){
        _min = min;
        _max = max;
        _fAvg = count==0L?0.0f:fSum/count;
    }

    /**
     * 平均值
     * @return 平均值
     */
    public float getAvg(){ return _fAvg; }
}
