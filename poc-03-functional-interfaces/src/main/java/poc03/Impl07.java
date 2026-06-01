package poc03;

import java.util.function.Function;

/**
 * IMPL 07 — Function Composition
 *
 * "Composition" means combining functions to build bigger functions.
 * Instead of nesting calls like f(g(x)), you build a new function
 * that does both steps in sequence.
 *
 * Function.andThen(after)
 *   - Apply THIS function first, then apply `after` to the result.
 *   - Order: this → after
 *   - Think: "do this, THEN do that"
 *
 * Function.compose(before)
 *   - Apply `before` first, then apply THIS function to the result.
 *   - Order: before → this
 *   - Think: "first do that, THEN apply this"
 *
 * andThen and compose are mirrors of each other:
 *   f.andThen(g)  is the same as  g.compose(f)
 *
 * Why compose instead of just calling methods inline?
 *   - You can build reusable pipelines.
 *   - You can pass the composed function to other methods.
 *   - Cleaner than deeply nested function calls.
 */
public class Impl07 {

    public static void main(String[] args) {

        Function<String, String> trim    = s -> s.trim();
        Function<String, String> upper   = s -> s.toUpperCase();
        Function<String, String> exclaim = s -> s + "!";

        // --- andThen: left to right ---
        // trim → upper → exclaim
        Function<String, String> pipeline = trim.andThen(upper).andThen(exclaim);
        System.out.println(pipeline.apply("  hello world  "));
        // "  hello world  " → "hello world" → "HELLO WORLD" → "HELLO WORLD!"

        // --- compose: right to left ---
        // exclaim.compose(upper).compose(trim)
        // means: first trim, then upper, then exclaim — same as above
        Function<String, String> sameViCompose = exclaim.compose(upper).compose(trim);
        System.out.println(sameViCompose.apply("  hello world  "));
        // same output: "HELLO WORLD!"

        // --- Numeric pipeline ---
        Function<Integer, Integer> times2  = n -> n * 2;
        Function<Integer, Integer> plus10  = n -> n + 10;
        Function<Integer, String>  toLabel = n -> "Value: " + n;

        // 5 → 10 → 20 → "Value: 20"
        Function<Integer, String> numPipeline = times2.andThen(plus10).andThen(toLabel);
        System.out.println(numPipeline.apply(5));   // Value: 20
        System.out.println(numPipeline.apply(0));   // Value: 10
        System.out.println(numPipeline.apply(-3));  // Value: 4

        // --- Building reusable pipeline stages ---
        // You can store partial pipelines and reuse them
        Function<String, String> normalize = trim.andThen(upper);

        System.out.println("\nReuse normalize:");
        System.out.println(normalize.apply("  java  "));    // JAVA
        System.out.println(normalize.apply("  spring  "));  // SPRING

        // Extend the pipeline later
        Function<String, String> normalizeAndExclaim = normalize.andThen(exclaim);
        System.out.println(normalizeAndExclaim.apply("  hello  ")); // HELLO!

        // --- Why not just nest?  ---
        // Instead of: exclaim.apply(upper.apply(trim.apply("  hi  ")))
        // You write:  pipeline.apply("  hi  ")
        // The composed form is more readable and reusable.
        String ugly   = exclaim.apply(upper.apply(trim.apply("  hi  ")));
        String clean  = pipeline.apply("  hi  ");
        System.out.println("\nnested vs composed:");
        System.out.println("ugly:  " + ugly);   // HI!
        System.out.println("clean: " + clean);  // HI!
    }
}
