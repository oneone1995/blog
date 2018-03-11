package com.github.oneone1995;

import com.github.oneone1995.domain.Apple;
import com.github.oneone1995.strategy.AppleGreenPredicate;
import com.github.oneone1995.strategy.AppleStrategy;

import java.util.ArrayList;
import java.util.List;

public class FilteringApples {
    public static void main(String[] args) {
        //苹果列表
        List<Apple> inventory = Apple.inventory;

        //调用绿色苹果筛选器
        System.out.println("普通绿色苹果筛选器：");
        System.out.println(filterGreenApples(inventory));
        //调用重量大于150克的苹果筛选器
        System.out.println("普通重量苹果筛选器：");
        System.out.println(filterHeavyApples(inventory));
        //调用将颜色作为参数的苹果筛选器
        System.out.println("将颜色作为参数的苹果筛选器：");
        System.out.println(filterApplesByColor(inventory, "red"));
        //使用策略设计模式的苹果筛选器,这里具体传入绿色苹果筛选策略
        System.out.println("使用了策略模式的筛选器，传入绿色苹果筛选策略：");
        System.out.println(filterApples(inventory, new AppleGreenPredicate()));
        //使用匿名内部类代替具体的策略对象
        System.out.println("使用了策略模式的筛选器，传入匿名内部类：");
        System.out.println(filterApples(inventory, new AppleStrategy() {
            @Override
            public boolean predicate(Apple apple) {
                return "green".equals(apple.getColor());
            }
        }));
        System.out.println(filterApples(inventory, new AppleStrategy() {
            @Override
            public boolean predicate(Apple apple) {
                return apple.getWeight() > 150;
            }
        }));
        //使用Lambda表达式
        System.out.println("使用了Lambda表达式：");
        System.out.println(filterApples(inventory, apple -> apple.getWeight() > 150));
    }

    /**
     * 第一个需求，筛选出绿色的苹果
     * @param inventory 待筛选的苹果列表
     * @return 绿色苹果
     */
    private static List<Apple> filterGreenApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>(inventory.size());
        for (Apple apple : inventory) {
            if ("green".equals(apple.getColor())) {
                result.add(apple);
            }
        }
        return result;
    }

    /**
     * 第二个需求，筛选出重量大于150克的苹果
     * @param inventory 待筛选的苹果列表
     * @return 重量大于150克的苹果
     */
    private static List<Apple> filterHeavyApples(List<Apple> inventory) {
        List<Apple> result = new ArrayList<>(inventory.size());
        for (Apple apple : inventory) {
            if (apple.getWeight() > 150) {
                result.add(apple);
            }
        }
        return result;
    }

    /**
     * 将颜色作为参数的过滤器
     * @param inventory 待筛选的苹果列表
     * @param color 需要筛选什么颜色
     * @return 符合要求的苹果列表
     */
    private static List<Apple> filterApplesByColor(List<Apple> inventory, String color) {
        List<Apple> result = new ArrayList<>(inventory.size());
        for (Apple apple : inventory) {
            if (color.equals(apple.getColor())) {
                result.add(apple);
            }
        }
        return result;
    }

    /**
     * 使用策略设计模式改进后的筛选器，因为AppleStrategy为一个函数式接口，因此这里同样适用于传入Lambda表达式作为参数
     * @param inventory 待筛选的苹果列表
     * @param strategy 封装了具体筛选策略的策略对象，也可以是一个Lambda表达式
     * @return 符合筛选策略要求的苹果列表
     */
    private static List<Apple> filterApples(List<Apple> inventory, AppleStrategy strategy) {
        List<Apple> result = new ArrayList<>(inventory.size());
        for (Apple apple : inventory) {
            if (strategy.predicate(apple)) {
                result.add(apple);
            }
        }
        return result;
    }
}
