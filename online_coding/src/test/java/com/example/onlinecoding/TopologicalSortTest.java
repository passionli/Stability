package com.example.onlinecoding;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 拓扑排序测试类
 * 
 * 测试覆盖以下场景：
 * 1. 空图（节点数为0）
 * 2. 单节点图
 * 3. 正常有向无环图（DAG）
 * 4. 存在环的图（应返回空列表）
 * 5. 多源节点图（多个入度为0的节点）
 * 6. 链式结构（线性依赖）
 * 7. 完全孤立节点
 */
class TopologicalSortTest {

    /**
     * 测试空图（节点数为0）
     * 输入：n=0, edges=[]
     * 预期：返回空列表
     */
    @Test
    void testEmptyGraph() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(0, edges);
        assertTrue(kahnResult.isEmpty(), "空图Kahn算法应返回空列表");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(0, edges);
        assertTrue(dfsResult.isEmpty(), "空图DFS算法应返回空列表");
    }

    /**
     * 测试单节点图
     * 输入：n=1, edges=[]
     * 预期：返回 [0]
     */
    @Test
    void testSingleNode() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(1, edges);
        assertEquals(Arrays.asList(0), kahnResult, "单节点图Kahn算法应返回[0]");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(1, edges);
        assertEquals(Arrays.asList(0), dfsResult, "单节点图DFS算法应返回[0]");
    }

    /**
     * 测试正常有向无环图（DAG）
     * 图结构：
     *     0 → 1 → 3
     *     ↓    ↘
     *     2 → 4
     * 预期：返回有效的拓扑序，如 [0, 2, 1, 3, 4] 或 [0, 1, 2, 3, 4]
     */
    @Test
    void testNormalDAG() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{0, 2});
        edges.add(new int[]{1, 3});
        edges.add(new int[]{1, 4});
        edges.add(new int[]{2, 4});
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(5, edges);
        assertEquals(5, kahnResult.size(), "Kahn算法应返回所有5个节点");
        assertTrue(ts.isValidTopologicalSort(5, edges, kahnResult), "Kahn算法结果应为有效拓扑序");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(5, edges);
        assertEquals(5, dfsResult.size(), "DFS算法应返回所有5个节点");
        assertTrue(ts.isValidTopologicalSort(5, edges, dfsResult), "DFS算法结果应为有效拓扑序");
    }

    /**
     * 测试存在环的图
     * 图结构：0 → 1 → 2 → 0（形成环）
     * 预期：返回空列表
     */
    @Test
    void testCyclicGraph() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{1, 2});
        edges.add(new int[]{2, 0}); // 形成环
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(3, edges);
        assertTrue(kahnResult.isEmpty(), "Kahn算法应检测到环并返回空列表");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(3, edges);
        assertTrue(dfsResult.isEmpty(), "DFS算法应检测到环并返回空列表");
    }

    /**
     * 测试多源节点图（多个入度为0的节点）
     * 图结构：
     *     0 → 2
     *     1 → 2
     * 预期：返回 [0, 1, 2] 或 [1, 0, 2]
     */
    @Test
    void testMultipleSources() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 2});
        edges.add(new int[]{1, 2});
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(3, edges);
        assertEquals(3, kahnResult.size(), "Kahn算法应返回所有3个节点");
        assertTrue(ts.isValidTopologicalSort(3, edges, kahnResult), "Kahn算法结果应为有效拓扑序");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(3, edges);
        assertEquals(3, dfsResult.size(), "DFS算法应返回所有3个节点");
        assertTrue(ts.isValidTopologicalSort(3, edges, dfsResult), "DFS算法结果应为有效拓扑序");
    }

    /**
     * 测试链式结构（线性依赖）
     * 图结构：0 → 1 → 2 → 3
     * 预期：返回 [0, 1, 2, 3]
     */
    @Test
    void testChainStructure() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{1, 2});
        edges.add(new int[]{2, 3});
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(4, edges);
        assertEquals(Arrays.asList(0, 1, 2, 3), kahnResult, "Kahn算法应返回链式顺序");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(4, edges);
        assertEquals(Arrays.asList(0, 1, 2, 3), dfsResult, "DFS算法应返回链式顺序");
    }

    /**
     * 测试完全孤立节点（无边）
     * 输入：n=3, edges=[]
     * 预期：返回任意顺序的 [0, 1, 2]
     */
    @Test
    void testIsolatedNodes() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(3, edges);
        assertEquals(3, kahnResult.size(), "Kahn算法应返回所有3个节点");
        assertTrue(ts.isValidTopologicalSort(3, edges, kahnResult), "Kahn算法结果应为有效拓扑序");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(3, edges);
        assertEquals(3, dfsResult.size(), "DFS算法应返回所有3个节点");
        assertTrue(ts.isValidTopologicalSort(3, edges, dfsResult), "DFS算法结果应为有效拓扑序");
    }

    /**
     * 测试复杂DAG图
     * 图结构：
     *     0 → 1    3 → 4
     *     ↓    ↘    ↓
     *     2 → 5 ← 6
     * 预期：返回有效的拓扑序
     */
    @Test
    void testComplexDAG() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{0, 2});
        edges.add(new int[]{1, 5});
        edges.add(new int[]{2, 5});
        edges.add(new int[]{3, 4});
        edges.add(new int[]{6, 4});
        edges.add(new int[]{6, 5});
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(7, edges);
        assertEquals(7, kahnResult.size(), "Kahn算法应返回所有7个节点");
        assertTrue(ts.isValidTopologicalSort(7, edges, kahnResult), "Kahn算法结果应为有效拓扑序");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(7, edges);
        assertEquals(7, dfsResult.size(), "DFS算法应返回所有7个节点");
        assertTrue(ts.isValidTopologicalSort(7, edges, dfsResult), "DFS算法结果应为有效拓扑序");
    }

    /**
     * 测试自环（节点指向自己）
     * 图结构：0 → 0（自环）
     * 预期：返回空列表
     */
    @Test
    void testSelfLoop() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 0}); // 自环
        
        List<Integer> kahnResult = ts.kahnTopologicalSort(1, edges);
        assertTrue(kahnResult.isEmpty(), "Kahn算法应检测到自环并返回空列表");
        
        List<Integer> dfsResult = ts.dfsTopologicalSort(1, edges);
        assertTrue(dfsResult.isEmpty(), "DFS算法应检测到自环并返回空列表");
    }

    /**
     * 测试验证方法的正确性
     */
    @Test
    void testValidationMethod() {
        TopologicalSort ts = new TopologicalSort();
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{0, 2});
        edges.add(new int[]{1, 3});
        
        // 有效拓扑序
        List<Integer> validOrder = Arrays.asList(0, 1, 2, 3);
        assertTrue(ts.isValidTopologicalSort(4, edges, validOrder), "应识别有效拓扑序");
        
        // 无效拓扑序（1在0之前）
        List<Integer> invalidOrder = Arrays.asList(1, 0, 2, 3);
        assertFalse(ts.isValidTopologicalSort(4, edges, invalidOrder), "应识别无效拓扑序");
        
        // 无效拓扑序（节点数量不对）
        List<Integer> wrongSizeOrder = Arrays.asList(0, 1, 2);
        assertFalse(ts.isValidTopologicalSort(4, edges, wrongSizeOrder), "应识别节点数量不正确的情况");
    }
}