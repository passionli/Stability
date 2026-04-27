package com.example.stability.data_structures.basic

import android.util.Log

/**
 * 链表是一种线性数据结构，它的每个元素都包含一个数据域和一个指针域
 * 特点：插入和删除时间复杂度 O(1)，访问时间复杂度 O(n)
 */

/**
 * 链表节点类
 */
class ListNode(var value: Int) {
    var next: ListNode? = null
}

class LinkedListExample {
    
    /**
     * 运行链表示例
     */
    fun runLinkedListExample() {
        Log.d("DataStructures", "=== LinkedListExample.runLinkedListExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 创建链表
        val head = createLinkedList()
        Log.d("DataStructures", "创建了一个链表")
        printLinkedList(head)
        
        // 2. 查找元素
        val target = 3
        val node = findNode(head, target)
        Log.d("DataStructures", "查找元素 $target: ${node?.value ?: "Not found"}")
        
        // 3. 插入元素
        val newHead = insertAtHead(head, 0)
        Log.d("DataStructures", "在头部插入元素 0 后:")
        printLinkedList(newHead)
        
        val newHead2 = insertAtTail(newHead, 6)
        Log.d("DataStructures", "在尾部插入元素 6 后:")
        printLinkedList(newHead2)
        
        val newHead3 = insertAtPosition(newHead2, 2, 10)
        Log.d("DataStructures", "在位置 2 插入元素 10 后:")
        printLinkedList(newHead3)
        
        // 4. 删除元素
        val newHead4 = deleteAtHead(newHead3)
        Log.d("DataStructures", "删除头部元素后:")
        printLinkedList(newHead4)
        
        val newHead5 = deleteAtTail(newHead4)
        Log.d("DataStructures", "删除尾部元素后:")
        printLinkedList(newHead5)
        
        val newHead6 = deleteAtPosition(newHead5, 2)
        Log.d("DataStructures", "删除位置 2 的元素后:")
        printLinkedList(newHead6)
        
        Log.d("DataStructures", "=== LinkedListExample.runLinkedListExample completed ===")
    }
    
    /**
     * 创建一个链表
     */
    private fun createLinkedList(): ListNode {
        val head = ListNode(1)
        var current = head
        
        for (i in 2..5) {
            current.next = ListNode(i)
            current = current.next!!
        }
        
        return head
    }
    
    /**
     * 打印链表
     */
    private fun printLinkedList(head: ListNode?) {
        val sb = StringBuilder()
        var current = head
        
        while (current != null) {
            sb.append(current.value)
            if (current.next != null) {
                sb.append(" -> ")
            }
            current = current.next
        }
        
        Log.d("DataStructures", "链表: $sb")
    }
    
    /**
     * 查找元素
     * 时间复杂度: O(n)
     */
    private fun findNode(head: ListNode?, target: Int): ListNode? {
        var current = head
        
        while (current != null) {
            if (current.value == target) {
                return current
            }
            current = current.next
        }
        
        return null
    }
    
    /**
     * 在头部插入元素
     * 时间复杂度: O(1)
     */
    private fun insertAtHead(head: ListNode?, value: Int): ListNode {
        val newNode = ListNode(value)
        newNode.next = head
        return newNode
    }
    
    /**
     * 在尾部插入元素
     * 时间复杂度: O(n)
     */
    private fun insertAtTail(head: ListNode?, value: Int): ListNode {
        val newNode = ListNode(value)
        
        if (head == null) {
            return newNode
        }
        
        var current = head
        while (current?.next != null) {
            current = current.next
        }
        
        current?.next = newNode
        return head
    }
    
    /**
     * 在指定位置插入元素
     * 时间复杂度: O(n)
     */
    private fun insertAtPosition(head: ListNode?, position: Int, value: Int): ListNode {
        if (position == 0) {
            return insertAtHead(head, value)
        }
        
        val newNode = ListNode(value)
        var current = head
        var count = 0
        
        while (current != null && count < position - 1) {
            current = current.next
            count++
        }
        
        if (current != null) {
            newNode.next = current.next
            current.next = newNode
        }
        
        return head!!
    }
    
    /**
     * 删除头部元素
     * 时间复杂度: O(1)
     */
    private fun deleteAtHead(head: ListNode?): ListNode? {
        if (head == null) {
            return null
        }
        
        return head.next
    }
    
    /**
     * 删除尾部元素
     * 时间复杂度: O(n)
     */
    private fun deleteAtTail(head: ListNode?): ListNode? {
        if (head == null || head.next == null) {
            return null
        }
        
        var current = head
        while (current?.next?.next != null) {
            current = current.next
        }
        
        current?.next = null
        return head
    }
    
    /**
     * 删除指定位置的元素
     * 时间复杂度: O(n)
     */
    private fun deleteAtPosition(head: ListNode?, position: Int): ListNode? {
        if (head == null) {
            return null
        }
        
        if (position == 0) {
            return head.next
        }
        
        var current = head
        var count = 0
        
        while (current != null && count < position - 1) {
            current = current.next
            count++
        }
        
        if (current?.next != null) {
            current.next = current.next?.next
        }
        
        return head
    }
}