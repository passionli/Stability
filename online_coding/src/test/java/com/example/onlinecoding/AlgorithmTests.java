package com.example.onlinecoding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 算法综合测试类
 * 
 * 测试以下算法：
 * 1. 斐波那契数列
 * 2. 两数之和
 * 3. 反转链表
 */
class AlgorithmTests {

    // ==================== 斐波那契数列测试 ====================

    /**
     * 测试斐波那契数列 - 基础测试
     */
    @Test
    void testFibonacciBasic() {
        // F(0) = 0
        assertEquals(0, Fibonacci.fibonacciRecursive(0));
        assertEquals(0, Fibonacci.fibonacciDP(0));
        assertEquals(0, Fibonacci.fibonacciDPArray(0));
        assertEquals(0, Fibonacci.fibonacciMatrix(0));
        
        // F(1) = 1
        assertEquals(1, Fibonacci.fibonacciRecursive(1));
        assertEquals(1, Fibonacci.fibonacciDP(1));
        assertEquals(1, Fibonacci.fibonacciDPArray(1));
        assertEquals(1, Fibonacci.fibonacciMatrix(1));
        
        // F(2) = 1
        assertEquals(1, Fibonacci.fibonacciRecursive(2));
        assertEquals(1, Fibonacci.fibonacciDP(2));
        
        // F(3) = 2
        assertEquals(2, Fibonacci.fibonacciRecursive(3));
        assertEquals(2, Fibonacci.fibonacciDP(3));
        
        // F(5) = 5
        assertEquals(5, Fibonacci.fibonacciRecursive(5));
        assertEquals(5, Fibonacci.fibonacciDP(5));
        assertEquals(5, Fibonacci.fibonacciMatrix(5));
        
        // F(10) = 55
        assertEquals(55, Fibonacci.fibonacciRecursive(10));
        assertEquals(55, Fibonacci.fibonacciDP(10));
        assertEquals(55, Fibonacci.fibonacciMatrix(10));
    }

    /**
     * 测试斐波那契数列 - 较大数值
     */
    @Test
    void testFibonacciLargeN() {
        // F(40) = 102334155
        assertEquals(102334155L, Fibonacci.fibonacciDP(40));
        assertEquals(102334155L, Fibonacci.fibonacciMatrix(40));
        
        // F(50) = 12586269025
        assertEquals(12586269025L, Fibonacci.fibonacciDP(50));
        assertEquals(12586269025L, Fibonacci.fibonacciMatrix(50));
    }

    /**
     * 测试斐波那契数列 - 异常情况
     */
    @Test
    void testFibonacciNegativeInput() {
        assertThrows(IllegalArgumentException.class, () -> Fibonacci.fibonacciRecursive(-1));
        assertThrows(IllegalArgumentException.class, () -> Fibonacci.fibonacciDP(-5));
        assertThrows(IllegalArgumentException.class, () -> Fibonacci.fibonacciMatrix(-10));
    }

    // ==================== 两数之和测试 ====================

    /**
     * 测试两数之和 - 基础测试
     */
    @Test
    void testTwoSumBasic() {
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        
        int[] result1 = TwoSum.twoSumBruteForce(nums, target);
        int[] result2 = TwoSum.twoSumHashMap(nums, target);
        int[] result3 = TwoSum.twoSumTwoPointers(nums, target);
        
        // 验证结果（顺序可以不同）
        assertTrue((result1[0] == 0 && result1[1] == 1) || (result1[0] == 1 && result1[1] == 0));
        assertTrue((result2[0] == 0 && result2[1] == 1) || (result2[0] == 1 && result2[1] == 0));
        // 双指针法返回的索引顺序可能不同，只要两个数正确即可
        assertTrue(nums[result3[0]] + nums[result3[1]] == target);
    }

    /**
     * 测试两数之和 - 第二个例子
     */
    @Test
    void testTwoSumAnotherCase() {
        int[] nums = {3, 2, 4};
        int target = 6;
        
        int[] result = TwoSum.twoSumHashMap(nums, target);
        assertTrue(nums[result[0]] + nums[result[1]] == target);
        assertEquals(2, result.length);
    }

    /**
     * 测试两数之和 - 无结果情况
     */
    @Test
    void testTwoSumNoResult() {
        int[] nums = {1, 2, 3, 4};
        int target = 10;
        
        int[] result1 = TwoSum.twoSumBruteForce(nums, target);
        int[] result2 = TwoSum.twoSumHashMap(nums, target);
        
        assertEquals(0, result1.length);
        assertEquals(0, result2.length);
    }

    /**
     * 测试两数之和 - 边界情况
     */
    @Test
    void testTwoSumEdgeCases() {
        // 空数组
        int[] result1 = TwoSum.twoSumHashMap(null, 5);
        assertEquals(0, result1.length);
        
        // 单元素数组
        int[] single = {5};
        int[] result2 = TwoSum.twoSumHashMap(single, 5);
        assertEquals(0, result2.length);
        
        // 两个元素数组
        int[] twoElements = {2, 3};
        int[] result3 = TwoSum.twoSumHashMap(twoElements, 5);
        assertEquals(2, result3.length);
        assertEquals(0, result3[0]);
        assertEquals(1, result3[1]);
    }

    // ==================== 反转链表测试 ====================

    /**
     * 测试反转链表 - 基础测试
     */
    @Test
    void testReverseLinkedListBasic() {
        int[] values = {1, 2, 3, 4, 5};
        ListNode<Integer> head = ListNode.createIntList(values);
        
        // 迭代方式
        ListNode<Integer> reversed1 = ReverseLinkedList.reverseIterative(head);
        assertNotNull(reversed1);
        assertEquals(5, reversed1.getVal());
        assertEquals(4, reversed1.getNext().getVal());
        assertEquals(3, reversed1.getNext().getNext().getVal());
        
        // 重新创建链表用于其他测试
        head = ListNode.createIntList(values);
        
        // 递归方式
        ListNode<Integer> reversed2 = ReverseLinkedList.reverseRecursive(head);
        assertNotNull(reversed2);
        assertEquals(5, reversed2.getVal());
    }

    /**
     * 测试反转链表 - 空链表
     */
    @Test
    void testReverseLinkedListEmpty() {
        ListNode<Integer> head = null;
        
        ListNode<Integer> reversed = ReverseLinkedList.reverseIterative(head);
        assertNull(reversed);
    }

    /**
     * 测试反转链表 - 单节点链表
     */
    @Test
    void testReverseLinkedListSingleNode() {
        ListNode<Integer> head = new ListNode<>(10);
        
        ListNode<Integer> reversed = ReverseLinkedList.reverseIterative(head);
        assertNotNull(reversed);
        assertEquals(10, reversed.getVal());
        assertNull(reversed.getNext());
    }

    /**
     * 测试反转链表 - 两节点链表
     */
    @Test
    void testReverseLinkedListTwoNodes() {
        ListNode<Integer> head = new ListNode<>(1);
        head.setNext(new ListNode<>(2));
        
        ListNode<Integer> reversed = ReverseLinkedList.reverseIterative(head);
        assertNotNull(reversed);
        assertEquals(2, reversed.getVal());
        assertEquals(1, reversed.getNext().getVal());
        assertNull(reversed.getNext().getNext());
    }

    /**
     * 测试反转链表 - 头插法
     */
    @Test
    void testReverseLinkedListHeadInsert() {
        int[] values = {1, 2, 3};
        ListNode<Integer> head = ListNode.createIntList(values);
        
        ListNode<Integer> reversed = ReverseLinkedList.reverseHeadInsert(head);
        assertNotNull(reversed);
        assertEquals(3, reversed.getVal());
        assertEquals(2, reversed.getNext().getVal());
        assertEquals(1, reversed.getNext().getNext().getVal());
        assertNull(reversed.getNext().getNext().getNext());
    }

    /**
     * 测试反转链表 - 栈方式
     */
    @Test
    void testReverseLinkedListUsingStack() {
        int[] values = {1, 2, 3, 4};
        ListNode<Integer> head = ListNode.createIntList(values);
        
        ListNode<Integer> reversed = ReverseLinkedList.reverseUsingStack(head);
        assertNotNull(reversed);
        assertEquals(4, reversed.getVal());
        assertEquals(3, reversed.getNext().getVal());
        assertEquals(2, reversed.getNext().getNext().getVal());
        assertEquals(1, reversed.getNext().getNext().getNext().getVal());
    }
}
