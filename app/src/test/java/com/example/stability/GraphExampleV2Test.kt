package com.example.stability

import com.example.stability.data_structures.advanced.Graph
import com.example.stability.data_structures.advanced.GraphNode
import com.example.stability.data_structures.advanced.GraphTraversal
import org.junit.Test
import org.junit.Assert.*

/**
 * GraphExampleV2 的单元测试类
 */
class GraphExampleV2Test {

    /**
     * 测试 GraphNode 类的基本功能
     */
    @Test
    fun testGraphNodeBasic() {
        val node1 = GraphNode(1)
        val node2 = GraphNode(2)
        
        assertEquals(1, node1.value)
        assertEquals(2, node2.value)
        assertEquals(0, node1.getChildCount())
        assertEquals(0, node1.getParentCount())
        assertFalse(node1.hasChild(node2))
    }

    /**
     * 测试 GraphNode 添加子节点
     */
    @Test
    fun testGraphNodeAddChild() {
        val node1 = GraphNode(1)
        val node2 = GraphNode(2)
        
        node1.addChild(node2)
        
        assertTrue(node1.hasChild(node2))
        assertEquals(1, node1.getChildCount())
        assertEquals(1, node2.getParentCount())
        assertTrue(node2.parents.contains(node1))
    }

    /**
     * 测试 GraphNode 删除子节点
     */
    @Test
    fun testGraphNodeRemoveChild() {
        val node1 = GraphNode(1)
        val node2 = GraphNode(2)
        
        node1.addChild(node2)
        node1.removeChild(node2)
        
        assertFalse(node1.hasChild(node2))
        assertEquals(0, node1.getChildCount())
        assertEquals(0, node2.getParentCount())
    }

    /**
     * 测试 Graph 添加有向边
     */
    @Test
    fun testGraphAddEdge() {
        val graph = Graph()
        
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        
        assertEquals(3, graph.getNodeCount())
        
        val node1 = graph.getNode(1)
        val node2 = graph.getNode(2)
        val node3 = graph.getNode(3)
        
        assertNotNull(node1)
        assertNotNull(node2)
        assertNotNull(node3)
        
        assertTrue(node1!!.hasChild(node2!!))
        assertTrue(node2.hasChild(node3!!))
        assertFalse(node1.hasChild(node3))
    }

    /**
     * 测试 Graph 添加无向边
     */
    @Test
    fun testGraphAddUndirectedEdge() {
        val graph = Graph()
        
        graph.addUndirectedEdge(1, 2)
        
        assertEquals(2, graph.getNodeCount())
        
        val node1 = graph.getNode(1)
        val node2 = graph.getNode(2)
        
        assertNotNull(node1)
        assertNotNull(node2)
        
        assertTrue(node1!!.hasChild(node2!!))
        assertTrue(node2.hasChild(node1))
    }

    /**
     * 测试 Graph 移除节点
     */
    @Test
    fun testGraphRemoveNode() {
        val graph = Graph()
        
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        
        assertEquals(3, graph.getNodeCount())
        
        val node2 = graph.getNode(2)
        assertNotNull(node2)
        
        graph.removeNode(node2!!)
        
        assertEquals(2, graph.getNodeCount())
        assertNull(graph.getNode(2))
        
        val node1 = graph.getNode(1)
        val node3 = graph.getNode(3)
        
        assertNotNull(node1)
        assertNotNull(node3)
        assertFalse(node1!!.hasChild(node3!!))
    }

    /**
     * 测试深度优先遍历（DFS）
     */
    @Test
    fun testDFS() {
        val graph = Graph()
        graph.addUndirectedEdge(0, 1)
        graph.addUndirectedEdge(0, 2)
        graph.addUndirectedEdge(1, 3)
        graph.addUndirectedEdge(1, 4)
        graph.addUndirectedEdge(2, 4)
        
        val visited = mutableListOf<Int>()
        GraphTraversal.dfs(graph, 0) { value ->
            visited.add(value)
        }
        
        assertEquals(5, visited.size)
        assertTrue(visited.contains(0))
        assertTrue(visited.contains(1))
        assertTrue(visited.contains(2))
        assertTrue(visited.contains(3))
        assertTrue(visited.contains(4))
    }

    /**
     * 测试广度优先遍历（BFS）
     */
    @Test
    fun testBFS() {
        val graph = Graph()
        graph.addUndirectedEdge(0, 1)
        graph.addUndirectedEdge(0, 2)
        graph.addUndirectedEdge(1, 3)
        graph.addUndirectedEdge(1, 4)
        graph.addUndirectedEdge(2, 4)
        
        val visited = mutableListOf<Int>()
        GraphTraversal.bfs(graph, 0) { value ->
            visited.add(value)
        }
        
        assertEquals(5, visited.size)
        assertTrue(visited.contains(0))
        assertTrue(visited.contains(1))
        assertTrue(visited.contains(2))
        assertTrue(visited.contains(3))
        assertTrue(visited.contains(4))
        
        // BFS 应该先访问完当前层再访问下一层
        val index0 = visited.indexOf(0)
        val index1 = visited.indexOf(1)
        val index2 = visited.indexOf(2)
        val index3 = visited.indexOf(3)
        
        assertTrue(index0 < index1)
        assertTrue(index0 < index2)
        assertTrue(index1 < index3)
        assertTrue(index2 < index3)
    }

    /**
     * 测试路径检测 - 存在路径
     */
    @Test
    fun testHasPath_Exists() {
        val graph = Graph()
        graph.addUndirectedEdge(0, 1)
        graph.addUndirectedEdge(1, 2)
        graph.addUndirectedEdge(2, 3)
        graph.addUndirectedEdge(3, 4)
        
        assertTrue(GraphTraversal.hasPath(graph, 0, 4))
        assertTrue(GraphTraversal.hasPath(graph, 0, 2))
        assertTrue(GraphTraversal.hasPath(graph, 1, 3))
    }

    /**
     * 测试路径检测 - 不存在路径
     */
    @Test
    fun testHasPath_NotExists() {
        val graph = Graph()
        graph.addUndirectedEdge(0, 1)
        graph.addUndirectedEdge(1, 2)
        graph.addUndirectedEdge(3, 4)
        
        assertFalse(GraphTraversal.hasPath(graph, 0, 3))
        assertFalse(GraphTraversal.hasPath(graph, 2, 4))
    }

    /**
     * 测试拓扑排序（DFS 版本）
     */
    @Test
    fun testTopologicalSort_DFS() {
        val graph = Graph()
        graph.addEdge(0, 1)
        graph.addEdge(0, 2)
        graph.addEdge(1, 3)
        graph.addEdge(2, 3)
        graph.addEdge(2, 4)
        graph.addEdge(3, 5)
        graph.addEdge(4, 5)
        
        val result = GraphTraversal.topologicalSort(graph)
        
        assertEquals(6, result.size)
        
        // 验证拓扑顺序：对于每条边(u, v)，u 应该在 v 之前
        val index0 = result.indexOf(0)
        val index1 = result.indexOf(1)
        val index2 = result.indexOf(2)
        val index3 = result.indexOf(3)
        val index4 = result.indexOf(4)
        val index5 = result.indexOf(5)
        
        assertTrue(index0 < index1)
        assertTrue(index0 < index2)
        assertTrue(index1 < index3)
        assertTrue(index2 < index3)
        assertTrue(index2 < index4)
        assertTrue(index3 < index5)
        assertTrue(index4 < index5)
    }

    /**
     * 测试拓扑排序（Kahn 算法版本）
     */
    @Test
    fun testTopologicalSort_Kahn() {
        val graph = Graph()
        graph.addEdge(0, 1)
        graph.addEdge(0, 2)
        graph.addEdge(1, 3)
        graph.addEdge(2, 3)
        graph.addEdge(2, 4)
        graph.addEdge(3, 5)
        graph.addEdge(4, 5)
        
        val result = GraphTraversal.topologicalSortV2(graph)
        
        assertEquals(6, result.size)
        
        // 验证拓扑顺序
        val index0 = result.indexOf(0)
        val index1 = result.indexOf(1)
        val index2 = result.indexOf(2)
        val index3 = result.indexOf(3)
        val index4 = result.indexOf(4)
        val index5 = result.indexOf(5)
        
        assertTrue(index0 < index1)
        assertTrue(index0 < index2)
        assertTrue(index1 < index3)
        assertTrue(index2 < index3)
        assertTrue(index2 < index4)
        assertTrue(index3 < index5)
        assertTrue(index4 < index5)
    }

    /**
     * 测试拓扑排序检测环（DFS 版本）
     */
    @Test
    fun testTopologicalSort_DetectCycle_DFS() {
        val graph = Graph()
        graph.addEdge(0, 1)
        graph.addEdge(1, 2)
        graph.addEdge(2, 0) // 形成环
        
        val result = GraphTraversal.topologicalSort(graph)
        
        assertTrue(result.isEmpty())
    }

    /**
     * 测试拓扑排序检测环（Kahn 算法版本）
     */
    @Test
    fun testTopologicalSort_DetectCycle_Kahn() {
        val graph = Graph()
        graph.addEdge(0, 1)
        graph.addEdge(1, 2)
        graph.addEdge(2, 0) // 形成环
        
        val result = GraphTraversal.topologicalSortV2(graph)
        
        assertTrue(result.isEmpty())
    }

    /**
     * 测试空图
     */
    @Test
    fun testEmptyGraph() {
        val graph = Graph()
        
        assertEquals(0, graph.getNodeCount())
        assertEquals(0, graph.getEdgeCount())
        assertNull(graph.getNode(1))
        
        val resultDFS = GraphTraversal.topologicalSort(graph)
        assertTrue(resultDFS.isEmpty())
        
        val resultKahn = GraphTraversal.topologicalSortV2(graph)
        assertTrue(resultKahn.isEmpty())
    }

    /**
     * 测试单节点图
     */
    @Test
    fun testSingleNodeGraph() {
        val graph = Graph()
        graph.addEdge(0, 0) // 自环
        
        assertEquals(1, graph.getNodeCount())
        
        // 自环应该被检测为环
        val resultDFS = GraphTraversal.topologicalSort(graph)
        assertTrue(resultDFS.isEmpty())
        
        val resultKahn = GraphTraversal.topologicalSortV2(graph)
        assertTrue(resultKahn.isEmpty())
    }

    /**
     * 测试图的清除功能
     */
    @Test
    fun testGraphClear() {
        val graph = Graph()
        graph.addEdge(0, 1)
        graph.addEdge(1, 2)
        
        assertEquals(3, graph.getNodeCount())
        
        graph.clear()
        
        assertEquals(0, graph.getNodeCount())
        assertEquals(0, graph.getEdgeCount())
    }
}