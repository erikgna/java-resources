package poc02;

import java.lang.reflect.Field;

/**
 * Impl07 — MODIFY: Reading and writing field values.
 *
 * Field.get(instance)       → read the value of a field from an object.
 * Field.set(instance, value) → write a new value into a field of an object.
 *
 * For primitive fields (int, long, boolean, etc.) there are typed helpers:
 *   getInt(instance), setInt(instance, value)
 *   getLong, setLong, getBoolean, setBoolean, etc.
 * These avoid boxing overhead and make intent clearer.
 *
 * Same rule as always: private fields need setAccessible(true) before get/set.
 *
 * The unlock sequence for fields:
 *   1. getDeclaredField(name)
 *   2. field.setAccessible(true)
 *   3. field.get(instance) or field.set(instance, value)
 *
 * Write this from memory after reading it once.
 */
public class Impl07 {

    public static void main(String[] args) throws Exception {

        Person person = new Person("Dave", 35);
        Class<?> cls  = Person.class;

        System.out.println("Original person: " + person);
        // Person{name='Dave', age=35, id='P-1'} (id number depends on how many created)


        // ── READ a private String field ───────────────────────────────────────
        System.out.println("\n=== Reading private field 'name' ===");
        Field nameField = cls.getDeclaredField("name");
        nameField.setAccessible(true);

        // get(instance) returns Object — must cast to the actual type.
        String currentName = (String) nameField.get(person);
        System.out.println("name = " + currentName);  // Dave


        // ── WRITE a private String field ─────────────────────────────────────
        System.out.println("\n=== Writing to private field 'name' ===");
        nameField.set(person, "Dave Modified");

        // Verify the change took effect — call the normal getter.
        System.out.println("After set: " + person.getName());  // Dave Modified
        System.out.println("Full object: " + person);


        // ── READ a private int field ──────────────────────────────────────────
        System.out.println("\n=== Reading private int field 'age' ===");
        Field ageField = cls.getDeclaredField("age");
        ageField.setAccessible(true);

        // Option A: get(instance) returns Object — Integer (autoboxed from int).
        Object ageAsObject = ageField.get(person);
        System.out.println("age (as Object): " + ageAsObject + " type=" + ageAsObject.getClass().getSimpleName());
        // 35  type=Integer

        // Option B: getInt(instance) returns the primitive int directly — no boxing.
        int ageAsPrimitive = ageField.getInt(person);
        System.out.println("age (as int primitive): " + ageAsPrimitive);
        // 35


        // ── WRITE a private int field ─────────────────────────────────────────
        System.out.println("\n=== Writing to private int field 'age' ===");

        // Option A: set(instance, value) — autoboxes the int to Integer.
        ageField.set(person, 99);
        System.out.println("After set(99): age=" + person.getAge());  // 99

        // Option B: setInt(instance, value) — no boxing, slightly more efficient.
        ageField.setInt(person, 100);
        System.out.println("After setInt(100): age=" + person.getAge());  // 100


        // ── READ a STATIC field ───────────────────────────────────────────────
        // For static fields, pass null as the instance to get/set — there is no instance.
        System.out.println("\n=== Reading private static field 'instanceCount' ===");
        Field countField = cls.getDeclaredField("instanceCount");
        countField.setAccessible(true);

        int count = countField.getInt(null); // null = static, no instance
        System.out.println("instanceCount = " + count);

        // WRITE to a static field — same: pass null as instance.
        System.out.println("\n=== Writing to private static field 'instanceCount' ===");
        countField.setInt(null, 999); // set to arbitrary value
        System.out.println("After override: instanceCount = " + countField.getInt(null));  // 999
        System.out.println("Via normal API: " + Person.getInstanceCount());                // 999
    }
}
