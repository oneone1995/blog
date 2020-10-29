package com.github.oneone1995.mybatis.mapper;

import com.github.oneone1995.mybatis.domain.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {
    @Select("select * from user")
    List<User> findAll();
}
