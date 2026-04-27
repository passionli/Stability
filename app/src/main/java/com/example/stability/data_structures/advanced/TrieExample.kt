package com.example.stability.data_structures.advanced

import android.util.Log

/**
 * 前缀树（Trie）是一种用于高效存储和检索字符串的数据结构
 * 特点：可以快速查找字符串是否存在，以及查找具有特定前缀的所有字符串
 */
class TrieExample {
    
    private val root = TrieNode()
    
    /**
     * 前缀树节点类
     */
    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isEndOfWord = false
    }
    
    /**
     * 运行前缀树示例
     */
    fun runTrieExample() {
        Log.d("DataStructures", "=== TrieExample.runTrieExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 插入单词
        insert("apple")
        insert("app")
        insert("application")
        insert("banana")
        insert("ball")
        insert("cat")
        Log.d("DataStructures", "插入单词完成")
        
        // 2. 查找单词
        val exists1 = search("apple")
        Log.d("DataStructures", "单词 'apple' 是否存在: $exists1")
        
        val exists2 = search("app")
        Log.d("DataStructures", "单词 'app' 是否存在: $exists2")
        
        val exists3 = search("appl")
        Log.d("DataStructures", "单词 'appl' 是否存在: $exists3")
        
        val exists4 = search("orange")
        Log.d("DataStructures", "单词 'orange' 是否存在: $exists4")
        
        // 3. 查找前缀
        val hasPrefix1 = startsWith("app")
        Log.d("DataStructures", "是否有以 'app' 为前缀的单词: $hasPrefix1")
        
        val hasPrefix2 = startsWith("ban")
        Log.d("DataStructures", "是否有以 'ban' 为前缀的单词: $hasPrefix2")
        
        val hasPrefix3 = startsWith("dog")
        Log.d("DataStructures", "是否有以 'dog' 为前缀的单词: $hasPrefix3")
        
        Log.d("DataStructures", "=== TrieExample.runTrieExample completed ===")
    }
    
    /**
     * 插入单词
     * 时间复杂度: O(n)，其中 n 是单词的长度
     */
    private fun insert(word: String) {
        var current = root
        
        for (char in word) {
            // 如果当前字符不存在于子节点中，创建一个新节点
            if (!current.children.containsKey(char)) {
                current.children[char] = TrieNode()
            }
            // 移动到子节点
            current = current.children[char]!!
        }
        
        // 标记单词结束
        current.isEndOfWord = true
        Log.d("DataStructures", "插入单词: $word")
    }
    
    /**
     * 查找单词
     * 时间复杂度: O(n)，其中 n 是单词的长度
     */
    private fun search(word: String): Boolean {
        var current = root
        
        for (char in word) {
            // 如果当前字符不存在于子节点中，单词不存在
            if (!current.children.containsKey(char)) {
                return false
            }
            // 移动到子节点
            current = current.children[char]!!
        }
        
        // 检查是否是单词的结束
        return current.isEndOfWord
    }
    
    /**
     * 查找前缀
     * 时间复杂度: O(n)，其中 n 是前缀的长度
     */
    private fun startsWith(prefix: String): Boolean {
        var current = root
        
        for (char in prefix) {
            // 如果当前字符不存在于子节点中，前缀不存在
            if (!current.children.containsKey(char)) {
                return false
            }
            // 移动到子节点
            current = current.children[char]!!
        }
        
        return true
    }
}