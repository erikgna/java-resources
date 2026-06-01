package poc02;

/**
 * Impl01 — READ: Getting the Class object (three ways).
 *
 * The entry point to ALL reflection is java.lang.Class<T>.
 * Before you can read fields, invoke methods, or do anything else,
 * you need a Class<?> object that represents the class you want to inspect.
 *
 * There are exactly three ways to obtain it:
 *
 *   1. CLASS LITERAL      — Person.class
 *      Resolved at compile time. No exception possible.
 *      Use when you know the type at compile time.
 *
 *   2. INSTANCE METHOD    — someObject.getClass()
 *      Resolved at runtime from a live object.
 *      Returns the ACTUAL runtime type, which may be a subclass.
 *
 *   3. CLASS.FORNAME()    — Class.forName("poc02.Person")
 *      Resolved at runtime from a fully-qualified class name string.
 *      This is the most dynamic form — the string can come from a config file,
 *      user input, or anywhere. Throws ClassNotFoundException if the name is wrong.
 *      This is how frameworks like Spring load your classes without importing them.
 *
 * Reference allowed for Impl01. Write Impl02 from memory.
 */
public class Impl01 {

    public static void main(String[] args) throws ClassNotFoundException {

        // ── WAY 1: class literal ─────────────────────────────────────────────
        // The .class suffix is a compile-time operator — no object needed.
        // The result is a Class<Person> object (the generic type is known at compile time).
        Class<Person> clazz1 = Person.class;
        System.out.println("Way 1 — class literal    : " + clazz1);
        // prints: class poc02.Person


        // ── WAY 2: getClass() on an instance ─────────────────────────────────
        // Every object in Java inherits getClass() from java.lang.Object.
        // Note: the return type is Class<?> (wildcard), not Class<Person>,
        // because the compiler cannot guarantee the runtime type at this call site.
        Person p = new Person("Alice", 30);
        Class<?> clazz2 = p.getClass();
        System.out.println("Way 2 — getClass()       : " + clazz2);
        // prints: class poc02.Person


        // ── WAY 3: Class.forName() ────────────────────────────────────────────
        // Takes a FULLY QUALIFIED class name (package + class name).
        // Throws ClassNotFoundException if the class doesn't exist on the classpath.
        // The JVM loads and initializes the class the first time this is called.
        Class<?> clazz3 = Class.forName("poc02.Person");
        System.out.println("Way 3 — Class.forName()  : " + clazz3);
        // prints: class poc02.Person


        // ── ALL THREE REFER TO THE SAME OBJECT ───────────────────────────────
        // Java guarantees there is exactly ONE Class object per class per ClassLoader.
        // So all three variables above point to the same object in memory.
        System.out.println("\nAll three are the same object in memory:");
        System.out.println("clazz1 == clazz2 : " + (clazz1 == clazz2));  // true
        System.out.println("clazz2 == clazz3 : " + (clazz2 == clazz3));  // true


        // ── READING BASIC CLASS METADATA ─────────────────────────────────────
        // Once you have a Class<?> you can start reading metadata about it.
        // This is the "Read" part of the POC. Fields, methods, constructors come later.
        Class<?> cls = Person.class;

        // Simple class name (no package)
        System.out.println("\n--- Class metadata ---");
        System.out.println("Simple name   : " + cls.getSimpleName());   // Person
        System.out.println("Full name     : " + cls.getName());          // poc02.Person
        System.out.println("Package name  : " + cls.getPackageName());   // poc02

        // Superclass — every non-Object class has one.
        // Person does not explicitly extend anything, so it implicitly extends Object.
        System.out.println("Superclass    : " + cls.getSuperclass());    // class java.lang.Object

        // Interfaces this class implements directly (Person implements none).
        Class<?>[] interfaces = cls.getInterfaces();
        System.out.println("Interfaces    : " + interfaces.length);      // 0

        // Is it an interface? An enum? An array? A primitive?
        System.out.println("isInterface   : " + cls.isInterface());      // false
        System.out.println("isEnum        : " + cls.isEnum());           // false
        System.out.println("isArray       : " + cls.isArray());          // false
        System.out.println("isPrimitive   : " + cls.isPrimitive());      // false
    }
}
