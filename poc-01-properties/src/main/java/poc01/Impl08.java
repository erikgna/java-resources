package poc01;

import java.util.Properties;

/**
 * Impl08 — SYSTEM PROPERTIES: write and clear.
 *
 * Rules: write this WITHOUT looking at Impl07.
 *
 * Concepts covered:
 *   - System.setProperty(key, value)   → adds or replaces a system property
 *   - System.clearProperty(key)        → removes a system property
 *   - System.getProperties() is LIVE   → mutating the returned object affects System.getProperty()
 *
 * KEY INSIGHT — System.getProperties() is NOT a copy:
 *   The object returned by System.getProperties() is the same object the JVM uses internally.
 *   If you call getProperties().put("key", "value"), that key is now visible via System.getProperty("key").
 *   DANGER: put() accepts Object keys/values (not just String). A non-String value will break
 *   any code that calls System.getProperty() later, because getProperty() does instanceof String.
 *   Always use System.setProperty() (which enforces String types) instead of getProperties().put().
 *
 * DANGER — modifying real system properties:
 *   Changing properties like "file.separator" mid-run can break the entire JVM.
 *   This file only touches custom keys (prefixed with "poc01.") and restores them afterwards.
 */
public class Impl08 {

    public static void main(String[] args) {

        // ── 1. SET a custom system property ──────────────────────────────────

        // setProperty(key, value) is like getProperties().setProperty(key, value) but thread-safe.
        // Returns the previous value (or null if the key was new).
        String previous = System.setProperty("poc01.config.path", "/etc/myapp/config");
        System.out.println("previous value: " + previous);   // null (first time)

        // Read it back immediately.
        System.out.println("poc01.config.path = " + System.getProperty("poc01.config.path"));  // /etc/myapp/config

        // Set it again — returns the OLD value.
        String old = System.setProperty("poc01.config.path", "/etc/myapp/config-v2");
        System.out.println("old value: " + old);             // /etc/myapp/config
        System.out.println("new value: " + System.getProperty("poc01.config.path"));  // /etc/myapp/config-v2


        // ── 2. CLEAR a system property ────────────────────────────────────────

        // clearProperty(key) removes the key. Returns the value it had, or null.
        String removed = System.clearProperty("poc01.config.path");
        System.out.println("removed value:    " + removed);                              // /etc/myapp/config-v2
        System.out.println("after clear:      " + System.getProperty("poc01.config.path"));  // null


        // ── 3. SHOW that System.getProperties() is LIVE ───────────────────────

        // Get the live Properties object.
        Properties live = System.getProperties();

        // Mutate it directly using put() (not setProperty).
        // This is NOT recommended in real code (no type safety) but demonstrates the live nature.
        live.put("poc01.direct.put", "set via put()");

        // The mutation is visible via System.getProperty() even though we used put() directly.
        System.out.println("\npoc01.direct.put = " + System.getProperty("poc01.direct.put"));  // set via put()

        // Clean up the test key.
        System.clearProperty("poc01.direct.put");
        System.out.println("after clear: " + System.getProperty("poc01.direct.put"));  // null


        // ── 4. DANGER — put() with a non-String value ─────────────────────────

        // This is what happens when someone puts a non-String value.
        // put() allows it because Hashtable<Object,Object> accepts any Object.
        live.put("poc01.numeric", 42);   // Integer, not String!

        // getProperty() checks instanceof String — it returns NULL for non-String values.
        String fromGetProperty = System.getProperty("poc01.numeric");
        System.out.println("\npoc01.numeric via getProperty() = " + fromGetProperty);  // null (!)

        // But get() (the raw Hashtable method) returns the actual Integer.
        Object fromGet = live.get("poc01.numeric");
        System.out.println("poc01.numeric via get()          = " + fromGet);  // 42

        // Clean up.
        live.remove("poc01.numeric");

        System.out.println("\nAll poc01.* keys cleared. Done.");
    }
}
