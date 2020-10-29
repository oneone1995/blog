package mapperscan;

import com.github.oneone1995.mybatis.mapper.UserMapper;
import mapperscan.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MapperScanRunner {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        UserMapper userMapper = applicationContext.getBean(UserMapper.class);
        userMapper.findAll().forEach(System.out::println);
    }
}
