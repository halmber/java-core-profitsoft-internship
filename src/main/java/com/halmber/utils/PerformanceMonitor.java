package com.halmber.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

/**
 * Utility class for monitoring application performance metrics.
 * Provides detailed statistics about execution time, memory usage, and thread activity.
 */
public class PerformanceMonitor {
    private final long startTime;
    private final long startMemory;
    private final long startCpuTime;
    private final int threadCount;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;

    public PerformanceMonitor(int threadCount) {
        this.threadCount = threadCount;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();

        // Capture start metrics
        this.startTime = System.nanoTime();
        this.startMemory = getUsedMemory();
        this.startCpuTime = threadBean.getCurrentThreadCpuTime();
    }

    private long getUsedMemory() {
        return memoryBean.getHeapMemoryUsage().getUsed() +
                memoryBean.getNonHeapMemoryUsage().getUsed();
    }

    public void printReport() {
        long endTime = System.nanoTime();
        long endMemory = getUsedMemory();
        long endCpuTime = threadBean.getCurrentThreadCpuTime();

        // Calculate metrics
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        double durationMillis = (endTime - startTime) / 1_000_000.0;
        long memoryUsed = Math.max(0, endMemory - startMemory);
        double memoryMB = memoryUsed / (1024.0 * 1024.0);
        long cpuTimeUsed = endCpuTime - startCpuTime;
        double cpuSeconds = cpuTimeUsed / 1_000_000_000.0;

        // Print report
        printSeparator();
        System.out.println("                 PERFORMANCE REPORT");
        printSeparator();

        System.out.println("\n📊 EXECUTION METRICS:");
        System.out.printf("   Total execution time:     %.3f seconds (%.2f ms)%n",
                durationSeconds, durationMillis);
        System.out.printf("   Memory used:              %.2f MB%n", memoryMB);
        System.out.printf("   CPU time used:            %.3f seconds%n", cpuSeconds);
        System.out.printf("   Thread pool size:         %d threads%n", threadCount);

        if (threadCount > 0) {
            System.out.printf("   Avg time per thread:      %.2f ms%n",
                    durationMillis / threadCount);
        }

        printSeparator();
    }

    private void printSeparator() {
        System.out.println("=".repeat(70));
    }
}
