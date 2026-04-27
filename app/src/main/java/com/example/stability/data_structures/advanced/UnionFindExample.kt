package com.example.stability.data_structures.advanced

import android.util.Log

/**
 * 并查集是一种用于处理集合合并和查询的树形数据结构
 * 特点：可以高效地进行元素所属集合的查询和集合的合并操作
 */
class UnionFindExample {
    
    private var parent: IntArray
    private var rank: IntArray
    
    /**
     * 构造函数
     */
    constructor(size: Int) {
        parent = IntArray(size)
        rank = IntArray(size)
        
        // 初始化每个元素的父节点为自己
        for (i in 0 until size) {
            parent[i] = i
            rank[i] = 1
        }
    }
    
    /**
     * 运行并查集示例
     */
    fun runUnionFindExample() {
        Log.d("DataStructures", "=== UnionFindExample.runUnionFindExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 创建并查集，包含 10 个元素
        val uf = UnionFindExample(10)
        Log.d("DataStructures", "创建了一个大小为 10 的并查集")
        
        // 合并集合
        uf.union(0, 1)
        uf.union(2, 3)
        uf.union(4, 5)
        uf.union(6, 7)
        uf.union(8, 9)
        uf.union(0, 2)
        uf.union(4, 6)
        uf.union(0, 4)
        Log.d("DataStructures", "合并操作完成")
        
        // 查询元素所属的集合
        for (i in 0 until 10) {
            val root = uf.find(i)
            Log.d("DataStructures", "元素 $i 的根节点: $root")
        }
        
        // 检查两个元素是否在同一个集合中
        val isConnected1 = uf.isConnected(0, 9)
        Log.d("DataStructures", "元素 0 和 9 是否在同一个集合中: $isConnected1")
        
        val isConnected2 = uf.isConnected(0, 8)
        Log.d("DataStructures", "元素 0 和 8 是否在同一个集合中: $isConnected2")
        
        // 合并 0 和 8
        uf.union(0, 8)
        Log.d("DataStructures", "合并元素 0 和 8 后")
        
        // 再次检查
        val isConnected3 = uf.isConnected(0, 9)
        Log.d("DataStructures", "元素 0 和 9 是否在同一个集合中: $isConnected3")
        
        Log.d("DataStructures", "=== UnionFindExample.runUnionFindExample completed ===")
    }
    
    /**
     * 查找元素所属集合的根节点
     * 时间复杂度: 接近 O(1)
     */
    private fun find(x: Int): Int {
        // 路径压缩：将 x 的父节点直接指向根节点
        if (parent[x] != x) {
            parent[x] = find(parent[x])
        }
        return parent[x]
    }
    
    /**
     * 合并两个元素所属的集合
     * 时间复杂度: 接近 O(1)
     */
    private fun union(x: Int, y: Int) {
        val rootX = find(x)
        val rootY = find(y)
        
        // 如果已经在同一个集合中，不需要合并
        if (rootX == rootY) {
            return
        }
        
        // 按秩合并：将秩小的树合并到秩大的树中
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX
        } else {
            // 秩相等，合并后秩加 1
            parent[rootY] = rootX
            rank[rootX]++
        }
        
        Log.d("DataStructures", "合并元素 $x 和 $y")
    }
    
    /**
     * 检查两个元素是否在同一个集合中
     */
    private fun isConnected(x: Int, y: Int): Boolean {
        return find(x) == find(y)
    }
}