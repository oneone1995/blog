# 二维数组中的查找
## 题目描述
在一个二维数组中，每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。

```java
public class Solution {
    public boolean Find(int target, int [][] array) {
        //write code here
    }
}
```
[牛客网OJ链接](https://www.nowcoder.com/practice/abc3fe2ce8e146608e868a70efebf62e?tpId=13&tqId=11154&tPage=1&rp=1&ru=/ta/coding-interviews&qru=/ta/coding-interviews/question-ranking)

---
## 解题思路
遍历的方法这里就不说了，两层for循环遍历查找即可。这里说另外一种方法，根据题目的意思我们可以知道任何一个数x左边的数都比x小，x下边的数都比x大。因此我们可以通过比较x和查找目标的大小来缩小查找范围，并且为了方便移动坐标我们将x的出发点设置在右上角，此时如果target比x小，target必定在x左边，即将x的横坐标向左走一步，如果target比x大，target必定在下边，即将x的纵坐标向下走一步。

按以上方法不停的缩小范围即能得到查找结果是否存在。唯一要考虑的只剩下循环的终止条件。这个终止条件很容易想出，因为我们是从右上角开始缩小范围(这里的缩小范围指的是x横坐标减小，纵坐标增大)，所以当x的横坐标小于0或者纵坐标大于二维数组的```length - 1```即结束查找。

---
## 参考代码
```java
public class Solution {
    public boolean Find(int target, int [][] array) {
        //先定位到右上角元素,这个位置的元素左边的都比它小，下边的都比它大
        int row = 0;
        int col = array[0].length - 1;

        while (row <= array.length - 1 && col >= 0) {
            int current = array[row][col];
            if (current == target) {
                return true;
            } else if (current > target) {
                //当前元素x比目标元素大，则目标元素在x左边
                col--;
            } else {
                //当前元素x比目标元素小，则目标元素在x下边
                row++;
            }
        }
        return false;
    }
}
```
>说明：代码在牛客网的OJ是通过运行的，如果使用其它OJ可能需要更改。另外防止我手滑粘贴错导致不能运行的，原工程在2017-10/coding-interviews/find-in-two-dimensional-array


(完 2017年10月1日)