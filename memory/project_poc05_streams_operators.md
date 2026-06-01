---
name: project-poc05-streams-operators
description: POC 05 Streams Operators complete — Supplier, Consumer, BinaryOperator, UnaryOperator, 10 impls, 67 tests
metadata:
  type: project
---

POC 05 complete: Supplier, Consumer, BiConsumer, UnaryOperator, BinaryOperator. 10 impl files, 67 tests, 0 failures. Java 11, package poc05, Maven project at poc-05-streams-operators/.

**Why:** Next item in java-resources.md roadmap after POC 04 (Streams: Lists/Arrays/Map/Filter/Predicates).

**How to apply:** Follow-on POC should continue the roadmap — next is Concurrency: ExecutorService.

Impl coverage:
- Impl01: Supplier basics, lazy init, factory
- Impl02: Supplier + Stream.generate(), stateful suppliers, int[] trick
- Impl03: Consumer basics, forEach, andThen chaining
- Impl04: BiConsumer, Map.forEach, andThen
- Impl05: UnaryOperator basics, identity(), List.replaceAll, andThen/compose
- Impl06: UnaryOperator + Stream.iterate(), Fibonacci, Java 9 bounded form
- Impl07: BinaryOperator basics, minBy/maxBy, method refs
- Impl08: BinaryOperator + Stream.reduce() — both forms (with/without identity), Optional
- Impl09: Composition — andThen/compose, Consumer chains
- Impl10: Integrated ETL pipeline — all four interfaces together
