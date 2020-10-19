package com.dianwoda.open.monitor.config;

import com.dianwoba.monitor.client.MonitorUtil;
import com.dianwoba.monitor.client.MonitorUtilImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = {"com.dianwoda.open"})
@ConditionalOnClass(name = "com.dianwoba.monitor.client.MonitorUtil")
public class HikariMonitorConfiguration {

    @Bean
    public MonitorUtil monitorUtil() {
        return new MonitorUtilImpl();
    }
}
