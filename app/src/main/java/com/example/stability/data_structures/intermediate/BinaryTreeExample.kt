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
data class TreeNode(val value: Int, val left: TreeNode? = null, val right: TreeNode? = null)

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
        
        // 6. 按层打印
        Log.d("DataStructures", "按层打印:")
        printLevelOrder(root)
        
        // 7. 查找元素
        val target = 5
        val found = search(root, target)
        Log.d("DataStructures", "查找元素 $target: $found")
        
        // 8. 计算二叉树的高度
        val height = calculateHeight(root)
        Log.d("DataStructures", "二叉树的高度: $height")
        
        // 9. 计算二叉树的节点数
        val nodeCount = countNodes(root)
        Log.d("DataStructures", "二叉树的节点数: $nodeCount")
        
        Log.d("DataStructures", "=== BinaryTreeExample.runBinaryTreeExample completed ===")
    }
    
    /**
     * 创建一个二叉树（使用不可变节点）
     */
    private fun createBinaryTree(): TreeNode {
        return TreeNode(
            value = 1,
            left = TreeNode(
                value = 2,
                left = TreeNode(4),
                right = TreeNode(5)
            ),
            right = TreeNode(
                value = 3,
                left = TreeNode(6),
                right = TreeNode(7)
            )
        )
    }
    
    /**
     * 前序遍历：根 -> 左 -> 右
     */
    private fun preorderTraversal(node: TreeNode?) {
        if (node == null) return
        
        Log.d("DataStructures", "节点值: ${node.value}")
        preorderTraversal(node.left)
        preorderTraversal(node.right)
    }
    
    /**
     * 中序遍历：左 -> 根 -> 右
     */
    private fun inorderTraversal(node: TreeNode?) {
        if (node == null) return
        
        inorderTraversal(node.left)
        Log.d("DataStructures", "节点值: ${node.value}")
        inorderTraversal(node.right)
    }
    
    /**
     * 后序遍历：左 -> 右 -> 根
     */
    private fun postorderTraversal(node: TreeNode?) {
        if (node == null) return
        
        postorderTraversal(node.left)
        postorderTraversal(node.right)
        Log.d("DataStructures", "节点值: ${node.value}")
    }
    
    /**
     * 层序遍历：使用序列操作
     */
    private fun levelOrderTraversal(root: TreeNode?) {
        root ?: return
        
        generateSequence(listOf(root)) { level ->
            level.flatMap { node ->
                listOfNotNull(node.left, node.right)
            }.takeIf { it.isNotEmpty() }
        }.forEach { level ->
            level.forEach { node ->
                Log.d("DataStructures", "节点值: ${node.value}")
            }
        }
    }
    
    /**
     * 按层打印二叉树（使用序列操作）
     */
    private fun printLevelOrder(root: TreeNode?) {
        root ?: return
        
        generateSequence(listOf(root) to 1) { (level, depth) ->
            val nextLevel = level.flatMap { node ->
                listOfNotNull(node.left, node.right)
            }
            nextLevel.takeIf { it.isNotEmpty() }?.let { it to depth + 1 }
        }.forEach { (level, depth) ->
            val values = level.joinToString(" ") { it.value.toString() }
            Log.d("DataStructures", "第${depth}层: $values")
        }
    }
    
    /**
     * 查找元素
     */
    private fun search(node: TreeNode?, target: Int): Boolean = when {
        node == null -> false
        node.value == target -> true
        else -> search(node.left, target) || search(node.right, target)
    }
    
    /**
     * 计算二叉树的高度
     */
    private fun calculateHeight(node: TreeNode?): Int = when (node) {
        null -> 0
        else -> maxOf(calculateHeight(node.left), calculateHeight(node.right)) + 1
    }
    
    /**
     * 计算二叉树的节点数
     */
    private fun countNodes(node: TreeNode?): Int = when (node) {
        null -> 0
        else -> countNodes(node.left) + countNodes(node.right) + 1
    }
}