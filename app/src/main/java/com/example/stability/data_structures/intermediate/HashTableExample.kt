package com.example.stability.data_structures.intermediate

import android.util.Log
import java.util.LinkedList

/**
 * 哈希表是一种通过哈希函数将键映射到值的数据结构
 * 特点：插入、查找和删除操作的平均时间复杂度都是 O(1)
 */
class HashTableExample {
    
    private val capacity = 10
    private val table = Array<LinkedList<Entry>>(capacity) { LinkedList() }
    
    /**
     * 哈希表条目类
     */
    private data class Entry(val key: String, val value: Int)
    
    /**
     * 运行哈希表示例
     */
    fun runHashTableExample() {
        Log.d("DataStructures", "=== HashTableExample.runHashTableExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 插入元素
        put("Alice", 25)
        put("Bob", 30)
        put("Charlie", 35)
        put("David", 40)
        put("Eve", 45)
        Log.d("DataStructures", "插入元素完成")
        
        // 2. 查找元素
        val aliceAge = get("Alice")
        Log.d("DataStructures", "Alice 的年龄: $aliceAge")
        
        val bobAge = get("Bob")
        Log.d("DataStructures", "Bob 的年龄: $bobAge")
        
        val frankAge = get("Frank")
        Log.d("DataStructures", "Frank 的年龄: $frankAge")
        
        // 3. 更新元素
        put("Alice", 26)
        val updatedAliceAge = get("Alice")
        Log.d("DataStructures", "更新后 Alice 的年龄: $updatedAliceAge")
        
        // 4. 删除元素
        val removed = remove("Bob")
        Log.d("DataStructures", "删除 Bob: $removed")
        val bobAgeAfterRemoval = get("Bob")
        Log.d("DataStructures", "删除后 Bob 的年龄: $bobAgeAfterRemoval")
        
        // 5. 检查哈希表是否为空
        val isEmpty = isEmpty()
        Log.d("DataStructures", "哈希表是否为空: $isEmpty")
        
        Log.d("DataStructures", "=== HashTableExample.runHashTableExample completed ===")
    }
    
    /**
     * 哈希函数
     */
    private fun hash(key: String): Int {
        return key.fold(0) { acc, char ->
            (acc * 31 + char.code) % capacity
        }
    }
    
    /**
     * 插入元素
     * 时间复杂度: 平均 O(1)
     */
    private fun put(key: String, value: Int) {
        val index = hash(key)
        val bucket = table[index]
        
        // 使用 removeIf 删除已存在的键
        bucket.removeIf { it.key == key }
        
        // 添加新条目
        bucket.add(Entry(key, value))
        Log.d("DataStructures", "插入元素: $key -> $value")
    }
    
    /**
     * 查找元素
     * 时间复杂度: 平均 O(1)
     */
    private fun get(key: String): Int? {
        val index = hash(key)
        return table[index].firstOrNull { it.key == key }?.value
    }
    
    /**
     * 删除元素
     * 时间复杂度: 平均 O(1)
     */
    private fun remove(key: String): Boolean {
        val index = hash(key)
        return table[index].removeIf { it.key == key }
    }
    
    /**
     * 检查哈希表是否为空（使用函数式操作）
     */
    private fun isEmpty(): Boolean {
        return table.none { it.isNotEmpty() }
    }
}