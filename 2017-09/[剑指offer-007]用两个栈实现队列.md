# 用两个栈实现队列
## 题目描述
    用两个栈来实现一个队列，完成队列的Push和Pop操作。 队列中的元素为int类型。

```java
import java.util.Stack;

public class Solution {
    Stack<Integer> stack1 = new Stack<Integer>();
    Stack<Integer> stack2 = new Stack<Integer>();
    
    public void push(int node) {
        //write code here
    }
    
    public int pop() {
        //write code here
    }
}
```
[牛客网OJ链接](https://www.nowcoder.com/practice/54275ddae22f475981afa2244dd448c6?tpId=13&tqId=11158&tPage=1&rp=1&ru=%2Fta%2Fcoding-interviews&qru=%2Fta%2Fcoding-interviews%2Fquestion-ranking)

---
## 解题思路
题目的最终目的是要实现一个队列，但是只给了我们两个栈。我们都知道队列的特点是先进先出，而栈的特点是先进后出，两者在某种意义上来说达到的效果是相反的，队列可以看作是栈的倒置。想到这里，我们其实就可以很容易的将题意化解为如何将一个先进后出的栈倒置。

我们假设有x1,x2,x3三个元素需要先后入栈stack1，很明显在x2元素入栈前x1已经在栈中，我们要想达到倒置的目的就必须先把x1先pop出来等x2入栈后再将x1压回。x3元素同理。而结合题目，我们的思路是可行的，因为题目友好地为我们提供了两个栈，第二个栈stack2刚好可以用来存放我们需要临时pop出来的元素。

以上我们已经完成了一个栈的倒置，即完成了一个先进先出的队列。虽然这部分只完成了跟题意中需要我们完成的Push操作，但我们知道我们的stack1实际上已经是一个队列，相应的Pop操作只需要调用stack1本身的
pop()方法即可。

## 参考代码
```java
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
}
```

>说明：代码在牛客网的OJ是通过运行的，如果使用其它OJ可能需要更改。另外防止我手滑粘贴错导致不能运行的，原工程在2017-09/coding-interviews/stack2queue

(完 2017年9月3日)