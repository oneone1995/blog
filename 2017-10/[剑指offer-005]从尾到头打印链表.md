# 从尾到头打印链表
## 题目描述
输入一个链表，从尾到头打印链表每个节点的值。

```java
/**
*    public class ListNode {
*        int val;
*        ListNode next = null;
*
*        ListNode(int val) {
*            this.val = val;
*        }
*    }
*
*/
import java.util.ArrayList;
public class Solution {
    public ArrayList<Integer> printListFromTailToHead(ListNode listNode) {
        // write code here
    }
}
```
[牛客网OJ链接](https://www.nowcoder.com/practice/d0267f7f55b3412ba93bd35cfa8e8035?tpId=13&tqId=11156&tPage=1&rp=1&ru=/ta/coding-interviews&qru=/ta/coding-interviews/question-ranking)

---
## 解题思路
简单的递归可以解决这个题目。用一个全局的```ArrayList```来保存最后输出的结果，我们只要将往```ArrayList```中添加元素的操作放在递归操作的后面即可，这样就先可以通过递归走到链表的最后一个节点，碰到空节点后递归开始返回，返回过程中往list中丢数据。这样list中保存的结果便是链表元素的逆序。

---
## 参考代码
```java
public class Solution {

    ArrayList<Integer> arrayList = new ArrayList<>();

    public ArrayList<Integer> printListFromTailToHead(ListNode listNode) {
        if (listNode == null) {
            //这里不返回null是牛客网OJ要求链表为空时输出空的list而不是null
            //因为我们的arrayList是全局的，因此在方法返回值没有要求的情况下这里只要能告诉递归可以开始返回就行。
            return arrayList;
        }
        printListFromTailToHead(listNode.next);
        //添加元素的代码会一直等待递归开始返回后才执行
        arrayList.add(listNode.val);

        return arrayList;
    }

}
```
>说明：代码在牛客网的OJ是通过运行的，如果使用其它OJ可能需要更改。另外防止我手滑粘贴错导致不能运行的，原工程在2017-10/coding-interviews/print-list-from-tail-to-head


(完 2017年10月2日)