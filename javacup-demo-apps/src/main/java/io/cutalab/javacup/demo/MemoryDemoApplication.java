package io.cutalab.javacup.demo;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MemoryDemoApplication {

    private static final List<byte[]> LEAK_BUCKET = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        DemoMode mode = DemoMode.fromArgs(args);

        long pid = ProcessHandle.current().pid();

        System.out.println("Javacup memory demo started");
        System.out.println("PID: " + pid);
        System.out.println("Mode: " + mode);
        System.out.println("Command: " + System.getProperty("sun.java.command", "(unknown)"));
        System.out.println("Use Ctrl+C to stop.");

        while (true) {
            if (mode == DemoMode.LEAK) {
                allocateLeakChunk();
            } else if (mode == DemoMode.BURST) {
                allocateTemporaryBurst();
            }

            printStatus(mode);
            Thread.sleep(2_000);
        }
    }

    private static void allocateLeakChunk() {
        LEAK_BUCKET.add(new byte[1024 * 1024]);
    }

    private static void allocateTemporaryBurst() {
        List<byte[]> temporary = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            temporary.add(new byte[1024 * 1024]);
        }
    }

    private static void printStatus(DemoMode mode) {
        Runtime runtime = Runtime.getRuntime();

        long usedMb = toMb(runtime.totalMemory() - runtime.freeMemory());
        long committedMb = toMb(runtime.totalMemory());
        long maxMb = toMb(runtime.maxMemory());

        System.out.printf(
                "[%s] mode=%s pid=%d heapUsed=%dMB heapCommitted=%dMB heapMax=%dMB retainedChunks=%d uptime=%dms%n",
                LocalDateTime.now(),
                mode,
                ProcessHandle.current().pid(),
                usedMb,
                committedMb,
                maxMb,
                LEAK_BUCKET.size(),
                ManagementFactory.getRuntimeMXBean().getUptime()
        );
    }

    private static long toMb(long bytes) {
        return bytes / 1024 / 1024;
    }

    private enum DemoMode {
        NORMAL,
        BURST,
        LEAK;

        static DemoMode fromArgs(String[] args) {
            for (String arg : args) {
                if ("--mode=leak".equalsIgnoreCase(arg) || "leak".equalsIgnoreCase(arg)) {
                    return LEAK;
                }

                if ("--mode=burst".equalsIgnoreCase(arg) || "burst".equalsIgnoreCase(arg)) {
                    return BURST;
                }

                if ("--mode=normal".equalsIgnoreCase(arg) || "normal".equalsIgnoreCase(arg)) {
                    return NORMAL;
                }
            }

            return NORMAL;
        }
    }
}
