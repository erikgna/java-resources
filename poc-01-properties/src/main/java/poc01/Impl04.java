package poc01;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Impl04 — LOAD + ITERATE from memory.
 *
 * Rules: write this WITHOUT looking at Impl03.
 *
 * Added vs Impl03:
 *   - entrySet() iteration       → iterate key+value pairs as Map.Entry
 *   - propertyNames() (old API)  → returns an Enumeration<?> — older, avoid in new code
 *   - stringPropertyNames() (new API) → returns Set<String> — prefer this
 *
 * Key distinction:
 *   propertyNames() → Enumeration, includes defaults, may include non-String keys if put() was used.
 *   stringPropertyNames() → Set<String>, includes defaults, ONLY returns String keys.
 *   In new code: always use stringPropertyNames().
 */
public class Impl04 {

    public static void main(String[] args) throws IOException {

        // ── LOAD from classpath ───────────────────────────────────────────────

        Properties props = new Properties();
        InputStream is = Impl04.class.getClassLoader().getResourceAsStream("config.properties");
        props.load(is);
        is.close();

        // ── LOAD from String (merge) ──────────────────────────────────────────

        props.load(new StringReader("extra.key=extra.value\nextra.num=42"));

        System.out.println("Total entries: " + props.size());


        // ── ITERATE with entrySet() ───────────────────────────────────────────

        // entrySet() returns a Set<Map.Entry<Object,Object>>.
        // Keys and values are Object (not String) because Properties extends Hashtable<Object,Object>.
        // In practice they are always Strings if you only used setProperty() / load().
        System.out.println("\nAll entries via entrySet():");
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            // Cast to String is safe here because we only used setProperty() and load().
            String key   = (String) entry.getKey();
            String value = (String) entry.getValue();
            System.out.printf("  [%-30s] -> [%s]%n", key, value);
        }


        // ── COMPARE propertyNames() vs stringPropertyNames() ─────────────────

        // propertyNames() is the old JDK 1.0 API — returns an Enumeration<?>.
        // Enumerations are the predecessor of Iterator; they work but are clunky.
        System.out.println("\npropertyNames() (old Enumeration API):");
        Enumeration<?> oldKeys = props.propertyNames();
        int count = 0;
        while (oldKeys.hasMoreElements()) {
            String key = (String) oldKeys.nextElement();
            count++;
        }
        System.out.println("  count: " + count);

        // stringPropertyNames() is the modern API — returns Set<String>.
        // Prefer this in all new code.
        System.out.println("stringPropertyNames() (modern Set API):");
        System.out.println("  count: " + props.stringPropertyNames().size());

        // Both should return the same count here because all keys are Strings.
        System.out.println("  counts match: " + (count == props.stringPropertyNames().size()));
    }
}
