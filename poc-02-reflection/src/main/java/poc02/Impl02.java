package poc02;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Impl02 — READ: Inspecting fields.
 *
 * Key distinction you MUST understand:
 *
 *   getFields()         → public fields ONLY, includes inherited ones.
 *   getDeclaredFields() → ALL fields declared directly in THIS class
 *                         (private, protected, public, package-private),
 *                         but NOT inherited fields.
 *
 * Rule of thumb: always use getDeclaredFields() when exploring a class.
 * Use getFields() only when you specifically want the public API surface.
 *
 * Each field is represented by a java.lang.reflect.Field object.
 * From Field you can read: name, type, modifiers, annotations.
 *
 * Write this from memory after reading it once.
 */
public class Impl02 {

    public static void main(String[] args) {

        Class<?> cls = Person.class;

        // ── getDeclaredFields() — all fields, this class only ─────────────────
        System.out.println("=== getDeclaredFields() ===");
        Field[] allFields = cls.getDeclaredFields();

        for (Field field : allFields) {

            // getName() — the variable name as written in source code
            String name = field.getName();

            // getType() — returns a Class<?> representing the field's type
            // e.g. for "private String name" → class java.lang.String
            Class<?> type = field.getType();

            // getModifiers() returns an int bitmask encoding all modifiers
            // (public, private, protected, static, final, etc.)
            // Modifier.toString() converts that bitmask to a readable string.
            int    modBits = field.getModifiers();
            String modStr  = Modifier.toString(modBits);

            System.out.printf("  %-20s type=%-25s modifiers=[%s]%n",
                    name, type.getSimpleName(), modStr);
        }
        // Expected output (order may vary):
        //   name                 type=String                  modifiers=[private]
        //   age                  type=int                     modifiers=[private]
        //   id                   type=String                  modifiers=[private final]
        //   instanceCount        type=int                     modifiers=[private static]


        // ── Modifier bitmask helpers ──────────────────────────────────────────
        // Instead of parsing the string, use Modifier's boolean methods.
        System.out.println("\n=== Modifier checks on each field ===");
        for (Field field : allFields) {
            int m = field.getModifiers();
            System.out.printf("  %-20s private=%-6b static=%-6b final=%b%n",
                    field.getName(),
                    Modifier.isPrivate(m),
                    Modifier.isStatic(m),
                    Modifier.isFinal(m));
        }
        // name                 private=true   static=false  final=false
        // age                  private=true   static=false  final=false
        // id                   private=true   static=false  final=true
        // instanceCount        private=true   static=true   final=false


        // ── getFields() — public fields only (none in Person) ────────────────
        System.out.println("\n=== getFields() (public only) ===");
        Field[] publicFields = cls.getFields();
        System.out.println("Count: " + publicFields.length);
        // Person has zero public fields, so this prints: Count: 0


        // ── Look up a SPECIFIC field by name ─────────────────────────────────
        // getDeclaredField(name) throws NoSuchFieldException if not found.
        // We'll handle exceptions properly in Impl09 — for now use a try-catch.
        System.out.println("\n=== getDeclaredField(\"name\") ===");
        try {
            Field nameField = cls.getDeclaredField("name");
            System.out.println("Found field: " + nameField);
            // prints: private java.lang.String poc02.Person.name
        } catch (NoSuchFieldException e) {
            System.out.println("Field not found: " + e.getMessage());
        }
    }
}
