package com.example.onlinecoding;

/**
 * 反转链表算法
 * 
 * 反转一个单链表，将链表的方向完全颠倒。
 * 
 * 示例：
 * 输入: 1 -> 2 -> 3 -> 4 -> 5 -> null
 * 输出: 5 -> 4 -> 3 -> 2 -> 1 -> null
 */
public class ReverseLinkedList {

    /**
     * 迭代方式反转链表
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     * 
     * 思路：使用三个指针遍历链表，依次改变节点的next指针方向
     * 
     * @param head 原链表的头节点
     * @return 反转后的链表头节点
     */
    public static <T> ListNode<T> reverseIterative(ListNode<T> head) {
        ListNode<T> prev = null;    // 前一个节点
        ListNode<T> current = head; // 当前节点
        ListNode<T> next = null;    // 下一个节点
        
        while (current != null) {
            // 保存下一个节点
            next = current.getNext();
            // 反转当前节点的指针
            current.setNext(prev);
            // 向前移动指针
            prev = current;
            current = next;
        }
        
        // prev 现在是新的头节点
        return prev;
    }

    /**
     * 递归方式反转链表
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(n) - 递归调用栈深度
     * 
     * 思路：递归到链表末尾，然后从后往前依次反转指针
     * 
     * @param head 原链表的头节点
     * @return 反转后的链表头节点
     */
    public static <T> ListNode<T> reverseRecursive(ListNode<T> head) {
        // 递归终止条件：空链表或单节点链表
        if (head == null || head.getNext() == null) {
            return head;
        }
        
        // 递归反转剩余部分
        ListNode<T> newHead = reverseRecursive(head.getNext());
        
        // 反转当前节点的指针
        head.getNext().setNext(head);
        head.setNext(null);
        
        return newHead;
    }

    /**
     * 头插法反转链表
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     * 
     * 思路：创建一个新的虚拟头节点，然后依次将原链表的每个节点插入到新链表的头部
     * 
     * @param head 原链表的头节点
     * @return 反转后的链表头节点
     */
    public static <T> ListNode<T> reverseHeadInsert(ListNode<T> head) {
        ListNode<T> newHead = null;  // 新链表的头节点
        
        while (head != null) {
            // 保存下一个节点
            ListNode<T> next = head.getNext();
            // 将当前节点插入到新链表头部
            head.setNext(newHead);
            newHead = head;
            // 移动到下一个节点
            head = next;
        }
        
        return newHead;
    }

    /**
     * 使用栈反转链表
     * 
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     * 
     * 思路：将所有节点压入栈，然后依次弹出重新连接
     * 
     * @param head 原链表的头节点
     * @return 反转后的链表头节点
     */
    public static <T> ListNode<T> reverseUsingStack(ListNode<T> head) {
        if (head == null || head.getNext() == null) {
            return head;
        }
        
        // 创建栈并压入所有节点
        java.util.Stack<ListNode<T>> stack = new java.util.Stack<>();
        ListNode<T> current = head;
        
        while (current != null) {
            stack.push(current);
            current = current.getNext();
        }
        
        // 弹出栈顶节点作为新的头节点
        ListNode<T> newHead = stack.pop();
        current = newHead;
        
        // 依次弹出节点并连接
        while (!stack.isEmpty()) {
            current.setNext(stack.pop());
            current = current.getNext();
        }
        
        // 最后一个节点的next设为null
        current.setNext(null);
        
        return newHead;
    }

    /**
     * 主方法：演示反转链表的各种方式
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 创建示例链表
        int[] values = {1, 2, 3, 4, 5};
        ListNode<Integer> head = ListNode.createIntList(values);
        
        System.out.println("原链表:");
        ListNode.printList(head);
        
        // 测试迭代方式
        ListNode<Integer> reversed1 = reverseIterative(head);
        System.out.println("\n迭代方式反转后:");
        ListNode.printList(reversed1);
        
        // 重新创建链表用于测试其他方法
        head = ListNode.createIntList(values);
        
        // 测试递归方式
        ListNode<Integer> reversed2 = reverseRecursive(head);
        System.out.println("\n递归方式反转后:");
        ListNode.printList(reversed2);
        
        // 重新创建链表
        head = ListNode.createIntList(values);
        
        // 测试头插法
        ListNode<Integer> reversed3 = reverseHeadInsert(head);
        System.out.println("\n头插法反转后:");
        ListNode.printList(reversed3);
        
        // 重新创建链表
        head = ListNode.createIntList(values);
        
        // 测试栈方式
        ListNode<Integer> reversed4 = reverseUsingStack(head);
        System.out.println("\n栈方式反转后:");
        ListNode.printList(reversed4);
        
        // 测试空链表和单节点链表
        System.out.println("\n\n测试边界情况:");
        
        // 空链表
        ListNode<Integer> emptyList = null;
        System.out.println("空链表反转:");
        ListNode.printList(reverseIterative(emptyList));
        
        // 单节点链表
        ListNode<Integer> singleNode = new ListNode<>(10);
        System.out.println("\n单节点链表反转:");
        ListNode.printList(reverseIterative(singleNode));
    }
}
