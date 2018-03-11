package com.github.oneone1995;

import com.github.oneone1995.domain.Apple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Java8自带的函数式接口演示
 */
public class FunctionInterfaceDemo {
    public static void main(String[] args) {
        //1. 通过Java8自带函数式接口Predicate<T>实现苹果筛选
        System.out.println("通过Java8自带函数式接口Predicate<T>实现苹果筛选:");
        List<Apple> inventory = Apple.inventory;
        System.out.println(filterApples(inventory, apple -> apple.getWeight() > 100));

        //2. 使用Consumer接口打印一个List中的所有元素
        System.out.println("使用Consumer接口打印一个List中的所有元素");
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5);
        forEach(integerList, integer -> System.out.println(integer));

        //3. 使用Function接口映射一个列表元素中的某些属性到另一个列表
        System.out.println("使用Function接口映射一个String列表的长度到另一个列表:");
        List<String> stringList = Arrays.asList("lambda", "in", "action");
        List<Integer> mapResult = map(stringList, s -> s.length());
        System.out.println(mapResult);
    }

    /**
     * 使用更为通用的Java8自带函数式接口Predicate<T>来作为筛选器参数
     * @param inventory 待筛选的苹果列表
     * @param predicate Predicate<T>接口实例，可以是一个Lambda表达式
     * @return 符合筛选要求的苹果列表
     */
    private static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> predicate) {
        List<Apple> result = new ArrayList<>(inventory.size());
        for (Apple apple : inventory) {
            if (predicate.test(apple)) {
                result.add(apple);
            }
        }
        return result;
    }

    /**
     * 使用Consumer接口打印一个List中的所有元素
     * @param list 需要操作的list
     * @param consumer 对list中的元素做特定操作的函数式接口对象实例，可以是一个Lambda表达式
     * @param <T> list中元素的类型泛型
     */
    private static <T> void forEach(List<T> list, Consumer<T> consumer) {
        for (T t : list) {
            consumer.accept(t);
        }
    }

    /**
     * 使用Function接口映射一个列表元素中的某些属性到另一个列表
     * @param list 需要操作得list
     * @param function 对list中的元素进行转换的函数式接口对象实例，可以是一个Lambda表达式
     * @param <T> function接收的对象
     * @param <R> function转换后的对象
     * @return 存储了元素经过function转换后的list
     */
    private static <T, R> List<R> map(List<T> list, Function<T, R> function) {
        List<R> result = new ArrayList<>(list.size());

        for (T t : list) {
            result.add(function.apply(t));
        }
        return result;
    }
}
