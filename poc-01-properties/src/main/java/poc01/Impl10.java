package poc01;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Impl10 — CLEANEST COMBINED VERSION + THREAD SAFETY TEST.
 *
 * Rules: write this WITHOUT looking at any previous Impl file.
 * This is the final exam: if you can write this from memory, you own the API.
 *
 * This file combines:
 *   1. Load from classpath + defaults chain
 *   2. Store as .properties (UTF-8) and XML, then reload
 *   3. System properties profile
 *   4. Concurrent write test — prove Properties is thread-safe without external locks
 *
 * Thread safety explained:
 *   Properties is backed by a ConcurrentHashMap.
 *   setProperty() is synchronized on the Properties instance (source: line ~230).
 *   Reads via getProperty() are NOT synchronized but are safe because ConcurrentHashMap
 *   provides lock-free reads.
 *   Conclusion: concurrent setProperty() calls from multiple threads are safe,
 *   but you must not rely on order or atomicity of multi-step operations (read-then-write).
 */
public class Impl10 {

    public static void main(String[] args) throws IOException, InterruptedException {

        // ── 1. LOAD + DEFAULTS CHAIN ──────────────────────────────────────────

        // Base defaults — fallback values used if a key is not in the main config.
        Properties defaults = new Properties();
        defaults.setProperty("db.host",     "localhost");
        defaults.setProperty("db.port",     "5432");
        defaults.setProperty("log.level",   "INFO");

        // Main config — loaded from classpath, uses defaults as fallback.
        Properties config = new Properties(defaults);
        InputStream is = Impl10.class.getClassLoader().getResourceAsStream("config.properties");
        if (is != null) {
            config.load(is);
            is.close();
        }

        System.out.println("── Config with defaults ──");
        // db.host is in config.properties → overrides default.
        System.out.println("db.host    = " + config.getProperty("db.host"));
        // log.level is NOT in config.properties → falls back to default "INFO".
        System.out.println("log.level  = " + config.getProperty("log.level"));
        System.out.println("app.name   = " + config.getProperty("app.name"));
        System.out.println("total keys = " + config.stringPropertyNames().size());


        // ── 2. STORE AS UTF-8 .PROPERTIES + RELOAD ───────────────────────────

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        config.store(writer, "Impl10 — UTF-8 store");
        writer.close();

        Properties reloadedFromText = new Properties();
        // Load using a Reader so UTF-8 encoding is honoured (not ISO-8859-1 from InputStream).
        reloadedFromText.load(new java.io.InputStreamReader(
            new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8));

        System.out.println("\n── After .properties round-trip ──");
        System.out.println("app.name = " + reloadedFromText.getProperty("app.name"));
        System.out.println("db.port  = " + reloadedFromText.getProperty("db.port"));


        // ── 3. STORE AS XML + RELOAD ──────────────────────────────────────────

        ByteArrayOutputStream xmlBos = new ByteArrayOutputStream();
        config.storeToXML(xmlBos, "Impl10 — XML store");

        Properties reloadedFromXml = new Properties();
        reloadedFromXml.loadFromXML(new ByteArrayInputStream(xmlBos.toByteArray()));

        System.out.println("\n── After XML round-trip ──");
        System.out.println("app.name = " + reloadedFromXml.getProperty("app.name"));
        System.out.println("db.host  = " + reloadedFromXml.getProperty("db.host"));


        // ── 4. SYSTEM PROPERTIES PROFILE ─────────────────────────────────────

        System.out.println("\n── System profile ──");
        printSystemProfile();


        // ── 5. CONCURRENT WRITE TEST ──────────────────────────────────────────

        // Shared Properties object — accessed from multiple threads simultaneously.
        Properties shared = new Properties();
        int threadCount = 10;
        int entriesPerThread = 100;

        // CountDownLatch makes all threads start at exactly the same time.
        // This maximises thread contention and makes the test meaningful.
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    startLatch.await();  // wait for the signal to start together
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                // Each thread writes 100 unique keys so there are no key collisions.
                for (int i = 0; i < entriesPerThread; i++) {
                    String key   = "thread-" + threadId + "-key-" + i;
                    String value = "value-" + threadId + "-" + i;
                    shared.setProperty(key, value);  // synchronized — safe from multiple threads
                }
                doneLatch.countDown();
            });
        }

        // Fire all threads simultaneously.
        startLatch.countDown();

        // Wait for all threads to finish (5 second timeout).
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        int expectedSize = threadCount * entriesPerThread;  // 1000
        System.out.println("\n── Concurrent write test ──");
        System.out.println("All threads finished: " + finished);
        System.out.println("Expected size: " + expectedSize);
        System.out.println("Actual size:   " + shared.size());
        System.out.println("Test PASSED:   " + (shared.size() == expectedSize));
    }

    // Prints a formatted summary of key JVM/OS facts from system properties.
    private static void printSystemProfile() {
        String[][] profile = {
            {"Java version",   System.getProperty("java.version")},
            {"Java vendor",    System.getProperty("java.vendor")},
            {"OS name",        System.getProperty("os.name")},
            {"OS arch",        System.getProperty("os.arch")},
            {"OS version",     System.getProperty("os.version")},
            {"User name",      System.getProperty("user.name")},
            {"User home",      System.getProperty("user.home")},
            {"Working dir",    System.getProperty("user.dir")},
            {"File separator", System.getProperty("file.separator")},
        };
        for (String[] row : profile) {
            System.out.printf("  %-20s = %s%n", row[0], row[1]);
        }

        // Print total number of system properties (interesting to count).
        List<String> keys = new ArrayList<>(System.getProperties().stringPropertyNames());
        Collections.sort(keys);
        System.out.println("  Total sys props    = " + keys.size());
    }
}
