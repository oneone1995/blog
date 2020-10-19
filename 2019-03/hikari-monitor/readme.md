# hikari监控工具

## 引入

```groovy
compile 'com.dianwoda.open:connection-pool-monitor:1.1-SNAPSHOT'
compile 'com.zaxxer:HikariCP:3.2.0'
```

引入monitor-client包

>注意:如果你以前的项目已经引入了HikariCP的包，则需要将版本升级到3.2.0

## 使用

### 配置

- 使用spring原生配置方式

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.type=com.zaxxer.hikari.HikariDataSource
spring.datasource.url=jdbc:mysql://localhost:3306/learn?serverTimezone=GMT
spring.datasource.username=root
spring.datasource.password=E815C***m
```

- 主类上使用`@EnableHikariMonitor`注解

- 特别说明:

1. 默认情况下数据库连接池大小初始化为10，如果需要自定义,添加配置项`spring.datasource.hikari.maximum-pool-size=`,建议配置成20

2. 默认情况下连接池为fixed，即连接池最大连接和最小空闲连接一致，如果需要自定义，添加配置项`spring.datasource.hikari.max-pool-size`和`spring.datasource.hikari.min-idle`。建议使用默认值


### 上线后去Grafana配置监控项

各个监控项的key如下:

| key                      | 描述                                       |
|-------------------------------|--------------------------------------------|
| hikaricp.connections.min      |minIdle,最小空闲数量                       |
| hikaricp.connections          |连接池当前所有连接数                       |
| hikaricp.connections.idle     |当前空闲连接数量                           |
| hikaricp.connections.max      |maxPoolSize,连接池最大连接数               |
| hikaricp.connections.creation |创建一个新的连接所需的时间                 |
| hikaricp.connections.active   |活跃连接数                                 |
| hikaricp.connections.pending  |当前排队获取连接的线程数                   |
| hikaricp.connections.acquire  |获取连接时间                               |
| hikaricp.connections.usage    |一个事务执行耗时。即连接被租用到归还的耗时 |
| hikaricp.connections.timeout  |从池中获取连接超时的数量                   |

## 未完善的功能

- [] 多数据源的时候处理
- [] 和第三方框架整合时的功能，利如sharding-jdbc
- [] 一些细节