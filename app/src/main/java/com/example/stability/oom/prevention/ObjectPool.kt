package com.example.stability.oom.prevention

import com.example.stability.oom.utils.OomLog
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 对象池
 * 用于复用对象，减少内存分配和 GC 压力
 * @param T 对象类型
 * @param creator 对象创建器，用于创建新对象
 * @param resetter 对象重置器，用于重置对象状态（可选）
 * @param maxSize 最大池大小，超过此大小的对象将被丢弃
 */
class ObjectPool<T>(
    private val creator: () -> T,
    private val resetter: ((T) -> Unit)? = null,
    private val maxSize: Int = 100
) {
    
    /**
     * 对象池存储（使用线程安全的队列）
     */
    private val pool = ConcurrentLinkedDeque<T>()
    
    /**
     * 获取对象
     * 如果池中有可用对象，则返回；否则创建新对象
     * @return 对象实例
     */
    fun acquire(): T {
        // 从池中获取对象
        val obj = pool.poll()
        
        return if (obj != null) {
            OomLog.d("ObjectPool", "Acquired object from pool")
            obj
        } else {
            OomLog.d("ObjectPool", "Created new object")
            creator()
        }
    }
    
    /**
     * 释放对象回池
     * 如果池未满，则将对象放回池中；否则丢弃
     * @param obj 需要释放的对象
     */
    fun release(obj: T) {
        // 如果设置了重置器，先重置对象状态
        resetter?.invoke(obj)
        
        // 如果池未满，将对象放回池中
        if (pool.size < maxSize) {
            pool.offer(obj)
            OomLog.d("ObjectPool", "Object released to pool, current size: ${pool.size}")
        } else {
            // 池已满，丢弃对象
            OomLog.d("ObjectPool", "Pool is full, object discarded")
        }
    }
    
    /**
     * 清空对象池
     */
    fun clear() {
        pool.clear()
        OomLog.d("ObjectPool", "Pool cleared")
    }
    
    /**
     * 获取当前池大小
     * @return 当前池中对象数量
     */
    fun size(): Int {
        return pool.size
    }
    
    /**
     * 检查池是否为空
     * @return true 如果池为空，否则 false
     */
    fun isEmpty(): Boolean {
        return pool.isEmpty()
    }
    
    /**
     * 预填充对象池
     * @param count 预填充的对象数量
     */
    fun prefill(count: Int) {
        repeat(count) {
            if (pool.size < maxSize) {
                pool.offer(creator())
            }
        }
        OomLog.d("ObjectPool", "Prefilled ${pool.size} objects")
    }
    
    /**
     * 获取池状态信息
     * @return 池状态字符串
     */
    fun getStatus(): String {
        return buildString {
            append("ObjectPool Status:\n")
            append("  Current Size: ${pool.size}\n")
            append("  Max Size: $maxSize\n")
            append("  Has Resetter: ${resetter != null}")
        }
    }
}

/**
 * 对象池构建器
 * 提供链式调用方式创建对象池
 */
class ObjectPoolBuilder<T> {
    
    private lateinit var creatorFunc: () -> T
    private var resetter: ((T) -> Unit)? = null
    private var maxSize = 100
    
    /**
     * 设置对象创建器
     */
    fun creator(creator: () -> T): ObjectPoolBuilder<T> {
        this.creatorFunc = creator
        return this
    }
    
    /**
     * 设置对象重置器
     */
    fun resetter(resetter: (T) -> Unit): ObjectPoolBuilder<T> {
        this.resetter = resetter
        return this
    }
    
    /**
     * 设置最大池大小
     */
    fun maxSize(maxSize: Int): ObjectPoolBuilder<T> {
        this.maxSize = maxSize
        return this
    }
    
    /**
     * 构建对象池
     */
    fun build(): ObjectPool<T> {
        require(::creatorFunc.isInitialized) { "Creator must be set" }
        return ObjectPool(creatorFunc, resetter, maxSize)
    }
}
