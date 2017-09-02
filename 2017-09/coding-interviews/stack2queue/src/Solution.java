import java.util.Stack;

public class Solution {
    Stack<Integer> stack1 = new Stack<Integer>();
    Stack<Integer> stack2 = new Stack<Integer>();

    public void push(int node) {
        //压栈之前先将栈里的元素全部pop出来并push到另一个栈
        while (!stack1.empty()) {
            stack2.push(stack1.pop());
        }
        //压入新元素
        stack1.push(node);

        //将之前pop出去的元素再压回来
        while (!stack2.empty()) {
            stack1.push(stack2.pop());
        }
    }

    public int pop() {
        return stack1.pop();
    }

    /*
    //调试时使用,粘贴到各OJ时请注释
    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.push(1);
        solution.push(2);
        solution.pop();
        solution.push(3);
        solution.push(4);
        solution.pop();
    }
    */
}
