# POC 04 — Java Streams: Lists, Arrays, Map, Filter, Predicates

## What Is This?

This POC teaches Java Streams from the ground up.
A Stream is not a collection — it is a **pipeline** of operations over a data source.

You will learn:
- What a Stream is and how it differs from a List
- How to create Streams from Lists and Arrays
- How to filter elements with `filter()` and `Predicate<T>`
- How to transform elements with `map()`
- How to gather results with `collect()`
- How to sort, deduplicate, paginate with `sorted()`, `distinct()`, `limit()`, `skip()`
- How to aggregate with `reduce()`
- How to flatten nested structures with `flatMap()`
- How to build real pipelines combining all of the above

---

## The Mental Model

Think of a stream like an **assembly line**:

```
[Data Source] → [Station 1] → [Station 2] → [Station 3] → [Output]
  (List/Array)   (filter)      (map)          (sorted)      (collect)
```

- Data flows **left to right**, one element at a time.
- **Intermediate operations** (filter, map, sorted) are **lazy** — they set up the pipeline but do nothing until triggered.
- **Terminal operations** (forEach, collect, count, reduce) **trigger execution** and produce a result or side effect.
- After a terminal operation, the stream is **spent** — you cannot reuse it. Create a new one from the source.
- The **original list is never modified**. Streams always produce new values.

---

## Two Types of Operations

| Type         | Returns     | Lazy? | Examples                          |
|--------------|-------------|-------|-----------------------------------|
| Intermediate | `Stream<T>` | Yes   | `filter`, `map`, `sorted`, `flatMap`, `distinct`, `limit`, `skip` |
| Terminal     | Value/void  | No    | `forEach`, `collect`, `count`, `reduce`, `min`, `max`, `anyMatch` |

---

## Implementation Files

### Impl01 — What Is a Stream?

**Concept:** Create streams from lists. Understand lazy evaluation.

```java
List<String> fruits = List.of("apple", "banana", "cherry");

fruits.stream()
    .forEach(System.out::println);  // terminal: triggers execution
```

Key lesson: nothing runs until the terminal operation. Add a `filter()` and a `map()` — no output appears until you add `forEach()` at the end. Java processes each element through the full pipeline before moving to the next element.

---

### Impl02 — Streams from Arrays

**Concept:** `Arrays.stream()`, `IntStream`, numeric operations.

```java
int[] scores = {85, 92, 78, 95};

int sum     = Arrays.stream(scores).sum();
double avg  = Arrays.stream(scores).average().getAsDouble();
int max     = Arrays.stream(scores).max().getAsInt();
```

**Why `IntStream` and not `Stream<Integer>`?**  
`Stream<Integer>` boxes each `int` into an `Integer` object — one heap allocation per number. `IntStream` holds raw primitives. For large numeric workloads, always prefer `IntStream`.

**Range generation** (no array needed):
```java
IntStream.range(1, 6)       // 1, 2, 3, 4, 5   (end is exclusive)
IntStream.rangeClosed(1, 6) // 1, 2, 3, 4, 5, 6 (end is inclusive)
```

---

### Impl03 — filter(): Keep Only What Matches

**Concept:** Remove elements that do not satisfy a condition.

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

numbers.stream()
    .filter(n -> n % 2 == 0) // keep evens
    .filter(n -> n > 4)       // keep > 4 — only 6, 8 survive
    .collect(Collectors.toList());
```

`filter()` takes a **Predicate** — a lambda that returns `true` or `false`.
- `true` → element survives
- `false` → element is discarded

Chain multiple `filter()` calls — each one narrows the stream further.

---

### Impl04 — map(): Transform Every Element

**Concept:** Change the value or type of each element.

```java
List<String> fruits = List.of("apple", "banana");

// String → String
fruits.stream().map(String::toUpperCase).collect(Collectors.toList()); // [APPLE, BANANA]

// String → Integer (type changes)
fruits.stream().map(String::length).collect(Collectors.toList()); // [5, 6]
```

`map()` guarantees: **same count in, same count out**. Every element enters, every element exits (transformed).

Use `mapToInt()` when you need `IntStream` from `Stream<String>`:
```java
fruits.stream().mapToInt(String::length).sum(); // total length of all names
```

---

### Impl05 — Predicate\<T\>: Named, Composable Conditions

**Concept:** Store filter logic in a variable. Combine predicates.

```java
Predicate<Integer> isEven       = n -> n % 2 == 0;
Predicate<Integer> isPositive   = n -> n > 0;

Predicate<Integer> isEvenAndPos = isEven.and(isPositive);   // both must be true
Predicate<Integer> isEvenOrPos  = isEven.or(isPositive);    // either must be true
Predicate<Integer> isOdd        = isEven.negate();           // flip the result
```

Why name your predicates?
- **Readability**: `filter(isAdult)` is clearer than `filter(age -> age >= 18)` repeated 5 times.
- **Reuse**: one definition, used in multiple stream pipelines.
- **Composition**: build complex conditions from simple ones without a long lambda.

`Predicate.not(pred)` is the static version of `.negate()` (Java 11+):
```java
.filter(Predicate.not(String::isEmpty)) // keep non-empty strings
```

---

### Impl06 — collect(): Gathering Results

**Concept:** Turn the stream back into a collection.

```java
// Into a List
.collect(Collectors.toList())

// Into a Set (removes duplicates, unordered)
.collect(Collectors.toSet())

// Concatenate strings
.collect(Collectors.joining(", "))          // "apple, banana"
.collect(Collectors.joining(", ", "[", "]")) // "[apple, banana]"

// Group by a key → Map<Key, List<Value>>
.collect(Collectors.groupingBy(p -> p.category))

// Count per group → Map<Key, Long>
.collect(Collectors.groupingBy(w -> w, Collectors.counting()))
```

`groupingBy()` is one of the most powerful collectors. It produces a `Map` where each key maps to a list of elements that share that key.

---

### Impl07 — sorted(), distinct(), limit(), skip()

**Concept:** Control ordering, deduplication, and size.

```java
stream.sorted()                            // natural order (asc)
stream.sorted(Comparator.reverseOrder())   // descending
stream.sorted(Comparator.comparingInt(String::length)) // by length

stream.distinct()  // remove duplicates (uses .equals())

stream.limit(3)    // take first 3 only
stream.skip(2)     // discard first 2, keep the rest
```

**Pagination pattern** (skip + limit):
```java
int pageSize = 10;
int page = 2;
stream.skip(page * pageSize).limit(pageSize)
```

**Top-N pattern** (sorted + limit):
```java
stream.sorted(Comparator.reverseOrder()).limit(3) // top 3 biggest
```

---

### Impl08 — reduce(): Collapse to a Single Value

**Concept:** Aggregate all elements into one result.

```java
// Sum (identity = 0, neutral for addition)
Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum); // 15

// Product (identity = 1, neutral for multiplication)
Stream.of(1, 2, 3, 4, 5).reduce(1, (a, b) -> a * b); // 120

// Longest string (no identity → Optional)
Optional<String> longest = Stream.of("fig", "banana", "kiwi")
    .reduce((a, b) -> a.length() >= b.length() ? a : b);
```

**Two signatures:**
- `reduce(identity, accumulator)` → always returns `T` (identity returned on empty stream)
- `reduce(accumulator)` → returns `Optional<T>` (empty optional on empty stream)

**When to use `reduce` vs `collect`:**
- `reduce` → you need a single **value** (sum, max, concatenated string)
- `collect` → you need a **collection** (List, Set, Map)

---

### Impl09 — flatMap(): Flatten Nested Structures

**Concept:** Each element produces multiple elements; flatten them into one stream.

```java
// List of Lists → flat stream
List<List<Integer>> matrix = List.of(List.of(1,2), List.of(3,4), List.of(5,6));
matrix.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList()); // [1, 2, 3, 4, 5, 6]

// Sentences → individual words
List<String> sentences = List.of("hello world", "foo bar");
sentences.stream()
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .collect(Collectors.toList()); // [hello, world, foo, bar]
```

**map vs flatMap:**
- `map(row -> row)` — each list stays a list → `Stream<List<T>>`
- `flatMap(List::stream)` — each list is expanded → `Stream<T>` (all elements merged)

---

### Impl10 — Real-World Pipelines (Everything Combined)

**Concept:** Multiple operations chained on a `List<Product>` dataset.

Examples:
```java
// Total price of all electronics
catalog.stream()
    .filter(p -> p.category.equals("Electronics"))
    .mapToDouble(p -> p.price)
    .sum();

// Average price per category
catalog.stream()
    .collect(Collectors.groupingBy(p -> p.category,
             Collectors.averagingDouble(p -> p.price)));

// Top 3 cheapest product names
catalog.stream()
    .sorted(Comparator.comparingDouble(p -> p.price))
    .limit(3)
    .map(p -> p.name)
    .collect(Collectors.toList());
```

**Terminal boolean checks:**
```java
.anyMatch(predicate)  // at least one element matches
.allMatch(predicate)  // every element matches
.noneMatch(predicate) // no element matches
```

---

## Operations Quick Reference

| Operation      | Type         | What it does                        |
|----------------|--------------|-------------------------------------|
| `filter(pred)` | Intermediate | Keep elements where pred = true     |
| `map(fn)`      | Intermediate | Transform each element              |
| `mapToInt(fn)` | Intermediate | Transform to IntStream (no boxing)  |
| `flatMap(fn)`  | Intermediate | Expand + flatten 1-to-many          |
| `sorted()`     | Intermediate | Sort in natural order               |
| `distinct()`   | Intermediate | Remove duplicates                   |
| `limit(n)`     | Intermediate | Keep first n elements               |
| `skip(n)`      | Intermediate | Discard first n elements            |
| `forEach(fn)`  | Terminal     | Consume each element (side effect)  |
| `collect(c)`   | Terminal     | Gather into List/Set/Map            |
| `count()`      | Terminal     | Return number of elements           |
| `reduce(fn)`   | Terminal     | Aggregate to single value           |
| `min/max(cmp)` | Terminal     | Smallest/largest element            |
| `anyMatch(p)`  | Terminal     | True if at least one matches        |
| `allMatch(p)`  | Terminal     | True if all match                   |
| `noneMatch(p)` | Terminal     | True if none match                  |
| `sum()`        | Terminal     | IntStream/DoubleStream total        |
| `average()`    | Terminal     | IntStream/DoubleStream mean         |

---

## Tests

77 tests covering:
- Happy paths for all operations
- Empty stream behavior
- Single-element streams
- Boundary values for `limit`/`skip`
- `Optional.empty()` vs `Optional.of()`
- `UnsupportedOperationException` on unmodifiable lists
- Stream reuse throwing `IllegalStateException`
- Parameterized tests for `filter` conditions

```bash
cd poc-04-streams
mvn test
```

---

## What to Practice Next

1. Rewrite each Impl from memory without looking. Do this 10 times.
2. Explore what happens internally: add `System.out.println` inside `filter` and `map` lambdas to observe element-by-element processing order.
3. Try to break streams intentionally: reuse a consumed stream, apply `reduce` with the wrong identity, call `.average()` on an empty array.
4. Read `java.util.stream.Stream` source code. Look at how `filter()` wraps the pipeline as a `StatelessOp`.
5. Build a pipeline that processes a CSV file line by line using `Files.lines(Path)`.
