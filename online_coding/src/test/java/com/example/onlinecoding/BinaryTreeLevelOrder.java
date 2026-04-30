package com.example.onlinecoding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 二叉树层序遍历（BFS）实现
 * 
 * 层序遍历，也称为广度优先搜索（Breadth-First Search, BFS），
 * 是一种按层级顺序访问二叉树节点的遍历方式。
 * 
 * 核心思想：
 * 1. 使用队列（Queue）作为辅助数据结构
 * 2. 从根节点开始，按层处理节点
 * 3. 先访问当前层所有节点，再进入下一层
 */
public class BinaryTreeLevelOrder {

    /**
     * 使用迭代方式（队列）实现层序遍历
     * 
     * 算法步骤：
     * 1. 如果根节点为空，返回空列表
     * 2. 创建队列并将根节点入队
     * 3. 当队列不为空时：
     *    a. 记录当前队列大小（即当前层节点数）
     *    b. 循环处理当前层的所有节点
     *    c. 将每个节点的左右子节点（如果存在）入队
     *    d. 将当前层节点值加入结果列表
     * 
     * @param root 二叉树的根节点
     * @return 按层组织的节点值列表，例如 [[1], [2, 3], [4, 5, 6]]
     */
    public List<List<Integer>> levelOrderIterative(BinaryTreeNode<Integer> root) {
        // 结果列表，每个子列表代表一层
        List<List<Integer>> result = new ArrayList<>();
        
        // 边界条件：空树直接返回空列表
        if (root == null) {
            return result;
        }
        
        // 使用 LinkedList 作为队列，因为它实现了 Queue 接口
        Queue<BinaryTreeNode<Integer>> queue = new LinkedList<>();
        // 将根节点入队
        queue.offer(root);
        
        // 队列不为空时继续遍历
        while (!queue.isEmpty()) {
            // 获取当前层的节点数量
            int levelSize = queue.size();
            // 当前层的节点值列表
            List<Integer> currentLevel = new ArrayList<>();
            
            // 遍历当前层的所有节点
            for (int i = 0; i < levelSize; i++) {
                // 出队一个节点
                BinaryTreeNode<Integer> node = queue.poll();
                // 将节点值加入当前层列表
                currentLevel.add(node.getValue());
                
                // 如果左子节点存在，入队
                if (node.getLeft() != null) {
                    queue.offer(node.getLeft());
                }
                // 如果右子节点存在，入队
                if (node.getRight() != null) {
                    queue.offer(node.getRight());
                }
            }
            
            // 将当前层加入结果列表
            result.add(currentLevel);
        }
        
        return result;
    }

    /**
     * 使用递归方式实现层序遍历
     * 
     * 算法思想：
     * 通过深度优先搜索（DFS）遍历树，同时记录当前节点所在的层级，
     * 将节点值添加到对应层级的列表中。
     * 
     * @param root 二叉树的根节点
     * @return 按层组织的节点值列表
     */
    public List<List<Integer>> levelOrderRecursive(BinaryTreeNode<Integer> root) {
        List<List<Integer>> result = new ArrayList<>();
        // 调用递归辅助方法，从第0层开始
        dfsHelper(root, 0, result);
        return result;
    }

    /**
     * 递归辅助方法：深度优先搜索并按层级收集节点值
     * 
     * @param node   当前节点
     * @param level  当前节点所在层级（从0开始）
     * @param result 结果列表
     */
    private void dfsHelper(BinaryTreeNode<Integer> node, int level, List<List<Integer>> result) {
        // 递归终止条件：节点为空
        if (node == null) {
            return;
        }
        
        // 如果当前层级的列表还不存在，创建新列表
        if (result.size() == level) {
            result.add(new ArrayList<>());
        }
        
        // 将当前节点值添加到对应层级的列表中
        result.get(level).add(node.getValue());
        
        // 递归遍历左子树，层级+1
        dfsHelper(node.getLeft(), level + 1, result);
        // 递归遍历右子树，层级+1
        dfsHelper(node.getRight(), level + 1, result);
    }

    /**
     * 将层序遍历结果转换为字符串，便于打印输出
     * 
     * @param result 层序遍历结果
     * @return 格式化的字符串表示
     */
    public String formatResult(List<List<Integer>> result) {
        if (result == null || result.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < result.size(); i++) {
            List<Integer> level = result.get(i);
            sb.append("[");
            for (int j = 0; j < level.size(); j++) {
                sb.append(level.get(j));
                if (j < level.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            if (i < result.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 打印层序遍历结果
     * 
     * @param result 层序遍历结果
     */
    public void printResult(List<List<Integer>> result) {
        System.out.println(formatResult(result));
    }

    /**
     * 构建示例二叉树用于测试
     * 
     * 构建的二叉树结构：
     *        1
     *       / \
     *      2   3
     *     / \   \
     *    4   5   6
     * 
     * @return 构建好的二叉树根节点
     */
    public static BinaryTreeNode<Integer> buildSampleTree() {
        // 创建节点
        BinaryTreeNode<Integer> root = new BinaryTreeNode<>(1);
        BinaryTreeNode<Integer> node2 = new BinaryTreeNode<>(2);
        BinaryTreeNode<Integer> node3 = new BinaryTreeNode<>(3);
        BinaryTreeNode<Integer> node4 = new BinaryTreeNode<>(4);
        BinaryTreeNode<Integer> node5 = new BinaryTreeNode<>(5);
        BinaryTreeNode<Integer> node6 = new BinaryTreeNode<>(6);
        
        // 构建树结构
        root.setLeft(node2);
        root.setRight(node3);
        node2.setLeft(node4);
        node2.setRight(node5);
        node3.setRight(node6);
        
        return root;
    }

    /**
     * 主方法：演示层序遍历的使用
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 构建示例二叉树
        BinaryTreeNode<Integer> root = buildSampleTree();
        
        // 创建层序遍历实例
        BinaryTreeLevelOrder traversal = new BinaryTreeLevelOrder();
        
        // 迭代方式遍历
        System.out.println("=== 迭代方式（队列） ===");
        List<List<Integer>> iterativeResult = traversal.levelOrderIterative(root);
        traversal.printResult(iterativeResult);
        
        // 递归方式遍历
        System.out.println("=== 递归方式（DFS） ===");
        List<List<Integer>> recursiveResult = traversal.levelOrderRecursive(root);
        traversal.printResult(recursiveResult);
    }
}
