package com.example.onlinecoding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 排序算法测试类
 * 
 * 测试覆盖以下场景：
 * 1. 空数组
 * 2. 单元素数组
 * 3. 已排序数组
 * 4. 逆序数组
 * 5. 随机数组
 * 6. 包含重复元素的数组
 */
class SortingAlgorithmsTest {

    /**
     * 测试空数组排序
     */
    @Test
    void testEmptyArray() {
        int[] empty = {};
        
        // 快速排序
        int[] quickArr = SortingAlgorithms.copyArray(empty);
        SortingAlgorithms.quickSort(quickArr);
        assertTrue(SortingAlgorithms.isSorted(quickArr));
        
        // 归并排序
        int[] mergeArr = SortingAlgorithms.copyArray(empty);
        SortingAlgorithms.mergeSort(mergeArr);
        assertTrue(SortingAlgorithms.isSorted(mergeArr));
        
        // 冒泡排序
        int[] bubbleArr = SortingAlgorithms.copyArray(empty);
        SortingAlgorithms.bubbleSort(bubbleArr);
        assertTrue(SortingAlgorithms.isSorted(bubbleArr));
        
        // 插入排序
        int[] insertionArr = SortingAlgorithms.copyArray(empty);
        SortingAlgorithms.insertionSort(insertionArr);
        assertTrue(SortingAlgorithms.isSorted(insertionArr));
        
        // 选择排序
        int[] selectionArr = SortingAlgorithms.copyArray(empty);
        SortingAlgorithms.selectionSort(selectionArr);
        assertTrue(SortingAlgorithms.isSorted(selectionArr));
    }

    /**
     * 测试单元素数组排序
     */
    @Test
    void testSingleElement() {
        int[] single = {5};
        
        // 快速排序
        int[] quickArr = SortingAlgorithms.copyArray(single);
        SortingAlgorithms.quickSort(quickArr);
        assertTrue(SortingAlgorithms.isSorted(quickArr));
        assertArrayEquals(new int[]{5}, quickArr);
        
        // 归并排序
        int[] mergeArr = SortingAlgorithms.copyArray(single);
        SortingAlgorithms.mergeSort(mergeArr);
        assertTrue(SortingAlgorithms.isSorted(mergeArr));
        assertArrayEquals(new int[]{5}, mergeArr);
        
        // 冒泡排序
        int[] bubbleArr = SortingAlgorithms.copyArray(single);
        SortingAlgorithms.bubbleSort(bubbleArr);
        assertTrue(SortingAlgorithms.isSorted(bubbleArr));
        assertArrayEquals(new int[]{5}, bubbleArr);
        
        // 插入排序
        int[] insertionArr = SortingAlgorithms.copyArray(single);
        SortingAlgorithms.insertionSort(insertionArr);
        assertTrue(SortingAlgorithms.isSorted(insertionArr));
        assertArrayEquals(new int[]{5}, insertionArr);
        
        // 选择排序
        int[] selectionArr = SortingAlgorithms.copyArray(single);
        SortingAlgorithms.selectionSort(selectionArr);
        assertTrue(SortingAlgorithms.isSorted(selectionArr));
        assertArrayEquals(new int[]{5}, selectionArr);
    }

    /**
     * 测试已排序数组排序
     */
    @Test
    void testAlreadySorted() {
        int[] sorted = {1, 2, 3, 4, 5, 6, 7};
        
        // 快速排序
        int[] quickArr = SortingAlgorithms.copyArray(sorted);
        SortingAlgorithms.quickSort(quickArr);
        assertTrue(SortingAlgorithms.isSorted(quickArr));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, quickArr);
        
        // 归并排序
        int[] mergeArr = SortingAlgorithms.copyArray(sorted);
        SortingAlgorithms.mergeSort(mergeArr);
        assertTrue(SortingAlgorithms.isSorted(mergeArr));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, mergeArr);
        
        // 冒泡排序
        int[] bubbleArr = SortingAlgorithms.copyArray(sorted);
        SortingAlgorithms.bubbleSort(bubbleArr);
        assertTrue(SortingAlgorithms.isSorted(bubbleArr));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, bubbleArr);
        
        // 插入排序
        int[] insertionArr = SortingAlgorithms.copyArray(sorted);
        SortingAlgorithms.insertionSort(insertionArr);
        assertTrue(SortingAlgorithms.isSorted(insertionArr));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, insertionArr);
        
        // 选择排序
        int[] selectionArr = SortingAlgorithms.copyArray(sorted);
        SortingAlgorithms.selectionSort(selectionArr);
        assertTrue(SortingAlgorithms.isSorted(selectionArr));
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7}, selectionArr);
    }

    /**
     * 测试逆序数组排序
     */
    @Test
    void testReverseOrder() {
        int[] reversed = {7, 6, 5, 4, 3, 2, 1};
        int[] expected = {1, 2, 3, 4, 5, 6, 7};
        
        // 快速排序
        int[] quickArr = SortingAlgorithms.copyArray(reversed);
        SortingAlgorithms.quickSort(quickArr);
        assertTrue(SortingAlgorithms.isSorted(quickArr));
        assertArrayEquals(expected, quickArr);
        
        // 归并排序
        int[] mergeArr = SortingAlgorithms.copyArray(reversed);
        SortingAlgorithms.mergeSort(mergeArr);
        assertTrue(SortingAlgorithms.isSorted(mergeArr));
        assertArrayEquals(expected, mergeArr);
        
        // 冒泡排序
        int[] bubbleArr = SortingAlgorithms.copyArray(reversed);
        SortingAlgorithms.bubbleSort(bubbleArr);
        assertTrue(SortingAlgorithms.isSorted(bubbleArr));
        assertArrayEquals(expected, bubbleArr);
        
        // 插入排序
        int[] insertionArr = SortingAlgorithms.copyArray(reversed);
        SortingAlgorithms.insertionSort(insertionArr);
        assertTrue(SortingAlgorithms.isSorted(insertionArr));
        assertArrayEquals(expected, insertionArr);
        
        // 选择排序
        int[] selectionArr = SortingAlgorithms.copyArray(reversed);
        SortingAlgorithms.selectionSort(selectionArr);
        assertTrue(SortingAlgorithms.isSorted(selectionArr));
        assertArrayEquals(expected, selectionArr);
    }

    /**
     * 测试随机数组排序
     */
    @Test
    void testRandomArray() {
        int[] random = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        
        // 快速排序
        int[] quickArr = SortingAlgorithms.copyArray(random);
        SortingAlgorithms.quickSort(quickArr);
        assertTrue(SortingAlgorithms.isSorted(quickArr));
        assertArrayEquals(expected, quickArr);
        
        // 归并排序
        int[] mergeArr = SortingAlgorithms.copyArray(random);
        SortingAlgorithms.mergeSort(mergeArr);
        assertTrue(SortingAlgorithms.isSorted(mergeArr));
        assertArrayEquals(expected, mergeArr);
        
        // 冒泡排序
        int[] bubbleArr = SortingAlgorithms.copyArray(random);
        SortingAlgorithms.bubbleSort(bubbleArr);
        assertTrue(SortingAlgorithms.isSorted(bubbleArr));
        assertArrayEquals(expected, bubbleArr);
        
        // 插入排序
        int[] insertionArr = SortingAlgorithms.copyArray(random);
        SortingAlgorithms.insertionSort(insertionArr);
        assertTrue(SortingAlgorithms.isSorted(insertionArr));
        assertArrayEquals(expected, insertionArr);
        
        // 选择排序
        int[] selectionArr = SortingAlgorithms.copyArray(random);
        SortingAlgorithms.selectionSort(selectionArr);
        assertTrue(SortingAlgorithms.isSorted(selectionArr));
        assertArrayEquals(expected, selectionArr);
    }

    /**
     * 测试包含重复元素的数组排序
     */
    @Test
    void testDuplicateElements() {
        int[] duplicates = {5, 3, 8, 5, 2, 8, 1, 3};
        int[] expected = {1, 2, 3, 3, 5, 5, 8, 8};
        
        // 快速排序
        int[] quickArr = SortingAlgorithms.copyArray(duplicates);
        SortingAlgorithms.quickSort(quickArr);
        assertTrue(SortingAlgorithms.isSorted(quickArr));
        assertArrayEquals(expected, quickArr);
        
        // 归并排序
        int[] mergeArr = SortingAlgorithms.copyArray(duplicates);
        SortingAlgorithms.mergeSort(mergeArr);
        assertTrue(SortingAlgorithms.isSorted(mergeArr));
        assertArrayEquals(expected, mergeArr);
        
        // 冒泡排序
        int[] bubbleArr = SortingAlgorithms.copyArray(duplicates);
        SortingAlgorithms.bubbleSort(bubbleArr);
        assertTrue(SortingAlgorithms.isSorted(bubbleArr));
        assertArrayEquals(expected, bubbleArr);
        
        // 插入排序
        int[] insertionArr = SortingAlgorithms.copyArray(duplicates);
        SortingAlgorithms.insertionSort(insertionArr);
        assertTrue(SortingAlgorithms.isSorted(insertionArr));
        assertArrayEquals(expected, insertionArr);
        
        // 选择排序
        int[] selectionArr = SortingAlgorithms.copyArray(duplicates);
        SortingAlgorithms.selectionSort(selectionArr);
        assertTrue(SortingAlgorithms.isSorted(selectionArr));
        assertArrayEquals(expected, selectionArr);
    }

    /**
     * 测试null数组
     */
    @Test
    void testNullArray() {
        // 所有排序算法对null数组应不抛异常
        assertDoesNotThrow(() -> SortingAlgorithms.quickSort(null));
        assertDoesNotThrow(() -> SortingAlgorithms.mergeSort(null));
        assertDoesNotThrow(() -> SortingAlgorithms.bubbleSort(null));
        assertDoesNotThrow(() -> SortingAlgorithms.insertionSort(null));
        assertDoesNotThrow(() -> SortingAlgorithms.selectionSort(null));
    }
}
