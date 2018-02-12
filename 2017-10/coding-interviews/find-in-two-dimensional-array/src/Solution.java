public class Solution {
    public boolean Find(int target, int[][] array) {
        //先定位到右上角元素,这个位置的元素左边的都比它小，下边的都比它大
        int row = 0; //横坐标
        int col = array[0].length - 1; //纵坐标

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

    //遍历的方法
    @Deprecated
    public boolean findByTraverse(int target, int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] == target) {
                    return true;
                }
            }
        }
        return false;
    }


    //调试时使用,粘贴到各OJ时请注释
    public static void main(String[] args) {
        Solution solution = new Solution();
        int[][] array = new int[][]{{1, 2, 8, 9}, {2, 4, 9, 12}, {4, 7, 10, 13}, {6, 8, 11, 15}};
        System.out.println(solution.Find(8, array));
        System.out.println(solution.findByTraverse(4, array));
    }
}