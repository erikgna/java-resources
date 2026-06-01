package poc02;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Impl04 — READ: Inspecting constructors + creating instances via reflection.
 *
 * A Constructor is similar to a Method, but:
 *   - It has no return type (it always returns an instance of the class).
 *   - It cannot be static, abstract, or final.
 *   - You call newInstance() on it to create a new object.
 *
 * getDeclaredConstructors() → all constructors (any access level) in THIS class.
 * getConstructors()         → public constructors only.
 *
 * The most important use: calling a PRIVATE constructor from outside the class.
 * Normal Java code cannot do this. Reflection can, after setAccessible(true).
 *
 * Write this from memory after reading it once.
 */
public class Impl04 {

    public static void main(String[] args) throws Exception {

        Class<?> cls = Person.class;

        // ── List all constructors ─────────────────────────────────────────────
        System.out.println("=== getDeclaredConstructors() ===");
        Constructor<?>[] constructors = cls.getDeclaredConstructors();

        for (Constructor<?> ctor : constructors) {
            Class<?>[] paramTypes = ctor.getParameterTypes();
            String     modStr     = Modifier.toString(ctor.getModifiers());

            StringBuilder params = new StringBuilder("(");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) params.append(", ");
                params.append(paramTypes[i].getSimpleName());
            }
            params.append(")");

            System.out.printf("  [%-10s] %s%s%n", modStr, cls.getSimpleName(), params);
        }
        // Expected:
        //   [public    ] Person(String, int)
        //   [private   ] Person()


        // ── Create instance via PUBLIC constructor ────────────────────────────
        // getDeclaredConstructor(paramTypes...) throws NoSuchMethodException if not found.
        // newInstance(args...) invokes the constructor and returns the new object.
        System.out.println("\n=== Creating instance via public constructor ===");

        // Get the constructor that takes (String, int)
        Constructor<?> publicCtor = cls.getDeclaredConstructor(String.class, int.class);
        // newInstance() creates the object. Returns Object, so we cast.
        Person p1 = (Person) publicCtor.newInstance("Alice", 30);
        System.out.println("Created: " + p1);
        // prints: Person{name='Alice', age=30, id='P-1'}


        // ── Create instance via PRIVATE constructor ───────────────────────────
        // getDeclaredConstructor() with no args gets the no-arg constructor.
        // Without setAccessible(true), newInstance() throws IllegalAccessException.
        System.out.println("\n=== Creating instance via private constructor ===");

        Constructor<?> privateCtor = cls.getDeclaredConstructor(); // no-arg

        // This line is the key: it tells the JVM to bypass the access check.
        // After this call, even private constructors/methods/fields can be invoked.
        privateCtor.setAccessible(true);

        Person p2 = (Person) privateCtor.newInstance(); // no args needed
        System.out.println("Created via private ctor: " + p2);
        // prints: Person{name='Unknown', age=0, id='P-ANON'}


        // ── What happens WITHOUT setAccessible(true) ─────────────────────────
        // Let's prove it fails if we don't call setAccessible first.
        System.out.println("\n=== Without setAccessible — expect IllegalAccessException ===");
        Constructor<?> blockedCtor = cls.getDeclaredConstructor();
        // NOTE: do NOT call setAccessible here
        try {
            Person p3 = (Person) blockedCtor.newInstance();
            System.out.println("Should not reach here: " + p3);
        } catch (IllegalAccessException e) {
            // This is the expected error when trying to invoke private without permission.
            System.out.println("Caught: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
    }
}
