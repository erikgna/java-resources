package poc02;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Impl09 — ERROR CONDITIONS: What goes wrong and why.
 *
 * These are the five exceptions you WILL encounter when using reflection.
 * Understanding what each one means is mandatory — don't just catch Exception.
 *
 * 1. ClassNotFoundException
 *    — Class.forName("bad.Name") — the class doesn't exist on the classpath.
 *
 * 2. NoSuchMethodException
 *    — getDeclaredMethod("wrongName") — method doesn't exist, or wrong param types.
 *
 * 3. NoSuchFieldException
 *    — getDeclaredField("wrongName") — field doesn't exist.
 *
 * 4. IllegalAccessException
 *    — invoke() or get/set() on a private member WITHOUT setAccessible(true).
 *
 * 5. InvocationTargetException
 *    — the INVOKED method threw an exception.
 *    — reflection wraps the real exception — call getCause() to unwrap it.
 *
 * Write this from memory after reading it once.
 */
public class Impl09 {

    public static void main(String[] args) {

        // ── 1. ClassNotFoundException ─────────────────────────────────────────
        System.out.println("=== 1. ClassNotFoundException ===");
        try {
            // Typo in class name → class not found on classpath
            Class<?> cls = Class.forName("poc02.Preson"); // "Preson" not "Person"
            System.out.println("Should not reach: " + cls);
        } catch (ClassNotFoundException e) {
            System.out.println("Caught: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            // poc02.Preson
        }


        // ── 2. NoSuchMethodException ──────────────────────────────────────────
        System.out.println("\n=== 2. NoSuchMethodException ===");

        // Case A: wrong method name
        try {
            Person.class.getDeclaredMethod("greetzzz"); // doesn't exist
        } catch (NoSuchMethodException e) {
            System.out.println("Wrong name: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }

        // Case B: correct name but wrong parameter types
        // "greetWith" takes a String, but we pass int.class here
        try {
            Person.class.getDeclaredMethod("greetWith", int.class); // wrong type
        } catch (NoSuchMethodException e) {
            System.out.println("Wrong params: " + e.getClass().getSimpleName() + " — " + e.getMessage());
            // Message shows the full signature that was searched for
        }


        // ── 3. NoSuchFieldException ───────────────────────────────────────────
        System.out.println("\n=== 3. NoSuchFieldException ===");
        try {
            Person.class.getDeclaredField("lastName"); // doesn't exist
        } catch (NoSuchFieldException e) {
            System.out.println("Caught: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }


        // ── 4. IllegalAccessException ─────────────────────────────────────────
        System.out.println("\n=== 4. IllegalAccessException ===");
        Person person = new Person("Frank", 50);

        // A: accessing a private method without setAccessible
        try {
            Method secret = Person.class.getDeclaredMethod("secret");
            // NOT calling setAccessible(true) here
            secret.invoke(person);
        } catch (NoSuchMethodException e) {
            System.out.println("Unexpected: " + e);
        } catch (IllegalAccessException e) {
            System.out.println("Method invoke without access: " + e.getClass().getSimpleName());
        } catch (InvocationTargetException e) {
            System.out.println("Unexpected invocation error: " + e.getCause());
        }

        // B: accessing a private field without setAccessible
        try {
            Field nameField = Person.class.getDeclaredField("name");
            // NOT calling setAccessible(true) here
            nameField.get(person);
        } catch (NoSuchFieldException e) {
            System.out.println("Unexpected: " + e);
        } catch (IllegalAccessException e) {
            System.out.println("Field get without access: " + e.getClass().getSimpleName());
        }


        // ── 5. InvocationTargetException ─────────────────────────────────────
        System.out.println("\n=== 5. InvocationTargetException ===");
        // This is the sneakiest one. It happens when the INVOKED method throws.
        // Reflection catches that throw and rewraps it.
        // You MUST call getCause() to see the real exception.
        try {
            Method explode = Person.class.getDeclaredMethod("explode");
            explode.setAccessible(true);
            explode.invoke(person); // person.explode() will throw RuntimeException
        } catch (NoSuchMethodException | IllegalAccessException e) {
            System.out.println("Unexpected setup error: " + e);
        } catch (InvocationTargetException e) {
            System.out.println("Wrapper type : " + e.getClass().getSimpleName());

            // getCause() is the real exception thrown by the method
            Throwable realException = e.getCause();
            System.out.println("Real cause   : " + realException.getClass().getSimpleName());
            System.out.println("Real message : " + realException.getMessage());
            // RuntimeException — This method always blows up intentionally.
        }

        System.out.println("\n--- Summary ---");
        System.out.println("ClassNotFoundException     : bad class name string");
        System.out.println("NoSuchMethodException      : method/constructor not found (name or params wrong)");
        System.out.println("NoSuchFieldException       : field not found");
        System.out.println("IllegalAccessException     : private without setAccessible(true)");
        System.out.println("InvocationTargetException  : the called method itself threw — check getCause()");
    }
}
