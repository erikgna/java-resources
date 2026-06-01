# POC-01: Java Properties API

Your first Java POC. The goal is **deep understanding** — not just making things work, but knowing *why* they work and *when* they break.

---

## What is `java.util.Properties`?

`Properties` is a Java class used to store **key-value pairs** where both keys and values are Strings. Its main use case is **configuration files** — the `.properties` format you see everywhere in Java projects:

```properties
db.host=localhost
db.port=5432
app.name=MyApp
```

You can also read/write **System properties** — built-in values the JVM populates at startup, like `java.version`, `os.name`, and `user.home`.

**Under the hood (important!):**
`Properties` claims to extend `Hashtable`, but since JDK 11 it actually stores data in a `ConcurrentHashMap`. The Hashtable parent is a historical relic from 1995 that can't be removed without breaking existing code. You will verify this by reading the JDK source.

---

## Project Structure

```
poc-01-properties/
  pom.xml                              ← Maven build file (Java 11, JUnit 5)
  README.md                            ← this file
  src/
    main/
      java/poc01/
        Impl01.java   ← CREATE + READ: new Properties, setProperty, getProperty
        Impl02.java   ← CREATE + READ from memory (no peeking)
        Impl03.java   ← LOAD: from classpath file and from StringReader
        Impl04.java   ← LOAD from memory: entrySet, old vs new iteration API
        Impl05.java   ← STORE: OutputStream vs Writer encoding difference, XML
        Impl06.java   ← STORE from memory: round-trips for .properties and XML
        Impl07.java   ← SYSTEM: read java.version, os.name, user.home, all keys
        Impl08.java   ← SYSTEM from memory: setProperty, clearProperty, live object
        Impl09.java   ← EDGE CASES: defaults chain, null keys, non-String values
        Impl10.java   ← COMBINED from memory: everything + concurrent write test
      resources/
        config.properties              ← example config file loaded in the Impl files
    test/
      java/poc01/
        PropertiesTest.java            ← full test suite (read these as documentation)
```

---

## What Each Impl Teaches

| File    | Can look at  | What you learn |
|---------|-------------|----------------|
| Impl01  | Everything  | `new Properties()`, `setProperty`, `getProperty`, null for missing keys |
| Impl02  | Nothing     | Same from memory + `containsKey`, `isEmpty` |
| Impl03  | Impl01 only | `load(InputStream)` from classpath, `load(Reader)` from a String, `stringPropertyNames()` |
| Impl04  | Nothing     | Load from memory + `entrySet()` vs `propertyNames()` vs `stringPropertyNames()` |
| Impl05  | Javadoc only| `store(OutputStream)` uses ISO-8859-1 (escapes Unicode!), `store(Writer)` doesn't, `storeToXML` |
| Impl06  | Nothing     | Full store+reload round-trips from memory |
| Impl07  | Javadoc only| `System.getProperties()`, all well-known system keys |
| Impl08  | Nothing     | `System.setProperty`, `System.clearProperty`, live object danger |
| Impl09  | JDK source  | Defaults chain (3 levels), empty values, null key → NPE, non-String trap |
| Impl10  | Nothing     | Everything from memory + concurrent write test |

**Rule:** If you had to peek at a previous file while writing a "from memory" Impl — write that Impl again until you don't need to.

---

## How to Run

### Compile everything
```bash
cd poc-01-properties
mvn compile
```

### Run a single Impl file
Each Impl has a `main()` method. Run it with:
```bash
mvn compile exec:java -Dexec.mainClass="poc01.Impl01"
mvn compile exec:java -Dexec.mainClass="poc01.Impl02"
# ... up to Impl10
```

### Run all tests
```bash
mvn test
```

### Run a single test class
```bash
mvn test -Dtest=PropertiesTest
```

### Run a single test method
```bash
mvn test -Dtest="PropertiesTest#testNullKeyThrowsNpe"
```

---

## The Critical Encoding Bug (Read This)

This is the #1 real-world mistake with `Properties`:

```java
// WRONG: "é" becomes é in the file because OutputStream uses ISO-8859-1
props.store(new FileOutputStream("config.properties"), "comment");

// CORRECT: "é" stays as "é" because Writer uses UTF-8
Writer w = new OutputStreamWriter(new FileOutputStream("config.properties"), StandardCharsets.UTF_8);
props.store(w, "comment");
w.close();
```

Always use `store(Writer)` with an explicit `OutputStreamWriter(stream, UTF-8)` when your values contain non-ASCII characters (accented letters, symbols, non-Latin scripts).

---

## What to Read in the JDK Source

The JDK source lives at:
```
~/.sdkman/candidates/java/26.0.1-tem/lib/src.zip
```
Extract it and open `java.base/java/util/Properties.java`.

Read these sections **in order**:

1. **Constructor (~line 215)** — See `super((Void) null)`. This bypasses Hashtable's storage. The real data goes into `ConcurrentHashMap<Object,Object> map` at ~line 167.

2. **`getProperty()` (~line 1144)** — Find the `oval instanceof String` check. This is why putting a non-String value with `put()` causes `getProperty()` to silently return `null`.

3. **Hashtable override section (~line 1297)** — Every Hashtable method (put, get, remove, etc.) is overridden to delegate to `this.map`. The Hashtable parent is a shell.

4. **`store0()` (~line 914)** — Find where keys are sorted alphabetically before writing. Find the date comment that's always written.

5. **`load0()` (~line 413)** — The character-by-character parser. This is where comment lines are skipped, separators are detected, and `\uXXXX` sequences are decoded.

---

## Understanding Checklist

Answer these **without looking at any code**. If you can't, go back and re-read the relevant Impl:

1. Where does Properties actually store its data? (not Hashtable)
2. Why does `getProperty()` return null after `props.put("k", 42)`?
3. Does `store()` write the entries from the defaults chain?
4. What encoding does `store(OutputStream, ...)` use? What about `store(Writer, ...)`?
5. Is `System.getProperties()` a copy or the live internal object?
6. What happens if you call `props.put(null, "value")`?
7. Why does Properties extend Hashtable even though it doesn't use it for storage?
8. Write `getProperty()` from memory including the defaults-chain recursion and the `instanceof String` check.
9. Write a `load()` + `store()` round-trip that correctly handles Unicode in both formats.
10. Write a concurrent `setProperty()` test and explain why it is safe.

---

## Common Mistakes to Avoid

| Mistake | Why it fails |
|---------|-------------|
| `props.put(key, value)` instead of `props.setProperty(key, value)` | `put()` accepts non-String values that silently break `getProperty()` |
| `store(OutputStream)` with non-ASCII values | Non-ASCII becomes `\uXXXX` — use `store(Writer)` with UTF-8 |
| `store(InputStream)` for loading a UTF-8 file | `load(InputStream)` uses ISO-8859-1 — use `load(Reader)` with a UTF-8 `InputStreamReader` |
| Mutating `System.getProperties()` with `put()` | Bypasses type checks — put a non-String, break all callers |
| Assuming `store()` writes defaults | It doesn't — only the top-level Properties is written |
| Checking `getProperty() == null` for "not configured" | Empty string `""` is a valid value — also check `isEmpty()` if needed |
