package com.incarcloud.concurrent;

/**
 * 性能计数 最大值 最小值
 */
public class PerfMM<T extends Comparable<T>> {
    protected T _min = null;
    protected T _max = null;

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
