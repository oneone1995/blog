import java.util.ArrayList;

class ListNode {
    int val;
    ListNode next = null;

    ListNode(int val) {
        this.val = val;
    }
}

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

    //调试时使用,粘贴到各OJ时请注释
    public static void main(String[] args) {
        Solution solution = new Solution();
        ListNode listNode = new ListNode(1);
        listNode.next = new ListNode(2);
        listNode.next.next = new ListNode(3);
        listNode.next.next.next = new ListNode(4);
        listNode.next.next.next.next = new ListNode(5);

        System.out.println(solution.printListFromTailToHead(listNode));
    }
}