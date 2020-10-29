package com.github.oneone1995.mybatis.config;

import com.github.oneone1995.mybatis.mapper.UserMapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@org.springframework.context.annotation.Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        return JdbcConnectionPool.create("jdbc:h2:file:./testDB", "root", "root");
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        return factoryBean.getObject();
    }

    @Bean
    public UserMapper userMapperFactory() throws Exception {
        MapperFactoryBean<UserMapper> userMapperFactory = new MapperFactoryBean<>(UserMapper.class);
        userMapperFactory.setSqlSessionFactory(sqlSessionFactory());
        Configuration configuration = sqlSessionFactory().getConfiguration();
        configuration.addMapper(UserMapper.class);
        return userMapperFactory.getObject();
    }

    /*
    @Bean
    public XXXMapper xxxMapperFactory() throws Exception {
        MapperFactoryBean<XXXMapper> xxxMapperFactory = new MapperFactoryBean<>(XXXMapper.class);
        xxxMapperFactory.setSqlSessionFactory(sqlSessionFactory());
        Configuration configuration = sqlSessionFactory().getConfiguration();
        configuration.addMapper(XXXMapper.class);
        return xxxMapperFactory.getObject();
    }
     */
}
