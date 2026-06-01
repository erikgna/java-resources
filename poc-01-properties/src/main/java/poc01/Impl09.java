package poc01;

import java.io.StringReader;
import java.util.Properties;

/**
 * Impl09 — DEFAULTS CHAIN + EDGE CASES.
 *
 * Reference allowed: JDK source for getProperty() and load0().
 *
 * Concepts covered:
 *   - Defaults chain (3 levels)           → Properties can have a fallback Properties object
 *   - Empty string value                  → setProperty("k", "") returns "" not null
 *   - Whitespace in values                → preserved exactly
 *   - put(key, Integer) → getProperty()   → returns null (non-String value trap)
 *   - put(null, value)                    → NullPointerException from ConcurrentHashMap
 *   - put(key, null)                      → NullPointerException from ConcurrentHashMap
 *   - Multi-line value via backslash      → joined into single string, leading whitespace trimmed
 *   - Colon and space separators          → parsed the same as =
 *
 * The defaults chain is like inheritance for configuration:
 *   base (lowest priority) → middle → top (highest priority).
 *   getProperty() walks the chain: checks "this" first, then defaults, then defaults.defaults.
 */
public class Impl09 {

    public static void main(String[] args) throws Exception {

        // ── 1. THREE-LEVEL DEFAULTS CHAIN ────────────────────────────────────

        // Level 1: base defaults (lowest priority)
        Properties base = new Properties();
        base.setProperty("color",  "red");     // only in base
        base.setProperty("shape",  "square");  // overridden in middle
        base.setProperty("weight", "heavy");   // overridden at top level

        // Level 2: middle defaults — pass "base" as constructor argument.
        // The constructor argument is the fallback (defaults) object.
        Properties middle = new Properties(base);
        middle.setProperty("shape",  "circle"); // overrides base "shape"
        middle.setProperty("size",   "medium"); // only in middle

        // Level 3: top — the object actually used in code.
        Properties top = new Properties(middle);
        top.setProperty("weight", "light");   // overrides base "weight"
        top.setProperty("texture", "smooth"); // only in top

        System.out.println("── Defaults chain lookups ──");
        System.out.println("color   = " + top.getProperty("color"));    // red    (from base, via middle)
        System.out.println("shape   = " + top.getProperty("shape"));    // circle (from middle, base overridden)
        System.out.println("weight  = " + top.getProperty("weight"));   // light  (from top, all overridden)
        System.out.println("size    = " + top.getProperty("size"));     // medium (from middle)
        System.out.println("texture = " + top.getProperty("texture"));  // smooth (top only)
        System.out.println("missing = " + top.getProperty("missing"));  // null   (not in any level)

        // stringPropertyNames() returns ALL keys visible in the chain.
        System.out.println("\nAll visible keys via top.stringPropertyNames(): " + top.stringPropertyNames());


        // ── 2. EMPTY STRING VALUE ─────────────────────────────────────────────

        Properties p = new Properties();
        p.setProperty("empty.key", "");

        // getProperty returns "" (empty string) — NOT null.
        // A null check alone is not sufficient: you also need an isEmpty() check.
        String emptyVal = p.getProperty("empty.key");
        System.out.println("\nempty.key  = [" + emptyVal + "]");           // []
        System.out.println("is null?   = " + (emptyVal == null));         // false
        System.out.println("is empty?  = " + emptyVal.isEmpty());         // true


        // ── 3. WHITESPACE PRESERVATION ────────────────────────────────────────

        // When you call setProperty() directly, whitespace is stored exactly as given.
        p.setProperty("spaces", "   three leading spaces");
        System.out.println("\n[spaces] = [" + p.getProperty("spaces") + "]");
        // Output: [   three leading spaces]

        // When loaded from a .properties FILE, the parser strips leading whitespace
        // around the separator. To preserve leading spaces in a file, escape the first space:
        //   key=\   three spaces
        // Let's verify this by loading from a String.
        Properties fromString = new Properties();
        fromString.load(new StringReader("padded=   stripped\npadded2=\\   kept"));
        System.out.println("[padded]  = [" + fromString.getProperty("padded") + "]");   // [stripped]  — leading spaces stripped
        System.out.println("[padded2] = [" + fromString.getProperty("padded2") + "]");  // [   kept]   — backslash escapes space


        // ── 4. NON-STRING VALUE TRAP: put(key, Integer) ───────────────────────

        // put() is inherited from Hashtable<Object,Object> and accepts any Object.
        // DO NOT use put() in production code — use setProperty() instead.
        p.put("numeric", 42);  // stores an Integer, not a String

        // getProperty() at source line ~1145 checks: if (oval instanceof String)
        // 42 is NOT an instanceof String, so getProperty() returns null.
        String fromGetProp = p.getProperty("numeric");
        System.out.println("\nnumeric via getProperty() = " + fromGetProp);  // null  ← the trap!

        // The value IS there — you just can't reach it via getProperty().
        Object fromGet = p.get("numeric");
        System.out.println("numeric via get()          = " + fromGet);        // 42


        // ── 5. NULL KEY → NullPointerException ────────────────────────────────

        // Properties stores data in a ConcurrentHashMap.
        // ConcurrentHashMap does NOT allow null keys or null values.
        // Trying to use null triggers NullPointerException immediately.
        System.out.println("\nAttempting put(null, value):");
        try {
            p.put(null, "value");  // NPE here
        } catch (NullPointerException e) {
            System.out.println("NullPointerException caught for null KEY: " + e.getMessage());
        }

        System.out.println("\nAttempting put(key, null):");
        try {
            p.put("key", null);  // NPE here
        } catch (NullPointerException e) {
            System.out.println("NullPointerException caught for null VALUE: " + e.getMessage());
        }


        // ── 6. MULTI-LINE VALUE VIA BACKSLASH ─────────────────────────────────

        // In .properties files, a \ at the end of a line continues the value.
        // Leading whitespace on the continuation line is STRIPPED.
        Properties multi = new Properties();
        multi.load(new StringReader(
            "address=123 Main St \\\n" +
            "        Suite 400 \\\n" +
            "        Springfield"
        ));
        // All three lines are joined; the leading spaces on continuation lines are removed.
        System.out.println("\naddress = [" + multi.getProperty("address") + "]");
        // Output: [123 Main St Suite 400 Springfield]


        // ── 7. SEPARATOR STYLES ────────────────────────────────────────────────

        // The .properties format supports three separators: =, :, and whitespace.
        Properties separators = new Properties();
        separators.load(new StringReader(
            "key.equals=value with equals\n" +
            "key.colon: value with colon\n" +
            "key.space value with space"
        ));
        System.out.println("\nkey.equals = " + separators.getProperty("key.equals"));  // value with equals
        System.out.println("key.colon  = " + separators.getProperty("key.colon"));    // value with colon
        System.out.println("key.space  = " + separators.getProperty("key.space"));    // value with space
    }
}
