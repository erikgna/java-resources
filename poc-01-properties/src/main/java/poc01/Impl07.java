package poc01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Impl07 — SYSTEM PROPERTIES: read.
 *
 * The JVM maintains a global Properties object accessible via System.getProperties().
 * It is populated at JVM startup with information about the runtime environment.
 *
 * Concepts covered:
 *   - System.getProperties()       → returns the live global Properties object
 *   - System.getProperty(key)      → reads a single system property
 *   - System.getProperty(key, def) → reads with a fallback default
 *   - The well-known built-in keys every Java developer should know
 *
 * Reference allowed: only the Javadoc for System.getProperty().
 */
public class Impl07 {

    public static void main(String[] args) {

        // ── 1. READ a single system property ─────────────────────────────────

        // System.getProperty(key) is a shortcut for System.getProperties().getProperty(key).
        // Returns null if the key does not exist.
        String javaVersion = System.getProperty("java.version");
        System.out.println("java.version = " + javaVersion);

        // Two-arg form: return "unknown" if the property is not set.
        String notSet = System.getProperty("this.does.not.exist", "unknown");
        System.out.println("nonexistent  = " + notSet);  // unknown


        // ── 2. WELL-KNOWN built-in system properties ──────────────────────────

        System.out.println("\n── Java runtime ──");
        System.out.println("java.version        = " + System.getProperty("java.version"));
        System.out.println("java.vendor         = " + System.getProperty("java.vendor"));
        System.out.println("java.home           = " + System.getProperty("java.home"));
        System.out.println("java.class.path     = " + System.getProperty("java.class.path"));

        System.out.println("\n── Operating system ──");
        System.out.println("os.name             = " + System.getProperty("os.name"));
        System.out.println("os.arch             = " + System.getProperty("os.arch"));
        System.out.println("os.version          = " + System.getProperty("os.version"));

        System.out.println("\n── User environment ──");
        System.out.println("user.name           = " + System.getProperty("user.name"));
        System.out.println("user.home           = " + System.getProperty("user.home"));
        System.out.println("user.dir            = " + System.getProperty("user.dir"));   // current working directory

        System.out.println("\n── File system separators ──");
        // These are OS-specific.  On Unix: /, :, \n.  On Windows: \\, ;, \r\n.
        System.out.println("file.separator      = " + System.getProperty("file.separator"));
        System.out.println("path.separator      = " + System.getProperty("path.separator"));
        // line.separator is not printable, so show its length and char codes.
        String ls = System.getProperty("line.separator");
        System.out.println("line.separator      = (length=" + ls.length() + ", bytes=" + java.util.Arrays.toString(ls.getBytes()) + ")");


        // ── 3. GET the entire System Properties object ────────────────────────

        // System.getProperties() returns the LIVE Properties object — not a copy.
        // Mutating this object affects ALL future calls to System.getProperty().
        Properties allSystemProps = System.getProperties();

        System.out.println("\n── Total system properties at startup: " + allSystemProps.size());

        // Print ALL system properties sorted alphabetically.
        List<String> sortedKeys = new ArrayList<>(allSystemProps.stringPropertyNames());
        Collections.sort(sortedKeys);

        System.out.println("\n── All system properties (sorted) ──");
        for (String key : sortedKeys) {
            System.out.printf("  %-45s = %s%n", key, System.getProperty(key));
        }
    }
}
