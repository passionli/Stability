package com.example.onlinecoding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 二分查找算法测试类
 * 
 * 测试覆盖以下场景：
 * 1. 空数组
 * 2. 单元素数组
 * 3. 目标值在数组开头
 * 4. 目标值在数组中间
 * 5. 目标值在数组结尾
 * 6. 目标值不存在
 * 7. 包含重复元素的数组
 */
class BinarySearchTest {

    /**
     * 测试空数组
     */
    @Test
    void testEmptyArray() {
        int[] empty = {};
        
        assertEquals(-1, BinarySearch.searchIterative(empty, 5));
        assertEquals(-1, BinarySearch.searchRecursive(empty, 5));
        assertEquals(-1, BinarySearch.searchFirstOccurrence(empty, 5));
        assertEquals(-1, BinarySearch.searchLastOccurrence(empty, 5));
        assertArrayEquals(new int[]{-1, -1}, BinarySearch.searchRange(empty, 5));
    }

    /**
     * 测试null数组
     */
    @Test
    void testNullArray() {
        assertEquals(-1, BinarySearch.searchIterative(null, 5));
        assertEquals(-1, BinarySearch.searchRecursive(null, 5));
        assertEquals(-1, BinarySearch.searchFirstOccurrence(null, 5));
        assertEquals(-1, BinarySearch.searchLastOccurrence(null, 5));
        assertArrayEquals(new int[]{-1, -1}, BinarySearch.searchRange(null, 5));
    }

    /**
     * 测试单元素数组
     */
    @Test
    void testSingleElement() {
        int[] single = {5};
        
        // 目标值存在
        assertEquals(0, BinarySearch.searchIterative(single, 5));
        assertEquals(0, BinarySearch.searchRecursive(single, 5));
        assertEquals(0, BinarySearch.searchFirstOccurrence(single, 5));
        assertEquals(0, BinarySearch.searchLastOccurrence(single, 5));
        assertArrayEquals(new int[]{0, 0}, BinarySearch.searchRange(single, 5));
        
        // 目标值不存在
        assertEquals(-1, BinarySearch.searchIterative(single, 3));
        assertEquals(-1, BinarySearch.searchRecursive(single, 3));
    }

    /**
     * 测试目标值在数组开头
     */
    @Test
    void testTargetAtBeginning() {
        int[] arr = {1, 3, 5, 7, 9, 11, 13};
        
        assertEquals(0, BinarySearch.searchIterative(arr, 1));
        assertEquals(0, BinarySearch.searchRecursive(arr, 1));
    }

    /**
     * 测试目标值在数组中间
     */
    @Test
    void testTargetInMiddle() {
        int[] arr = {1, 3, 5, 7, 9, 11, 13};
        
        // 中间位置
        assertEquals(3, BinarySearch.searchIterative(arr, 7));
        assertEquals(3, BinarySearch.searchRecursive(arr, 7));
        
        // 偏左位置
        assertEquals(1, BinarySearch.searchIterative(arr, 3));
        assertEquals(1, BinarySearch.searchRecursive(arr, 3));
        
        // 偏右位置
        assertEquals(5, BinarySearch.searchIterative(arr, 11));
        assertEquals(5, BinarySearch.searchRecursive(arr, 11));
    }

    /**
     * 测试目标值在数组结尾
     */
    @Test
    void testTargetAtEnd() {
        int[] arr = {1, 3, 5, 7, 9, 11, 13};
        
        assertEquals(6, BinarySearch.searchIterative(arr, 13));
        assertEquals(6, BinarySearch.searchRecursive(arr, 13));
    }

    /**
     * 测试目标值不存在
     */
    @Test
    void testTargetNotExist() {
        int[] arr = {1, 3, 5, 7, 9, 11, 13};
        
        // 目标值小于最小值
        assertEquals(-1, BinarySearch.searchIterative(arr, 0));
        assertEquals(-1, BinarySearch.searchRecursive(arr, 0));
        
        // 目标值大于最大值
        assertEquals(-1, BinarySearch.searchIterative(arr, 15));
        assertEquals(-1, BinarySearch.searchRecursive(arr, 15));
        
        // 目标值在两个元素之间
        assertEquals(-1, BinarySearch.searchIterative(arr, 4));
        assertEquals(-1, BinarySearch.searchRecursive(arr, 4));
        
        assertEquals(-1, BinarySearch.searchIterative(arr, 8));
        assertEquals(-1, BinarySearch.searchRecursive(arr, 8));
    }

    /**
     * 测试包含重复元素的数组
     */
    @Test
    void testDuplicateElements() {
        int[] arr = {2, 4, 4, 4, 6, 8, 8, 10};
        
        // 测试查找第一个出现位置
        assertEquals(1, BinarySearch.searchFirstOccurrence(arr, 4));
        assertEquals(5, BinarySearch.searchFirstOccurrence(arr, 8));
        assertEquals(-1, BinarySearch.searchFirstOccurrence(arr, 5));
        
        // 测试查找最后一个出现位置
        assertEquals(3, BinarySearch.searchLastOccurrence(arr, 4));
        assertEquals(6, BinarySearch.searchLastOccurrence(arr, 8));
        assertEquals(-1, BinarySearch.searchLastOccurrence(arr, 5));
        
        // 测试查找范围
        assertArrayEquals(new int[]{1, 3}, BinarySearch.searchRange(arr, 4));
        assertArrayEquals(new int[]{5, 6}, BinarySearch.searchRange(arr, 8));
        assertArrayEquals(new int[]{-1, -1}, BinarySearch.searchRange(arr, 5));
    }

    /**
     * 测试偶数长度数组
     */
    @Test
    void testEvenLengthArray() {
        int[] arr = {1, 3, 5, 7, 9, 11};
        
        assertEquals(0, BinarySearch.searchIterative(arr, 1));
        assertEquals(2, BinarySearch.searchIterative(arr, 5));
        assertEquals(4, BinarySearch.searchIterative(arr, 9));
        assertEquals(5, BinarySearch.searchIterative(arr, 11));
        assertEquals(-1, BinarySearch.searchIterative(arr, 6));
    }
}
