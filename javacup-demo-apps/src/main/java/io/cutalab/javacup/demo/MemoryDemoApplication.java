package io.cutalab.javacup.demo;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.security.SecureClassLoader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryDemoApplication {

    private static final List<byte[]> RETAINED_CHUNKS = new ArrayList<>();
    private static final List<ClassLoader> RETAINED_CLASS_LOADERS = new ArrayList<>();
    private static long generatedClassCounter = 0;

    public static void main(String[] args) throws Exception {
        DemoMode mode = DemoMode.fromArgs(args);

        System.out.println("Javacup demo memory application");
        System.out.println("PID: " + ProcessHandle.current().pid());
        System.out.println("Mode: " + mode);
        System.out.println("Started at: " + Instant.now());
        System.out.println();

        while (true) {
            if (mode == DemoMode.LEAK) {
                allocateLeakingChunk();
            } else if (mode == DemoMode.BURST) {
                allocateBurst();
            } else if (mode == DemoMode.METASPACE) {
                generateMetaspaceBatch();
            }

            printStatus(mode);
            Thread.sleep(2_000L);
        }
    }

    private static void allocateLeakingChunk() {
        RETAINED_CHUNKS.add(new byte[2 * 1024 * 1024]);
    }

    private static void allocateBurst() {
        List<byte[]> temporary = new ArrayList<>();

        for (int index = 0; index < 25; index++) {
            temporary.add(new byte[1024 * 1024]);
        }
    }

    private static void generateMetaspaceBatch() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            System.out.println("No system Java compiler available. Run this demo with a JDK, not a JRE.");
            return;
        }

        int classesPerBatch = 20;
        Map<String, byte[]> compiledClasses = new ConcurrentHashMap<>();
        MemoryFileManager fileManager = new MemoryFileManager(compiler, compiledClasses);

        List<JavaSourceFromString> sources = new ArrayList<>();

        for (int index = 0; index < classesPerBatch; index++) {
            long id = generatedClassCounter++;
            String className = "io.cutalab.javacup.demo.generated.GeneratedMetaspaceClass" + id;
            String source = """
                    package io.cutalab.javacup.demo.generated;

                    public class %s {
                        private final String value = "%s";

                        public String value() {
                            return value;
                        }
                    }
                    """.formatted("GeneratedMetaspaceClass" + id, "generated-" + id);

            sources.add(new JavaSourceFromString(className, source));
        }

        Boolean success = compiler.getTask(
                null,
                fileManager,
                null,
                null,
                null,
                sources
        ).call();

        if (!Boolean.TRUE.equals(success)) {
            System.out.println("Metaspace demo compilation batch failed.");
            return;
        }

        MemoryClassLoader classLoader = new MemoryClassLoader(compiledClasses);

        for (String className : compiledClasses.keySet()) {
            try {
                Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException exception) {
                System.out.println("Could not load generated class: " + className);
            }
        }

        RETAINED_CLASS_LOADERS.add(classLoader);
    }

    private static void printStatus(DemoMode mode) {
        Runtime runtime = Runtime.getRuntime();

        long heapUsedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long heapCommittedMb = runtime.totalMemory() / 1024 / 1024;
        long heapMaxMb = runtime.maxMemory() / 1024 / 1024;
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

        MetaspaceSnapshot metaspace = readMetaspace();

        System.out.printf(
                "[%s] mode=%s pid=%d heapUsed=%dMB heapCommitted=%dMB heapMax=%dMB metaspaceUsed=%dMB retainedChunks=%d retainedClassLoaders=%d generatedClasses=%d uptime=%dms%n",
                Instant.now(),
                mode,
                ProcessHandle.current().pid(),
                heapUsedMb,
                heapCommittedMb,
                heapMaxMb,
                metaspace.usedMb(),
                RETAINED_CHUNKS.size(),
                RETAINED_CLASS_LOADERS.size(),
                generatedClassCounter,
                uptimeMs
        );
    }

    private static MetaspaceSnapshot readMetaspace() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace".equals(pool.getName())) {
                MemoryUsage usage = pool.getUsage();
                return new MetaspaceSnapshot(usage.getUsed() / 1024 / 1024);
            }
        }

        return new MetaspaceSnapshot(-1);
    }

    private enum DemoMode {
        NORMAL,
        BURST,
        LEAK,
        METASPACE;

        private static DemoMode fromArgs(String[] args) {
            for (String arg : args) {
                if ("--mode=leak".equalsIgnoreCase(arg) || "leak".equalsIgnoreCase(arg)) {
                    return LEAK;
                }

                if ("--mode=burst".equalsIgnoreCase(arg) || "burst".equalsIgnoreCase(arg)) {
                    return BURST;
                }

                if ("--mode=metaspace".equalsIgnoreCase(arg) || "metaspace".equalsIgnoreCase(arg)) {
                    return METASPACE;
                }

                if ("--mode=normal".equalsIgnoreCase(arg) || "normal".equalsIgnoreCase(arg)) {
                    return NORMAL;
                }
            }

            return NORMAL;
        }
    }

    private record MetaspaceSnapshot(long usedMb) {
    }

    private static final class JavaSourceFromString extends SimpleJavaFileObject {

        private final String source;

        private JavaSourceFromString(String className, String source) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }

    private static final class MemoryFileManager extends javax.tools.ForwardingJavaFileManager<javax.tools.JavaFileManager> {

        private final Map<String, byte[]> compiledClasses;

        private MemoryFileManager(JavaCompiler compiler, Map<String, byte[]> compiledClasses) {
            super(compiler.getStandardFileManager(null, null, null));
            this.compiledClasses = compiledClasses;
        }

        @Override
        public javax.tools.JavaFileObject getJavaFileForOutput(
                Location location,
                String className,
                javax.tools.JavaFileObject.Kind kind,
                javax.tools.FileObject sibling
        ) {
            return new ByteArrayJavaClass(className, compiledClasses);
        }
    }

    private static final class ByteArrayJavaClass extends SimpleJavaFileObject {

        private final String className;
        private final Map<String, byte[]> compiledClasses;

        private ByteArrayJavaClass(String className, Map<String, byte[]> compiledClasses) {
            super(URI.create("bytes:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
            this.className = className;
            this.compiledClasses = compiledClasses;
        }

        @Override
        public OutputStream openOutputStream() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() {
                    compiledClasses.put(className, toByteArray());
                }
            };
        }
    }

    private static final class MemoryClassLoader extends SecureClassLoader {

        private final Map<String, byte[]> compiledClasses;

        private MemoryClassLoader(Map<String, byte[]> compiledClasses) {
            this.compiledClasses = Map.copyOf(compiledClasses);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = compiledClasses.get(name);

            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }

            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
