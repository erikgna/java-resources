package poc01;

import java.util.Properties;

/**
 * Impl01 — CREATE + READ basics.
 *
 * Concepts covered:
 *   - new Properties()
 *   - setProperty(key, value)
 *   - getProperty(key)                         → returns null if not found
 *   - getProperty(key, defaultValue)           → returns defaultValue if not found
 *   - size()                                   → how many entries
 *
 * Reference allowed for this file. Impl02 must be written from memory.
 *
 * Before coding: understand that Properties is a Map<String,String> in practice,
 * even though its class declaration is "Properties extends Hashtable<Object,Object>".
 * The Hashtable is bypassed internally — real storage is a ConcurrentHashMap.
 * You will verify this when you read the JDK source.
 */
public class Impl01 {

    public static void main(String[] args) {

        // ── 1. CREATE ────────────────────────────────────────────────────────

        // Creates an empty Properties object.
        // Under the hood this allocates a ConcurrentHashMap to store key-value pairs.
        Properties props = new Properties();

        // setProperty(key, value) stores a String key + String value.
        // Always use setProperty() instead of the inherited put() —
        // put() accepts Object keys/values which silently break the API later.
        props.setProperty("app.name", "MyApp");
        props.setProperty("app.version", "1.0");
        props.setProperty("app.debug", "false");

        // size() returns the number of entries stored directly in this Properties object.
        // (Entries in a "defaults" chain are NOT counted here.)
        System.out.println("Size after adding 3 entries: " + props.size());  // 3


        // ── 2. READ ──────────────────────────────────────────────────────────

        // getProperty(key) returns the String value for the key, or null if not found.
        // No exception is thrown for a missing key — you MUST check for null yourself.
        String name = props.getProperty("app.name");
        System.out.println("app.name    = " + name);           // MyApp

        String version = props.getProperty("app.version");
        System.out.println("app.version = " + version);        // 1.0

        // Reading a key that does NOT exist → returns null (not an exception).
        String missing = props.getProperty("nonexistent.key");
        System.out.println("missing key = " + missing);        // null

        // Two-argument form: returns the second argument (default) if the key is missing.
        // This is safer than a manual null check when you have a sensible fallback.
        String host = props.getProperty("db.host", "localhost");
        System.out.println("db.host (with default) = " + host);  // localhost

        // Two-argument form when the key IS present — default is ignored.
        String appName = props.getProperty("app.name", "FallbackName");
        System.out.println("app.name (key exists, default ignored) = " + appName);  // MyApp


        // ── 3. OBSERVE the toString output ──────────────────────────────────

        // Printing a Properties object directly calls toString() on the internal ConcurrentHashMap.
        // Output looks like: {app.name=MyApp, app.version=1.0, app.debug=false}
        // Note: order is NOT guaranteed (HashMap internals).
        System.out.println("props.toString() = " + props);
    }
}
