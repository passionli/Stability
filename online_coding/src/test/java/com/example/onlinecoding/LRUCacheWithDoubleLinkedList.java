
package com.example.onlinecoding;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用 HashMap + 双向链表实现 LRU 缓存
 * 
 * 核心数据结构：
 * 1. HashMap: 存储 key -> Node 映射，实现 O(1) 查找
 * 2. 双向链表: 维护访问顺序，表头为最新访问，表尾为最久未访问
 * 
 * 设计要点：
 * - 使用虚拟头节点(head)和虚拟尾节点(tail)简化边界处理
 * - 每次访问节点时将其移动到表头
 * - 插入新节点时添加到表头，超过容量时删除表尾
 * 
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class LRUCacheWithDoubleLinkedList<K, V> implements LRUCache<K, V> {

    /**
     * 双向链表节点类
     * 存储键值对及前后指针
     */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        /**
         * 节点构造函数
         * 
         * @param key 键
         * @param value 值
         */
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * 虚拟头节点，不存储实际数据
     * 用于简化链表头部的插入和删除操作
     */
    private final Node<K, V> head;

    /**
     * 虚拟尾节点，不存储实际数据
     * 用于简化链表尾部的删除操作
     */
    private final Node<K, V> tail;

    /**
     * HashMap，存储 key -> Node 的映射
     * 实现 O(1) 的查找效率
     */
    private final Map<K, Node<K, V>> cache;

    /**
     * 缓存容量，超过此容量时会自动淘汰最久未使用的数据
     */
    private final int capacity;

    /**
     * 当前缓存大小（已存储的数据量）
     */
    private int size;

    /**
     * 构造函数
     * 
     * @param capacity 缓存容量
     * @throws IllegalArgumentException 如果容量小于等于 0
     */
    public LRUCacheWithDoubleLinkedList(int capacity) {
        // 校验容量参数
        if (capacity <= 0) {
            throw new IllegalArgumentException("容量必须大于 0");
        }

        this.capacity = capacity;
        this.size = 0;

        // 初始化虚拟头节点和虚拟尾节点
        head = new Node<>(null, null);
        tail = new Node<>(null, null);

        // 将头节点和尾节点相连，形成空链表
        head.next = tail;
        tail.prev = head;

        // 初始化 HashMap
        cache = new HashMap<>();
    }

    /**
     * 获取缓存值
     * 
     * 操作步骤：
     * 1. 从 HashMap 中查找对应的节点
     * 2. 如果节点存在，将其移动到链表头部（标记为最近使用）
     * 3. 返回节点的值
     * 
     * @param key 键
     * @return 值，如果不存在返回 null
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        // 从 HashMap 中查找节点
        Node<K, V> node = cache.get(key);

        // 如果节点不存在，返回 null
        if (node == null) {
            return null;
        }

        // 将节点移动到链表头部，表示最近使用
        moveToHead(node);

        // 返回节点的值
        return node.value;
    }

    /**
     * 存入缓存
     * 
     * 操作步骤：
     * 1. 从 HashMap 中查找对应的节点
     * 2. 如果节点存在：更新值，并将其移动到链表头部
     * 3. 如果节点不存在：创建新节点，添加到链表头部，并存入 HashMap
     * 4. 如果超过容量：删除链表尾部节点（最久未使用），并从 HashMap 中移除
     * 
     * @param key 键
     * @param value 值
     */
    @Override
    public V put(K key, V value) {
        // 从 HashMap 中查找节点
        Node<K, V> node = cache.get(key);
        
        // 保存旧值用于返回
        V oldValue = null;

        if (node == null) {
            // 节点不存在，创建新节点
            Node<K, V> newNode = new Node<>(key, value);

            // 将新节点添加到链表头部
            addToHead(newNode);

            // 将新节点存入 HashMap
            cache.put(key, newNode);

            // 增加当前大小
            size++;

            // 如果超过容量，删除最久未使用的节点（链表尾部）
            if (size > capacity) {
                // 移除链表尾部节点
                Node<K, V> removedNode = removeTail();

                // 从 HashMap 中移除对应的 key
                cache.remove(removedNode.key);

                // 减少当前大小
                size--;
            }
        } else {
            // 节点存在，保存旧值并更新
            oldValue = node.value;
            node.value = value;

            // 将节点移动到链表头部，表示最近使用
            moveToHead(node);
        }
        
        // 返回旧值
        return oldValue;
    }

    /**
     * 删除缓存
     * 
     * 操作步骤：
     * 1. 从 HashMap 中查找对应的节点
     * 2. 如果节点存在：从链表中移除该节点，从 HashMap 中移除该 key
     * 
     * @param key 键
     * @return 被删除的值，如果 key 不存在返回 null
     */
    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        // 从 HashMap 中查找节点
        Node<K, V> node = cache.get(key);

        // 如果节点不存在，返回 null
        if (node == null) {
            return null;
        }

        // 从链表中移除该节点
        removeNode(node);

        // 从 HashMap 中移除该 key
        cache.remove(key);

        // 减少当前大小
        size--;

        // 返回被删除的值
        return node.value;
    }

    /**
     * 获取当前缓存大小
     * 
     * @return 当前缓存数据量
     */
    @Override
    public int size() {
        return size;
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
     * 判断缓存是否为空
     * 
     * @return 如果缓存为空返回 true，否则返回 false
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        // 清空 HashMap
        cache.clear();

        // 重置链表，只保留虚拟头节点和虚拟尾节点
        head.next = tail;
        tail.prev = head;

        // 重置大小
        size = 0;
    }

    /**
     * 将节点添加到链表头部
     * 
     * 操作顺序：
     * 1. 设置新节点的 prev 为 head
     * 2. 设置新节点的 next 为 head.next
     * 3. 设置原 head.next 的 prev 为新节点
     * 4. 设置 head.next 为新节点
     * 
     * @param node 要添加的节点
     */
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * 从链表中移除指定节点
     * 
     * 操作顺序：
     * 1. 设置被移除节点前驱的 next 为被移除节点的后继
     * 2. 设置被移除节点后继的 prev 为被移除节点的前驱
     * 
     * @param node 要移除的节点
     */
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * 将节点移动到链表头部
     * 
     * 操作顺序：
     * 1. 先从链表中移除该节点
     * 2. 再将该节点添加到链表头部
     * 
     * @param node 要移动的节点
     */
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    /**
     * 移除链表尾部节点（最久未使用的节点）
     * 
     * @return 被移除的节点
     */
    private Node<K, V> removeTail() {
        // 链表尾部节点是 tail.prev（因为 tail 是虚拟尾节点）
        Node<K, V> tailNode = tail.prev;

        // 移除该节点
        removeNode(tailNode);

        // 返回被移除的节点
        return tailNode;
    }

    /**
     * 返回缓存的字符串表示，用于调试和测试
     * 
     * @return 缓存内容的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LRUCacheWithDoubleLinkedList {");
        sb.append("capacity=").append(capacity);
        sb.append(", size=").append(size);
        sb.append(", entries=[");

        // 遍历链表，从 head.next 开始到 tail.prev 结束
        Node<K, V> current = head.next;
        while (current != tail) {
            sb.append(current.key).append("=").append(current.value);
            if (current.next != tail) {
                sb.append(", ");
            }
            current = current.next;
        }

        sb.append("]}");
        return sb.toString();
    }
}
