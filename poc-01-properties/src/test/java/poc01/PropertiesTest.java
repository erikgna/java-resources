package poc01;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full test suite for java.util.Properties.
 *
 * Organized with JUnit 5 @Nested classes so each API area is grouped together.
 * Read these tests as executable documentation — each one proves a specific behavior.
 */
class PropertiesTest {

    // ── CREATE TESTS ─────────────────────────────────────────────────────────

    @Nested
    class CreateTests {

        @Test
        void testNewPropertiesIsEmpty() {
            // A freshly created Properties has no entries.
            Properties p = new Properties();
            assertTrue(p.isEmpty());
            assertEquals(0, p.size());
        }

        @Test
        void testSetPropertyReturnsPreviousValue() {
            // setProperty returns the OLD value, or null if the key is new.
            Properties p = new Properties();
            assertNull(p.setProperty("key", "first"));       // null: key was new
            assertEquals("first", p.setProperty("key", "second")); // "first": was the old value
        }

        @Test
        void testConstructorWithDefaultsChain() {
            // When Properties is constructed with another Properties as defaults,
            // getProperty() falls back to the defaults object if the key is not found locally.
            Properties defaults = new Properties();
            defaults.setProperty("fallback.key", "fallback-value");

            Properties p = new Properties(defaults);
            p.setProperty("own.key", "own-value");

            assertEquals("own-value",      p.getProperty("own.key"));      // found in p
            assertEquals("fallback-value", p.getProperty("fallback.key")); // found in defaults
            assertNull(p.getProperty("nonexistent"));                       // not found anywhere
        }

        @Test
        void testDefaultsNotCountedInSize() {
            // size() only counts entries in the top-level Properties — not defaults.
            Properties defaults = new Properties();
            defaults.setProperty("d1", "v1");
            defaults.setProperty("d2", "v2");

            Properties p = new Properties(defaults);
            p.setProperty("own", "val");

            // size() = 1, because only "own" lives in p directly.
            assertEquals(1, p.size());

            // But stringPropertyNames() includes defaults keys.
            assertEquals(3, p.stringPropertyNames().size());
        }
    }

    // ── READ TESTS ────────────────────────────────────────────────────────────

    @Nested
    class ReadTests {

        @Test
        void testGetPropertyReturnsValue() {
            // Basic happy path: key was set, value is returned.
            Properties p = new Properties();
            p.setProperty("name", "Alice");
            assertEquals("Alice", p.getProperty("name"));
        }

        @Test
        void testGetPropertyMissingKeyReturnsNull() {
            // Missing keys return null — no exception is thrown.
            Properties p = new Properties();
            assertNull(p.getProperty("does.not.exist"));
        }

        @Test
        void testGetPropertyWithDefaultReturnsFallback() {
            // Two-arg form returns the default value when key is absent.
            Properties p = new Properties();
            assertEquals("fallback", p.getProperty("missing", "fallback"));
        }

        @Test
        void testGetPropertyWithDefaultIgnoredWhenKeyPresent() {
            // The default is NOT used when the key exists.
            Properties p = new Properties();
            p.setProperty("key", "real");
            assertEquals("real", p.getProperty("key", "ignored-default"));
        }

        @Test
        void testStringPropertyNamesIncludesDefaults() {
            // stringPropertyNames() traverses the defaults chain.
            Properties base = new Properties();
            base.setProperty("base.key", "base.val");

            Properties top = new Properties(base);
            top.setProperty("top.key", "top.val");

            // Both "base.key" and "top.key" appear.
            assertTrue(top.stringPropertyNames().contains("base.key"));
            assertTrue(top.stringPropertyNames().contains("top.key"));
        }

        @Test
        void testGetPropertySkipsNonStringValues() {
            // put() allows non-String values (inherited from Hashtable<Object,Object>).
            // getProperty() checks "oval instanceof String" internally.
            // If the value is not a String, getProperty() returns null even though the key exists.
            Properties p = new Properties();
            p.put("numeric", 42);  // Integer, not String

            // getProperty returns null for non-String values — the silent trap.
            assertNull(p.getProperty("numeric"));

            // But get() (raw Hashtable method) returns the actual Integer.
            assertEquals(42, p.get("numeric"));
        }
    }

    // ── STORE TESTS ───────────────────────────────────────────────────────────

    @Nested
    class StoreTests {

        @Test
        void testStoreAndReloadRoundTrip() throws IOException {
            // Values must survive a store → reload cycle.
            Properties original = new Properties();
            original.setProperty("alpha", "one");
            original.setProperty("beta",  "two");

            // Store to bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            original.store(bos, "test");

            // Reload from the same bytes.
            Properties loaded = new Properties();
            loaded.load(new ByteArrayInputStream(bos.toByteArray()));

            assertEquals("one", loaded.getProperty("alpha"));
            assertEquals("two", loaded.getProperty("beta"));
        }

        @Test
        void testStoreXmlAndReloadRoundTrip() throws IOException {
            // XML round-trip must preserve all values.
            Properties original = new Properties();
            original.setProperty("xml.key", "xml-value");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            original.storeToXML(bos, "xml-test");

            Properties loaded = new Properties();
            loaded.loadFromXML(new ByteArrayInputStream(bos.toByteArray()));

            assertEquals("xml-value", loaded.getProperty("xml.key"));
        }

        @Test
        void testStoreOutputStreamEscapesNonAscii() throws IOException {
            // store(OutputStream) uses ISO-8859-1 internally.
            // Characters outside ISO-8859-1 (like Japanese) are escaped to \\uXXXX.
            Properties p = new Properties();
            p.setProperty("word", "こんにちは");  // Japanese — outside ISO-8859-1

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            p.store(bos, null);

            // Read the raw output bytes as ISO-8859-1 text.
            String raw = bos.toString("ISO-8859-1");

            // The Japanese characters must appear as \\uXXXX escape sequences.
            assertTrue(raw.contains("\\u"),
                "Expected \\uXXXX escapes for non-ASCII characters in OutputStream store");
            assertFalse(raw.contains("こ"),
                "Raw Japanese characters must NOT appear when storing via OutputStream");
        }

        @Test
        void testStoreWriterDoesNotEscapeUnicode() throws IOException {
            // store(Writer) uses the encoding of the Writer.
            // StringWriter is in-memory UTF-16, so no escaping occurs.
            Properties p = new Properties();
            p.setProperty("word", "こんにちは");

            StringWriter sw = new StringWriter();
            p.store(sw, null);

            // The Japanese characters MUST appear unescaped.
            assertTrue(sw.toString().contains("こんにちは"),
                "Japanese characters must appear as-is when storing via Writer");
        }

        @Test
        void testStoreKeysSorted() throws IOException {
            // NOTE: Key sorting in store() is JDK-version dependent.
            // JDK 26 sorts keys alphabetically; Java 11 does NOT sort them.
            // This test just verifies that ALL keys appear in the output.
            Properties p = new Properties();
            p.setProperty("zebra",  "z");
            p.setProperty("apple",  "a");
            p.setProperty("mango",  "m");

            StringWriter sw = new StringWriter();
            p.store(sw, null);
            String output = sw.toString();

            // All three keys must be present in the stored output.
            assertTrue(output.contains("zebra"), "zebra must be in stored output");
            assertTrue(output.contains("apple"), "apple must be in stored output");
            assertTrue(output.contains("mango"), "mango must be in stored output");
        }

        @Test
        void testDefaultsNotWrittenByStore() throws IOException {
            // store() only writes the top-level Properties — defaults are NOT included.
            Properties defaults = new Properties();
            defaults.setProperty("default.key", "default-value");

            Properties top = new Properties(defaults);
            top.setProperty("top.key", "top-value");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            top.store(bos, null);
            String output = bos.toString("ISO-8859-1");

            // "top.key" must appear in the output.
            assertTrue(output.contains("top.key"));

            // "default.key" must NOT appear — it lives only in defaults.
            assertFalse(output.contains("default.key"),
                "Defaults must not be written by store()");
        }
    }

    // ── SYSTEM PROPERTY TESTS ─────────────────────────────────────────────────

    @Nested
    class SystemTests {

        // Clean up any system properties we set during tests.
        // @AfterEach runs after EVERY test method in this class.
        @AfterEach
        void cleanUpTestProperties() {
            System.clearProperty("poc01.test.key");
        }

        @Test
        void testSystemGetPropertyJavaVersion() {
            // java.version is always set by the JVM at startup.
            assertNotNull(System.getProperty("java.version"),
                "java.version must always be present");
        }

        @Test
        void testSystemGetPropertyMissingReturnsNull() {
            assertNull(System.getProperty("this.does.not.exist.xyz"));
        }

        @Test
        void testSystemSetAndGetProperty() {
            // Setting a custom property makes it readable via getProperty.
            System.setProperty("poc01.test.key", "hello");
            assertEquals("hello", System.getProperty("poc01.test.key"));
        }

        @Test
        void testSystemClearProperty() {
            System.setProperty("poc01.test.key", "before-clear");
            System.clearProperty("poc01.test.key");
            assertNull(System.getProperty("poc01.test.key"),
                "Property must be null after clearProperty()");
        }

        @Test
        void testSystemGetPropertiesIsLiveObject() {
            // System.getProperties() returns the LIVE object — not a copy.
            // Mutating it directly affects System.getProperty().
            Properties live = System.getProperties();
            live.put("poc01.test.key", "set-via-put");

            // Verify the mutation is visible through the normal API.
            assertEquals("set-via-put", System.getProperty("poc01.test.key"),
                "Mutation of the live Properties object must be visible via System.getProperty()");

            // Restore.
            live.remove("poc01.test.key");
        }
    }

    // ── EDGE CASE TESTS ───────────────────────────────────────────────────────

    @Nested
    class EdgeCaseTests {

        @Test
        void testEmptyFile() throws IOException {
            // Loading from an empty InputStream results in an empty Properties.
            Properties p = new Properties();
            p.load(new StringReader(""));
            assertTrue(p.isEmpty());
        }

        @Test
        void testEmptyStringValue() {
            // setProperty("k", "") stores an empty string — NOT null.
            Properties p = new Properties();
            p.setProperty("k", "");
            assertNotNull(p.getProperty("k"));   // not null
            assertEquals("", p.getProperty("k")); // empty string
        }

        @Test
        void testLoadTwiceMerges() throws IOException {
            // Loading twice into the same Properties merges entries.
            // Duplicate keys from the second load overwrite those from the first.
            Properties p = new Properties();
            p.load(new StringReader("a=1\nb=2"));
            p.load(new StringReader("b=99\nc=3")); // "b" overwritten, "c" added

            assertEquals("1",  p.getProperty("a"));  // from first load
            assertEquals("99", p.getProperty("b"));  // overwritten by second load
            assertEquals("3",  p.getProperty("c"));  // from second load
            assertEquals(3, p.size());
        }

        @Test
        void testLineWithBackslashContinuation() throws IOException {
            // A \ at the end of a line continues the value on the next line.
            // Leading whitespace on the continuation line is stripped.
            Properties p = new Properties();
            p.load(new StringReader("key=one \\\n        two \\\n        three"));
            assertEquals("one two three", p.getProperty("key"));
        }

        @Test
        void testColonSeparatorParsed() throws IOException {
            // Key-value pairs can use : as the separator (same as =).
            Properties p = new Properties();
            p.load(new StringReader("key.colon: colon-value"));
            assertEquals("colon-value", p.getProperty("key.colon"));
        }

        @Test
        void testSpaceSeparatorParsed() throws IOException {
            // A space between key and value also works as a separator.
            Properties p = new Properties();
            p.load(new StringReader("key.space space-value"));
            assertEquals("space-value", p.getProperty("key.space"));
        }

        @Test
        void testCommentLinesSkipped() throws IOException {
            // Lines starting with # or ! are treated as comments and ignored.
            Properties p = new Properties();
            p.load(new StringReader(
                "# this is a comment\n" +
                "! this is also a comment\n" +
                "real.key=real-value"
            ));
            assertEquals(1, p.size());
            assertEquals("real-value", p.getProperty("real.key"));
        }

        @Test
        void testNullKeyThrowsNpe() {
            // ConcurrentHashMap (the internal store) does not permit null keys.
            Properties p = new Properties();
            assertThrows(NullPointerException.class, () -> p.put(null, "value"));
        }

        @Test
        void testNullValueThrowsNpe() {
            // ConcurrentHashMap does not permit null values either.
            Properties p = new Properties();
            assertThrows(NullPointerException.class, () -> p.put("key", null));
        }

        @Test
        void testUnicodeEscapeDecodedOnLoad() throws IOException {
            // Unicode escape sequences in a .properties file are decoded when loaded.
            Properties p = new Properties();
            p.load(new StringReader("cafe=Caf\\u00E9"));  // é = é
            assertEquals("Café", p.getProperty("cafe"));
        }
    }

    // ── THREAD SAFETY TESTS ───────────────────────────────────────────────────

    @Nested
    class ThreadSafetyTests {

        @Test
        void testConcurrentWritesProduceFinalSize() throws InterruptedException {
            // 10 threads each write 100 unique keys → expected final size = 1000.
            // setProperty() is synchronized so no writes are lost.
            Properties shared = new Properties();
            int threads = 10;
            int perThread = 100;

            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done  = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads);

            for (int t = 0; t < threads; t++) {
                final int tid = t;
                pool.submit(() -> {
                    try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    for (int i = 0; i < perThread; i++) {
                        shared.setProperty("t" + tid + "-k" + i, "v");
                    }
                    done.countDown();
                });
            }

            start.countDown();  // release all threads at once
            assertTrue(done.await(10, TimeUnit.SECONDS), "Threads must finish within 10 seconds");
            pool.shutdown();

            assertEquals(threads * perThread, shared.size(),
                "All writes must survive concurrent access — no entries lost");
        }

        @Test
        void testConcurrentReadWriteNoDeadlock() throws InterruptedException {
            // Mix of readers and writers must complete without deadlock.
            Properties shared = new Properties();
            for (int i = 0; i < 50; i++) {
                shared.setProperty("init-key-" + i, "init-value");
            }

            int threads = 20;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done  = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

            for (int t = 0; t < threads; t++) {
                final int tid = t;
                pool.submit(() -> {
                    try {
                        start.await();
                        if (tid % 2 == 0) {
                            // Even threads: writers
                            for (int i = 0; i < 50; i++) {
                                shared.setProperty("write-key-" + tid + "-" + i, "v");
                            }
                        } else {
                            // Odd threads: readers
                            for (int i = 0; i < 50; i++) {
                                shared.getProperty("init-key-" + (i % 50));
                            }
                        }
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            assertTrue(done.await(10, TimeUnit.SECONDS), "Must complete without deadlock");
            pool.shutdown();
            assertTrue(errors.isEmpty(), "No exceptions during concurrent read+write");
        }
    }
}
