package com.example.onlinecoding;

/**
 * 二叉树节点定义
 * 
 * 二叉树是一种每个节点最多有两个子节点的树形数据结构，
 * 分别称为左子节点和右子节点。
 *
 * @param <T> 节点值的类型
 */
public class BinaryTreeNode<T> {

    /**
     * 节点存储的值
     */
    T value;

    /**
     * 左子节点引用
     */
    BinaryTreeNode<T> left;

    /**
     * 右子节点引用
     */
    BinaryTreeNode<T> right;

    /**
     * 构造函数：创建一个只有值的节点，左右子节点为空
     *
     * @param value 节点的值
     */
    public BinaryTreeNode(T value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }

    /**
     * 构造函数：创建一个完整的节点，包含左右子节点
     *
     * @param value 节点的值
     * @param left  左子节点
     * @param right 右子节点
     */
    public BinaryTreeNode(T value, BinaryTreeNode<T> left, BinaryTreeNode<T> right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    /**
     * 获取节点值
     *
     * @return 节点的值
     */
    public T getValue() {
        return value;
    }

    /**
     * 设置节点值
     *
     * @param value 新的节点值
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * 获取左子节点
     *
     * @return 左子节点引用
     */
    public BinaryTreeNode<T> getLeft() {
        return left;
    }

    /**
     * 设置左子节点
     *
     * @param left 左子节点引用
     */
    public void setLeft(BinaryTreeNode<T> left) {
        this.left = left;
    }

    /**
     * 获取右子节点
     *
     * @return 右子节点引用
     */
    public BinaryTreeNode<T> getRight() {
        return right;
    }

    /**
     * 设置右子节点
     *
     * @param right 右子节点引用
     */
    public void setRight(BinaryTreeNode<T> right) {
        this.right = right;
    }

    /**
     * 判断是否为叶子节点（没有子节点）
     *
     * @return 如果是叶子节点返回 true，否则返回 false
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * 返回节点的字符串表示
     *
     * @return 节点值的字符串形式
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
