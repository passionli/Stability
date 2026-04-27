package com.example.stability.data_structures

import android.util.Log
import com.example.stability.data_structures.advanced.GraphExample
import com.example.stability.data_structures.advanced.TrieExample
import com.example.stability.data_structures.advanced.UnionFindExample
import com.example.stability.data_structures.basic.ArrayExample
import com.example.stability.data_structures.basic.LinkedListExample
import com.example.stability.data_structures.basic.QueueExample
import com.example.stability.data_structures.basic.StackExample
import com.example.stability.data_structures.intermediate.BinaryTreeExample
import com.example.stability.data_structures.intermediate.HashTableExample
import com.example.stability.data_structures.intermediate.HeapExample

/**
 * 数据结构学习主类，用于运行所有数据结构示例
 */
class DataStructuresMain {
    
    /**
     * 运行所有数据结构示例
     */
    fun runAllExamples() {
        Log.d("DataStructures", "=== DataStructuresMain.runAllExamples called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行初级数据结构示例
        runBasicExamples()
        
        // 运行中级数据结构示例
        runIntermediateExamples()
        
        // 运行高级数据结构示例
        runAdvancedExamples()
        
        Log.d("DataStructures", "=== DataStructuresMain.runAllExamples completed ===")
    }
    
    /**
     * 运行初级数据结构示例
     */
    private fun runBasicExamples() {
        Log.d("DataStructures", "=== 运行初级数据结构示例 ===")
        
        // 数组示例
        val arrayExample = ArrayExample()
        arrayExample.runArrayExample()
        
        // 链表示例
        val linkedListExample = LinkedListExample()
        linkedListExample.runLinkedListExample()
        
        // 栈示例
        val stackExample = StackExample()
        stackExample.runStackExample()
        
        // 队列示例
        val queueExample = QueueExample()
        queueExample.runQueueExample()
        
        Log.d("DataStructures", "=== 初级数据结构示例运行完成 ===")
    }
    
    /**
     * 运行中级数据结构示例
     */
    private fun runIntermediateExamples() {
        Log.d("DataStructures", "=== 运行中级数据结构示例 ===")
        
        // 二叉树示例
        val binaryTreeExample = BinaryTreeExample()
        binaryTreeExample.runBinaryTreeExample()
        
        // 堆示例
        val heapExample = HeapExample()
        heapExample.runHeapExample()
        
        // 哈希表示例
        val hashTableExample = HashTableExample()
        hashTableExample.runHashTableExample()
        
        Log.d("DataStructures", "=== 中级数据结构示例运行完成 ===")
    }
    
    /**
     * 运行高级数据结构示例
     */
    private fun runAdvancedExamples() {
        Log.d("DataStructures", "=== 运行高级数据结构示例 ===")
        
        // 图示例
        val graphExample = GraphExample()
        graphExample.runGraphExample()
        
        // 前缀树示例
        val trieExample = TrieExample()
        trieExample.runTrieExample()
        
        // 并查集示例
        val unionFindExample = UnionFindExample(10)
        unionFindExample.runUnionFindExample()
        
        Log.d("DataStructures", "=== 高级数据结构示例运行完成 ===")
    }
}