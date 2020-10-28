package com.example.entity;

/**
 * MaxPQ
 *
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-28 17:34
 **/
public class MaxMQ<Key extends Comparable<Key>> {

    // 存储元素的数组
    private Key[] pq;

    // 当前Priority Queue 中的元素个数
    private int N = 0;

    public MaxMQ(int cap) {
        // 索引0不用，所以多分配一个空间
        pq = (Key[]) new Comparable[cap + 1];
    }

    /**
     * 返回当前队列中最大的元素
     *
     * @return
     */
    public Key max() {
        return pq[1];
    }

    public void insert(Key k) {

    }
}
