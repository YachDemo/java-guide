package com.example.entity;

/**
 * MaxPQ
 *
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-28 17:34
 **/
public class MaxPQ<Key extends Comparable<Key>> {

    // 存储元素的数组
    private Key[] pq;

    // 当前Priority Queue 中的元素个数
    private int N = 0;

    public MaxPQ(int cap) {
        // 索引0不用，所以多分配一个空间
        pq = (Key[]) new Comparable[cap + 1];
    }

    // 父节点的索引
    int parent(int root) {
        return root / 2;
    }

    int left(int root) {
        return root * 2;
    }

    int right(int root) {
        return root * 2 + 1;
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
        N++;
        // 先把新元素加到最后
        pq[N] = k;
        // 然后让他上浮到正确的位置
        swim(N);
    }

    public Key delMax() {
        // 最大堆的堆顶就是最大元素
        Key max = pq[1];
        // 把这个最大元素换到最后，删除之
        exch(1, N);
        pq[N] = null;
        N--;
        sink(1);
        return max;
    }

    /* 上浮第k个元素，以维护最大堆的性质 */
    private void swim(int k) {
        while (k > 1 && less(parent(k), k)) {
            exch(parent(k), k);
            k = parent(k);
        }
    }

    /* 下沉第k个元素，以维护最大堆的性质 */
    private void sink(int k) {
        while (left(k) <= N) {
            // 先假设左边节点较大
            int older = left(k);
            // 如果右边节点存在，比一下大小
            if (right(k) <= N && less(older, right(k))) older = right(k);
            // 节点k比两个孩子都大，就不必下沉了
            if (less(older, k)) break;
            // 否则，不符合最大堆的结构，下沉节点k
            exch(k, older);
            k = older;
        }
    }

    /* 交换数组的两个元素 */
    private void exch(int i, int j) {
        Key temp = pq[i];
        pq[i] = pq[j];
        pq[j] = temp;
    }

    /* pq[i] 是否比 pq[j]小 */
    private boolean less(int i, int j) {
        return pq[i].compareTo(pq[j]) < 0;
    }
}
