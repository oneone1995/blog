package com.github.oneone1995.mybatis;

import com.github.oneone1995.mybatis.mapper.UserMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.DataSource;

public class WithoutSpringRunner {

    public static void main(String[] args) {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:file:./testDB", "root", "root");

        TransactionFactory transactionFactory =
                new JdbcTransactionFactory();
        Environment environment =
                new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(UserMapper.class);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            userMapper.findAll().forEach(System.out::println);
        }
    }
}
