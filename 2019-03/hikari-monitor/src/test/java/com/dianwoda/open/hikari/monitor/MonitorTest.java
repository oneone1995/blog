package com.dianwoda.open.hikari.monitor;

import com.dianwoda.open.monitor.annotation.EnableHikariMonitor;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationConfiguration.class)
public class MonitorTest {

    @Resource
    private HikariDataSource dataSource;

    @Test
    public void test() throws SQLException {
        System.out.println(dataSource.getMetricRegistry());
        String sql = "select * from `user` where id = 1";
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(String.format("id=%s,name=%s,sex=%s", resultSet.getLong("id"), resultSet.getString("name"),  resultSet.getString("sex")));
        }
    }
}

@EnableHikariMonitor
@SpringBootApplication
class TestApplicationConfiguration {
}
