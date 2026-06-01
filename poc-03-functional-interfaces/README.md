# POC 03 — Functional Interfaces

## What is a Functional Interface?

A **functional interface** is a Java interface with **exactly one abstract method**.

That single method defines the "shape" of the function — what goes in, what comes out.

```java
@FunctionalInterface
interface MathOperation {
    int operate(int a, int b);  // ONE abstract method
}
```

The `@FunctionalInterface` annotation is optional, but recommended. It tells the compiler
to enforce the one-abstract-method rule. If you accidentally add a second abstract method,
you get a compile error immediately.

---

## Why Do Functional Interfaces Exist?

Java 8 (2014) introduced **lambdas**. A lambda is shorthand for implementing a functional
interface. Without functional interfaces, lambdas have no type — they need one to attach to.

```java
// OLD WAY: anonymous class (lots of boilerplate)
MathOperation add = new MathOperation() {
    @Override
    public int operate(int a, int b) {
        return a + b;
    }
};

// NEW WAY: lambda (same thing, shorter)
MathOperation add = (a, b) -> a + b;
```

The compiler sees that `MathOperation` needs an `operate(int, int)` method,
so it wires the lambda `(a, b) -> a + b` into that method automatically.

---

## The Four Built-in Functional Interfaces

Java ships with many functional interfaces in `java.util.function`. These four are the core:

| Interface        | Method              | In → Out      | Use case                          |
|-----------------|---------------------|---------------|-----------------------------------|
| `Function<T,R>` | `R apply(T t)`      | T → R         | Transform one type into another   |
| `Predicate<T>`  | `boolean test(T t)` | T → boolean   | Test a condition (filter)         |
| `Consumer<T>`   | `void accept(T t)`  | T → nothing   | Side effects (print, save, log)   |
| `Supplier<T>`   | `T get()`           | nothing → T   | Produce a value (factory, lazy)   |

---

## Implementations Overview

### Impl01 — Custom `@FunctionalInterface`
Define your own functional interface. Understand what `@FunctionalInterface` does,
how lambdas replace anonymous classes, and how `default`/`static` methods are allowed.

**Key idea:** A custom interface gives you full control over the method signature.

---

### Impl02 — `Function<T, R>`
The workhorse. Takes one input, returns one output (different types allowed).

```java
Function<String, Integer> length = s -> s.length();
length.apply("hello"); // 5
```

Composition via `andThen` (left → right) and `compose` (right → left).

---

### Impl03 — `Predicate<T>`
A yes/no question about a value. Returns `boolean`.

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
isEven.test(4); // true
```

Combine with `and()`, `or()`, `negate()` to build complex conditions without nested if statements.

---

### Impl04 — `Consumer<T>` and `BiConsumer<T,U>`
Does something with a value, returns nothing. Pure side effects.

```java
Consumer<String> print = System.out::println;
print.accept("hello"); // prints "hello"
```

Chain multiple consumers with `andThen()`. `BiConsumer` takes two inputs.

---

### Impl05 — `Supplier<T>`
Produces a value on demand. Takes no input.

```java
Supplier<List<String>> lazy = () -> expensiveComputation();
// Nothing runs yet — only when you call:
lazy.get(); // NOW it runs
```

Key concept: **lazy evaluation**. The lambda body only executes when `get()` is called.
Useful for deferred/expensive work, factory patterns, and default values.

---

### Impl06 — `BiFunction`, `UnaryOperator`, `BinaryOperator`
Variations for common patterns:

| Interface            | Inputs | Output  | Example                              |
|---------------------|--------|---------|--------------------------------------|
| `BiFunction<T,U,R>` | T, U   | R       | `(String, Integer) → String`        |
| `UnaryOperator<T>`  | T      | T       | `String → String` (trim, uppercase) |
| `BinaryOperator<T>` | T, T   | T       | `(int, int) → int` (add, max)       |

`UnaryOperator<T>` extends `Function<T,T>`.
`BinaryOperator<T>` extends `BiFunction<T,T,T>`.

---

### Impl07 — Function Composition
Build pipelines by combining functions step by step.

```java
Function<String, String> pipeline =
    ((Function<String, String>) String::trim)
    .andThen(String::toUpperCase)
    .andThen(s -> s + "!");

pipeline.apply("  hello  "); // "HELLO!"
```

- `f.andThen(g)` — apply `f` first, then `g` (left to right)
- `f.compose(g)` — apply `g` first, then `f` (right to left)
- `f.andThen(g)` is the same as `g.compose(f)`

Composition lets you build reusable pipeline stages instead of nesting method calls.

---

### Impl08 — Method References

Shorthand for lambdas that just call one method. Four kinds:

| Kind                          | Syntax                   | Lambda equivalent              |
|------------------------------|--------------------------|--------------------------------|
| Static method                 | `ClassName::staticMethod`| `(args) -> Class.method(args)` |
| Particular instance           | `instance::method`       | `(args) -> obj.method(args)`  |
| Arbitrary instance (type ref) | `ClassName::method`      | `obj -> obj.method()`         |
| Constructor                   | `ClassName::new`         | `(args) -> new Class(args)`   |

```java
Function<String, Integer> parse = Integer::parseInt;      // static
Function<String, String>  lower = String::toLowerCase;    // arbitrary instance
Function<String, Person>  make  = Person::new;            // constructor
```

---

### Impl09 — Custom Generic FI + Checked Exceptions

**The problem:** Java's `Function<T,R>` cannot throw checked exceptions.
The compiler rejects any lambda that calls a method declaring `throws SomeCheckedException`.

**The solution:** Define a custom `ThrowingFunction<T,R>` whose method declares `throws Exception`,
then wrap it into a normal `Function` using a helper that catches and re-throws as `RuntimeException`.

```java
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;  // checked exception declared here
}

// Wrap it for use anywhere a normal Function is expected
static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> fn) {
    return t -> {
        try {
            return fn.apply(t);
        } catch (Exception e) {
            throw new RuntimeException("Wrapped", e);
        }
    };
}
```

This pattern is used in production Java code whenever lambdas need to call checked APIs
(file I/O, JDBC, JSON parsing, network calls).

---

### Impl10 — Real-World Data Pipeline

Puts everything together. Filters, transforms, and consumes a list of domain objects
using composed functional interfaces.

```
data source (Supplier)
    → filter (Predicate)
    → transform (Function)
    → consume (Consumer)
    → collect results
```

This pattern is exactly what Java Streams do internally. Understanding it here
makes the Stream API much easier to grasp when you get to it.

---

## Mental Models

**Function** = a pipe: something goes in, something comes out.
**Predicate** = a gate: something goes in, yes/no comes out.
**Consumer** = a drain: something goes in, nothing comes out (but side effects happen).
**Supplier** = a tap: nothing goes in, something comes out.

---

## What's Allowed Inside a `@FunctionalInterface`

| Allowed?  | What                                              |
|-----------|--------------------------------------------------|
| Required  | Exactly ONE abstract method                       |
| Allowed   | Any number of `default` methods (they have bodies)|
| Allowed   | Any number of `static` methods (they have bodies) |
| Allowed   | Methods overriding `java.lang.Object` methods     |
| Forbidden | Two or more abstract methods                      |

---

## Key Things That Trip Up Beginners

### 1. Lambdas can't modify local variables from the enclosing scope
```java
int count = 0;
Consumer<String> c = s -> count++; // COMPILE ERROR — count must be effectively final
```
Workaround: use a one-element array (`int[] count = {0};`), or use a mutable object.

### 2. `compose` is backwards from what you might expect
`f.compose(g)` applies `g` FIRST, then `f`. The name "compose" comes from math: `f ∘ g`.
When in doubt, use `andThen` — it reads left to right, which is more natural.

### 3. Method reference vs lambda — same runtime behavior
`String::toUpperCase` and `s -> s.toUpperCase()` compile to the same thing.
Use method references when they're clearer; lambdas when the logic is more than a single call.

### 4. `@FunctionalInterface` is optional but always use it on your own interfaces
Without it, adding a second abstract method silently breaks all callers at runtime. The annotation
makes the compiler catch the mistake at the definition site instead.

---

## Files

```
src/
  main/java/poc03/
    Impl01.java   — Custom @FunctionalInterface, lambda basics
    Impl02.java   — Function<T, R>
    Impl03.java   — Predicate<T>
    Impl04.java   — Consumer<T>, BiConsumer<T,U>
    Impl05.java   — Supplier<T>, lazy evaluation
    Impl06.java   — BiFunction, UnaryOperator, BinaryOperator
    Impl07.java   — Function composition (andThen / compose)
    Impl08.java   — Method references (all 4 kinds)
    Impl09.java   — ThrowingFunction, checked exception wrapper
    Impl10.java   — Real-world pipeline (synthesis)
  test/java/poc03/
    FunctionalInterfacesTest.java   — 47 tests covering all implementations
```

## Run

```bash
mvn test           # run all 47 tests
mvn compile        # compile only
```
