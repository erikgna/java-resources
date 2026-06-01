package poc03;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * IMPL 08 — Method References
 *
 * A method reference is a shorthand for a lambda that just calls one method.
 *
 * Instead of:  s -> s.toUpperCase()
 * Write:       String::toUpperCase
 *
 * There are FOUR kinds of method references:
 *
 * 1. Static method reference
 *    Syntax:  ClassName::staticMethod
 *    Lambda:  (args) -> ClassName.staticMethod(args)
 *    Example: Integer::parseInt   →   s -> Integer.parseInt(s)
 *
 * 2. Instance method of a PARTICULAR (specific) object
 *    Syntax:  instance::method
 *    Lambda:  (args) -> instance.method(args)
 *    Example: myString::toUpperCase  →  () -> myString.toUpperCase()
 *    The object is captured (fixed) at the time of the reference.
 *
 * 3. Instance method of an ARBITRARY object (type reference)
 *    Syntax:  ClassName::instanceMethod
 *    Lambda:  (obj, args) -> obj.instanceMethod(args)
 *    Example: String::toLowerCase  →  s -> s.toLowerCase()
 *    The object is NOT captured — it's supplied as the first argument.
 *
 * 4. Constructor reference
 *    Syntax:  ClassName::new
 *    Lambda:  (args) -> new ClassName(args)
 *    Example: ArrayList::new  →  () -> new ArrayList<>()
 */
public class Impl08 {

    static class Person {
        String name;
        int age;

        // Constructor used in constructor reference demo
        Person(String name) {
            this.name = name;
            this.age = 0;
        }

        // Static method — can be referenced as Person::greet
        static String greet(String name) {
            return "Hello, " + name + "!";
        }

        // Instance method — can be referenced as person::getName or Person::getName
        String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Person{" + name + "}";
        }
    }

    public static void main(String[] args) {

        // --- Kind 1: Static method reference ---
        // Integer.parseInt(String) is a static method
        Function<String, Integer> parseIntLambda = s -> Integer.parseInt(s);
        Function<String, Integer> parseIntRef    = Integer::parseInt;         // same thing

        System.out.println("Kind 1 - Static:");
        System.out.println(parseIntLambda.apply("42")); // 42
        System.out.println(parseIntRef.apply("42"));    // 42

        // Custom static method
        Function<String, String> greetLambda = name -> Person.greet(name);
        Function<String, String> greetRef    = Person::greet;  // same thing

        System.out.println(greetRef.apply("World"));  // Hello, World!

        // --- Kind 2: Instance method of a PARTICULAR object ---
        String prefix = "LOG: ";
        // `prefix` is captured — this reference always uses THIS string object
        Function<String, String> addPrefixLambda = msg -> prefix.concat(msg);
        Function<String, String> addPrefixRef    = prefix::concat;  // same thing

        System.out.println("\nKind 2 - Particular instance:");
        System.out.println(addPrefixRef.apply("server started")); // LOG: server started

        // --- Kind 3: Instance method of an ARBITRARY object (type reference) ---
        // The object is NOT fixed — it's the argument supplied at call time
        Function<String, String> toLowerLambda = s -> s.toLowerCase();
        Function<String, String> toLowerRef    = String::toLowerCase;  // same thing

        System.out.println("\nKind 3 - Arbitrary instance (type ref):");
        System.out.println(toLowerRef.apply("HELLO"));  // hello
        System.out.println(toLowerRef.apply("JAVA"));   // java

        // When the instance method takes an argument, use BiFunction
        // String.contains(CharSequence) takes one arg → BiFunction<String, String, Boolean>
        BiFunction<String, String, Boolean> containsLambda = (s, sub) -> s.contains(sub);
        BiFunction<String, String, Boolean> containsRef    = String::contains;  // same thing

        System.out.println(containsRef.apply("hello world", "world")); // true
        System.out.println(containsRef.apply("hello world", "xyz"));   // false

        // Person::getName → arbitrary Person instance, no extra args
        Function<Person, String> getNameLambda = p -> p.getName();
        Function<Person, String> getNameRef    = Person::getName;  // same thing

        Person alice = new Person("Alice");
        System.out.println(getNameRef.apply(alice)); // Alice

        // --- Kind 4: Constructor reference ---
        // Supplier — no args → new ArrayList
        Supplier<ArrayList<String>> listFactory = ArrayList::new;
        ArrayList<String> list = listFactory.get();
        list.add("item");
        System.out.println("\nKind 4 - Constructor:");
        System.out.println(list);  // [item]

        // Function — one arg → new Person(name)
        Function<String, Person> personFactory = Person::new;
        Person bob = personFactory.apply("Bob");
        System.out.println(bob);  // Person{Bob}

        // Use with a list of names
        List<String> names = List.of("Alice", "Bob", "Carol");
        System.out.println("\nCreate persons from names:");
        names.stream()
             .map(Person::new)             // constructor ref: name → new Person(name)
             .map(Person::getName)         // type ref: person → person.getName()
             .forEach(System.out::println); // static-like: Consumer via PrintStream instance
        // Alice
        // Bob
        // Carol
    }
}
