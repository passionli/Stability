package com.example.stability.oom.prevention

import com.example.stability.oom.utils.OomLog
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 对象池操作结果密封类
 */
sealed class PoolOperation<T> {
    data class AcquiredFromPool<T>(val obj: T) : PoolOperation<T>()
    data class CreatedNew<T>(val obj: T) : PoolOperation<T>()
    data class ReleasedToPool<T>(val obj: T) : PoolOperation<T>()
    class Discarded<T> : PoolOperation<T>()
}

/**
 * 对象池
 * 用于复用对象，减少内存分配和 GC 压力
 */
class ObjectPool<T>(
    private val creator: () -> T,
    private val resetter: ((T) -> Unit)? = null,
    private val maxSize: Int = 100,
    private val logger: ((String) -> Unit)? = { OomLog.d("ObjectPool", it) }
) {
    
    private val pool = ConcurrentLinkedDeque<T>()
    
    /**
     * 获取对象（纯逻辑）
     */
    private fun acquireInternal(): PoolOperation<T> {
        return pool.poll()?.let {
            PoolOperation.AcquiredFromPool<T>(it)
        } ?: PoolOperation.CreatedNew(creator())
    }
    
    /**
     * 获取对象（带副作用）
     */
    fun acquire(): T {
        val result = acquireInternal()
        when (result) {
            is PoolOperation.AcquiredFromPool -> logger?.invoke("Acquired object from pool")
            is PoolOperation.CreatedNew -> logger?.invoke("Created new object")
            else -> {}
        }
        return when (result) {
            is PoolOperation.AcquiredFromPool -> result.obj
            is PoolOperation.CreatedNew -> result.obj
            else -> throw IllegalStateException("Unexpected pool operation result")
        }
    }
    
    /**
     * 释放对象回池（纯逻辑）
     */
    private fun releaseInternal(obj: T): PoolOperation<T> {
        resetter?.invoke(obj)
        return if (pool.size < maxSize) {
            pool.offer(obj)
            PoolOperation.ReleasedToPool(obj)
        } else {
            PoolOperation.Discarded()
        }
    }
    
    /**
     * 释放对象回池（带副作用）
     */
    fun release(obj: T) {
        val result = releaseInternal(obj)
        when (result) {
            is PoolOperation.ReleasedToPool -> logger?.invoke("Object released to pool, current size: ${pool.size}")
            is PoolOperation.Discarded -> logger?.invoke("Pool is full, object discarded")
            else -> {}
        }
    }
    
    fun clear() {
        pool.clear()
        logger?.invoke("Pool cleared")
    }
    
    fun size(): Int = pool.size
    fun isEmpty(): Boolean = pool.isEmpty()
    
    /**
     * 预填充对象池（使用函数式方式）
     */
    fun prefill(count: Int) {
        generateSequence { creator() }
            .take(count)
            .takeWhile { pool.size < maxSize }
            .forEach { pool.offer(it) }
        
        logger?.invoke("Prefilled ${pool.size} objects")
    }
    
    fun getStatus(): String = buildString {
        append("ObjectPool Status:\n")
        append("  Current Size: ${pool.size}\n")
        append("  Max Size: $maxSize\n")
        append("  Has Resetter: ${resetter != null}")
    }
}

class ObjectPoolBuilder<T> {
    
    private lateinit var creatorFunc: () -> T
    private var resetter: ((T) -> Unit)? = null
    private var maxSize = 100
    private var logger: ((String) -> Unit)? = null
    
    fun creator(creator: () -> T): ObjectPoolBuilder<T> {
        this.creatorFunc = creator
        return this
    }
    
    fun resetter(resetter: (T) -> Unit): ObjectPoolBuilder<T> {
        this.resetter = resetter
        return this
    }
    
    fun maxSize(maxSize: Int): ObjectPoolBuilder<T> {
        this.maxSize = maxSize
        return this
    }
    
    fun logger(logger: (String) -> Unit): ObjectPoolBuilder<T> {
        this.logger = logger
        return this
    }
    
    fun build(): ObjectPool<T> {
        require(::creatorFunc.isInitialized) { "Creator must be set" }
        return ObjectPool(creatorFunc, resetter, maxSize, logger)
    }
}