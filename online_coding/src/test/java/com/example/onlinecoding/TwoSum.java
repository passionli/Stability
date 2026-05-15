package com.example.onlinecoding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 两数之和问题
 * 
 * 给定一个整数数组 nums 和一个整数目标值 target，
 * 请你在该数组中找出和为目标值 target 的那两个整数，
 * 并返回它们的数组下标。
 * 
 * 假设：
 * - 每种输入只会对应一个答案
 * - 不能重复使用同一个元素
 * - 返回的下标顺序可以任意
 */
public class TwoSum {

    /**
     * 暴力解法
     * 
     * 时间复杂度：O(n²)
     * 空间复杂度：O(1)
     * 
     * @param nums   整数数组
     * @param target 目标值
     * @return 两个数的索引数组，如果不存在返回空数组
     */
    public static int[] twoSumBruteForce(int[] nums, int target) {
        if (nums == null || nums.length < 2) {
            return new int[0];
        }
        
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        
        return new int[0];
    }

    /**
     * 哈希表优化解法
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     * 
     * 思路：遍历数组时，用哈希表存储已遍历的数字及其索引，
     * 对于当前数字，检查哈希表中是否存在 target - nums[i]
     * 
     * @param nums   整数数组
     * @param target 目标值
     * @return 两个数的索引数组，如果不存在返回空数组
     */
    public static int[] twoSumHashMap(int[] nums, int target) {
        if (nums == null || nums.length < 2) {
            return new int[0];
        }
        
        Map<Integer, Integer> map = new HashMap<>();
        
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            
            // 检查哈希表中是否存在补数
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }
            
            // 将当前数字和索引存入哈希表
            map.put(nums[i], i);
        }
        
        return new int[0];
    }

    /**
     * 双指针解法（需要先排序，会改变原数组顺序）
     * 
     * 时间复杂度：O(n log n) - 主要是排序的时间
     * 空间复杂度：O(n) - 需要额外空间存储索引信息
     * 
     * @param nums   整数数组
     * @param target 目标值
     * @return 两个数的索引数组，如果不存在返回空数组
     */
    public static int[] twoSumTwoPointers(int[] nums, int target) {
        if (nums == null || nums.length < 2) {
            return new int[0];
        }
        
        // 创建包含值和原始索引的数组
        int[][] numsWithIndex = new int[nums.length][2];
        for (int i = 0; i < nums.length; i++) {
            numsWithIndex[i][0] = nums[i];
            numsWithIndex[i][1] = i;
        }
        
        // 按值排序
        Arrays.sort(numsWithIndex, (a, b) -> Integer.compare(a[0], b[0]));
        
        // 双指针
        int left = 0;
        int right = nums.length - 1;
        
        while (left < right) {
            int sum = numsWithIndex[left][0] + numsWithIndex[right][0];
            
            if (sum == target) {
                return new int[]{numsWithIndex[left][1], numsWithIndex[right][1]};
            } else if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
        
        return new int[0];
    }

    /**
     * 打印结果
     * 
     * @param result 结果数组
     */
    public static void printResult(int[] result) {
        if (result == null || result.length == 0) {
            System.out.println("未找到满足条件的两个数");
        } else {
            System.out.println("[" + result[0] + ", " + result[1] + "]");
        }
    }

    /**
     * 主方法：演示两数之和问题的解法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        
        System.out.println("数组: " + Arrays.toString(nums));
        System.out.println("目标值: " + target);
        
        System.out.println("\n暴力解法:");
        int[] result1 = twoSumBruteForce(nums, target);
        printResult(result1);
        
        System.out.println("\n哈希表解法:");
        int[] result2 = twoSumHashMap(nums, target);
        printResult(result2);
        
        System.out.println("\n双指针解法:");
        int[] result3 = twoSumTwoPointers(nums, target);
        printResult(result3);
        
        // 测试另一个例子
        int[] nums2 = {3, 2, 4};
        int target2 = 6;
        
        System.out.println("\n\n数组: " + Arrays.toString(nums2));
        System.out.println("目标值: " + target2);
        
        System.out.println("哈希表解法:");
        int[] result4 = twoSumHashMap(nums2, target2);
        printResult(result4);
    }
}
