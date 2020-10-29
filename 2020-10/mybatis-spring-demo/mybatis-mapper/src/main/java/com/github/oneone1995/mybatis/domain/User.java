package com.github.oneone1995.mybatis.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {
    private Long id;

    private String name;

    private Integer age;
}
