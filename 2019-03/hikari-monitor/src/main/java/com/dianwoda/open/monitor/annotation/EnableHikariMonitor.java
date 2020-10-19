package com.dianwoda.open.monitor.annotation;

import com.dianwoda.open.monitor.config.HikariMonitorConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({HikariMonitorConfiguration.class})
public @interface EnableHikariMonitor {

}
