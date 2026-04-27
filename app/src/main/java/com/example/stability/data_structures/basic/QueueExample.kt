package com.example.stability.data_structures.basic

import android.util.Log

/**
 * 队列是一种先进先出（FIFO）的数据结构
 * 特点：enqueue（入队）和 dequeue（出队）操作的时间复杂度都是 O(1)
 */
class QueueExample {
    
    private val queue = mutableListOf<Int>()
    
    /**
     * 运行队列示例
     */
    fun runQueueExample() {
        Log.d("DataStructures", "=== QueueExample.runQueueExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 入队操作
        enqueue(1)
        enqueue(2)
        enqueue(3)
        enqueue(4)
        enqueue(5)
        Log.d("DataStructures", "入队操作完成，队列的大小: ${queue.size}")
        printQueue()
        
        // 2. 查看队首元素
        val frontElement = peek()
        Log.d("DataStructures", "队首元素: $frontElement")
        
        // 3. 出队操作
        val dequeuedElement = dequeue()
        Log.d("DataStructures", "出队元素: $dequeuedElement")
        printQueue()
        
        // 4. 再次出队
        dequeue()
        Log.d("DataStructures", "再次出队后:")
        printQueue()
        
        // 5. 检查队列是否为空
        val isEmpty = isEmpty()
        Log.d("DataStructures", "队列是否为空: $isEmpty")
        
        // 6. 清空队列
        clear()
        Log.d("DataStructures", "清空队列后:")
        printQueue()
        Log.d("DataStructures", "队列是否为空: ${isEmpty()}")
        
        // 7. 测试队列的应用 - 模拟打印机任务队列
        simulatePrinterQueue()
        
        Log.d("DataStructures", "=== QueueExample.runQueueExample completed ===")
    }
    
    /**
     * 入队操作
     * 时间复杂度: O(1)
     */
    private fun enqueue(value: Int) {
        queue.add(value)
        Log.d("DataStructures", "入队: $value")
    }
    
    /**
     * 出队操作
     * 时间复杂度: O(n) - 因为需要移除列表的第一个元素
     * 注：如果使用 LinkedList 实现，时间复杂度可以达到 O(1)
     */
    private fun dequeue(): Int? {
        if (isEmpty()) {
            Log.d("DataStructures", "队列为空，无法出队")
            return null
        }
        
        val value = queue.removeAt(0)
        Log.d("DataStructures", "出队: $value")
        return value
    }
    
    /**
     * 查看队首元素
     * 时间复杂度: O(1)
     */
    private fun peek(): Int? {
        if (isEmpty()) {
            Log.d("DataStructures", "队列为空，无法查看队首元素")
            return null
        }
        
        return queue[0]
    }
    
    /**
     * 检查队列是否为空
     */
    private fun isEmpty(): Boolean {
        return queue.isEmpty()
    }
    
    /**
     * 清空队列
     */
    private fun clear() {
        queue.clear()
        Log.d("DataStructures", "队列已清空")
    }
    
    /**
     * 打印队列
     */
    private fun printQueue() {
        Log.d("DataStructures", "队列内容: $queue")
    }
    
    /**
     * 模拟打印机任务队列
     * 应用：任务调度、消息队列等
     */
    private fun simulatePrinterQueue() {
        Log.d("DataStructures", "=== 模拟打印机任务队列 ===")
        
        // 创建任务队列
        val printerQueue = mutableListOf<String>()
        
        // 添加打印任务
        printerQueue.add("文档1.pdf")
        printerQueue.add("文档2.pdf")
        printerQueue.add("文档3.pdf")
        Log.d("DataStructures", "添加打印任务后: $printerQueue")
        
        // 处理打印任务
        while (printerQueue.isNotEmpty()) {
            val task = printerQueue.removeAt(0)
            Log.d("DataStructures", "正在打印: $task")
            // 模拟打印时间
            Thread.sleep(500)
            Log.d("DataStructures", "打印完成: $task")
        }
        
        Log.d("DataStructures", "所有打印任务已完成")
    }
}