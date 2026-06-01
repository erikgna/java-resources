package poc01;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PropertiesPoc {

    public static void main(String[] args) throws Exception {
        createAndRead();
        defaultsChain();
        systemProperties();
        nonStringTrap();
        concurrentWrites();
    }

    static void section(String title) {
        System.out.println("\n== " + title + " ==");
    }

    static void createAndRead() {
        section("create + read");
        Properties p = new Properties();
        p.setProperty("app.name", "MyApp");
        p.setProperty("app.version", "1.0");

        System.out.println("app.name              = " + p.getProperty("app.name"));
        System.out.println("missing key (null)    = " + p.getProperty("missing"));
        System.out.println("missing with default  = " + p.getProperty("db.host", "localhost"));
        System.out.println("setProperty returns old = " + p.setProperty("app.name", "NewApp"));
        System.out.println("size                  = " + p.size());
    }

    static void defaultsChain() {
        section("defaults chain");
        Properties base = new Properties();
        base.setProperty("color", "red");
        base.setProperty("shape", "square");

        Properties top = new Properties(base);
        top.setProperty("shape", "circle");

        System.out.println("color (from base)     = " + top.getProperty("color"));
        System.out.println("shape (overridden)    = " + top.getProperty("shape"));
        System.out.println("size ignores defaults = " + top.size());
        System.out.println("names include defaults= " + top.stringPropertyNames());
    }

    static void systemProperties() {
        section("system properties");
        System.out.println("java.version          = " + System.getProperty("java.version"));
        System.out.println("os.name               = " + System.getProperty("os.name"));

        System.setProperty("poc01.key", "hello");
        System.out.println("after set             = " + System.getProperty("poc01.key"));

        System.getProperties().put("poc01.live", "via live object");
        System.out.println("live object visible   = " + System.getProperty("poc01.live"));

        System.clearProperty("poc01.key");
        System.clearProperty("poc01.live");
        System.out.println("after clear (null)    = " + System.getProperty("poc01.key"));
    }

    static void nonStringTrap() {
        section("non-string trap");
        Properties p = new Properties();
        p.put("numeric", 42);
        System.out.println("getProperty (null)    = " + p.getProperty("numeric"));
        System.out.println("get (raw Integer)     = " + p.get("numeric"));

        try {
            p.put(null, "value");
        } catch (NullPointerException e) {
            System.out.println("null key throws NPE   = true");
        }
    }

    static void concurrentWrites() throws Exception {
        section("concurrent writes");
        Properties shared = new Properties();
        int threads = 10;
        int perThread = 100;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int i = 0; i < perThread; i++) {
                    shared.setProperty("t" + tid + "-k" + i, "v");
                }
                done.countDown();
            });
        }

        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        System.out.println("expected size         = " + (threads * perThread));
        System.out.println("actual size           = " + shared.size());
        System.out.println("no writes lost        = " + (shared.size() == threads * perThread));
    }
}
