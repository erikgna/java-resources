package poc02;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Impl03 — READ: Inspecting methods.
 *
 * Same declared vs non-declared distinction as with fields:
 *
 *   getMethods()         → public methods ONLY, includes inherited (from Object too!).
 *   getDeclaredMethods() → ALL methods in THIS class only (all access levels),
 *                          NOT inherited ones.
 *
 * Each method is represented by java.lang.reflect.Method.
 * From Method you can read: name, return type, parameter types, modifiers, exceptions.
 *
 * Write this from memory after reading it once.
 */
public class Impl03 {

    public static void main(String[] args) {

        Class<?> cls = Person.class;

        // ── getDeclaredMethods() — all methods declared in Person ─────────────
        System.out.println("=== getDeclaredMethods() ===");
        Method[] allMethods = cls.getDeclaredMethods();

        for (Method method : allMethods) {

            // getName() — the method name
            String name = method.getName();

            // getReturnType() — what the method returns (Class<?> for the type)
            Class<?> returnType = method.getReturnType();

            // getParameterTypes() — array of Class<?>, one per parameter
            // Empty array [] if the method takes no arguments.
            Class<?>[] paramTypes = method.getParameterTypes();

            // getModifiers() — same bitmask as fields
            String modStr = Modifier.toString(method.getModifiers());

            // Build a readable parameter list string
            StringBuilder params = new StringBuilder("(");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) params.append(", ");
                params.append(paramTypes[i].getSimpleName());
            }
            params.append(")");

            System.out.printf("  [%-16s] %-20s %s → %s%n",
                    modStr, name, params, returnType.getSimpleName());
        }
        // Expected — each declared method in Person:
        //   greet(), greetWith(String), secret(), explode(), getInstanceCount(),
        //   getName(), getAge(), getId(), toString()


        // ── getMethods() — public + inherited (includes Object's methods) ─────
        System.out.println("\n=== getMethods() (public only, includes inherited) ===");
        Method[] publicMethods = cls.getMethods();
        System.out.println("Count: " + publicMethods.length);
        // Will be larger — includes: greet, greetWith, getInstanceCount,
        // getName, getAge, getId, toString, PLUS: equals, hashCode, wait, notify,
        // notifyAll, getClass (all inherited from Object).


        // ── Look up a SPECIFIC method by name + parameter types ───────────────
        // getMethod(name, paramTypes...)       → public methods only
        // getDeclaredMethod(name, paramTypes...) → any access level in this class
        //
        // IMPORTANT: you must pass the parameter types as Class<?> objects.
        // "greet" takes no params → pass nothing after the name.
        // "greetWith" takes a String → pass String.class.
        System.out.println("\n=== Looking up specific methods ===");

        try {
            // No-arg method
            Method greet = cls.getDeclaredMethod("greet");
            System.out.println("Found: " + greet);
            // prints: public java.lang.String poc02.Person.greet()

            // Method with one String parameter
            Method greetWith = cls.getDeclaredMethod("greetWith", String.class);
            System.out.println("Found: " + greetWith);
            // prints: public java.lang.String poc02.Person.greetWith(java.lang.String)

            // Private method
            Method secret = cls.getDeclaredMethod("secret");
            System.out.println("Found: " + secret);
            // prints: private java.lang.String poc02.Person.secret()

            // Static method
            Method countMethod = cls.getDeclaredMethod("getInstanceCount");
            System.out.println("Found: " + countMethod);
            System.out.println("Is static: " + Modifier.isStatic(countMethod.getModifiers())); // true

        } catch (NoSuchMethodException e) {
            System.out.println("Method not found: " + e.getMessage());
        }
    }
}
