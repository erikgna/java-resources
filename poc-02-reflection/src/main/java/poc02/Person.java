package poc02;

/**
 * Person — the TARGET class for all Reflection POC implementations.
 *
 * This class is intentionally designed with a mix of:
 *   - private and public fields (including a final field)
 *   - a static counter field
 *   - a public constructor and a private constructor
 *   - public methods and a private method
 *   - a static method
 *
 * You will NEVER call this class directly in the Impl files.
 * You will always go through the Reflection API to interact with it.
 * That is the whole point.
 */
public class Person {

    // Private fields — normal code OUTSIDE this class cannot read or write these.
    // Reflection can, once you call setAccessible(true).
    private String name;
    private int age;

    // final field — once assigned in the constructor, normal Java cannot reassign it.
    // Reflection on Java 11 CAN still write to it. We will test this in Impl08.
    private final String id;

    // Static field — belongs to the class, not to any single Person instance.
    private static int instanceCount = 0;

    // ── CONSTRUCTORS ─────────────────────────────────────────────────────────

    // Public constructor — anyone can call this normally.
    public Person(String name, int age) {
        this.name   = name;
        this.age    = age;
        this.id     = "P-" + (++instanceCount);
    }

    // Private constructor — normal code OUTSIDE this class cannot call this.
    // Reflection can, via getDeclaredConstructor() + setAccessible(true) + newInstance().
    private Person() {
        this.name = "Unknown";
        this.age  = 0;
        this.id   = "P-ANON";
    }

    // ── METHODS ──────────────────────────────────────────────────────────────

    // Public method — callable by anyone.
    public String greet() {
        return "Hi, I'm " + name + " and I'm " + age + " years old.";
    }

    // Public method with a parameter.
    public String greetWith(String greeting) {
        return greeting + ", I'm " + name + "!";
    }

    // Private method — normal code OUTSIDE this class cannot call this.
    // Reflection can, via getDeclaredMethod() + setAccessible(true) + invoke().
    private String secret() {
        return "My secret ID is: " + id;
    }

    // Private method that throws — used in Impl09 to test InvocationTargetException.
    private void explode() {
        throw new RuntimeException("This method always blows up intentionally.");
    }

    // Static method — called on the class, not on an instance.
    // When invoking static methods via reflection, pass null as the instance.
    public static int getInstanceCount() {
        return instanceCount;
    }

    // ── GETTERS ──────────────────────────────────────────────────────────────

    public String getName()  { return name; }
    public int    getAge()   { return age;  }
    public String getId()    { return id;   }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + ", id='" + id + "'}";
    }
}
