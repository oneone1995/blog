package mapperscan.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.github.oneone1995.mybatis.mapper")
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
}
