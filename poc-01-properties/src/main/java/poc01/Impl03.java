package poc01;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;

/**
 * Impl03 — LOAD from InputStream and Reader + iterate all keys.
 *
 * Concepts covered:
 *   - load(InputStream)      → loads from a byte stream (file on disk, classpath resource)
 *   - load(Reader)           → loads from a character stream (String, file with explicit encoding)
 *   - stringPropertyNames()  → returns all keys as a Set<String>
 *   - entrySet() iteration   → iterate all key-value pairs
 *
 * Key question answered here:
 *   "What happens if you call load() twice on the same Properties object?"
 *   → The second load MERGES into the first. It does NOT clear previous entries.
 *     If the same key appears in both, the second value wins.
 *
 * Reference allowed: only Impl01 for basic API usage. Read the Javadoc for load().
 */
public class Impl03 {

    public static void main(String[] args) throws IOException {

        // ── 1. LOAD from classpath InputStream ───────────────────────────────

        // getResourceAsStream() looks for the file inside src/main/resources/
        // at runtime. Maven copies that directory into the classpath.
        InputStream is = Impl03.class.getClassLoader().getResourceAsStream("config.properties");

        if (is == null) {
            // This happens if the file is not on the classpath. Run "mvn compile" first.
            System.out.println("ERROR: config.properties not found on classpath");
            return;
        }

        Properties props = new Properties();

        // load(InputStream) reads the file using ISO-8859-1 encoding.
        // Unicode escape sequences (backslash-u followed by 4 hex digits) in the file are decoded.
        props.load(is);
        is.close();  // Always close streams after use — they hold OS file handles.

        System.out.println("Loaded " + props.size() + " properties from config.properties");
        System.out.println("app.name = " + props.getProperty("app.name"));   // MyApp
        System.out.println("db.port  = " + props.getProperty("db.port"));    // 5432


        // ── 2. LOAD from StringReader (in-memory string) ─────────────────────

        // StringReader wraps a String so it can be used anywhere a Reader is expected.
        // This is useful for unit tests: no need for a real file on disk.
        String rawProperties = "user.name=Alice\nuser.age=30\napp.name=OverriddenApp";
        Properties extra = new Properties();
        extra.load(new StringReader(rawProperties));

        System.out.println("\nLoaded from StringReader:");
        System.out.println("user.name = " + extra.getProperty("user.name"));  // Alice
        System.out.println("user.age  = " + extra.getProperty("user.age"));   // 30


        // ── 3. LOAD TWICE → MERGE behavior ───────────────────────────────────

        // Load the classpath file again into the same "extra" object.
        // "app.name" already exists in extra as "OverriddenApp".
        // After the second load it will become "MyApp" (from config.properties).
        InputStream is2 = Impl03.class.getClassLoader().getResourceAsStream("config.properties");
        extra.load(is2);
        is2.close();

        // "user.name" was set in the first load — it STAYS because the second load doesn't clear.
        System.out.println("\nAfter second load (merge):");
        System.out.println("user.name = " + extra.getProperty("user.name"));  // Alice  (still there)
        System.out.println("app.name  = " + extra.getProperty("app.name"));   // MyApp  (overwritten)


        // ── 4. ITERATE with stringPropertyNames() ────────────────────────────

        // stringPropertyNames() returns a Set<String> of all keys.
        // It includes keys from the defaults chain (if one was set in the constructor).
        Set<String> keys = props.stringPropertyNames();
        System.out.println("\nAll keys from config.properties (" + keys.size() + " total):");
        for (String key : keys) {
            // Pad key to 30 chars for readable alignment.
            System.out.printf("  %-30s = %s%n", key, props.getProperty(key));
        }
    }
}
