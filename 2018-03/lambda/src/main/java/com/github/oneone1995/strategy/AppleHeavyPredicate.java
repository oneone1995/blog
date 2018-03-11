package com.github.oneone1995.strategy;

import com.github.oneone1995.domain.Apple;

public class AppleHeavyPredicate implements AppleStrategy {
    @Override
    public boolean predicate(Apple apple) {
        return apple.getWeight() > 150;
    }
}