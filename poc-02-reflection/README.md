# POC 02 — Reflection API: Read, Invoke, Modify

## What is Reflection?

Normal Java code is **static**: you write `person.getName()`, the compiler checks at compile time that `getName()` exists, and the JVM calls it. You know the types. The compiler enforces everything.

Reflection is **dynamic**: at runtime, you can ask any class *what fields and methods does it have?*, and then call them by name — even if you didn't know the class existed when you wrote your code.

Reflection lives in the `java.lang.reflect` package. The entry point is always `Class<?>`.

## Why Reflection Matters

You will not use reflection every day. But it is the engine inside:

- **JUnit 5** — calls your `@Test` methods by finding them at runtime.
- **Spring** — reads your annotations and injects dependencies into private fields.
- **Jackson/Gson** — serializes objects to JSON by reading fields reflectively.
- **Hibernate** — maps database rows to private Java fields.
- **Java serialization** — reads and writes all fields, including private ones.

Understanding reflection means you understand how those tools work at their core.

## The Three Capabilities

| Capability | What it means | Key classes |
|---|---|---|
| **Read** | Inspect class structure: fields, methods, constructors, metadata | `Class`, `Field`, `Method`, `Constructor` |
| **Invoke** | Call methods (public or private) at runtime by name | `Method.invoke()` |
| **Modify** | Read/write field values (public, private, even final) at runtime | `Field.get()`, `Field.set()` |

## The Target Class

`Person.java` is designed to exercise every corner of the Reflection API:

- `private` fields (`name`, `age`) — test read/write access bypass
- `private final` field (`id`) — test final field modification (Impl08)
- `private static` field (`instanceCount`) — test static access
- `public` constructor `Person(String, int)` — normal instantiation
- `private` constructor `Person()` — test private constructor invocation
- `private` method `secret()` — test private method invocation
- `private void` method `explode()` — test exception wrapping

**Rule:** In all `Impl` files, you interact with `Person` **only through reflection**. Never call `person.getName()` directly to test reflection — use `field.get(person)` or `method.invoke(person)`.

## Project Structure

```
poc-02-reflection/
├── src/
│   ├── main/java/poc02/
│   │   ├── Person.java          ← target class (read this first)
│   │   ├── Impl01.java          ← READ: getting the Class object (3 ways)
│   │   ├── Impl02.java          ← READ: inspecting fields
│   │   ├── Impl03.java          ← READ: inspecting methods
│   │   ├── Impl04.java          ← READ: constructors + instantiation
│   │   ├── Impl05.java          ← INVOKE: public + static methods
│   │   ├── Impl06.java          ← INVOKE: private methods (setAccessible)
│   │   ├── Impl07.java          ← MODIFY: private fields + static fields
│   │   ├── Impl08.java          ← MODIFY: final fields (dangerous!)
│   │   ├── Impl09.java          ← ERROR: all 5 reflection exceptions
│   │   └── Impl10.java          ← SYNTHESIS: full class inspector utility
│   └── test/java/poc02/
│       └── ReflectionTest.java  ← JUnit 5 tests for all behaviors
└── pom.xml
```

## Impl Sequence

| Impl | Topic | Key concept |
|------|-------|-------------|
| Impl01 | Class object | 3 ways to get `Class<?>`: literal, `getClass()`, `forName()` |
| Impl02 | Fields | `getDeclaredFields()` vs `getFields()`. `Modifier` bitmask. |
| Impl03 | Methods | `getDeclaredMethods()` vs `getMethods()`. Param type matching. |
| Impl04 | Constructors | `getDeclaredConstructors()`. Private constructor + `setAccessible`. |
| Impl05 | Invoke public | `method.invoke(instance, args...)`. Static = `invoke(null)`. |
| Impl06 | Invoke private | `setAccessible(true)` unlocks private. `InvocationTargetException` wrap. |
| Impl07 | Modify fields | `field.get/set`. `getInt/setInt` for primitives. `null` for static. |
| Impl08 | Modify final | Java 11 allows it. JIT inlining creates inconsistency. Never in prod. |
| Impl09 | Errors | 5 exceptions: `ClassNotFound`, `NoSuchMethod`, `NoSuchField`, `IllegalAccess`, `InvocationTarget`. |
| Impl10 | Inspector | Combines everything. Try it on `String.class`. |

## The Access Control Key

Every private/protected member is locked. There is one unlock:

```java
member.setAccessible(true);   // works for Field, Method, Constructor
```

This suppresses the access check **for that member object only**. It does not modify the class. A fresh `getDeclaredMethod("secret")` call returns a new locked object.

## How to Run

```bash
# Run all tests
mvn test

# Run a specific Impl (replace Impl01 with any class name)
mvn exec:java -Dexec.mainClass="poc02.Impl01"

# Run the class inspector (Impl10)
mvn exec:java -Dexec.mainClass="poc02.Impl10"
```

## The 10-Repetition Rule

After reading each `Impl` file:
1. Close it.
2. Write the same code from memory in a scratch file.
3. Repeat 10 times.
4. By Impl_10th you should write it without hesitation.

This builds **muscle memory**, not recognition. Recognition fades. Muscle memory stays.

## What to Explore Next (Internals)

These are the things you should read after completing all 10 impls:

**JDK Source (read at least these):**
- `java.lang.Class` — the root of all reflection. Read `getDeclaredFields()`.
- `java.lang.reflect.Field` — how `get()` and `set()` dispatch to native code.
- `java.lang.reflect.Method` — how `invoke()` routes to `MethodAccessor`.
- `sun.reflect.NativeMethodAccessorImpl` — the native-call implementation.
- `sun.reflect.ReflectionFactory` — how Java decides when to JIT an accessor.

**Questions to answer by reading source (not docs):**
1. What is the "inflation threshold"? After how many reflective calls does Java switch from native to bytecode-generated accessors?
2. What does `setAccessible(true)` actually do inside the JVM? Which flag does it flip?
3. Why does `getMethods()` include `Object`'s methods but `getDeclaredMethods()` doesn't?
4. What is `MethodHandles.lookup()`? How does it differ from `Method.invoke()`?

**Experiment to run:**
- Write a benchmark using `System.nanoTime()`.
- Compare: direct method call vs reflective call (cold) vs reflective call (warm, 1000+ invocations).
- You will see the inflection point where the JIT-compiled accessor kicks in.

## Common Beginner Mistakes

| Mistake | Correct approach |
|---|---|
| Using `getMethod()` to find private methods | Use `getDeclaredMethod()` — `getMethod()` only sees public |
| Forgetting `setAccessible(true)` | Private = locked. `setAccessible(true)` is the unlock. Always. |
| Not calling `getCause()` on `InvocationTargetException` | The real exception is wrapped. `e.getCause()` unwraps it. |
| Passing wrong param types to `getDeclaredMethod` | `greetWith(String)` → pass `String.class`, not `"String"` |
| Modifying `final` fields in production | JIT may inline the old value. Use `final` for immutability or don't use it. |

## Sources to Read

- OpenJDK source: `src/java.base/share/classes/java/lang/reflect/`
- JEP 416 (Java 18): Reimplement Core Reflection with Method Handles
- *Effective Java* 3rd ed., Item 65: Prefer interfaces to reflection
- *Java Reflection in Action* (Forman & Forman) — deep coverage
