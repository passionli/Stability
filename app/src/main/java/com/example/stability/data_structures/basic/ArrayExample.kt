package com.example.stability.data_structures.basic

import android.util.Log

/**
 * 数组是最基础的数据结构，它是一种连续的内存空间，用于存储相同类型的元素
 * 特点：随机访问时间复杂度 O(1)，插入和删除时间复杂度 O(n)
 */
class ArrayExample {
    
    /**
     * 运行数组示例
     */
    fun runArrayExample() {
        Log.d("DataStructures", "=== ArrayExample.runArrayExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 创建数组
        // 固定大小的数组
        val fixedArray = IntArray(5)
        Log.d("DataStructures", "创建了一个大小为 5 的整型数组")
        
        // 带初始值的数组
        val initializedArray = intArrayOf(1, 2, 3, 4, 5)
        Log.d("DataStructures", "创建了一个带初始值的数组: ${initializedArray.contentToString()}")
        
        // 2. 访问数组元素
        // 时间复杂度 O(1)
        val element = initializedArray[2]
        Log.d("DataStructures", "访问数组索引 2 的元素: $element")
        
        // 3. 修改数组元素
        // 时间复杂度 O(1)
        initializedArray[2] = 10
        Log.d("DataStructures", "修改数组索引 2 的元素后: ${initializedArray.contentToString()}")
        
        // 4. 遍历数组
        Log.d("DataStructures", "遍历数组元素:")
        for (i in initializedArray.indices) {
            Log.d("DataStructures", "索引 $i: ${initializedArray[i]}")
        }
        
        // 5. 数组的常见操作
        // 查找元素
        val target = 4
        val index = findElement(initializedArray, target)
        Log.d("DataStructures", "查找元素 $target 的索引: $index")
        
        // 插入元素（需要创建新数组）
        val arrayAfterInsertion = insertElement(initializedArray, 2, 20)
        Log.d("DataStructures", "在索引 2 插入元素 20 后: ${arrayAfterInsertion.contentToString()}")
        
        // 删除元素（需要创建新数组）
        val arrayAfterDeletion = deleteElement(arrayAfterInsertion, 3)
        Log.d("DataStructures", "删除索引 3 的元素后: ${arrayAfterDeletion.contentToString()}")
        
        Log.d("DataStructures", "=== ArrayExample.runArrayExample completed ===")
    }
    
    /**
     * 查找元素在数组中的索引
     * 时间复杂度: O(n)
     */
    private fun findElement(array: IntArray, target: Int): Int {
        for (i in array.indices) {
            if (array[i] == target) {
                return i
            }
        }
        return -1 // 元素不存在
    }
    
    /**
     * 在指定索引插入元素
     * 时间复杂度: O(n)
     */
    private fun insertElement(array: IntArray, index: Int, value: Int): IntArray {
        // 创建一个新数组，大小比原数组大 1
        val newArray = IntArray(array.size + 1)
        
        // 复制原数组中索引小于 index 的元素
        for (i in 0 until index) {
            newArray[i] = array[i]
        }
        
        // 插入新元素
        newArray[index] = value
        
        // 复制原数组中索引大于等于 index 的元素
        for (i in index until array.size) {
            newArray[i + 1] = array[i]
        }
        
        return newArray
    }
    
    /**
     * 删除指定索引的元素
     * 时间复杂度: O(n)
     */
    private fun deleteElement(array: IntArray, index: Int): IntArray {
        // 创建一个新数组，大小比原数组小 1
        val newArray = IntArray(array.size - 1)
        
        // 复制原数组中索引小于 index 的元素
        for (i in 0 until index) {
            newArray[i] = array[i]
        }
        
        // 复制原数组中索引大于 index 的元素
        for (i in index + 1 until array.size) {
            newArray[i - 1] = array[i]
        }
        
        return newArray
    }
}