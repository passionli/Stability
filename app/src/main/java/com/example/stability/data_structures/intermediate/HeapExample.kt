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
        heap.add(value)
        heapifyUpRecursive(heap.size - 1)
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
        
        val top = heap[0]
        if (heap.size > 1) {
            heap[0] = heap.last()
        }
        heap.removeLast()
        
        if (heap.isNotEmpty()) {
            heapifyDownRecursive(0)
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
    private fun isEmpty(): Boolean = heap.isEmpty()
    
    /**
     * 递归向上调整堆
     */
    private fun heapifyUpRecursive(index: Int) {
        if (index <= 0) return
        
        val parent = (index - 1) / 2
        
        if (heap[index] > heap[parent]) {
            swap(index, parent)
            heapifyUpRecursive(parent)
        }
    }
    
    /**
     * 递归向下调整堆
     */
    private fun heapifyDownRecursive(index: Int) {
        val leftChild = 2 * index + 1
        val rightChild = 2 * index + 2
        
        val largest = findLargestIndex(index, leftChild, rightChild)
        
        if (largest != index) {
            swap(index, largest)
            heapifyDownRecursive(largest)
        }
    }
    
    /**
     * 找到最大元素的索引
     */
    private fun findLargestIndex(current: Int, left: Int, right: Int): Int {
        var largest = current
        
        if (left < heap.size && heap[left] > heap[largest]) {
            largest = left
        }
        if (right < heap.size && heap[right] > heap[largest]) {
            largest = right
        }
        
        return largest
    }
    
    /**
     * 交换元素
     */
    private fun swap(i: Int, j: Int) {
        val temp = heap[i]
        heap[i] = heap[j]
        heap[j] = temp
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
     */
    private fun heapSort(array: IntArray) {
        val n = array.size
        
        // 使用函数式方式构建最大堆
        (n / 2 - 1 downTo 0).forEach { heapify(array, n, it) }
        
        // 使用函数式方式逐个提取元素
        (n - 1 downTo 1).forEach { i ->
            swap(array, 0, i)
            heapify(array, i, 0)
        }
    }
    
    /**
     * 调整堆（函数式风格）
     */
    private fun heapify(array: IntArray, n: Int, i: Int) {
        val largest = findLargestIndex(array, n, i, 2 * i + 1, 2 * i + 2)
        
        if (largest != i) {
            swap(array, i, largest)
            heapify(array, n, largest)
        }
    }
    
    /**
     * 找到数组中最大元素的索引
     */
    private fun findLargestIndex(array: IntArray, n: Int, current: Int, left: Int, right: Int): Int {
        var largest = current
        
        if (left < n && array[left] > array[largest]) {
            largest = left
        }
        if (right < n && array[right] > array[largest]) {
            largest = right
        }
        
        return largest
    }
    
    /**
     * 交换数组元素
     */
    private fun swap(array: IntArray, i: Int, j: Int) {
        val temp = array[i]
        array[i] = array[j]
        array[j] = temp
    }
}