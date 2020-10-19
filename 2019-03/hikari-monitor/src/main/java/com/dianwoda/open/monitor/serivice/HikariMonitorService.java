package com.dianwoda.open.monitor.serivice;

import com.dianwoba.monitor.client.MonitorPoint;
import com.dianwoba.monitor.client.MonitorUtil;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class HikariMonitorService {
    private String ip;
    private String applicationName;

    @Resource
    private MonitorUtil monitorUtil;

    public void doMonitor(MeterRegistry meterRegistry) {
        for (Meter m : meterRegistry.getMeters()) {
            if (m instanceof Timer) {
                writeTimer((Timer) m);
            }
            if (m instanceof DistributionSummary) {
                writeSummary((DistributionSummary) m);
            }
            if (m instanceof FunctionTimer) {
                writeTimer((FunctionTimer) m);
            }
            if (m instanceof TimeGauge) {
                writeGauge(m.getId(), ((TimeGauge) m).value(getBaseTimeUnit()));
            }
            if (m instanceof Gauge) {
                writeGauge(m.getId(), ((Gauge) m).value());
            }
            if (m instanceof FunctionCounter) {
                writeCounter(m.getId(), ((FunctionCounter) m).count());
            }
            if (m instanceof Counter) {
                writeCounter(m.getId(), ((Counter) m).count());
            }
            if (m instanceof LongTaskTimer) {
                writeLongTaskTimer((LongTaskTimer) m);
            }
        }
    }


    private void writeLongTaskTimer(LongTaskTimer timer) {
        Meter.Id id = timer.getId();
        MonitorPoint point = MonitorPoint.monitorKey(id.getName())
                .addTag(tags2Map(id.getTags()))
                .addTag("application", applicationName)
                .addTag("host", ip)
                .addField("active_tasks", timer.activeTasks())
                .addField("duration", timer.duration(getBaseTimeUnit()))
                .build();
        monitorUtil.writePoint(point);
    }

    private void writeCounter(Meter.Id id, double count) {
        MonitorPoint point = MonitorPoint.monitorKey(id.getName())
                .addTag(tags2Map(id.getTags()))
                .addTag("application", applicationName)
                .addTag("host", ip)
                .addField("value", count)
                .build();
        monitorUtil.writePoint(point);
    }

    private void writeGauge(Meter.Id id, Double value) {
        MonitorPoint point = MonitorPoint.monitorKey(id.getName())
                .addTag(tags2Map(id.getTags()))
                .addTag("application", applicationName)
                .addTag("host", ip)
                .addField("value", value)
                .build();
        monitorUtil.writePoint(point);
    }

    private void writeTimer(FunctionTimer timer) {
        Meter.Id id = timer.getId();
        MonitorPoint point = MonitorPoint.monitorKey(id.getName())
                .addTag(tags2Map(id.getTags()))
                .addTag("application", applicationName)
                .addTag("host", ip)
                .addField("sum", timer.totalTime(getBaseTimeUnit()))
                .addField("count", timer.count())
                .addField("mean", timer.mean(getBaseTimeUnit()))
                .build();
        monitorUtil.writePoint(point);
    }

    private void writeTimer(Timer timer) {
        Meter.Id id = timer.getId();
        MonitorPoint point = MonitorPoint.monitorKey(id.getName())
                .addTag(tags2Map(id.getTags()))
                .addTag("application", applicationName)
                .addTag("host", ip)
                .addField("sum", timer.totalTime(getBaseTimeUnit()))
                .addField("count", timer.count())
                .addField("mean", timer.mean(getBaseTimeUnit()))
                .addField("upper", timer.max(getBaseTimeUnit()))
                .addField("p99", timer.percentile(0.99, getBaseTimeUnit()))
                .addField("p95", timer.percentile(0.95, getBaseTimeUnit()))
                .addField("p90", timer.percentile(0.90, getBaseTimeUnit()))
                .build();
        monitorUtil.writePoint(point);
    }

    private void writeSummary(DistributionSummary summary) {
        Meter.Id id = summary.getId();
        MonitorPoint point = MonitorPoint.monitorKey(id.getName())
                .addTag(tags2Map(id.getTags()))
                .addTag("application", applicationName)
                .addTag("host", ip)
                .addField("sum", summary.totalAmount())
                .addField("count", summary.count())
                .addField("mean", summary.mean())
                .addField("max", summary.max())
                .build();
        monitorUtil.writePoint(point);
    }

    private TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    /**
     * 获取本地IP地址
     */
    private String getLocalAddr() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress().toString(); //获取本机ip
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    /**
     * 将Tags转变为Map
     */
    private Map<String, String> tags2Map(List<Tag> tags) {
        Map<String, String> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }


    @PostConstruct
    public void init() {
        this.ip = getLocalAddr();
        this.applicationName = System.getProperty("project.name", "unknown");
    }
}
