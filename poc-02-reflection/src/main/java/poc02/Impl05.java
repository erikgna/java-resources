package poc02;

import java.lang.reflect.Method;

/**
 * Impl05 — INVOKE: Calling public methods reflectively.
 *
 * Method.invoke(instance, args...) is the core invocation call.
 *
 *   - First argument is the object to call the method on.
 *   - Remaining arguments are the method parameters.
 *   - For static methods, the first argument is NULL (no instance needed).
 *   - Return type is Object — you must cast the result yourself.
 *   - void methods return null.
 *
 * Key rule: if you can get the method via getMethod() (public, no setAccessible needed),
 * then invoke() works directly. Private methods need setAccessible(true) — see Impl06.
 *
 * Write this from memory after reading it once.
 */
public class Impl05 {

    public static void main(String[] args) throws Exception {

        Person person = new Person("Bob", 25);
        Class<?> cls  = Person.class;

        // ── Invoke no-arg public method ───────────────────────────────────────
        // getDeclaredMethod("greet") — no params, so nothing after the name.
        // invoke(person) — call it on our person object.
        System.out.println("=== Invoke greet() ===");
        Method greet  = cls.getDeclaredMethod("greet");
        Object result = greet.invoke(person);
        System.out.println("Result: " + result);
        // prints: Hi, I'm Bob and I'm 25 years old.


        // ── Invoke method with a parameter ────────────────────────────────────
        // getDeclaredMethod("greetWith", String.class) — the second arg is the param type.
        // invoke(person, "Hello") — pass the actual argument value.
        System.out.println("\n=== Invoke greetWith(String) ===");
        Method greetWith = cls.getDeclaredMethod("greetWith", String.class);
        Object result2   = greetWith.invoke(person, "Hello");
        System.out.println("Result: " + result2);
        // prints: Hello, I'm Bob!


        // ── Invoke a STATIC method ────────────────────────────────────────────
        // Static methods belong to the class, not an instance.
        // Pass null as the first argument to invoke() — no instance needed.
        System.out.println("\n=== Invoke static getInstanceCount() ===");
        Method countMethod = cls.getDeclaredMethod("getInstanceCount");
        Object count       = countMethod.invoke(null); // null = no instance
        System.out.println("Instance count: " + count);
        // prints the number of Person objects created so far


        // ── Invoke multiple methods via a loop ────────────────────────────────
        // This pattern is common in frameworks: you have a list of method names
        // and you invoke each one dynamically.
        System.out.println("\n=== Invoke getters in a loop ===");
        String[] getterNames = {"getName", "getAge", "getId"};

        for (String getterName : getterNames) {
            Method getter = cls.getDeclaredMethod(getterName);
            Object value  = getter.invoke(person);
            System.out.println(getterName + "() = " + value);
        }
        // getName() = Bob
        // getAge()  = 25
        // getId()   = P-<number>


        // ── What invoke() returns for void methods ────────────────────────────
        // invoke() always returns Object. For void methods it returns null.
        // There is no void type — getDeclaredMethod() still works normally.
        // Person has no public void method, but we can check the declared return type.
        System.out.println("\n=== Return type of void method ===");
        // We use "explode" as an example of a void-returning method.
        // We won't actually CALL it here (it throws) — just inspect its return type.
        Method explode = cls.getDeclaredMethod("explode");
        System.out.println("Return type of explode(): " + explode.getReturnType());
        // prints: void
        System.out.println("Is void: " + (explode.getReturnType() == void.class));
        // prints: true
    }
}
