package com.example.stability.data_structures.intermediate

import android.util.Log

/**
 * 堆是一种特殊的完全二叉树，它的每个节点都大于等于（或小于等于）其子节点
 * 特点：最大堆的堆顶是最大元素，最小堆的堆顶是最小元素
 */
class HeapExample {
    
    private val heap = mutableListOf<Int>()
    
    /**
     * 运行堆示例
     */
    fun runHeapExample() {
        Log.d("DataStructures", "=== HeapExample.runHeapExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 插入元素
        insert(5)
        insert(3)
        insert(8)
        insert(4)
        insert(2)
        insert(7)
        insert(1)
        insert(6)
        Log.d("DataStructures", "插入元素完成，堆的大小: ${heap.size}")
        printHeap()
        
        // 2. 查看堆顶元素
        val topElement = peek()
        Log.d("DataStructures", "堆顶元素: $topElement")
        
        // 3. 删除堆顶元素
        val removedElement = remove()
        Log.d("DataStructures", "删除堆顶元素: $removedElement")
        printHeap()
        
        // 4. 再次删除堆顶元素
        remove()
        Log.d("DataStructures", "再次删除堆顶元素后:")
        printHeap()
        
        // 5. 检查堆是否为空
        val isEmpty = isEmpty()
        Log.d("DataStructures", "堆是否为空: $isEmpty")
        
        // 6. 测试堆的应用 - 堆排序
        val array = intArrayOf(5, 3, 8, 4, 2, 7, 1, 6)
        Log.d("DataStructures", "排序前: ${array.contentToString()}")
        heapSort(array)
        Log.d("DataStructures", "排序后: ${array.contentToString()}")
        
        Log.d("DataStructures", "=== HeapExample.runHeapExample completed ===")
    }
    
    /**
     * 插入元素
     * 时间复杂度: O(log n)
     */
    private fun insert(value: Int) {
        // 将元素添加到堆的末尾
        heap.add(value)
        // 向上调整堆
        heapifyUp(heap.size - 1)
        Log.d("DataStructures", "插入元素: $value")
    }
    
    /**
     * 删除堆顶元素
     * 时间复杂度: O(log n)
     */
    private fun remove(): Int? {
        if (isEmpty()) {
            Log.d("DataStructures", "堆为空，无法删除元素")
            return null
        }
        
        // 保存堆顶元素
        val top = heap[0]
        // 将堆的最后一个元素移到堆顶
        heap[0] = heap[heap.size - 1]
        // 删除最后一个元素
        heap.removeAt(heap.size - 1)
        // 向下调整堆
        if (heap.isNotEmpty()) {
            heapifyDown(0)
        }
        
        return top
    }
    
    /**
     * 查看堆顶元素
     * 时间复杂度: O(1)
     */
    private fun peek(): Int? {
        if (isEmpty()) {
            Log.d("DataStructures", "堆为空，无法查看堆顶元素")
            return null
        }
        
        return heap[0]
    }
    
    /**
     * 检查堆是否为空
     */
    private fun isEmpty(): Boolean {
        return heap.isEmpty()
    }
    
    /**
     * 向上调整堆
     */
    private fun heapifyUp(index: Int) {
        var current = index
        var parent = (current - 1) / 2
        
        while (current > 0 && heap[current] > heap[parent]) {
            // 交换当前节点和父节点
            val temp = heap[current]
            heap[current] = heap[parent]
            heap[parent] = temp
            
            current = parent
            parent = (current - 1) / 2
        }
    }
    
    /**
     * 向下调整堆
     */
    private fun heapifyDown(index: Int) {
        var current = index
        var leftChild = 2 * current + 1
        var rightChild = 2 * current + 2
        var largest = current
        
        // 找到当前节点、左子节点、右子节点中的最大值
        if (leftChild < heap.size && heap[leftChild] > heap[largest]) {
            largest = leftChild
        }
        if (rightChild < heap.size && heap[rightChild] > heap[largest]) {
            largest = rightChild
        }
        
        // 如果最大值不是当前节点，交换并继续调整
        if (largest != current) {
            val temp = heap[current]
            heap[current] = heap[largest]
            heap[largest] = temp
            
            heapifyDown(largest)
        }
    }
    
    /**
     * 打印堆
     */
    private fun printHeap() {
        Log.d("DataStructures", "堆内容: $heap")
    }
    
    /**
     * 堆排序
     * 时间复杂度: O(n log n)
     * 应用：优先队列、堆排序等
     */
    private fun heapSort(array: IntArray) {
        val n = array.size
        
        // 构建最大堆
        for (i in n / 2 - 1 downTo 0) {
            heapify(array, n, i)
        }
        
        // 逐个提取元素
        for (i in n - 1 downTo 1) {
            // 交换堆顶和当前末尾元素
            val temp = array[0]
            array[0] = array[i]
            array[i] = temp
            
            // 对剩余的堆进行调整
            heapify(array, i, 0)
        }
    }
    
    /**
     * 调整堆
     */
    private fun heapify(array: IntArray, n: Int, i: Int) {
        var largest = i
        val left = 2 * i + 1
        val right = 2 * i + 2
        
        if (left < n && array[left] > array[largest]) {
            largest = left
        }
        if (right < n && array[right] > array[largest]) {
            largest = right
        }
        
        if (largest != i) {
            val temp = array[i]
            array[i] = array[largest]
            array[largest] = temp
            
            heapify(array, n, largest)
        }
    }
}