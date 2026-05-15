package com.example.onlinecoding;

/**
 * 斐波那契数列
 * 
 * 斐波那契数列定义：
 * F(0) = 0
 * F(1) = 1
 * F(n) = F(n-1) + F(n-2) (n > 1)
 * 
 * 数列：0, 1, 1, 2, 3, 5, 8, 13, 21, 34, ...
 */
public class Fibonacci {

    /**
     * 递归方式计算斐波那契数
     * 
     * 时间复杂度：O(2^n) - 存在大量重复计算
     * 空间复杂度：O(n) - 递归调用栈深度
     * 
     * @param n 第n个斐波那契数（从0开始）
     * @return 第n个斐波那契数
     */
    public static long fibonacciRecursive(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n 必须是非负整数");
        }
        if (n <= 1) {
            return n;
        }
        return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2);
    }

    /**
     * 动态规划方式计算斐波那契数（迭代）
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     * 
     * @param n 第n个斐波那契数（从0开始）
     * @return 第n个斐波那契数
     */
    public static long fibonacciDP(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n 必须是非负整数");
        }
        if (n <= 1) {
            return n;
        }
        
        long prevPrev = 0;  // F(0)
        long prev = 1;      // F(1)
        long current = 0;   // F(n)
        
        for (int i = 2; i <= n; i++) {
            current = prevPrev + prev;
            prevPrev = prev;
            prev = current;
        }
        
        return current;
    }

    /**
     * 动态规划方式计算斐波那契数（数组存储）
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     * 
     * @param n 第n个斐波那契数（从0开始）
     * @return 第n个斐波那契数
     */
    public static long fibonacciDPArray(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n 必须是非负整数");
        }
        if (n <= 1) {
            return n;
        }
        
        long[] dp = new long[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        
        return dp[n];
    }

    /**
     * 矩阵快速幂方式计算斐波那契数
     * 
     * 时间复杂度：O(log n)
     * 空间复杂度：O(log n) - 递归栈深度
     * 
     * 原理：利用矩阵乘法性质
     * [F(n+1) F(n)  ]   =   [[1, 1], [1, 0]]^n
     * [F(n)   F(n-1)]
     * 
     * @param n 第n个斐波那契数（从0开始）
     * @return 第n个斐波那契数
     */
    public static long fibonacciMatrix(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n 必须是非负整数");
        }
        if (n <= 1) {
            return n;
        }
        
        long[][] result = matrixPower(new long[][]{{1, 1}, {1, 0}}, n - 1);
        return result[0][0];
    }

    /**
     * 矩阵幂运算
     * 
     * @param matrix 矩阵
     * @param power  幂次
     * @return 矩阵的power次幂
     */
    private static long[][] matrixPower(long[][] matrix, int power) {
        // 单位矩阵
        long[][] result = {{1, 0}, {0, 1}};
        
        while (power > 0) {
            if (power % 2 == 1) {
                result = matrixMultiply(result, matrix);
            }
            matrix = matrixMultiply(matrix, matrix);
            power /= 2;
        }
        
        return result;
    }

    /**
     * 2x2矩阵乘法
     * 
     * @param a 矩阵a
     * @param b 矩阵b
     * @return a * b
     */
    private static long[][] matrixMultiply(long[][] a, long[][] b) {
        return new long[][]{
            {a[0][0] * b[0][0] + a[0][1] * b[1][0], a[0][0] * b[0][1] + a[0][1] * b[1][1]},
            {a[1][0] * b[0][0] + a[1][1] * b[1][0], a[1][0] * b[0][1] + a[1][1] * b[1][1]}
        };
    }

    /**
     * 输出斐波那契数列的前n项
     * 
     * @param n 项数
     */
    public static void printFibonacciSequence(int n) {
        if (n <= 0) {
            return;
        }
        
        System.out.print("斐波那契数列前" + n + "项: ");
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(fibonacciDP(i));
        }
        System.out.println();
    }

    /**
     * 主方法：演示斐波那契数列的计算
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        int n = 10;
        
        System.out.println("=== 斐波那契数列 ===");
        printFibonacciSequence(n + 1);
        
        System.out.println("\n第 " + n + " 个斐波那契数:");
        System.out.println("递归方式: " + fibonacciRecursive(n));
        System.out.println("动态规划(迭代): " + fibonacciDP(n));
        System.out.println("动态规划(数组): " + fibonacciDPArray(n));
        System.out.println("矩阵快速幂: " + fibonacciMatrix(n));
        
        // 测试较大的n值（递归方式会很慢）
        int largeN = 40;
        System.out.println("\n第 " + largeN + " 个斐波那契数:");
        System.out.println("动态规划(迭代): " + fibonacciDP(largeN));
        System.out.println("矩阵快速幂: " + fibonacciMatrix(largeN));
    }
}
