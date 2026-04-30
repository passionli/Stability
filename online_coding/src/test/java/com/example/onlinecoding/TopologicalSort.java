package com.example.onlinecoding;

import java.util.*;

/**
 * 拓扑排序（Topological Sort）实现类
 * 
 * 拓扑排序是对有向无环图（DAG）的顶点进行排序的算法，
 * 使得对于图中的任意一条有向边 (u, v)，顶点 u 在排序结果中都出现在顶点 v 之前。
 * 
 * 本类实现了两种拓扑排序算法：
 * 1. Kahn 算法（基于 BFS 和入度表）
 * 2. DFS 后序遍历算法（基于深度优先搜索和栈）
 */
public class TopologicalSort {

    /**
     * 使用 Kahn 算法实现拓扑排序（BFS 方式）
     * 
     * 算法核心思想：
     * 1. 计算每个节点的入度（指向该节点的边数）
     * 2. 将所有入度为 0 的节点加入队列
     * 3. 依次从队列中取出节点，加入结果集
     * 4. 减少其邻接节点的入度，若入度变为 0 则加入队列
     * 5. 如果结果集大小小于节点总数，说明存在环
     * 
     * @param n     节点数量（节点编号从 0 到 n-1）
     * @param edges 边的列表，每条边用 int[2] 表示，int[0] 是起点，int[1] 是终点
     * @return 拓扑排序结果列表；如果图中存在环，返回空列表
     */
    public List<Integer> kahnTopologicalSort(int n, List<int[]> edges) {
        // 边界条件：空图直接返回空列表
        if (n <= 0) {
            return new ArrayList<>();
        }

        // 构建邻接表：Map<起点, 终点列表>
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        // 入度表：记录每个节点的入度
        int[] inDegree = new int[n];

        // 初始化邻接表和入度表
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            // 将终点添加到起点的邻接列表中
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            // 终点的入度加 1
            inDegree[to]++;
        }

        // 创建队列，用于存储入度为 0 的节点
        Queue<Integer> queue = new LinkedList<>();
        // 将所有入度为 0 的节点入队
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        // 存储拓扑排序结果
        List<Integer> result = new ArrayList<>();

        // 处理队列中的节点
        while (!queue.isEmpty()) {
            // 出队一个节点
            int currentNode = queue.poll();
            // 将节点加入结果集
            result.add(currentNode);

            // 遍历当前节点的所有邻接节点
            for (int neighbor : adjacencyList.getOrDefault(currentNode, new ArrayList<>())) {
                // 邻接节点的入度减 1
                inDegree[neighbor]--;
                // 如果入度变为 0，说明该节点的所有依赖都已处理完毕
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 检测环：如果结果集大小小于节点总数，说明存在环
        if (result.size() != n) {
            return new ArrayList<>();
        }

        return result;
    }

    /**
     * 使用 DFS 后序遍历实现拓扑排序
     * 
     * 算法核心思想：
     * 1. 对未访问的节点进行深度优先搜索
     * 2. 在 DFS 完成后将节点压入栈（后序遍历）
     * 3. 反转栈得到拓扑序
     * 4. 使用三色标记法检测环（0=未访问, 1=访问中, 2=已访问）
     * 
     * @param n     节点数量（节点编号从 0 到 n-1）
     * @param edges 边的列表，每条边用 int[2] 表示
     * @return 拓扑排序结果列表；如果图中存在环，返回空列表
     */
    public List<Integer> dfsTopologicalSort(int n, List<int[]> edges) {
        // 边界条件：空图直接返回空列表
        if (n <= 0) {
            return new ArrayList<>();
        }

        // 构建邻接表
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }

        // 访问状态数组：0=未访问, 1=访问中, 2=已访问
        int[] visited = new int[n];
        // 用于存储后序遍历结果的栈
        Stack<Integer> stack = new Stack<>();
        // 用于检测环的标志（使用数组是为了在递归中修改）
        boolean[] hasCycle = {false};

        // 对每个未访问的节点进行 DFS
        for (int i = 0; i < n; i++) {
            if (visited[i] == 0 && !hasCycle[0]) {
                dfsHelper(i, adjacencyList, visited, stack, hasCycle);
            }
        }

        // 如果存在环，返回空列表
        if (hasCycle[0]) {
            return new ArrayList<>();
        }

        // 反转栈得到拓扑序
        List<Integer> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result;
    }

    /**
     * DFS 辅助方法：递归遍历图并进行后序处理
     * 
     * @param node       当前节点
     * @param adj        邻接表
     * @param visited    访问状态数组
     * @param stack      存储结果的栈
     * @param hasCycle   是否存在环的标志
     */
    private void dfsHelper(int node, Map<Integer, List<Integer>> adj, 
                           int[] visited, Stack<Integer> stack, boolean[] hasCycle) {
        // 将节点标记为"访问中"（正在处理该节点的子树）
        visited[node] = 1;

        // 遍历当前节点的所有邻接节点
        for (int neighbor : adj.getOrDefault(node, new ArrayList<>())) {
            if (visited[neighbor] == 0) {
                // 如果邻接节点未访问，递归访问
                dfsHelper(neighbor, adj, visited, stack, hasCycle);
                // 检测到环时提前返回
                if (hasCycle[0]) {
                    return;
                }
            } else if (visited[neighbor] == 1) {
                // 如果邻接节点正在访问中，说明存在环
                hasCycle[0] = true;
                return;
            }
            // visited[neighbor] == 2: 邻接节点已访问完毕，无需处理
        }

        // 将节点标记为"已访问"（处理完毕）
        visited[node] = 2;
        // 后序遍历：将节点压入栈
        stack.push(node);
    }

    /**
     * 验证拓扑排序结果是否正确
     * 
     * @param n      节点数量
     * @param edges  边的列表
     * @param result 待验证的拓扑排序结果
     * @return true 如果结果是有效的拓扑排序，false 否则
     */
    public boolean isValidTopologicalSort(int n, List<int[]> edges, List<Integer> result) {
        // 检查结果是否包含所有节点
        if (result == null || result.size() != n) {
            return false;
        }

        // 创建节点到位置的映射
        Map<Integer, Integer> positionMap = new HashMap<>();
        for (int i = 0; i < result.size(); i++) {
            positionMap.put(result.get(i), i);
        }

        // 检查每条边是否满足拓扑顺序
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            // 如果起点的位置在终点之后，说明不是有效的拓扑排序
            if (positionMap.getOrDefault(from, -1) > positionMap.getOrDefault(to, -1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将结果列表转换为格式化字符串
     * 
     * @param result 拓扑排序结果
     * @return 格式化的字符串表示
     */
    public String formatResult(List<Integer> result) {
        if (result == null || result.isEmpty()) {
            return "[]";
        }
        return result.toString();
    }

    /**
     * 打印拓扑排序结果
     * 
     * @param result 拓扑排序结果
     */
    public void printResult(List<Integer> result) {
        System.out.println(formatResult(result));
    }

    /**
     * 主方法：演示拓扑排序的使用
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 示例图：
        //     0 → 1 → 3
        //     ↓    ↘
        //     2 → 4
        int n = 5;
        List<int[]> edges = new ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{0, 2});
        edges.add(new int[]{1, 3});
        edges.add(new int[]{1, 4});
        edges.add(new int[]{2, 4});

        // 创建拓扑排序实例
        TopologicalSort topologicalSort = new TopologicalSort();

        // 使用 Kahn 算法
        System.out.println("=== Kahn 算法（BFS） ===");
        List<Integer> kahnResult = topologicalSort.kahnTopologicalSort(n, edges);
        topologicalSort.printResult(kahnResult);
        System.out.println("验证结果: " + topologicalSort.isValidTopologicalSort(n, edges, kahnResult));

        // 使用 DFS 算法
        System.out.println("\n=== DFS 后序遍历算法 ===");
        List<Integer> dfsResult = topologicalSort.dfsTopologicalSort(n, edges);
        topologicalSort.printResult(dfsResult);
        System.out.println("验证结果: " + topologicalSort.isValidTopologicalSort(n, edges, dfsResult));

        // 测试存在环的图
        System.out.println("\n=== 测试存在环的图 ===");
        List<int[]> cyclicEdges = new ArrayList<>();
        cyclicEdges.add(new int[]{0, 1});
        cyclicEdges.add(new int[]{1, 2});
        cyclicEdges.add(new int[]{2, 0}); // 形成环

        List<Integer> cyclicResultKahn = topologicalSort.kahnTopologicalSort(3, cyclicEdges);
        System.out.println("Kahn 算法结果（环检测）: " + cyclicResultKahn);

        List<Integer> cyclicResultDfs = topologicalSort.dfsTopologicalSort(3, cyclicEdges);
        System.out.println("DFS 算法结果（环检测）: " + cyclicResultDfs);
    }
}