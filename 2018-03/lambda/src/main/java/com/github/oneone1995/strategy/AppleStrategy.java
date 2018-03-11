package com.github.oneone1995.strategy;

import com.github.oneone1995.domain.Apple;

/**
 * 封装了苹果属性判断条件的策略
 * 因为只有一个抽象方法，它同时也是一个函数式接口
 */
@FunctionalInterface
public interface AppleStrategy {
    boolean predicate(Apple apple);
}