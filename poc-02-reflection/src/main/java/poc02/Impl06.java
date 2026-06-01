package poc02;

import java.lang.reflect.Method;

/**
 * Impl06 — INVOKE: Calling PRIVATE methods reflectively.
 *
 * This is the power move of the Reflection API.
 * Private methods exist for encapsulation — they are internal implementation details.
 * Normal code respects that boundary. Reflection breaks it intentionally.
 *
 * The unlock sequence is always:
 *   1. getDeclaredMethod(name, paramTypes...)
 *   2. method.setAccessible(true)         ← the lock-pick
 *   3. method.invoke(instance, args...)
 *
 * setAccessible(true) suppresses Java's access control checks FOR THIS METHOD OBJECT.
 * It does NOT modify the class itself — the method is still marked private in bytecode.
 * It just tells this particular Method object: "skip the check when invoked."
 *
 * Real-world use: testing frameworks (like JUnit) use this to call private methods
 * in unit tests. ORM frameworks use it to set private fields. Serialization uses it.
 *
 * Write this from memory after reading it once.
 */
public class Impl06 {

    public static void main(String[] args) throws Exception {

        Person person = new Person("Carol", 40);
        Class<?> cls  = Person.class;


        // ── Step 1: Try calling private method WITHOUT setAccessible ─────────
        // getDeclaredMethod works fine — it just finds the method.
        // But invoke() will throw IllegalAccessException without the unlock.
        System.out.println("=== Without setAccessible — expect failure ===");
        Method secret = cls.getDeclaredMethod("secret");
        // canAccess(instance) replaced isAccessible() in Java 9.
        // Pass the instance for non-static members; pass null for static members.
        System.out.println("canAccess before: " + secret.canAccess(person)); // false

        try {
            Object result = secret.invoke(person);
            System.out.println("Should not reach here: " + result);
        } catch (IllegalAccessException e) {
            System.out.println("Caught expected: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
        }


        // ── Step 2: Unlock with setAccessible(true) and call it ───────────────
        System.out.println("\n=== With setAccessible(true) ===");
        secret.setAccessible(true);
        System.out.println("canAccess after: " + secret.canAccess(person));   // true

        Object result = secret.invoke(person);
        System.out.println("Result: " + result);
        // prints: My secret ID is: P-<number>


        // ── Private method that takes a parameter ─────────────────────────────
        // The pattern is identical regardless of parameters.
        // getDeclaredMethod("explode") — this method takes no args but throws.
        // We'll intentionally invoke it to see what happens to the exception.
        // NOTE: InvocationTargetException is explored in detail in Impl09.
        System.out.println("\n=== Invoking private void method that throws ===");
        Method explode = cls.getDeclaredMethod("explode");
        explode.setAccessible(true);

        try {
            explode.invoke(person); // person.explode() throws RuntimeException
        } catch (java.lang.reflect.InvocationTargetException e) {
            // When the INVOKED method throws, reflection wraps it in InvocationTargetException.
            // To get the REAL exception, call getCause().
            Throwable realCause = e.getCause();
            System.out.println("Wrapped in: " + e.getClass().getSimpleName());
            System.out.println("Real cause: " + realCause.getClass().getSimpleName()
                    + " — " + realCause.getMessage());
            // Real cause: RuntimeException — This method always blows up intentionally.
        }


        // ── IMPORTANT: setAccessible does NOT modify the class ────────────────
        // The Method object "secret" is now accessible, but the class itself is unchanged.
        // A NEW getDeclaredMethod("secret") call returns a fresh Method object
        // with accessibility set back to false.
        System.out.println("\n=== setAccessible is per Method object, not per class ===");
        Method freshSecret = cls.getDeclaredMethod("secret");
        System.out.println("Fresh method canAccess: " + freshSecret.canAccess(person)); // false
        System.out.println("Original method canAccess: " + secret.canAccess(person));   // true
    }
}
