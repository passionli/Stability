# 拓扑排序（Topological Sort）实现教程

## 一、拓扑排序概述

### 1.1 定义

拓扑排序是对有向无环图（Directed Acyclic Graph, DAG）的顶点进行排序的一种算法，使得对于图中的任意一条有向边 (u, v)，顶点 u 在排序结果中都出现在顶点 v 之前。

### 1.2 核心思想

拓扑排序的核心是在不违反依赖关系的前提下，对节点进行线性排序。如果图中存在环，则无法进行拓扑排序。

### 1.3 示意图

对于如下有向无环图：

```
    1 → 2 → 4
    ↓    ↘
    3 → 5
```

拓扑排序结果（可能有多种）：
- `[1, 2, 3, 4, 5]`
- `[1, 3, 2, 5, 4]`

### 1.4 应用场景

| 应用领域 | 具体场景 |
| :--- | :--- |
| **项目管理** | 任务调度，确定任务执行顺序 |
| **编译系统** | 编译顺序确定，处理文件依赖关系 |
| **课程安排** | 确定学习课程的先后顺序 |
| **依赖解析** | 软件包依赖解析、模块加载顺序 |
| **事件驱动系统** | 确定事件处理顺序 |

## 二、核心数据结构

### 2.1 图的表示

常用的图表示方法有两种：

**邻接表表示**：
```java
// Map<节点, 邻接节点列表>
Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
```

**邻接矩阵表示**：
```java
// matrix[i][j] = 1 表示有边从 i 到 j
int[][] adjacencyMatrix = new int[n][n];
```

**选择建议**：稀疏图适合邻接表，稠密图适合邻接矩阵。本文使用邻接表实现。

### 2.2 入度表

记录每个节点的入度（指向该节点的边数）：
```java
// inDegree[i] 表示节点 i 的入度
int[] inDegree = new int[n];
```

### 2.3 队列（用于 Kahn 算法）

用于存储入度为 0 的节点：
```java
Queue<Integer> queue = new LinkedList<>();
```

### 2.4 栈（用于 DFS 算法）

用于存储后序遍历结果：
```java
Stack<Integer> stack = new Stack<>();
```

## 三、Kahn 算法（BFS 方式）

### 3.1 算法步骤

```
1. 计算每个节点的入度
2. 将所有入度为 0 的节点加入队列
3. 创建结果列表
4. 当队列不为空时：
   a. 出队一个节点 u
   b. 将 u 加入结果列表
   c. 遍历 u 的所有邻接节点 v：
      i. 将 v 的入度减 1
      ii. 如果 v 的入度变为 0，将 v 入队
5. 如果结果列表大小小于节点总数，说明存在环，返回空列表或抛出异常
6. 返回结果列表
```

### 3.2 核心代码

```java
public List<Integer> kahnTopologicalSort(int n, List<int[]> edges) {
    // 构建邻接表
    Map<Integer, List<Integer>> adj = new HashMap<>();
    int[] inDegree = new int[n];
    
    for (int[] edge : edges) {
        int from = edge[0];
        int to = edge[1];
        adj.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        inDegree[to]++;
    }
    
    // 将入度为 0 的节点入队
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < n; i++) {
        if (inDegree[i] == 0) {
            queue.offer(i);
        }
    }
    
    List<Integer> result = new ArrayList<>();
    while (!queue.isEmpty()) {
        int node = queue.poll();
        result.add(node);
        
        // 更新邻接节点的入度
        for (int neighbor : adj.getOrDefault(node, new ArrayList<>())) {
            inDegree[neighbor]--;
            if (inDegree[neighbor] == 0) {
                queue.offer(neighbor);
            }
        }
    }
    
    // 检测环
    if (result.size() != n) {
        return new ArrayList<>(); // 存在环，返回空列表
    }
    
    return result;
}
```

### 3.3 算法图解

以图 `1 → 2 → 4, 1 → 3 → 5, 2 → 5` 为例：

```
初始状态：
入度: [0, 0, 1, 1, 2]  (节点0无，节点1入度0，节点2入度1，节点3入度1，节点4入度2)
队列: [1]

步骤1: 出队1, 结果=[1]
       更新邻接节点2和3的入度
       入度: [0, 0, 0, 0, 2]
       队列: [2, 3]

步骤2: 出队2, 结果=[1, 2]
       更新邻接节点4和5的入度
       入度: [0, 0, 0, 0, 1]
       队列: [3, 4]

步骤3: 出队3, 结果=[1, 2, 3]
       更新邻接节点5的入度
       入度: [0, 0, 0, 0, 0]
       队列: [4, 5]

步骤4: 出队4, 结果=[1, 2, 3, 4]
       无邻接节点
       队列: [5]

步骤5: 出队5, 结果=[1, 2, 3, 4, 5]
       无邻接节点
       队列: []

最终结果: [1, 2, 3, 4, 5]
```

## 四、DFS 后序遍历方式

### 4.1 算法步骤

```
1. 初始化访问状态数组（0=未访问, 1=访问中, 2=已访问）
2. 创建栈用于存储结果
3. 对每个未访问的节点调用 DFS
4. DFS 过程：
   a. 将节点标记为"访问中"
   b. 遍历所有邻接节点：
      i. 如果邻接节点未访问，递归访问
      ii. 如果邻接节点正在访问中，说明存在环
   c. 将节点标记为"已访问"
   d. 将节点压入栈
5. 反转栈得到拓扑序
```

### 4.2 核心代码

```java
public List<Integer> dfsTopologicalSort(int n, List<int[]> edges) {
    // 构建邻接表
    Map<Integer, List<Integer>> adj = new HashMap<>();
    for (int[] edge : edges) {
        int from = edge[0];
        int to = edge[1];
        adj.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }
    
    // 0=未访问, 1=访问中, 2=已访问
    int[] visited = new int[n];
    Stack<Integer> stack = new Stack<>();
    boolean[] hasCycle = {false};
    
    for (int i = 0; i < n; i++) {
        if (visited[i] == 0 && !hasCycle[0]) {
            dfs(i, adj, visited, stack, hasCycle);
        }
    }
    
    if (hasCycle[0]) {
        return new ArrayList<>(); // 存在环，返回空列表
    }
    
    // 反转栈得到拓扑序
    List<Integer> result = new ArrayList<>();
    while (!stack.isEmpty()) {
        result.add(stack.pop());
    }
    
    return result;
}

private void dfs(int node, Map<Integer, List<Integer>> adj, int[] visited, 
                 Stack<Integer> stack, boolean[] hasCycle) {
    // 标记为访问中
    visited[node] = 1;
    
    for (int neighbor : adj.getOrDefault(node, new ArrayList<>())) {
        if (visited[neighbor] == 0) {
            dfs(neighbor, adj, visited, stack, hasCycle);
        } else if (visited[neighbor] == 1) {
            // 发现环
            hasCycle[0] = true;
            return;
        }
        // visited[neighbor] == 2: 已访问过，无需处理
    }
    
    // 标记为已访问
    visited[node] = 2;
    // 后序：将节点压入栈
    stack.push(node);
}
```

### 4.3 算法图解

以图 `1 → 2 → 4, 1 → 3 → 5, 2 → 5` 为例：

```
DFS 遍历过程：
访问 1 → 访问 2 → 访问 4 → 4无邻接 → 压栈[4]
              ↓
              返回 2 → 访问 5 → 5无邻接 → 压栈[4, 5]
              ↓
              返回 2 → 压栈[4, 5, 2]
       ↓
       返回 1 → 访问 3 → 3无邻接(除已访问的5) → 压栈[4, 5, 2, 3]
       ↓
       返回 1 → 压栈[4, 5, 2, 3, 1]

栈内容: [4, 5, 2, 3, 1]
反转后: [1, 3, 2, 5, 4]
```

## 五、两种算法对比

| 特性 | Kahn 算法（BFS） | DFS 后序遍历 |
| :--- | :--- | :--- |
| **时间复杂度** | O(V + E) | O(V + E) |
| **空间复杂度** | O(V)（队列+入度表） | O(V)（递归栈+栈） |
| **环检测** | 结果大小 < V 则有环 | 访问状态检测 |
| **稳定性** | 队列顺序影响结果顺序 | DFS顺序影响结果顺序 |
| **实现难度** | 简单直观 | 稍复杂，需处理递归 |
| **适用场景** | 通用，适合求任意拓扑序 | 需要逆拓扑序时更方便 |

## 六、复杂度分析

### 6.1 时间复杂度

- **O(V + E)**：每个节点和每条边都被处理一次
  - V：节点数（Vertex）
  - E：边数（Edge）

### 6.2 空间复杂度

- **Kahn 算法**：O(V)
  - 邻接表：O(E)
  - 入度表：O(V)
  - 队列：O(V)（最坏情况）

- **DFS 算法**：O(V)
  - 邻接表：O(E)
  - 访问状态数组：O(V)
  - 递归栈：O(V)（最坏情况）
  - 结果栈：O(V)

## 七、边界情况处理

### 7.1 空图

```java
// 输入: n = 0, edges = []
// 输出: []
```

### 7.2 单节点图

```java
// 输入: n = 1, edges = []
// 输出: [0]
```

### 7.3 存在环的图

```java
// 图: 0 → 1 → 2 → 0 (形成环)
// 输入: n = 3, edges = [[0,1], [1,2], [2,0]]
// 输出: [] (无法拓扑排序)
```

### 7.4 多源节点图

```java
// 图: 0 → 2, 1 → 2
// 输入: n = 3, edges = [[0,2], [1,2]]
// 输出: [0, 1, 2] 或 [1, 0, 2]
```

## 八、测试用例设计

| 测试场景 | 输入描述 | 预期输出 |
| :--- | :--- | :--- |
| 空图 | n=0, edges=[] | [] |
| 单节点 | n=1, edges=[] | [0] |
| 正常DAG | n=5, edges=[[0,1],[0,2],[1,3],[2,3],[1,4]] | 合法拓扑序 |
| 存在环 | n=3, edges=[[0,1],[1,2],[2,0]] | [] |
| 多源节点 | n=3, edges=[[0,2],[1,2]] | [0,1,2]或[1,0,2] |
| 链式结构 | n=4, edges=[[0,1],[1,2],[2,3]] | [0,1,2,3] |

## 九、总结

拓扑排序是处理有向无环图依赖关系的重要算法，核心要点：

1. **Kahn 算法**：基于 BFS，使用入度表和队列，直观易懂
2. **DFS 算法**：基于后序遍历，使用栈反转得到拓扑序
3. **环检测**：两种算法都能检测图中是否存在环
4. **时间复杂度**：O(V + E)，线性复杂度
5. **应用广泛**：任务调度、编译顺序、依赖解析等

在实际应用中，Kahn 算法更常用，因为它不需要递归，避免了栈溢出风险，且更容易理解和实现。