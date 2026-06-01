package poc02;

import java.lang.reflect.Field;

/**
 * Impl08 — MODIFY: Writing to FINAL fields.
 *
 * The `final` keyword tells Java: "this field can only be assigned once,
 * in the constructor or at declaration." Normal code respects that.
 *
 * On Java 11: setAccessible(true) is enough. field.set() works even on final fields.
 * This is possible because final is enforced at COMPILE time and by the verifier —
 * the JVM does not stop a reflective write at runtime (on Java 11).
 *
 * IMPORTANT WARNING:
 *   - The JIT compiler (JVM's optimizer) may have INLINED the original value.
 *   - "Inlining" means: wherever you wrote person.getId(), the compiler replaced
 *     it with the literal value "P-1" in the compiled bytecode — no field read at all.
 *   - After a reflective write to the final field, code that went through the normal
 *     getter may still see the old value if it was inlined.
 *   - Reflective reads (field.get()) will see the new value.
 *   - This is the most dangerous behavior in this entire POC.
 *
 * On Java 17+: this behavior changed. JEP 416 restricted writes to final fields
 * in JDK classes. Writing to final fields in your OWN classes still works on Java 17
 * with setAccessible, but you may see warnings.
 *
 * Write this from memory after reading it once.
 */
public class Impl08 {

    public static void main(String[] args) throws Exception {

        Person person = new Person("Eve", 28);
        Class<?> cls  = Person.class;

        System.out.println("Before modification:");
        System.out.println("  person.getId()  = " + person.getId());
        // e.g. P-1


        // ── Modify the final 'id' field ───────────────────────────────────────
        Field idField = cls.getDeclaredField("id");
        idField.setAccessible(true); // unlocks both read and write

        // Read the current final value first
        System.out.println("  field.get()     = " + idField.get(person));

        // Write a new value — this should NOT be possible in normal Java code.
        idField.set(person, "HACKED-ID");

        System.out.println("\nAfter field.set(\"HACKED-ID\"):");

        // Read via reflection — will show the new value (reflection bypasses inlining)
        System.out.println("  field.get()     = " + idField.get(person));  // HACKED-ID

        // Read via normal getter — MAY or MAY NOT show new value depending on JIT
        System.out.println("  person.getId()  = " + person.getId());
        // On Java 11 with simple code: usually shows HACKED-ID.
        // In heavily optimized code (server JVM, repeated calls), may still show old value.

        // Read via toString() — calls getId() internally
        System.out.println("  toString()      = " + person);


        // ── Why this is dangerous ─────────────────────────────────────────────
        // If you write a serialization library or a test utility that modifies final fields,
        // you create a hidden inconsistency: reflective reads say one thing,
        // JIT-optimized calls say another. This causes nightmarish bugs.
        //
        // Rule: NEVER use this technique in production code.
        // Legitimate use: test utilities that need to inject mock IDs or reset singletons.
        // Even then — redesign the class to not use final if it needs to be test-injectable.
        System.out.println("\n--- Lesson ---");
        System.out.println("final = compile-time + verifier enforcement, NOT JVM runtime lock.");
        System.out.println("Reflection on Java 11 bypasses it. Java 17+ tightens this for JDK classes.");
        System.out.println("Never do this in production. It creates inlining inconsistencies.");
    }
}
