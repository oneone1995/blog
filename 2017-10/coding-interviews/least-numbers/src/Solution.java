import java.util.ArrayList;
import java.util.Arrays;

public class Solution {
    public ArrayList<Integer> GetLeastNumbers_Solution(int[] input, int k) {
        ArrayList<Integer> result = new ArrayList<>();
        //建堆
        int n = input.length;
        
        //所求的前k个数大于数组长度直接返回空集合
        if (k > n) {
            return result;
        }

        //将0 ~ n / 2 - 1位置上的元素按下沉方式建堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            sink(input, i, n);
        }

        //调整k次堆，分别将排序前k个数从堆中移除并加到容器ArrayList中
        for (int i = 0; i < k; i++) {
            swap(input, 0, --n);
            sink(input, 0, n);
            result.add(input[n]);
        }
        return result;
    }

    /**
     * 下沉方式建堆
     * @param a 堆对应的数组
     * @param k 数组中第k个元素
     * @param n 堆大小
     */
    private static void sink(int[] a, int k, int n) {
        //当前节点的左子节点 >= 堆大小时候说明当前节点已经下沉到堆底了
        while (2 * k + 1 < n) {
            //记录左子节点
            int j = 2 * k + 1;

            //左子节点和右子节点比较大小，如果右子更小那么需要与右子节点交换来完成下沉
            //这里先判断左子节点是不是堆中最后一个元素了，即防止当前节点只有左子节点而造成后面判断数组越界
            if (j != n - 1 && a[j] > a[j + 1]) {
                j++;
            }
            //如果当前节点已经比自己的子节点小了，那么直接跳出循环，当前节点已经符合堆有序的定义
            if (a[k] < a[j]) {
                break;
            }
            //否则和子节点中更小的那个交换
            swap(a, k, j);
            //更改需要下沉的节点的位置，即更新为子节点的位置
            k = j;
        }
    }

    /**
     * 用于交换数组中两个数的辅助方法
     * @param a 要交换的数所在的数组
     * @param i 第一个数在数组中的索引
     * @param j 第二个数在数组中的索引
     */
    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    //调试时使用,粘贴到各OJ时请注释
    public static void main(String[] args) {
        Solution solution = new Solution();
        int[] a = new int[]{4, 5, 1, 6, 2, 7, 3, 8};
        System.out.println(solution.GetLeastNumbers_Solution(a, 8));
        Arrays.stream(a).forEach(System.out::println);
    }
}