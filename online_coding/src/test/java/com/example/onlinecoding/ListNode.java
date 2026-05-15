package com.example.onlinecoding;

/**
 * 链表节点类
 * 
 * 单链表节点的基本实现，包含值和指向下一个节点的引用。
 * 
 * @param <T> 节点值的类型
 */
public class ListNode<T> {
    
    private T val;
    private ListNode<T> next;
    
    /**
     * 构造函数：创建一个空节点
     */
    public ListNode() {
        this.val = null;
        this.next = null;
    }
    
    /**
     * 构造函数：创建一个带有值的节点
     * 
     * @param val 节点的值
     */
    public ListNode(T val) {
        this.val = val;
        this.next = null;
    }
    
    /**
     * 构造函数：创建一个带有值和下一个节点引用的节点
     * 
     * @param val  节点的值
     * @param next 下一个节点
     */
    public ListNode(T val, ListNode<T> next) {
        this.val = val;
        this.next = next;
    }
    
    /**
     * 获取节点的值
     * 
     * @return 节点的值
     */
    public T getVal() {
        return val;
    }
    
    /**
     * 设置节点的值
     * 
     * @param val 新的节点值
     */
    public void setVal(T val) {
        this.val = val;
    }
    
    /**
     * 获取下一个节点
     * 
     * @return 下一个节点引用
     */
    public ListNode<T> getNext() {
        return next;
    }
    
    /**
     * 设置下一个节点
     * 
     * @param next 新的下一个节点引用
     */
    public void setNext(ListNode<T> next) {
        this.next = next;
    }
    
    /**
     * 将链表转换为字符串表示
     * 
     * @return 链表的字符串表示，格式为 "1 -> 2 -> 3 -> null"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ListNode<T> current = this;
        
        while (current != null) {
            sb.append(current.val);
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }
        
        sb.append(" -> null");
        return sb.toString();
    }
    
    /**
     * 根据数组创建链表
     * 
     * @param values 数组值
     * @param <T>    值的类型
     * @return 链表的头节点
     */
    public static <T> ListNode<T> createList(T[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        
        ListNode<T> head = new ListNode<>(values[0]);
        ListNode<T> current = head;
        
        for (int i = 1; i < values.length; i++) {
            current.next = new ListNode<>(values[i]);
            current = current.next;
        }
        
        return head;
    }
    
    /**
     * 根据整数数组创建链表
     * 
     * @param values 整数数组
     * @return 链表的头节点
     */
    public static ListNode<Integer> createIntList(int[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        
        ListNode<Integer> head = new ListNode<>(values[0]);
        ListNode<Integer> current = head;
        
        for (int i = 1; i < values.length; i++) {
            current.next = new ListNode<>(values[i]);
            current = current.next;
        }
        
        return head;
    }
    
    /**
     * 打印链表
     * 
     * @param head 链表头节点
     */
    public static void printList(ListNode<?> head) {
        if (head == null) {
            System.out.println("null");
            return;
        }
        
        ListNode<?> current = head;
        while (current != null) {
            System.out.print(current.val);
            if (current.next != null) {
                System.out.print(" -> ");
            }
            current = current.next;
        }
        System.out.println(" -> null");
    }
    
    /**
     * 获取链表长度
     * 
     * @param head 链表头节点
     * @return 链表长度
     */
    public static int getLength(ListNode<?> head) {
        int length = 0;
        ListNode<?> current = head;
        
        while (current != null) {
            length++;
            current = current.next;
        }
        
        return length;
    }
    
    /**
     * 比较两个链表是否相等
     * 
     * @param head1 第一个链表头节点
     * @param head2 第二个链表头节点
     * @return 如果两个链表相等返回true，否则返回false
     */
    public static boolean equals(ListNode<?> head1, ListNode<?> head2) {
        ListNode<?> current1 = head1;
        ListNode<?> current2 = head2;
        
        while (current1 != null && current2 != null) {
            if (!current1.val.equals(current2.val)) {
                return false;
            }
            current1 = current1.next;
            current2 = current2.next;
        }
        
        return current1 == null && current2 == null;
    }
}
