package com.example.stability.data_structures.advanced

import java.util.*

object LogUtils {
    private var logger: (String, String) -> Unit = { tag, message -> println("$tag: $message") }
    
    init {
        try {
            val logClass = Class.forName("android.util.Log")
            val method = logClass.getMethod("d", String::class.java, String::class.java)
            logger = { tag, message -> 
                try {
                    method.invoke(null, tag, message)
                } catch (e: Exception) {
                    println("$tag: $message")
                }
            }
        } catch (e: Exception) {
            // 不是 Android 环境，使用控制台输出
        }
    }
    
    fun d(tag: String, message: String) {
        logger(tag, message)
    }
}

/**
 * 图节点类
 * 封装节点的属性和行为
 */
class GraphNode(val value: Int) {
    private val _children = mutableListOf<GraphNode>()
    private val _parents = mutableListOf<GraphNode>()
    
    val children: List<GraphNode> get() = _children.toList()
    val parents: List<GraphNode> get() = _parents.toList()
    
    fun addChild(node: GraphNode) {
        if (!_children.contains(node)) {
            _children.add(node)
            node.addParent(this)
        }
    }
    
    private fun addParent(node: GraphNode) {
        if (!_parents.contains(node)) {
            _parents.add(node)
        }
    }
    
    fun removeChild(node: GraphNode) {
        if (_children.remove(node)) {
            node.removeParent(this)
        }
    }
    
    fun removeParent(node: GraphNode) {
        _parents.remove(node)
    }
    
    fun hasChild(node: GraphNode): Boolean = _children.contains(node)
    
    fun getChildCount(): Int = _children.size
    
    fun getParentCount(): Int = _parents.size
}

/**
 * 图类
 * 封装图的整体操作和状态管理
 */
class Graph {
    private val nodes = mutableMapOf<Int, GraphNode>()
    
    fun getOrCreateNode(value: Int): GraphNode {
        return nodes.getOrPut(value) { GraphNode(value) }
    }
    
    fun addEdge(sourceValue: Int, destValue: Int) {
        val source = getOrCreateNode(sourceValue)
        val dest = getOrCreateNode(destValue)
        source.addChild(dest)
        LogUtils.d("DataStructures", "添加边: $sourceValue -> $destValue")
    }
    
    fun addUndirectedEdge(sourceValue: Int, destValue: Int) {
        addEdge(sourceValue, destValue)
        addEdge(destValue, sourceValue)
    }
    
    fun getNode(value: Int): GraphNode? = nodes[value]
    
    fun containsNode(value: Int): Boolean = nodes.containsKey(value)
    
    fun removeNode(value: Int): GraphNode? {
        val node = nodes[value]
        node?.let { removeNode(it) }
        return node
    }
    
    fun removeNode(node: GraphNode): GraphNode {
        // 从 nodes Map 中移除节点
        nodes.remove(node.value)
        
        // 对于当前节点的每个子节点，从子节点的父节点列表中移除当前节点
        for (child in node.children) {
            child.removeParent(node)
        }
        
        // 对于当前节点的每个父节点，从父节点的子节点列表中移除当前节点
        for (parent in node.parents) {
            parent.removeChild(node)
        }
        
        return node
    }
    
    fun clear() {
        nodes.clear()
    }
    
    fun getNodeCount(): Int = nodes.size
    
    fun getEdgeCount(): Int {
        return nodes.values.sumOf { it.getChildCount() }
    }
    
    fun printGraph() {
        for ((value, node) in nodes) {
            val children = node.children.map { it.value }
            val parents = node.parents.map { it.value }
            LogUtils.d("DataStructures", "顶点 $value: 父节点=$parents, 子节点=$children")
        }
    }

    fun getNodeNoParent() : GraphNode? {
        for ((value, node) in nodes) {
            if (node.parents.isEmpty()) {
                return node
            }
        }
        return null
    }
}

/**
 * 图遍历算法类
 * 封装各种图遍历算法
 */
class GraphTraversal {
    
    companion object {
        
        /**
         * 深度优先遍历
         * 时间复杂度: O(V + E)
         */
        fun dfs(graph: Graph, startValue: Int, visitor: (Int) -> Unit) {
            val visited = mutableSetOf<Int>()
            val startNode = graph.getNode(startValue)
            startNode?.let { dfsHelper(it, visited, visitor) }
        }
        
        private fun dfsHelper(node: GraphNode, visited: MutableSet<Int>, visitor: (Int) -> Unit) {
            visited.add(node.value)
            visitor(node.value)
            
            for (child in node.children) {
                if (!visited.contains(child.value)) {
                    dfsHelper(child, visited, visitor)
                }
            }
        }
        
        /**
         * 广度优先遍历
         * 时间复杂度: O(V + E)
         */
        fun bfs(graph: Graph, startValue: Int, visitor: (Int) -> Unit) {
            val visited = mutableSetOf<Int>()
            val queue: Queue<GraphNode> = LinkedList()
            
            val startNode = graph.getNode(startValue)
            startNode?.let {
                visited.add(it.value)
                queue.offer(it)
                
                while (queue.isNotEmpty()) {
                    val node = queue.poll()
                    visitor(node.value)
                    
                    for (child in node.children) {
                        if (!visited.contains(child.value)) {
                            visited.add(child.value)
                            queue.offer(child)
                        }
                    }
                }
            }
        }
        
        /**
         * 检查两个顶点之间是否存在路径
         */
        fun hasPath(graph: Graph, sourceValue: Int, destValue: Int): Boolean {
            val visited = mutableSetOf<Int>()
            val source = graph.getNode(sourceValue)
            val dest = graph.getNode(destValue)
            
            if (source == null || dest == null) {
                return false
            }
            
            return hasPathHelper(source, dest.value, visited)
        }
        
        private fun hasPathHelper(current: GraphNode, destValue: Int, visited: MutableSet<Int>): Boolean {
            if (current.value == destValue) {
                return true
            }
            
            visited.add(current.value)
            
            for (child in current.children) {
                if (!visited.contains(child.value) && hasPathHelper(child, destValue, visited)) {
                    return true
                }
            }
            
            return false
        }

        /**
         * 拓扑排序 V2（Kahn 算法 - 基于入度的迭代方法）
         * 
         * Kahn 算法步骤：
         * 1. 计算所有节点的入度
         * 2. 将入度为 0 的节点加入队列
         * 3. 从队列中取出节点，加入结果列表
         * 4. 减少该节点所有邻居的入度
         * 5. 如果邻居入度变为 0，加入队列
         * 6. 重复步骤 3-5 直到队列为空
         * 
         * 如果结果列表长度不等于节点总数，说明图中存在环
         * 
         * 时间复杂度: O(V + E)
         * 空间复杂度: O(V)
         * 
         * @param graph 待排序的图
         * @return 拓扑排序结果列表，如果图中存在环则返回空列表
         */
        fun topologicalSortV2(graph: Graph): List<Int> {
            // 存储最终的拓扑排序结果
            val result = mutableListOf<Int>()
            // 图的节点总数
            val nodeCount = graph.getNodeCount()
            
            // 每一轮添加一个入度为 0 的节点
            // 循环最多执行 nodeCount 次（每个节点处理一次）
            for (i in 0 until nodeCount) {
                // 找到一个入度为 0 的节点（没有父节点的节点）
                val nodeWithNoParent = graph.getNodeNoParent()
                
                if (nodeWithNoParent == null) {
                    // 找不到入度为 0 的节点，说明图中存在环
                    LogUtils.d("DataStructures", "Kahn 算法检测到环，无法进行拓扑排序")
                    return emptyList()
                } else {
                    // 移除该节点（会自动更新其子节点的入度）
                    val removedNode = graph.removeNode(nodeWithNoParent)
                    // 将节点值加入结果列表
                    result.add(removedNode.value)
                    LogUtils.d("DataStructures", "Kahn 算法: 移除节点 ${removedNode.value}，加入结果")
                }
            }
            
            // 验证是否所有节点都被处理
            if (result.size != nodeCount) {
                LogUtils.d("DataStructures", "Kahn 算法: 图中可能存在环，仅处理了 ${result.size}/${nodeCount} 个节点")
                return emptyList()
            }
            
            return result
        }
        
        /**
         * 拓扑排序（Topological Sort）
         * 
         * 拓扑排序是对有向无环图（DAG）的顶点进行排序的一种算法，
         * 使得对于每条有向边(u, v)，顶点u在排序中都出现在顶点v之前。
         * 
         * 应用场景：
         * - 任务调度（任务之间有依赖关系）
         * - 课程安排（先修课程要求）
         * - 依赖解析（软件包依赖、模块依赖）
         * - 编译顺序确定
         * 
         * 实现算法：Kahn 算法的递归版本（基于深度优先搜索）
         * 时间复杂度: O(V + E)，V为顶点数，E为边数
         * 空间复杂度: O(V)
         * 
         * @param graph 待排序的图
         * @return 拓扑排序结果列表，如果图中存在环则返回空列表
         */
        fun topologicalSort(graph: Graph): List<Int> {
            // 存储最终的拓扑排序结果
            val result = mutableListOf<Int>()
            // 记录已完成访问的节点（永久标记）
            val visited = mutableSetOf<Int>()
            // 记录当前递归路径上的节点（临时标记，用于检测环）
            val tempMark = mutableSetOf<Int>()
            
            // 遍历图中所有节点
            for (nodeValue in graph.getNodeValues()) {
                val graphNode = graph.getNode(nodeValue)
                graphNode?.let {
                    // 如果节点未被访问过，则开始深度优先搜索
                    if (!visited.contains(nodeValue)) {
                        // 调用递归辅助函数进行拓扑排序
                        if (!topologicalSortHelper(it, visited, tempMark, result)) {
                            LogUtils.d("DataStructures", "图中存在环，无法进行拓扑排序")
                            return emptyList()
                        }
                    }
                }
            }
            
            // 反转结果列表，因为后序遍历得到的是逆拓扑序
            // 后序遍历：子节点先于父节点加入结果，反转后父节点在前
            result.reverse()
            return result
        }
        
        /**
         * 拓扑排序递归辅助函数
         * 
         * 采用深度优先搜索（DFS）的后序遍历方式：
         * 1. 临时标记当前节点（表示正在访问该节点的子树）
         * 2. 递归访问所有子节点
         * 3. 移除临时标记，添加永久标记
         * 4. 将当前节点加入结果列表
         * 
         * 环检测原理：
         * 在递归过程中，如果遇到一个带有临时标记的节点，说明存在回边，即图中存在环。
         * 
         * @param node 当前正在处理的节点
         * @param visited 已完成访问的节点集合（永久标记）
         * @param tempMark 当前递归路径上的节点集合（临时标记）
         * @param result 存储拓扑排序结果的列表
         * @return 如果图中存在环返回 false，否则返回 true
         */
        private fun topologicalSortHelper(
            node: GraphNode,
            visited: MutableSet<Int>,
            tempMark: MutableSet<Int>,
            result: MutableList<Int>
        ): Boolean {
            // 检查是否存在环：如果当前节点在临时标记集合中，说明存在回边
            // 这意味着我们正在访问一条已经在当前递归路径中的边，即存在环
            if (tempMark.contains(node.value)) {
                LogUtils.d("DataStructures", "检测到环：节点 ${node.value} 在当前递归路径中")
                return false
            }
            
            // 如果节点已经被永久标记（已完成访问），直接返回
            // 这表示该节点及其所有子节点都已经处理完毕
            if (visited.contains(node.value)) {
                return true
            }
            
            // 临时标记当前节点，表示正在访问该节点的子树
            // 这个标记在递归返回前会被移除
            tempMark.add(node.value)
            LogUtils.d("DataStructures", "临时标记节点: ${node.value}")
            
            // 递归遍历当前节点的所有子节点（邻接节点）
            // 确保所有依赖（子节点）都先于当前节点处理
            for (child in node.children) {
                LogUtils.d("DataStructures", "处理节点 ${node.value} 的子节点: ${child.value}")
                // 如果子节点处理过程中检测到环，立即返回 false
                if (!topologicalSortHelper(child, visited, tempMark, result)) {
                    return false
                }
            }
            
            // 移除临时标记，表示当前节点的子树已访问完毕
            tempMark.remove(node.value)
            LogUtils.d("DataStructures", "移除临时标记，永久标记节点: ${node.value}")
            
            // 永久标记当前节点为已访问
            visited.add(node.value)
            
            // 将当前节点加入结果列表（后序遍历）
            // 此时所有子节点都已加入列表，保证了依赖顺序
            result.add(node.value)
            LogUtils.d("DataStructures", "将节点 ${node.value} 加入拓扑排序结果")
            
            return true
        }
        
        private fun Graph.getNodeValues(): List<Int> {
            return (this as Any).let { graph ->
                val nodesField = graph.javaClass.getDeclaredField("nodes")
                nodesField.isAccessible = true
                val nodes = nodesField.get(graph) as Map<Int, GraphNode>
                nodes.keys.toList()
            }
        }
    }
}

/**
 * 图示例类
 * 展示图数据结构的各种操作
 */
class GraphExampleV2 {
    
    fun runGraphExample() {
        LogUtils.d("DataStructures", "=== GraphExampleV2.runGraphExample called ===")
        LogUtils.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 创建无向图
        val graph = Graph()
        createUndirectedGraph(graph)
        LogUtils.d("DataStructures", "创建了一个无向图")
        graph.printGraph()
        
        // 2. 深度优先遍历
        LogUtils.d("DataStructures", "深度优先遍历:")
        GraphTraversal.dfs(graph, 0) { value ->
            LogUtils.d("DataStructures", "访问顶点: $value")
        }
        
        // 3. 广度优先遍历
        LogUtils.d("DataStructures", "广度优先遍历:")
        GraphTraversal.bfs(graph, 0) { value ->
            LogUtils.d("DataStructures", "访问顶点: $value")
        }
        
        // 4. 检查两个顶点之间是否存在路径
        val hasPath = GraphTraversal.hasPath(graph, 0, 4)
        LogUtils.d("DataStructures", "顶点 0 到顶点 4 是否存在路径: $hasPath")
        
        val hasPath2 = GraphTraversal.hasPath(graph, 0, 5)
        LogUtils.d("DataStructures", "顶点 0 到顶点 5 是否存在路径: $hasPath2")
        
        // 5. 拓扑排序
        val dag = Graph()
        createDAG(dag)
        LogUtils.d("DataStructures", "创建了一个有向无环图 (DAG)")
        dag.printGraph()
        
        val topologicalOrder = GraphTraversal.topologicalSort(dag)
        LogUtils.d("DataStructures", "拓扑排序结果: $topologicalOrder")

        // 6. 拓扑排序 V2
        val dag2 = Graph()
        createDAG(dag2)
        LogUtils.d("DataStructures", "创建了一个有向无环图 (DAG)")
        dag2.printGraph()

        val topologicalOrder2 = GraphTraversal.topologicalSortV2(dag2)
        LogUtils.d("DataStructures", "拓扑排序结果: $topologicalOrder2")

        LogUtils.d("DataStructures", "=== GraphExampleV2.runGraphExample completed ===")
    }
    
    private fun createUndirectedGraph(graph: Graph) {
        graph.addUndirectedEdge(0, 1)
        graph.addUndirectedEdge(0, 2)
        graph.addUndirectedEdge(1, 3)
        graph.addUndirectedEdge(1, 4)
        graph.addUndirectedEdge(2, 4)
        graph.addUndirectedEdge(3, 4)
        graph.addUndirectedEdge(3, 5)
        graph.addUndirectedEdge(4, 5)
    }
    
    private fun createDAG(graph: Graph) {
        graph.addEdge(0, 1)
        graph.addEdge(0, 2)
        graph.addEdge(1, 3)
        graph.addEdge(2, 3)
        graph.addEdge(2, 4)
        graph.addEdge(3, 5)
        graph.addEdge(4, 5)
    }
}