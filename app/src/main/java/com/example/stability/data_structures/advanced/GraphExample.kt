package com.example.stability.data_structures.advanced

import android.util.Log
import java.util.*

/**
 * 图是一种由顶点和边组成的数据结构
 * 特点：可以表示各种复杂的关系，如社交网络、地图等
 */
class GraphExample {

    private class Node {
        var value : Int = 0
        // 出度
        var children = mutableListOf<Node>()
        // 入度
        var parents = mutableListOf<Node>()
    }

    private val nodes = mutableMapOf<Int, Node>()
    
    private val adjacencyList = mutableMapOf<Int, MutableList<Int>>()
    
    /**
     * 运行图示例
     */
    fun runGraphExample() {
        Log.d("DataStructures", "=== GraphExample.runGraphExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 创建无向图
        createGraph()
        Log.d("DataStructures", "创建了一个无向图")
        printGraph()
        
        // 2. 深度优先遍历
        Log.d("DataStructures", "深度优先遍历:")
        dfs(0)
        
        // 3. 广度优先遍历
        Log.d("DataStructures", "广度优先遍历:")
        bfs(0)
        
        // 4. 检查两个顶点之间是否存在路径
        val hasPath = hasPath(0, 4)
        Log.d("DataStructures", "顶点 0 到顶点 4 是否存在路径: $hasPath")
        
        val hasPath2 = hasPath(0, 5)
        Log.d("DataStructures", "顶点 0 到顶点 5 是否存在路径: $hasPath2")
        
        // 5. 拓扑排序
        // 清空当前图，创建有向无环图
        adjacencyList.clear()
        createDAG()
        Log.d("DataStructures", "创建了一个有向无环图 (DAG)")
        printGraph()
        
        val topologicalOrder = topologicalSort()
        Log.d("DataStructures", "拓扑排序结果: $topologicalOrder")
        
        Log.d("DataStructures", "=== GraphExample.runGraphExample completed ===")
    }
    
    /**
     * 创建一个图
     */
    private fun createGraph() {
        // 0 -> 1 -> 3
        // 0 -> 1 -> 3 -> 4
        // 0 -> 1 -> 3 -> 4 -> 5
        // 0 -> 1 -> 3 -> 5
        // 0 -> 1 -> 4
        // 0 -> 2 -> 4
        // 添加顶点和边
        addEdge(0, 1)
        addEdge(0, 2)
        addEdge(1, 3)
        addEdge(1, 4)
        addEdge(2, 4)
        addEdge(3, 4)
        addEdge(3, 5)
        addEdge(4, 5)
    }
    
    /**
     * 添加边
     */
    private fun addEdge(source: Int, destination: Int) {
        // 添加从 source 到 destination 的边
        if (!adjacencyList.containsKey(source)) {
            adjacencyList[source] = mutableListOf()
        }
        adjacencyList[source]?.add(destination)
        
        // 添加从 destination 到 source 的边（无向图）
        if (!adjacencyList.containsKey(destination)) {
            adjacencyList[destination] = mutableListOf()
        }
        adjacencyList[destination]?.add(source)
        
        Log.d("DataStructures", "添加边: $source -> $destination")
    }
    
    /**
     * 打印图
     */
    private fun printGraph() {
        adjacencyList.forEach { (vertex, neighbors) ->
            Log.d("DataStructures", "顶点 $vertex 的邻居: $neighbors")
        }
    }
    
    /**
     * 深度优先遍历
     * 时间复杂度: O(V + E)
     */
    private fun dfs(startVertex: Int) {
        val visited = mutableSetOf<Int>()
        dfsHelper(startVertex, visited)
    }
    
    /**
     * 深度优先遍历辅助函数
     */
    private fun dfsHelper(vertex: Int, visited: MutableSet<Int>) {
        // 标记当前顶点为已访问
        visited.add(vertex)
        Log.d("DataStructures", "访问顶点: $vertex")

        // 递归访问所有未访问的邻居
        adjacencyList[vertex].orEmpty()
            .filterNot { it in visited }
            .forEach { dfsHelper(it, visited) }
    }
    
    /**
     * 广度优先遍历
     * 时间复杂度: O(V + E)
     */
    private fun bfs(startVertex: Int) {
        val visited = mutableSetOf<Int>()
        val queue = ArrayDeque<Int>()

        // 标记起始顶点为已访问并加入队列
        visited.add(startVertex)
        queue.add(startVertex)

        generateSequence(queue.poll()) { queue.poll() }
            .onEach { vertex ->
                Log.d("DataStructures", "访问顶点: $vertex")
            }
            .flatMap { vertex ->
                adjacencyList[vertex].orEmpty()
                    .filterNot { it in visited }
                    .onEach { neighbor ->
                        visited.add(neighbor)
                        queue.add(neighbor)
                    }
            }
            .toList()
    }
    
    /**
     * 检查两个顶点之间是否存在路径
     */
    private fun hasPath(source: Int, destination: Int): Boolean {
        val visited = mutableSetOf<Int>()
        return hasPathHelper(source, destination, visited)
    }
    
    /**
     * 检查路径辅助函数
     */
    private fun hasPathHelper(source: Int, destination: Int, visited: MutableSet<Int>): Boolean =
        when {
            source == destination -> true
            else -> {
                visited.add(source)
                adjacencyList[source].orEmpty()
                    .filterNot { it in visited }
                    .any { hasPathHelper(it, destination, visited) }
            }
        }
    
    /**
     * 创建一个有向无环图 (DAG)
     */
    private fun createDAG() {
        // 添加顶点和有向边
        addDirectedEdge(0, 1)
        addDirectedEdge(0, 2)
        addDirectedEdge(1, 3)
        addDirectedEdge(2, 3)
        addDirectedEdge(2, 4)
        addDirectedEdge(3, 5)
        addDirectedEdge(4, 5)
    }
    
    /**
     * 添加有向边
     */
    private fun addDirectedEdge(source: Int, destination: Int) {
        // 添加从 source 到 destination 的边（仅单向）
        if (!adjacencyList.containsKey(source)) {
            adjacencyList[source] = mutableListOf()
        }
        adjacencyList[source]?.add(destination)
        
        // 确保目标顶点存在
        if (!adjacencyList.containsKey(destination)) {
            adjacencyList[destination] = mutableListOf()
        }
        
        Log.d("DataStructures", "添加有向边: $source -> $destination")
    }
    
    /**
     * 拓扑排序
     * 时间复杂度: O(V + E)
     * 应用：任务调度、课程安排、依赖解析等
     */
    private fun topologicalSort(): List<Int> {
        val result = mutableListOf<Int>()
        val visited = mutableSetOf<Int>()
        val tempMark = mutableSetOf<Int>() // 用于检测环

        // 对每个未访问的顶点进行深度优先搜索
        val hasCycle = adjacencyList.keys
            .filterNot { it in visited }
            .any { vertex ->
                !topologicalSortHelper(vertex, visited, tempMark, result)
            }

        return if (hasCycle) {
            Log.d("DataStructures", "图中存在环，无法进行拓扑排序")
            emptyList()
        } else {
            result.reversed()
        }
    }
    
    /**
     * 拓扑排序辅助函数
     */
    private fun topologicalSortHelper(
        vertex: Int,
        visited: MutableSet<Int>,
        tempMark: MutableSet<Int>,
        result: MutableList<Int>
    ): Boolean = when {
        // 检测环
        vertex in tempMark -> false
        vertex in visited -> true
        else -> {
            // 临时标记当前顶点
            tempMark.add(vertex)

            // 递归访问所有邻居
            val hasCycle = adjacencyList[vertex].orEmpty()
                .any { neighbor ->
                    !topologicalSortHelper(neighbor, visited, tempMark, result)
                }

            if (hasCycle) {
                false
            } else {
                // 移除临时标记
                tempMark.remove(vertex)
                // 标记为已访问
                visited.add(vertex)
                // 添加到结果列表
                result.add(vertex)
                true
            }
        }
    }
}