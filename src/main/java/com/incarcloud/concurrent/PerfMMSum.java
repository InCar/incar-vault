package com.incarcloud.concurrent;

/**
 * 性能计数器 最大值 最小值 总和
 */
class PerfMMSum<T extends Number & Comparable<T>> extends PerfMM<T> {
    private float _fSum;

    void put(T val){
        if(_min == null || val.compareTo(_min) < 0) _min = val;
        if(_max == null || val.compareTo(_max) > 0) _max = val;

        _fSum += val.floatValue();
    }

    float getSum(){ return _fSum; }

    void reset(){
        _min = null;
        _max = null;
        _fSum = 0.0f;
    }
}
