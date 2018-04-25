package com.incarcloud.concurrent;

/**
 * 性能计数 最大值 最小值
 */
public class PerfMM<T extends Comparable<T>> {
    protected T _min;
    protected T _max;

    /**
     * 初始化
     * @param min 可能的最小的数值
     * @param max 可能的最大的数值
     */
    public void init(T min, T max){
        _min = max;
        _max = min;
    }

    /**
     * 最大值
     * @return 最大值
     */
    public T getMin(){ return _min; }

    /**
     * 最小值
     * @return 最小值
     */
    public T getMax(){ return _max; }
}
