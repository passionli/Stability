# 二叉树层序遍历（BFS）实现教程

## 一、层序遍历概述

层序遍历（Level Order Traversal），也称为广度优先搜索（Breadth-First Search, BFS），是一种按层级顺序访问二叉树节点的遍历方式。

### 1.1 核心思想

从二叉树的根节点开始，按照从上到下、从左到右的顺序逐层访问每个节点。

### 1.2 遍历顺序示意图

对于如下二叉树：

```
        1
       / \
      2   3
     / \   \
    4   5   6
```

层序遍历结果：`[1, 2, 3, 4, 5, 6]`

分层输出结果：`[[1], [2, 3], [4, 5, 6]]`

### 1.3 应用场景

- 二叉树的层级打印
- 求二叉树的最大深度
- 求二叉树的最小深度
- 按层级输出二叉树节点
- 二叉树的序列化与反序列化

## 二、核心数据结构

### 2.1 二叉树节点定义

```java
public class BinaryTreeNode<T> {
    T value;           // 节点值
    BinaryTreeNode<T> left;   // 左子节点
    BinaryTreeNode<T> right;  // 右子节点
    
    public BinaryTreeNode(T value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }
}
```

### 2.2 队列（Queue）

层序遍历的核心辅助数据结构是队列，具有**先进先出（FIFO）**的特性。

| 操作 | 方法 | 说明 |
| :--- | :--- | :--- |
| 入队 | `add()` / `offer()` | 将元素添加到队列尾部 |
| 出队 | `remove()` / `poll()` | 移除并返回队列头部元素 |
| 查看队首 | `element()` / `peek()` | 返回但不移除队列头部元素 |
| 判断为空 | `isEmpty()` | 判断队列是否为空 |

## 三、迭代实现方式

### 3.1 算法步骤

```
1. 如果根节点为空，返回空列表
2. 创建队列并将根节点入队
3. 创建结果列表
4. 当队列不为空时：
   a. 记录当前队列大小（当前层节点数）
   b. 创建当前层的临时列表
   c. 循环当前层节点数次：
      i. 出队一个节点
      ii. 将节点值加入当前层列表
      iii. 如果左子节点存在，入队
      iv. 如果右子节点存在，入队
   d. 将当前层列表加入结果列表
5. 返回结果列表
```

### 3.2 核心代码

```java
public List<List<Integer>> levelOrder(BinaryTreeNode<Integer> root) {
    List<List<Integer>> result = new ArrayList<>();
    
    if (root == null) {
        return result;
    }
    
    Queue<BinaryTreeNode<Integer>> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        // 当前层节点数量
        int levelSize = queue.size();
        List<Integer> currentLevel = new ArrayList<>();
        
        for (int i = 0; i < levelSize; i++) {
            BinaryTreeNode<Integer> node = queue.poll();
            currentLevel.add(node.value);
            
            // 左子节点入队
            if (node.left != null) {
                queue.offer(node.left);
            }
            // 右子节点入队
            if (node.right != null) {
                queue.offer(node.right);
            }
        }
        
        result.add(currentLevel);
    }
    
    return result;
}
```

## 四、递归实现方式

### 4.1 算法思想

递归实现通过记录当前层级，使用深度优先搜索（DFS）的方式访问节点，将节点值添加到对应层级的列表中。

### 4.2 核心代码

```java
public List<List<Integer>> levelOrderRecursive(BinaryTreeNode<Integer> root) {
    List<List<Integer>> result = new ArrayList<>();
    dfs(root, 0, result);
    return result;
}

private void dfs(BinaryTreeNode<Integer> node, int level, List<List<Integer>> result) {
    if (node == null) {
        return;
    }
    
    // 如果当前层级列表不存在，创建新列表
    if (result.size() == level) {
        result.add(new ArrayList<>());
    }
    
    // 将当前节点值添加到对应层级
    result.get(level).add(node.value);
    
    // 递归遍历左子树
    dfs(node.left, level + 1, result);
    // 递归遍历右子树
    dfs(node.right, level + 1, result);
}
```

## 五、两种实现方式对比

| 特性 | 迭代方式（队列） | 递归方式（DFS） |
| :--- | :--- | :--- |
| **时间复杂度** | O(n) | O(n) |
| **空间复杂度** | O(n)（最坏情况：完全二叉树最后一层） | O(n)（最坏情况：斜树递归栈） |
| **内存使用** | 队列存储节点 | 递归调用栈 |
| **适用场景** | 通用，适合所有二叉树 | 代码简洁，但深度过大可能栈溢出 |
| **代码复杂度** | 中等 | 简单 |

## 六、复杂度分析

### 6.1 时间复杂度

- **O(n)**：每个节点恰好被访问一次

### 6.2 空间复杂度

- **迭代方式**：O(n)，在完全二叉树中，最后一层节点数最多为 n/2
- **递归方式**：O(h)，h 为树的高度，最坏情况（斜树）为 O(n)

## 七、边界情况处理

### 7.1 空树

```java
if (root == null) {
    return new ArrayList<>();
}
```

### 7.2 单节点树

```java
// 输入: root = new BinaryTreeNode<>(1)
// 输出: [[1]]
```

### 7.3 斜树

```java
// 左斜树输入:
//    1
//   /
//  2
// /
// 3
// 输出: [[1], [2], [3]]
```

## 八、测试用例设计

| 测试场景 | 输入描述 | 预期输出 |
| :--- | :--- | :--- |
| 空树 | `null` | `[]` |
| 单节点 | 只有根节点 1 | `[[1]]` |
| 完全二叉树 | 三层完全二叉树 | `[[1], [2, 3], [4, 5, 6, 7]]` |
| 左斜树 | 只有左子树 | `[[1], [2], [3]]` |
| 右斜树 | 只有右子树 | `[[1], [2], [3]]` |
| 普通二叉树 | 非完全二叉树 | 按层输出 |

## 九、总结

层序遍历（BFS）是二叉树遍历的重要方式，核心要点：

1. **迭代方式**：使用队列作为辅助数据结构，按层处理节点
2. **递归方式**：利用DFS的层级参数，将节点值添加到对应层级列表
3. **时间复杂度**：O(n)，每个节点访问一次
4. **空间复杂度**：O(n)，取决于树的结构

在实际应用中，迭代方式更为常用和安全，避免了递归栈溢出的风险。

