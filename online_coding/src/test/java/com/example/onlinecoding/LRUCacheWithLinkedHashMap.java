
package com.example.onlinecoding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 使用 LinkedHashMap 实现 LRU 缓存
 * 
 * LinkedHashMap 是 HashMap 的子类，内部维护了一个双向链表来维护元素顺序。
 * 通过设置 accessOrder = true，可以使其按访问顺序排序，从而实现 LRU 策略。
 * 
 * 核心原理：
 * 1. 继承 LinkedHashMap，构造时设置 accessOrder = true
 * 2. 重写 removeEldestEntry() 方法，当大小超过容量时自动删除最老条目
 * 
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class LRUCacheWithLinkedHashMap<K, V> extends LinkedHashMap<K, V> 
        implements LRUCache<K, V> {

    /**
     * 缓存容量，超过此容量时会自动淘汰最久未使用的数据
     */
    private final int capacity;

    /**
     * 构造函数
     * 
     * @param capacity 缓存容量
     * @throws IllegalArgumentException 如果容量小于等于 0
     */
    public LRUCacheWithLinkedHashMap(int capacity) {
        // 调用父类构造函数：
        // initialCapacity: 初始容量，设置为 capacity
        // loadFactor: 加载因子，0.75f 是默认值，当元素数量达到容量 * 加载因子时会扩容
        // accessOrder: true 表示按访问顺序排序，false 表示按插入顺序排序
        super(capacity, 0.75f, true);
        
        // 校验容量参数
        if (capacity <= 0) {
            throw new IllegalArgumentException("容量必须大于 0");
        }
        
        this.capacity = capacity;
    }

    /**
     * 获取缓存容量
     * 
     * @return 缓存最大容量
     */
    @Override
    public int capacity() {
        return capacity;
    }

    /**
     * 重写此方法以实现自动淘汰策略
     * 
     * 当此方法返回 true 时，LinkedHashMap 会自动删除最老的条目（链表头部的元素）。
     * 
     * @param eldest 最老的条目
     * @return 如果需要删除最老条目返回 true，否则返回 false
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // 当当前大小超过容量时，返回 true 触发自动删除
        return size() > capacity;
    }

    /**
     * 返回缓存的字符串表示，用于调试和测试
     * 
     * @return 缓存内容的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LRUCacheWithLinkedHashMap {");
        sb.append("capacity=").append(capacity);
        sb.append(", size=").append(size());
        sb.append(", entries=").append(super.toString());
        sb.append("}");
        return sb.toString();
    }
}
