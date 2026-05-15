package com.example.onlinecoding;

import java.util.Arrays;

/**
 * 排序算法集合
 * 
 * 包含多种经典排序算法的实现：
 * 1. 快速排序 (Quick Sort) - 分治思想，平均O(n log n)
 * 2. 归并排序 (Merge Sort) - 分治思想，稳定O(n log n)
 * 3. 冒泡排序 (Bubble Sort) - 交换相邻元素，O(n²)
 * 4. 插入排序 (Insertion Sort) - 插入到已排序部分，O(n²)
 * 5. 选择排序 (Selection Sort) - 选择最小元素，O(n²)
 */
public class SortingAlgorithms {

    /**
     * 快速排序
     * 
     * 核心思想：选择一个基准元素，将数组分为两部分，
     * 左边都小于等于基准，右边都大于等于基准，然后递归排序。
     * 
     * 时间复杂度：平均 O(n log n)，最坏 O(n²)
     * 空间复杂度：O(log n)
     * 不稳定排序
     * 
     * @param arr 待排序数组
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 快速排序递归辅助方法
     * 
     * @param arr   待排序数组
     * @param left  左边界索引
     * @param right 右边界索引
     */
    private static void quickSort(int[] arr, int left, int right) {
        if (left >= right) {
            return;
        }
        
        // 选择基准元素（这里选择中间位置的元素）
        int pivotIndex = left + (right - left) / 2;
        int pivot = arr[pivotIndex];
        
        // 分区操作，返回分区后的基准位置
        int partitionIndex = partition(arr, left, right, pivot);
        
        // 递归排序左半部分
        quickSort(arr, left, partitionIndex - 1);
        // 递归排序右半部分
        quickSort(arr, partitionIndex, right);
    }

    /**
     * 分区操作：将数组分为两部分
     * 
     * @param arr   数组
     * @param left  左边界
     * @param right 右边界
     * @param pivot 基准值
     * @return 分区后的基准位置
     */
    private static int partition(int[] arr, int left, int right, int pivot) {
        while (left <= right) {
            // 从左向右找大于等于基准的元素
            while (arr[left] < pivot) {
                left++;
            }
            // 从右向左找小于等于基准的元素
            while (arr[right] > pivot) {
                right--;
            }
            
            // 交换元素
            if (left <= right) {
                swap(arr, left, right);
                left++;
                right--;
            }
        }
        return left;
    }

    /**
     * 归并排序
     * 
     * 核心思想：将数组分成两半，分别排序后合并。
     * 
     * 时间复杂度：O(n log n)
     * 空间复杂度：O(n)
     * 稳定排序
     * 
     * @param arr 待排序数组
     */
    public static void mergeSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        int[] temp = new int[arr.length];
        mergeSort(arr, temp, 0, arr.length - 1);
    }

    /**
     * 归并排序递归辅助方法
     * 
     * @param arr   待排序数组
     * @param temp  临时数组用于合并
     * @param left  左边界
     * @param right 右边界
     */
    private static void mergeSort(int[] arr, int[] temp, int left, int right) {
        if (left >= right) {
            return;
        }
        
        // 找到中间位置
        int mid = left + (right - left) / 2;
        
        // 递归排序左半部分
        mergeSort(arr, temp, left, mid);
        // 递归排序右半部分
        mergeSort(arr, temp, mid + 1, right);
        // 合并两个有序部分
        merge(arr, temp, left, mid, right);
    }

    /**
     * 合并两个有序子数组
     * 
     * @param arr   原数组
     * @param temp  临时数组
     * @param left  左边界
     * @param mid   中间位置
     * @param right 右边界
     */
    private static void merge(int[] arr, int[] temp, int left, int mid, int right) {
        // 将数据复制到临时数组
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i];
        }
        
        int i = left;      // 左子数组指针
        int j = mid + 1;   // 右子数组指针
        int k = left;      // 合并后数组指针
        
        // 合并两个有序数组
        while (i <= mid && j <= right) {
            if (temp[i] <= temp[j]) {
                arr[k] = temp[i];
                i++;
            } else {
                arr[k] = temp[j];
                j++;
            }
            k++;
        }
        
        // 复制剩余的左子数组元素
        while (i <= mid) {
            arr[k] = temp[i];
            i++;
            k++;
        }
        
        // 右子数组剩余元素不需要复制，因为已经在原位置
    }

    /**
     * 冒泡排序
     * 
     * 核心思想：重复遍历数组，比较相邻元素，
     * 如果顺序错误就交换，直到没有交换发生。
     * 
     * 时间复杂度：O(n²)
     * 空间复杂度：O(1)
     * 稳定排序
     * 
     * @param arr 待排序数组
     */
    public static void bubbleSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        
        int n = arr.length;
        boolean swapped;
        
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            
            // 每轮冒泡后，最大元素已在正确位置
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    swapped = true;
                }
            }
            
            // 如果没有发生交换，说明数组已经有序
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * 插入排序
     * 
     * 核心思想：将未排序元素插入到已排序部分的正确位置。
     * 
     * 时间复杂度：O(n²)，但对于几乎有序的数组接近O(n)
     * 空间复杂度：O(1)
     * 稳定排序
     * 
     * @param arr 待排序数组
     */
    public static void insertionSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        
        int n = arr.length;
        
        for (int i = 1; i < n; i++) {
            int key = arr[i];  // 当前要插入的元素
            int j = i - 1;     // 已排序部分的最后一个元素索引
            
            // 将大于key的元素向右移动
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            
            // 插入key到正确位置
            arr[j + 1] = key;
        }
    }

    /**
     * 选择排序
     * 
     * 核心思想：每次从未排序部分选择最小元素，放到已排序部分的末尾。
     * 
     * 时间复杂度：O(n²)
     * 空间复杂度：O(1)
     * 不稳定排序
     * 
     * @param arr 待排序数组
     */
    public static void selectionSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        
        int n = arr.length;
        
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;  // 最小元素的索引
            
            // 找到未排序部分的最小元素
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            
            // 将最小元素交换到已排序部分的末尾
            if (minIndex != i) {
                swap(arr, i, minIndex);
            }
        }
    }

    /**
     * 交换数组中两个元素的位置
     * 
     * @param arr 数组
     * @param i   索引i
     * @param j   索引j
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 判断数组是否已排序
     * 
     * @param arr 数组
     * @return 如果数组升序排列返回true，否则返回false
     */
    public static boolean isSorted(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return true;
        }
        
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印数组内容
     * 
     * @param arr 数组
     */
    public static void printArray(int[] arr) {
        if (arr == null) {
            System.out.println("null");
            return;
        }
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 复制数组
     * 
     * @param arr 原数组
     * @return 新数组副本
     */
    public static int[] copyArray(int[] arr) {
        if (arr == null) {
            return null;
        }
        return Arrays.copyOf(arr, arr.length);
    }

    /**
     * 主方法：演示各种排序算法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        int[] original = {64, 34, 25, 12, 22, 11, 90};
        
        System.out.println("原始数组:");
        printArray(original);
        
        // 测试快速排序
        int[] quickArr = copyArray(original);
        quickSort(quickArr);
        System.out.println("\n快速排序结果:");
        printArray(quickArr);
        
        // 测试归并排序
        int[] mergeArr = copyArray(original);
        mergeSort(mergeArr);
        System.out.println("\n归并排序结果:");
        printArray(mergeArr);
        
        // 测试冒泡排序
        int[] bubbleArr = copyArray(original);
        bubbleSort(bubbleArr);
        System.out.println("\n冒泡排序结果:");
        printArray(bubbleArr);
        
        // 测试插入排序
        int[] insertionArr = copyArray(original);
        insertionSort(insertionArr);
        System.out.println("\n插入排序结果:");
        printArray(insertionArr);
        
        // 测试选择排序
        int[] selectionArr = copyArray(original);
        selectionSort(selectionArr);
        System.out.println("\n选择排序结果:");
        printArray(selectionArr);
    }
}
