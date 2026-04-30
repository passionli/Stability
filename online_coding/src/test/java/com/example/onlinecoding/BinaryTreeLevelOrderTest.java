package com.example.onlinecoding;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 二叉树层序遍历测试类
 * 
 * 测试覆盖以下场景：
 * 1. 空树
 * 2. 单节点树
 * 3. 完全二叉树
 * 4. 左斜树（只有左子树）
 * 5. 右斜树（只有右子树）
 * 6. 普通二叉树（非完全）
 */
class BinaryTreeLevelOrderTest {

    /**
     * 测试空树的层序遍历
     * 输入：null 根节点
     * 预期：返回空列表
     */
    @Test
    void testEmptyTree() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(null);
        assertTrue(iterativeResult.isEmpty(), "空树迭代方式应返回空列表");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(null);
        assertTrue(recursiveResult.isEmpty(), "空树递归方式应返回空列表");
    }

    /**
     * 测试单节点树的层序遍历
     * 输入：只有根节点 1
     * 预期：[[1]]
     */
    @Test
    void testSingleNode() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        assertEquals(1, iterativeResult.size(), "单节点树应只有一层");
        assertEquals(Arrays.asList(1), iterativeResult.get(0), "第一层应包含节点值1");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        assertEquals(1, recursiveResult.size(), "单节点树应只有一层");
        assertEquals(Arrays.asList(1), recursiveResult.get(0), "第一层应包含节点值1");
    }

    /**
     * 测试完全二叉树的层序遍历
     * 树结构：
     *        1
     *       / \
     *      2   3
     *     / \ / \
     *    4  5 6  7
     * 预期：[[1], [2, 3], [4, 5, 6, 7]]
     */
    @Test
    void testCompleteBinaryTree() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 构建完全二叉树
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        BinaryTreeNode<Integer> node2 = new BinaryTreeNode<>(2);
        BinaryTreeNode<Integer> node3 = new BinaryTreeNode<>(3);
        BinaryTreeNode<Integer> node4 = new BinaryTreeNode<>(4);
        BinaryTreeNode<Integer> node5 = new BinaryTreeNode<>(5);
        BinaryTreeNode<Integer> node6 = new BinaryTreeNode<>(6);
        BinaryTreeNode<Integer> node7 = new BinaryTreeNode<>(7);
        
        root.setLeft(node2);
        root.setRight(node3);
        node2.setLeft(node4);
        node2.setRight(node5);
        node3.setLeft(node6);
        node3.setRight(node7);
        
        // 预期结果
        List<List<Integer>> expected = new ArrayList<>();
        expected.add(Arrays.asList(1));
        expected.add(Arrays.asList(2, 3));
        expected.add(Arrays.asList(4, 5, 6, 7));
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        assertEquals(expected, iterativeResult, "完全二叉树迭代方式遍历结果不正确");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        assertEquals(expected, recursiveResult, "完全二叉树递归方式遍历结果不正确");
    }

    /**
     * 测试左斜树的层序遍历
     * 树结构：
     *    1
     *   /
     *  2
     * /
     * 3
     * 预期：[[1], [2], [3]]
     */
    @Test
    void testLeftSkewedTree() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 构建左斜树
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        BinaryTreeNode<Integer> node2 = new BinaryTreeNode<>(2);
        BinaryTreeNode<Integer> node3 = new BinaryTreeNode<>(3);
        
        root.setLeft(node2);
        node2.setLeft(node3);
        
        // 预期结果
        List<List<Integer>> expected = new ArrayList<>();
        expected.add(Arrays.asList(1));
        expected.add(Arrays.asList(2));
        expected.add(Arrays.asList(3));
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        assertEquals(expected, iterativeResult, "左斜树迭代方式遍历结果不正确");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        assertEquals(expected, recursiveResult, "左斜树递归方式遍历结果不正确");
    }

    /**
     * 测试右斜树的层序遍历
     * 树结构：
     * 1
     *  \
     *   2
     *    \
     *     3
     * 预期：[[1], [2], [3]]
     */
    @Test
    void testRightSkewedTree() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 构建右斜树
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        BinaryTreeNode<Integer> node2 = new BinaryTreeNode<>(2);
        BinaryTreeNode<Integer> node3 = new BinaryTreeNode<>(3);
        
        root.setRight(node2);
        node2.setRight(node3);
        
        // 预期结果
        List<List<Integer>> expected = new ArrayList<>();
        expected.add(Arrays.asList(1));
        expected.add(Arrays.asList(2));
        expected.add(Arrays.asList(3));
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        assertEquals(expected, iterativeResult, "右斜树迭代方式遍历结果不正确");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        assertEquals(expected, recursiveResult, "右斜树递归方式遍历结果不正确");
    }

    /**
     * 测试普通二叉树的层序遍历（非完全二叉树）
     * 树结构：
     *        1
     *       / \
     *      2   3
     *     /     \
     *    4       5
     *           /
     *          6
     * 预期：[[1], [2, 3], [4, 5], [6]]
     */
    @Test
    void testNormalBinaryTree() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 构建普通二叉树
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        BinaryTreeNode<Integer> node2 = new BinaryTreeNode<>(2);
        BinaryTreeNode<Integer> node3 = new BinaryTreeNode<>(3);
        BinaryTreeNode<Integer> node4 = new BinaryTreeNode<>(4);
        BinaryTreeNode<Integer> node5 = new BinaryTreeNode<>(5);
        BinaryTreeNode<Integer> node6 = new BinaryTreeNode<>(6);
        
        root.setLeft(node2);
        root.setRight(node3);
        node2.setLeft(node4);
        node3.setRight(node5);
        node5.setLeft(node6);
        
        // 预期结果
        List<List<Integer>> expected = new ArrayList<>();
        expected.add(Arrays.asList(1));
        expected.add(Arrays.asList(2, 3));
        expected.add(Arrays.asList(4, 5));
        expected.add(Arrays.asList(6));
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        assertEquals(expected, iterativeResult, "普通二叉树迭代方式遍历结果不正确");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        assertEquals(expected, recursiveResult, "普通二叉树递归方式遍历结果不正确");
    }

    /**
     * 测试两层二叉树的层序遍历
     * 树结构：
     *     1
     *    / \
     *   2   3
     * 预期：[[1], [2, 3]]
     */
    @Test
    void testTwoLevelTree() {
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 构建两层二叉树
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        BinaryTreeNode<Integer> node2 = new BinaryTreeNode<>(2);
        BinaryTreeNode<Integer> node3 = new BinaryTreeNode<>(3);
        
        root.setLeft(node2);
        root.setRight(node3);
        
        // 预期结果
        List<List<Integer>> expected = new ArrayList<>();
        expected.add(Arrays.asList(1));
        expected.add(Arrays.asList(2, 3));
        
        // 测试迭代方式
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        assertEquals(expected, iterativeResult, "两层二叉树迭代方式遍历结果不正确");
        
        // 测试递归方式
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        assertEquals(expected, recursiveResult, "两层二叉树递归方式遍历结果不正确");
    }
}
