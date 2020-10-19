package com.dianwoda.open.monitor.task;

import com.dianwoda.open.monitor.serivice.HikariMonitorService;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@ConditionalOnClass(HikariDataSource.class)
public class HikariMonitorTask {
    @Resource
    private HikariMonitorService hikariMonitorService;

    @Resource
    private HikariDataSource dataSource;

    @Scheduled(fixedRate = 1000)
    public void monitor() {
        hikariMonitorService.doMonitor((MeterRegistry) dataSource.getMetricRegistry());
    }

}
