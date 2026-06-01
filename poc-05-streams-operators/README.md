# POC 05 — Streams: Supplier, Consumer, BinaryOperator, UnaryOperator

## What This POC Covers

Four functional interfaces from `java.util.function` that power Java streams and pipelines:

| Interface | Input | Output | Purpose |
|---|---|---|---|
| `Supplier<T>` | nothing | `T` | produce a value |
| `Consumer<T>` | `T` | nothing (void) | consume a value, side effect |
| `UnaryOperator<T>` | `T` | `T` (same type) | transform a value |
| `BinaryOperator<T>` | `T, T` | `T` (same type) | combine two values into one |

---

## The Big Picture: How They Fit Together

Think of a data pipeline in four stages:

```
[Supplier] → data → [UnaryOperator] → clean data → [Consumer] → side effect
                                                ↘
                                           [BinaryOperator] → aggregate
```

Real example from Impl10:
```
Supplier produces raw orders
  ↓
UnaryOperator trims + uppercases each order
  ↓
Consumer logs each order + alerts if high value
  ↓
BinaryOperator folds all totals into a grand total
```

This is the ETL pattern (Extract → Transform → Load) expressed with Java functional interfaces.

---

## Supplier\<T\>

### What it is

A function with **no input and one output**. It produces a value on demand.

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

### The key insight: laziness

A `Supplier` is not a value. It is a **recipe** for a value. Nothing runs until you call `get()`.

```java
// This does NOT compute anything yet:
Supplier<String> greeting = () -> "Hello, Java!";

// This DOES compute:
String value = greeting.get(); // "Hello, Java!"
```

### Factory pattern

Each `get()` call can produce a **fresh object**:

```java
Supplier<StringBuilder> factory = () -> new StringBuilder();
StringBuilder a = factory.get(); // new instance
StringBuilder b = factory.get(); // different new instance
System.out.println(a == b); // false
```

### With `Stream.generate()`

`Stream.generate(Supplier<T>)` creates an **infinite stream**. The `Supplier` is called for each element. Always pair with `limit()` or another stopping mechanism.

```java
// Five "hello" strings:
Stream.generate(() -> "hello").limit(5).collect(Collectors.toList());
// [hello, hello, hello, hello, hello]

// Stateful counter using int[] trick (lambdas can't use mutable local vars):
int[] n = {0};
Stream.generate(() -> ++n[0]).limit(4).collect(Collectors.toList());
// [1, 2, 3, 4]
```

**Why the `int[]` trick?**
Java lambdas require captured variables to be effectively final (not reassigned).
A single-element array is a workaround: the array reference stays fixed,
but its contents can change.

### `Stream.generate()` vs `Stream.iterate()`

| | `generate(supplier)` | `iterate(seed, operator)` |
|---|---|---|
| Elements | **independent** (each is a fresh call) | **sequential** (each depends on previous) |
| Use for | constant values, random values, factories | number sequences, Fibonacci, step loops |

---

## Consumer\<T\>

### What it is

A function with **one input and no output**. It consumes a value and produces a side effect.

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
    default Consumer<T> andThen(Consumer<? super T> after) { ... }
}
```

### The key insight: side effects only

`Consumer` is the **side effect** interface. Its entire purpose is to do something observable:
print, log, save to DB, send an event, update a counter. It never transforms data.

```java
Consumer<String> logger = s -> System.out.println("[LOG] " + s);
logger.accept("Order created"); // [LOG] Order created
```

### With `forEach()`

The most common use. Every `.forEach(lambda)` call takes a `Consumer<T>`.

```java
List.of("Alice", "Bob").forEach(name -> System.out.println(name));
// or with method reference:
List.of("Alice", "Bob").forEach(System.out::println);
```

### `andThen()` — chaining consumers

Both consumers receive the **same original input**. No output flows between them (void → void).

```java
Consumer<String> log    = s -> System.out.println("[LOG]   " + s);
Consumer<String> save   = s -> System.out.println("[SAVED] " + s);
Consumer<String> both   = log.andThen(save);

both.accept("event");
// [LOG]   event
// [SAVED] event   ← same "event", not output of log
```

### `BiConsumer<T, U>` — two inputs

Same as `Consumer` but takes two arguments. Used heavily with `Map.forEach()`:

```java
Map<String, Integer> scores = Map.of("Alice", 95, "Bob", 80);
scores.forEach((name, score) -> System.out.println(name + ": " + score));
```

---

## UnaryOperator\<T\>

### What it is

A function with **one input of type T** and **one output of type T** (same type).

```java
@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {
    static <T> UnaryOperator<T> identity() { return t -> t; }
    // apply(T t) inherited from Function<T, T>
}
```

`UnaryOperator<T>` **extends** `Function<T, T>`. The difference is clarity:
`UnaryOperator` signals "same type in, same type out" — a transformation, not a conversion.

### Basic use

```java
UnaryOperator<String> upper = s -> s.toUpperCase();
upper.apply("hello"); // "HELLO"

UnaryOperator<Integer> square = n -> n * n;
square.apply(5); // 25
```

### `identity()` — the no-op operator

```java
UnaryOperator<String> noOp = UnaryOperator.identity();
noOp.apply("unchanged"); // "unchanged" — exact same object returned
```

Use `identity()` as a default when a transform is optional:
```java
UnaryOperator<String> transform = shouldTransform ? String::toUpperCase : UnaryOperator.identity();
```

### With `List.replaceAll()`

Modifies every element of a list **in place**:

```java
List<String> words = new ArrayList<>(List.of("hello", "world"));
words.replaceAll(String::toUpperCase); // mutates the list
// words is now [HELLO, WORLD]
```

### With `Stream.iterate()`

`Stream.iterate(seed, UnaryOperator<T>)` generates an infinite sequence where each element is
produced by applying the operator to the previous element:

```
seed, op(seed), op(op(seed)), op(op(op(seed))), ...
```

```java
// 1, 2, 4, 8, 16, ...
Stream.iterate(1, n -> n * 2).limit(5).collect(Collectors.toList());
// [1, 2, 4, 8, 16]

// Java 9 bounded form — like a for-loop:
// for (int i = 0; i < 10; i += 2)
Stream.iterate(0, n -> n < 10, n -> n + 2).collect(Collectors.toList());
// [0, 2, 4, 6, 8]
```

### Composition with `andThen()` and `compose()`

`UnaryOperator` inherits `andThen` and `compose` from `Function`.

```java
UnaryOperator<String> trim   = String::trim;
UnaryOperator<String> upper  = String::toUpperCase;

// andThen: trim first, then upper
Function<String, String> pipeline = trim.andThen(upper);
pipeline.apply("  hello  "); // "HELLO"

// compose: upper first, then trim
// f.compose(g) means: f(g(x))
```

**Important:** `andThen` and `compose` on `UnaryOperator<T>` return `Function<T,T>`, not `UnaryOperator<T>`. Java generics lose the `UnaryOperator` type during composition. Functionally identical, different declared type.

---

## BinaryOperator\<T\>

### What it is

A function with **two inputs of type T** and **one output of type T**.

```java
@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {
    static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) { ... }
    static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) { ... }
    // apply(T t1, T t2) inherited from BiFunction<T,T,T>
}
```

Think: `+`, `*`, `max`, `min` are all binary operators. Two things in, one thing out.

### Basic use

```java
BinaryOperator<Integer> add  = (a, b) -> a + b;
BinaryOperator<Integer> max  = Integer::max;
BinaryOperator<String>  longer = (a, b) -> a.length() >= b.length() ? a : b;

add.apply(3, 4);             // 7
max.apply(3, 9);             // 9
longer.apply("cat", "elephant"); // "elephant"
```

### `minBy()` and `maxBy()` — static factory methods

Build a `BinaryOperator` that picks the min or max using a `Comparator`:

```java
BinaryOperator<String> shortest =
    BinaryOperator.minBy(Comparator.comparingInt(String::length));

shortest.apply("cat", "elephant"); // "cat"
```

### With `Stream.reduce()`

`reduce()` uses a `BinaryOperator` to **fold** a stream of N elements into one value.

```
[1, 2, 3, 4, 5] with operator "+"
Step 1: apply(1, 2) = 3
Step 2: apply(3, 3) = 6
Step 3: apply(6, 4) = 10
Step 4: apply(10, 5) = 15
Result: 15
```

**Form 1** — with identity (always returns `T`, safe for empty streams):
```java
// identity = 0 for sum (adding nothing = 0)
int sum = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum); // 15

// empty stream returns identity:
int empty = Stream.<Integer>of().reduce(0, Integer::sum); // 0
```

**Form 2** — without identity (returns `Optional<T>`):
```java
Optional<Integer> max = Stream.of(3, 1, 9, 2).reduce(Integer::max);
max.isPresent(); // true
max.get();       // 9

// empty stream:
Optional<Integer> empty = Stream.<Integer>of().reduce(Integer::sum);
empty.isPresent(); // false  ← must handle this case
```

**Why `Optional` in Form 2?**
Without an identity value, an empty stream has no result. Java forces the caller to handle
the empty case explicitly. If the compiler let `reduce()` return `null`, you'd get a
`NullPointerException` at runtime — the classic Java bug. `Optional` prevents it.

---

## Composition: andThen vs compose

```
f.andThen(g)  = g(f(x))   ← f runs first, output goes to g
f.compose(g)  = f(g(x))   ← g runs first, output goes to f
```

Memory trick: `andThen` reads left-to-right (f then g). `compose` reads right-to-left (g before f).

```java
Function<Integer, Integer> add3   = n -> n + 3;
Function<Integer, Integer> times2 = n -> n * 2;

add3.andThen(times2).apply(5); // (5+3)*2 = 16  — add3 runs first
add3.compose(times2).apply(5); // (5*2)+3 = 13  — times2 runs first
```

---

## Common Mistakes

### 1. Consuming a stream twice

```java
Stream<String> stream = Stream.of("a", "b", "c");
stream.count(); // terminal — stream closed
stream.count(); // throws IllegalStateException!
```

### 2. Modifying captured state in parallel streams

```java
int[] sum = {0};
// WRONG in parallel streams — race condition!
list.parallelStream().forEach(n -> sum[0] += n);
// Use: list.stream().reduce(0, Integer::sum) instead
```

### 3. Forgetting `limit()` on `generate()` or `iterate()`

```java
Stream.generate(() -> "hello").forEach(System.out::println); // infinite loop!
Stream.generate(() -> "hello").limit(5).forEach(System.out::println); // correct
```

### 4. Expecting `Consumer.andThen` to chain outputs

```java
Consumer<String> c1 = s -> s.toUpperCase(); // this uppercases nothing
Consumer<String> c2 = s -> System.out.println(s); // prints original, not uppercased
c1.andThen(c2).accept("hello"); // prints "hello", NOT "HELLO"
// To transform AND consume: use Function first, then Consumer:
Function<String, String> upper = String::toUpperCase;
Consumer<String> print = System.out::println;
print.accept(upper.apply("hello")); // prints "HELLO"
```

---

## File Map

| File | Topic |
|---|---|
| `Impl01.java` | `Supplier<T>` — basics, factory pattern, laziness |
| `Impl02.java` | `Supplier<T>` with `Stream.generate()` — infinite streams, stateful suppliers |
| `Impl03.java` | `Consumer<T>` — basics, `forEach`, `andThen` chaining |
| `Impl04.java` | `BiConsumer<T,U>` — two inputs, `Map.forEach`, `andThen` |
| `Impl05.java` | `UnaryOperator<T>` — basics, `identity()`, `List.replaceAll`, `andThen` |
| `Impl06.java` | `UnaryOperator<T>` with `Stream.iterate()` — sequences, Fibonacci, bounded form |
| `Impl07.java` | `BinaryOperator<T>` — basics, method refs, `minBy`/`maxBy` |
| `Impl08.java` | `BinaryOperator<T>` with `Stream.reduce()` — both forms, Optional, pipelines |
| `Impl09.java` | Composing operators — `andThen`, `compose`, Consumer chains, mini ETL |
| `Impl10.java` | Integrated pipeline — all four interfaces, ETL pattern, loyalty tier demo |

---

## Running

```bash
# Run all tests
mvn test

# Run a single main class
mvn exec:java -Dexec.mainClass="poc05.Impl01"
mvn exec:java -Dexec.mainClass="poc05.Impl10"
```

## Test Count

67 tests. 0 failures.
