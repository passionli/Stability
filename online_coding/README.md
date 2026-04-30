
# LRU 缓存实现教程

## 一、LRU 缓存概述

LRU（Least Recently Used）即**最近最少使用**缓存策略，是一种常用的缓存淘汰算法。

### 1.1 核心思想
- 当缓存空间满时，优先淘汰**最久未被使用**的数据
- 每次访问数据时，将其标记为"最近使用"
- 新数据插入时，放在"最近使用"的位置

### 1.2 应用场景
- 浏览器缓存策略
- 数据库查询缓存
- 内存数据缓存
- Redis 缓存淘汰策略

### 1.3 工作原理示意图

```
初始状态（容量=3）：
HEAD <-> TAIL

插入 A：
HEAD <-> A <-> TAIL

插入 B：
HEAD <-> B <-> A <-> TAIL

插入 C：
HEAD <-> C <-> B <-> A <-> TAIL

访问 A（移到表头）：
HEAD <-> A <-> C <-> B <-> TAIL

插入 D（淘汰 B）：
HEAD <-> D <-> A <-> C <-> TAIL
```

## 二、两种实现方式对比

| 特性 | LinkedHashMap 实现 | HashMap + 双向链表 |
| :--- | :--- | :--- |
| **代码量** | 少，利用 JDK 内置实现 | 多，需手动实现链表 |
| **灵活性** | 较低，依赖 JDK | 高，完全可控 |
| **可读性** | 高，代码简洁 | 中，需要理解链表操作 |
| **适用场景** | 生产环境、快速实现 | 学习研究、面试 |
| **时间复杂度** | O(1) | O(1) |

## 三、实现方式详解

### 3.1 LinkedHashMap 实现

`LinkedHashMap` 是 HashMap 的子类，维护了一个双向链表来记录插入顺序或访问顺序。

**关键参数：**
- `accessOrder = true`：按访问顺序排序
- `accessOrder = false`：按插入顺序排序（默认）

**核心方法：**
- `removeEldestEntry(Map.Entry<K,V> eldest)`：当返回 true 时，自动删除最老的条目

### 3.2 HashMap + 双向链表实现

手动实现需要维护两个数据结构：

**HashMap**：
- 存储 `key -> Node` 的映射
- 实现 O(1) 的查找

**双向链表**：
- 维护访问顺序
- 表头（HEAD）：最新访问的数据
- 表尾（TAIL）：最久未访问的数据

**核心操作：**

| 操作 | 步骤 |
| :--- | :--- |
| **get(key)** | 1. 从 HashMap 查找节点<br>2. 如果存在，移到表头<br>3. 返回值 |
| **put(key, value)** | 1. 如果 key 存在，更新值并移到表头<br>2. 如果 key 不存在，创建新节点插入表头<br>3. 如果超过容量，删除表尾节点 |
| **remove(key)** | 1. 从 HashMap 查找节点<br>2. 从链表中移除该节点<br>3. 从 HashMap 中删除 |

## 四、核心代码解析

### 4.1 接口定义

```java
public interface LRUCache<K, V> {
    V get(K key);        // 获取缓存
    void put(K key, V value);  // 存入缓存
    V remove(K key);     // 删除缓存
    int size();          // 当前大小
    int capacity();      // 容量
    boolean isEmpty();   // 是否为空
    void clear();        // 清空缓存
}
```

### 4.2 LinkedHashMap 实现要点

```java
public class LRUCacheWithLinkedHashMap<K, V> extends LinkedHashMap<K, V> 
        implements LRUCache<K, V> {
    
    private final int capacity;
    
    public LRUCacheWithLinkedHashMap(int capacity) {
        // accessOrder = true 启用访问顺序
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // 当大小超过容量时自动淘汰
        return size() > capacity;
    }
}
```

### 4.3 双向链表节点定义

```java
private static class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;
    
    Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
```

### 4.4 双向链表操作

```java
// 添加节点到表头
private void addToHead(Node<K, V> node) {
    node.prev = head;
    node.next = head.next;
    head.next.prev = node;
    head.next = node;
}

// 移除节点
private void removeNode(Node<K, V> node) {
    node.prev.next = node.next;
    node.next.prev = node.prev;
}

// 移动节点到表头
private void moveToHead(Node<K, V> node) {
    removeNode(node);
    addToHead(node);
}

// 移除表尾节点
private Node<K, V> removeTail() {
    Node<K, V> tailNode = tail.prev;
    removeNode(tailNode);
    return tailNode;
}
```

## 五、复杂度分析

| 操作 | 时间复杂度 | 空间复杂度 |
| :--- | :--- | :--- |
| get() | O(1) | O(n) |
| put() | O(1) | O(n) |
| remove() | O(1) | O(n) |
| clear() | O(1) | O(n) |

**说明：**
- HashMap 的查找、插入、删除都是 O(1)
- 双向链表的节点移动也是 O(1)
- 空间复杂度 O(n) 是因为需要存储所有缓存数据

## 六、注意事项

### 6.1 线程安全
- 当前实现为**非线程安全**
- 生产环境使用需加锁或使用 `ConcurrentHashMap`

### 6.2 空值处理
- 允许存储 `null` 值
- `get()` 返回 `null` 可能表示：
  - key 不存在
  - key 对应的值就是 `null`
- 如需区分，可使用 `containsKey()` 方法

### 6.3 容量设置
- 容量过小：频繁淘汰，缓存命中率低
- 容量过大：内存占用高
- 根据实际业务场景设置合理容量

### 6.4 泛型设计
- 使用泛型支持任意类型的 key-value
- key 需要正确实现 `hashCode()` 和 `equals()` 方法

## 七、测试用例设计

| 测试场景 | 测试描述 | 预期结果 |
| :--- | :--- | :--- |
| 基本读写 | 写入后读取 | 返回正确值 |
| 容量淘汰 | 超过容量限制 | 淘汰最久未使用数据 |
| 更新值 | 对已存在的 key 写入新值 | 更新成功，位置移到表头 |
| 删除操作 | 删除指定 key | 返回被删除值 |
| 空缓存 | 从空缓存读取 | 返回 null |
| 边界容量 | 容量为 1 | 只能存一个元素 |

## 八、总结

LRU 缓存是一种高效的缓存淘汰策略，核心在于：
1. 使用 HashMap 实现 O(1) 查找
2. 使用双向链表维护访问顺序
3. 满容量时自动淘汰最久未使用的数据

两种实现方式各有优劣：
- **LinkedHashMap**：代码简洁，适合生产环境
- **HashMap + 双向链表**：原理清晰，适合学习和面试
