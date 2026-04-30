
package com.example.onlinecoding;

/**
 * LRU 缓存接口定义
 * 
 * LRU (Least Recently Used) 即最近最少使用缓存策略，
 * 当缓存满时，优先淘汰最久未使用的数据。
 * 
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public interface LRUCache<K, V> {

    /**
     * 获取缓存值
     * 
     * @param key 键
     * @return 值，如果不存在返回 null
     */
    V get(Object key);

    /**
     * 存入缓存
     * 
     * @param key 键
     * @param value 值
     * @return 之前关联的值，如果没有返回 null
     */
    V put(K key, V value);

    /**
     * 删除缓存
     * 
     * @param key 键
     * @return 被删除的值，如果 key 不存在返回 null
     */
    V remove(Object key);

    /**
     * 获取当前缓存大小（已存储的数据量）
     * 
     * @return 当前缓存数据量
     */
    int size();

    /**
     * 获取缓存容量（最大可存储的数据量）
     * 
     * @return 缓存最大容量
     */
    int capacity();

    /**
     * 判断缓存是否为空
     * 
     * @return 如果缓存为空返回 true，否则返回 false
     */
    boolean isEmpty();

    /**
     * 清空缓存
     */
    void clear();
}
