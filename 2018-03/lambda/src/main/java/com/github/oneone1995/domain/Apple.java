package com.github.oneone1995.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Apple {
    private Integer weight;
    private String color;

    public static final List<Apple> inventory = Arrays.asList(new Apple(80, "green"),
            new Apple(155, "green"),
            new Apple(120, "red"));
}
