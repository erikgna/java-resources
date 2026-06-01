# POC-01: Java Properties API

`java.util.Properties` stores String key-value pairs, mainly for `.properties` config files:

```properties
db.host=localhost
db.port=5432
app.name=MyApp
```

It also exposes the JVM's System properties (`java.version`, `os.name`, `user.home`, ...).

## What you need to know

- **Storage:** it claims to extend `Hashtable` but actually keeps data in a `ConcurrentHashMap`. The Hashtable parent is a 1995 relic kept for compatibility. Reads are lock-free and `setProperty` is synchronized, so concurrent writes are safe.
- **Use `setProperty`/`getProperty`, never `put`/`get`.** `put("k", 42)` stores a non-String, and `getProperty` then returns `null` because it checks `instanceof String`.
- **Missing key returns `null`** (no exception). Use `getProperty(key, default)` for a fallback. Note `""` is a valid value, so `== null` alone is not "not configured".
- **Encoding:** `store(OutputStream)` / `load(InputStream)` use ISO-8859-1, so non-ASCII gets escaped to `\uXXXX`. For UTF-8, wrap with a `Writer`/`Reader`:
  ```java
  Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
  props.store(w, "comment");
  ```
- **Defaults chain:** `new Properties(defaults)` adds fallback lookups. `size()` ignores defaults; `stringPropertyNames()` includes them; `store()` does **not** write them.
- **`load` twice merges** into the same object (later keys win); it does not clear.
- **`System.getProperties()` is the live object** — mutating it changes what `System.getProperty()` returns.

## Run

```bash
mvn compile exec:java -Dexec.mainClass="poc01.PropertiesPoc"
mvn test
```

`PropertiesPoc.java` runs each behavior in one `main()`. `PropertiesTest.java` proves each one as an executable spec.
