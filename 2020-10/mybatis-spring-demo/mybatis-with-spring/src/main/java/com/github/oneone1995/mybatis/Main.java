package com.github.oneone1995.mybatis;

import com.github.oneone1995.mybatis.config.AppConfig;
import com.github.oneone1995.mybatis.mapper.UserMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        UserMapper userMapper = applicationContext.getBean(UserMapper.class);
        userMapper.findAll().forEach(System.out::println);
    }
}
