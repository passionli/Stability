package com.example.onlinecoding;

/**
 * 二分查找算法
 * 
 * 二分查找（Binary Search）是一种高效的查找算法，
 * 要求数组必须是有序的。
 * 
 * 核心思想：
 * 1. 将目标值与中间元素比较
 * 2. 如果目标值等于中间元素，返回索引
 * 3. 如果目标值小于中间元素，在左半部分继续查找
 * 4. 如果目标值大于中间元素，在右半部分继续查找
 * 5. 如果查找范围为空，返回 -1
 * 
 * 时间复杂度：O(log n)
 * 空间复杂度：迭代方式 O(1)，递归方式 O(log n)
 */
public class BinarySearch {

    /**
     * 迭代方式实现二分查找
     * 
     * @param arr    有序数组（升序）
     * @param target 目标值
     * @return 目标值所在索引，如果不存在返回 -1
     */
    public static int searchIterative(int[] arr, int target) {
        if (arr == null || arr.length == 0) {
            return -1;
        }
        
        int left = 0;
        int right = arr.length - 1;
        
        while (left <= right) {
            // 计算中间位置，避免溢出
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                // 找到目标值，返回索引
                return mid;
            } else if (arr[mid] < target) {
                // 目标值在右半部分
                left = mid + 1;
            } else {
                // 目标值在左半部分
                right = mid - 1;
            }
        }
        
        // 未找到目标值
        return -1;
    }

    /**
     * 递归方式实现二分查找
     * 
     * @param arr    有序数组（升序）
     * @param target 目标值
     * @return 目标值所在索引，如果不存在返回 -1
     */
    public static int searchRecursive(int[] arr, int target) {
        if (arr == null || arr.length == 0) {
            return -1;
        }
        return searchRecursive(arr, target, 0, arr.length - 1);
    }

    /**
     * 递归辅助方法
     * 
     * @param arr    有序数组
     * @param target 目标值
     * @param left   左边界
     * @param right  右边界
     * @return 目标值所在索引，如果不存在返回 -1
     */
    private static int searchRecursive(int[] arr, int target, int left, int right) {
        // 递归终止条件：查找范围为空
        if (left > right) {
            return -1;
        }
        
        // 计算中间位置
        int mid = left + (right - left) / 2;
        
        if (arr[mid] == target) {
            // 找到目标值
            return mid;
        } else if (arr[mid] < target) {
            // 在右半部分继续查找
            return searchRecursive(arr, target, mid + 1, right);
        } else {
            // 在左半部分继续查找
            return searchRecursive(arr, target, left, mid - 1);
        }
    }

    /**
     * 查找第一个等于目标值的元素索引
     * 
     * @param arr    有序数组（升序）
     * @param target 目标值
     * @return 第一个目标值所在索引，如果不存在返回 -1
     */
    public static int searchFirstOccurrence(int[] arr, int target) {
        if (arr == null || arr.length == 0) {
            return -1;
        }
        
        int left = 0;
        int right = arr.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                // 找到目标值，继续向左查找是否有更早的出现
                result = mid;
                right = mid - 1;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }

    /**
     * 查找最后一个等于目标值的元素索引
     * 
     * @param arr    有序数组（升序）
     * @param target 目标值
     * @return 最后一个目标值所在索引，如果不存在返回 -1
     */
    public static int searchLastOccurrence(int[] arr, int target) {
        if (arr == null || arr.length == 0) {
            return -1;
        }
        
        int left = 0;
        int right = arr.length - 1;
        int result = -1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                // 找到目标值，继续向右查找是否有更晚的出现
                result = mid;
                left = mid + 1;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return result;
    }

    /**
     * 查找目标值的范围（第一个和最后一个出现位置）
     * 
     * @param arr    有序数组（升序）
     * @param target 目标值
     * @return 包含两个元素的数组，分别表示第一个和最后一个出现位置，
     *         如果不存在则返回 [-1, -1]
     */
    public static int[] searchRange(int[] arr, int target) {
        int first = searchFirstOccurrence(arr, target);
        int last = searchLastOccurrence(arr, target);
        return new int[]{first, last};
    }

    /**
     * 主方法：演示二分查找的使用
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19};
        
        System.out.println("数组: [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]");
        
        // 测试迭代方式
        int target = 7;
        int result1 = searchIterative(arr, target);
        System.out.println("\n迭代方式查找 " + target + ": 索引 = " + result1);
        
        // 测试递归方式
        int result2 = searchRecursive(arr, target);
        System.out.println("递归方式查找 " + target + ": 索引 = " + result2);
        
        // 测试查找不存在的元素
        int notFound = 4;
        int result3 = searchIterative(arr, notFound);
        System.out.println("\n查找不存在的元素 " + notFound + ": 索引 = " + result3);
        
        // 测试查找边界元素
        int firstElement = 1;
        int lastElement = 19;
        System.out.println("\n查找第一个元素 " + firstElement + ": 索引 = " + searchIterative(arr, firstElement));
        System.out.println("查找最后一个元素 " + lastElement + ": 索引 = " + searchIterative(arr, lastElement));
        
        // 测试重复元素
        int[] arrWithDuplicates = {2, 4, 4, 4, 6, 8, 8, 10};
        System.out.println("\n数组: [2, 4, 4, 4, 6, 8, 8, 10]");
        System.out.println("查找第一个 4: 索引 = " + searchFirstOccurrence(arrWithDuplicates, 4));
        System.out.println("查找最后一个 4: 索引 = " + searchLastOccurrence(arrWithDuplicates, 4));
        int[] range = searchRange(arrWithDuplicates, 8);
        System.out.println("查找 8 的范围: [" + range[0] + ", " + range[1] + "]");
    }
}
