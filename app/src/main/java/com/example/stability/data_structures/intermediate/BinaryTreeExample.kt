package com.example.stability.data_structures.intermediate

import android.util.Log
import java.util.*

/**
 * 二叉树是一种树形数据结构，每个节点最多有两个子节点
 * 特点：二叉树的遍历和搜索操作
 */

/**
 * 二叉树节点类
 */
class TreeNode(var value: Int) {
    var left: TreeNode? = null
    var right: TreeNode? = null
}

class BinaryTreeExample {
    
    /**
     * 运行二叉树示例
     */
    fun runBinaryTreeExample() {
        Log.d("DataStructures", "=== BinaryTreeExample.runBinaryTreeExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 创建二叉树
        val root = createBinaryTree()
        Log.d("DataStructures", "创建了一个二叉树")
        
        // 2. 前序遍历
        Log.d("DataStructures", "前序遍历:")
        preorderTraversal(root)
        
        // 3. 中序遍历
        Log.d("DataStructures", "中序遍历:")
        inorderTraversal(root)
        
        // 4. 后序遍历
        Log.d("DataStructures", "后序遍历:")
        postorderTraversal(root)
        
        // 5. 层序遍历
        Log.d("DataStructures", "层序遍历:")
        levelOrderTraversal(root)
        
        // 6. 查找元素
        val target = 5
        val found = search(root, target)
        Log.d("DataStructures", "查找元素 $target: $found")
        
        // 7. 计算二叉树的高度
        val height = calculateHeight(root)
        Log.d("DataStructures", "二叉树的高度: $height")
        
        // 8. 计算二叉树的节点数
        val nodeCount = countNodes(root)
        Log.d("DataStructures", "二叉树的节点数: $nodeCount")
        
        Log.d("DataStructures", "=== BinaryTreeExample.runBinaryTreeExample completed ===")
    }
    
    /**
     * 创建一个二叉树
     */
    private fun createBinaryTree(): TreeNode {
        val root = TreeNode(1)
        root.left = TreeNode(2)
        root.right = TreeNode(3)
        root.left?.left = TreeNode(4)
        root.left?.right = TreeNode(5)
        root.right?.left = TreeNode(6)
        root.right?.right = TreeNode(7)
        return root
    }
    
    /**
     * 前序遍历：根 -> 左 -> 右
     * 时间复杂度: O(n)
     */
    private fun preorderTraversal(node: TreeNode?) {
        if (node == null) return
        
        Log.d("DataStructures", "节点值: ${node.value}")
        preorderTraversal(node.left)
        preorderTraversal(node.right)
    }
    
    /**
     * 中序遍历：左 -> 根 -> 右
     * 时间复杂度: O(n)
     */
    private fun inorderTraversal(node: TreeNode?) {
        if (node == null) return
        
        inorderTraversal(node.left)
        Log.d("DataStructures", "节点值: ${node.value}")
        inorderTraversal(node.right)
    }
    
    /**
     * 后序遍历：左 -> 右 -> 根
     * 时间复杂度: O(n)
     */
    private fun postorderTraversal(node: TreeNode?) {
        if (node == null) return
        
        postorderTraversal(node.left)
        postorderTraversal(node.right)
        Log.d("DataStructures", "节点值: ${node.value}")
    }
    
    /**
     * 层序遍历：按层次从上到下，从左到右
     * 时间复杂度: O(n)
     */
    private fun levelOrderTraversal(root: TreeNode?) {
        if (root == null) return
        
        val queue: Queue<TreeNode> = LinkedList()
        queue.offer(root)
        
        while (queue.isNotEmpty()) {
            val node = queue.poll()
            Log.d("DataStructures", "节点值: ${node.value}")
            
            if (node.left != null) {
                queue.offer(node.left)
            }
            if (node.right != null) {
                queue.offer(node.right)
            }
        }
    }
    
    /**
     * 查找元素
     * 时间复杂度: O(n)
     */
    private fun search(node: TreeNode?, target: Int): Boolean {
        if (node == null) return false
        if (node.value == target) return true
        
        return search(node.left, target) || search(node.right, target)
    }
    
    /**
     * 计算二叉树的高度
     * 时间复杂度: O(n)
     */
    private fun calculateHeight(node: TreeNode?): Int {
        if (node == null) return 0
        
        val leftHeight = calculateHeight(node.left)
        val rightHeight = calculateHeight(node.right)
        
        return maxOf(leftHeight, rightHeight) + 1
    }
    
    /**
     * 计算二叉树的节点数
     * 时间复杂度: O(n)
     */
    private fun countNodes(node: TreeNode?): Int {
        if (node == null) return 0
        
        return countNodes(node.left) + countNodes(node.right) + 1
    }
}