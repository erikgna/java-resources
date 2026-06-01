package poc01;

import java.util.Properties;

/**
 * Impl02 — CREATE + READ from memory.
 *
 * Rules: write this WITHOUT looking at Impl01.
 * Goal: practice muscle memory for the basic API.
 *
 * Added vs Impl01:
 *   - containsKey(key)     → true/false — checks if key exists
 *   - isEmpty()            → true if no entries
 *   - System.out.println(props) → observe the ConcurrentHashMap toString format
 *
 * If you had to peek at Impl01 for any method name: that concept is not yet in memory.
 * Write Impl02 again on a blank file until you can do it without looking.
 */
public class Impl02 {

    public static void main(String[] args) {

        // ── CREATE ───────────────────────────────────────────────────────────

        // Empty Properties — no defaults, no entries yet.
        Properties props = new Properties();

        // isEmpty() is true before any entries are added.
        System.out.println("isEmpty before adding: " + props.isEmpty());  // true

        props.setProperty("color", "blue");
        props.setProperty("shape", "circle");
        props.setProperty("size", "large");

        System.out.println("isEmpty after adding:  " + props.isEmpty());  // false
        System.out.println("size: " + props.size());                      // 3


        // ── READ ─────────────────────────────────────────────────────────────

        // Basic read — key exists.
        System.out.println("color = " + props.getProperty("color"));  // blue

        // Read missing key — null returned, no exception.
        System.out.println("missing = " + props.getProperty("weight"));  // null

        // Read missing key with a default.
        System.out.println("weight (default) = " + props.getProperty("weight", "unknown"));  // unknown

        // containsKey() checks presence without fetching the value.
        // Note: containsKey() is inherited from Hashtable/Map and accepts Object, not just String.
        System.out.println("contains 'color':   " + props.containsKey("color"));    // true
        System.out.println("contains 'missing': " + props.containsKey("missing"));  // false

        // toString shows all entries as a map literal.
        // Order is unspecified — do NOT rely on it.
        System.out.println("all props: " + props);
    }
}
