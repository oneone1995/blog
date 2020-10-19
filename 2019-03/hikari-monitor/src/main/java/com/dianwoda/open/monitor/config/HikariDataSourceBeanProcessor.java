package com.dianwoda.open.monitor.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(HikariDataSource.class)
public class HikariDataSourceBeanProcessor implements BeanPostProcessor {
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource) {
            if (((HikariDataSource) bean).getMetricRegistry() == null) {
                ((HikariDataSource) bean).setMetricRegistry(meterRegistry);
            }
        }
        return bean;
    }
}
