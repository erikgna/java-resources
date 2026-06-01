package poc02;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Impl10 — SYNTHESIS: A full class inspector.
 *
 * This combines everything from Impl01–Impl09 into one utility that prints
 * a complete picture of any class: metadata, fields, constructors, methods.
 * Similar to what the `javap -private` command-line tool does.
 *
 * After writing this, you should be able to inspect ANY class — not just Person.
 * Try changing the argument in main() to String.class or Integer.class and
 * read what comes out. You will learn a lot about JDK internals.
 *
 * This is the capstone. Write from memory. No reference.
 */
public class Impl10 {

    public static void main(String[] args) throws Exception {
        // Inspect Person. After you understand it, try: String.class, Integer.class
        inspect(Person.class);
    }

    /**
     * Prints everything declared in the given class.
     * Does NOT descend into superclasses (that would be a further extension).
     */
    static void inspect(Class<?> cls) throws Exception {

        // ── 1. Class identity ─────────────────────────────────────────────────
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  CLASS INSPECTOR                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("Full name    : " + cls.getName());
        System.out.println("Simple name  : " + cls.getSimpleName());
        System.out.println("Package      : " + cls.getPackageName());
        System.out.println("Superclass   : " + (cls.getSuperclass() != null
                ? cls.getSuperclass().getName() : "(none — this is Object)"));
        System.out.println("Is interface : " + cls.isInterface());
        System.out.println("Is enum      : " + cls.isEnum());

        Class<?>[] ifaces = cls.getInterfaces();
        if (ifaces.length > 0) {
            System.out.print("Implements   : ");
            for (int i = 0; i < ifaces.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(ifaces[i].getSimpleName());
            }
            System.out.println();
        }


        // ── 2. Fields ─────────────────────────────────────────────────────────
        System.out.println("\n--- FIELDS ---");
        Field[] fields = cls.getDeclaredFields();
        if (fields.length == 0) {
            System.out.println("  (none)");
        }
        for (Field f : fields) {
            System.out.printf("  %-10s %-10s %s%n",
                    Modifier.toString(f.getModifiers()),
                    f.getType().getSimpleName(),
                    f.getName());
        }


        // ── 3. Constructors ───────────────────────────────────────────────────
        System.out.println("\n--- CONSTRUCTORS ---");
        Constructor<?>[] ctors = cls.getDeclaredConstructors();
        for (Constructor<?> c : ctors) {
            System.out.printf("  %-10s %s(%s)%n",
                    Modifier.toString(c.getModifiers()),
                    cls.getSimpleName(),
                    paramList(c.getParameterTypes()));
        }


        // ── 4. Methods ────────────────────────────────────────────────────────
        System.out.println("\n--- METHODS ---");
        Method[] methods = cls.getDeclaredMethods();
        for (Method m : methods) {
            System.out.printf("  %-20s %-10s %s(%s)%n",
                    Modifier.toString(m.getModifiers()),
                    m.getReturnType().getSimpleName(),
                    m.getName(),
                    paramList(m.getParameterTypes()));
        }


        // ── 5. Live field values from a fresh instance ─────────────────────────
        // Create a Person via reflection and read all its field values.
        // This shows the "read" part end-to-end.
        System.out.println("\n--- LIVE FIELD VALUES (new Person(\"Zara\", 22)) ---");
        try {
            Constructor<?> ctor = cls.getDeclaredConstructor(String.class, int.class);
            Object instance = ctor.newInstance("Zara", 22);

            for (Field f : fields) {
                f.setAccessible(true);
                Object value = f.get(instance); // null for static fields if no instance needed
                System.out.printf("  %-20s = %s%n", f.getName(), value);
            }
        } catch (NoSuchMethodException e) {
            // Class has no (String, int) constructor — skip live values
            System.out.println("  (skipped — no (String, int) constructor found)");
        }

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║  END                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // Helper: build a comma-separated parameter type list string.
    private static String paramList(Class<?>[] types) {
        if (types.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(types[i].getSimpleName());
        }
        return sb.toString();
    }
}
