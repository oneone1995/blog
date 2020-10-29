package com.github.oneone1995.mybatis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class H2Test {

    /**
     * 以嵌入式(本地)连接方式连接H2数据库
     */
    private static final String JDBC_URL = "jdbc:h2:file:./testDB";
    private static final String DRIVER_CLASS = "org.h2.Driver";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) throws Exception {
        //与数据库建立连接
        Class.forName(DRIVER_CLASS);
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement statement = conn.createStatement();
        //创建表
        statement.execute("CREATE TABLE  if not exists `user`(id INT(50) PRIMARY KEY, name VARCHAR(50) NOT NULL, age int(50) NOT NULL)");

        //插入数据
        statement.executeUpdate("INSERT INTO USER VALUES(1, '程咬金', 20) ");
        statement.executeUpdate("INSERT INTO USER VALUES(2, '孙尚香', 21) ");
        statement.executeUpdate("INSERT INTO USER VALUES(3, '猴子', 22) ");

        //查询数据
        ResultSet resultSet = statement.executeQuery("select * from `user`");
        while (resultSet.next()) {
            System.out.println(resultSet.getInt("id") + ", " + resultSet.getString("name") + ", " + resultSet.getString("age"));
        }
        //关闭连接
        statement.close();
        conn.close();
    }
}
