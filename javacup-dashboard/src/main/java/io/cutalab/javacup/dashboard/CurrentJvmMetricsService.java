package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.metrics.CurrentJvmMetrics;
import io.cutalab.javacup.core.metrics.MemoryUsageSnapshot;
import org.springframework.stereotype.Service;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;

@Service
public class CurrentJvmMetricsService {

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    public CurrentJvmMetrics readCurrentMetrics() {
        return new CurrentJvmMetrics(
                Instant.now(),
                toSnapshot(memoryMXBean.getHeapMemoryUsage()),
                toSnapshot(memoryMXBean.getNonHeapMemoryUsage()),
                threadMXBean.getThreadCount(),
                threadMXBean.getDaemonThreadCount(),
                threadMXBean.getPeakThreadCount(),
                threadMXBean.getTotalStartedThreadCount(),
                classLoadingMXBean.getLoadedClassCount(),
                classLoadingMXBean.getTotalLoadedClassCount(),
                classLoadingMXBean.getUnloadedClassCount(),
                runtimeMXBean.getUptime()
        );
    }

    private MemoryUsageSnapshot toSnapshot(MemoryUsage usage) {
        return new MemoryUsageSnapshot(
                usage.getUsed(),
                usage.getCommitted(),
                usage.getMax()
        );
    }
}
