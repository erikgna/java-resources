package poc03;

/**
 * IMPL 01 — What is a Functional Interface?
 *
 * A functional interface is an interface with EXACTLY ONE abstract method.
 * That single method defines the "shape" of the function.
 *
 * The @FunctionalInterface annotation is optional but recommended:
 *   - It makes the intent explicit (this is meant to be a functional interface).
 *   - The compiler enforces the one-abstract-method rule if you put it there.
 *
 * Why does this matter?
 *   Java 8 introduced lambdas. A lambda is shorthand for implementing
 *   a functional interface. Instead of writing an anonymous class with
 *   boilerplate, you write just the logic.
 *
 * Allowed in a functional interface:
 *   - ONE abstract method (required)
 *   - Any number of default methods (have a body, not abstract)
 *   - Any number of static methods (have a body, not abstract)
 *   - Methods from java.lang.Object (equals, hashCode, toString) — these don't count
 */
public class Impl01 {

    // --- Define a custom functional interface ---

    // @FunctionalInterface tells the compiler: "this must have exactly one abstract method"
    @FunctionalInterface
    interface MathOperation {
        // This is the ONE abstract method. No body. Must be implemented by caller.
        int operate(int a, int b);

        // default methods have a body — they don't count toward the "one abstract method" rule
        default String describe() {
            return "A math operation on two integers";
        }

        // static methods also allowed — they belong to the interface, not instances
        static MathOperation addition() {
            // This returns a lambda that implements operate(a, b) as addition
            return (a, b) -> a + b;
        }
    }

    // --- Using the functional interface ---

    public static void main(String[] args) {

        // OLD WAY: anonymous class (verbose, lots of boilerplate)
        MathOperation addOldWay = new MathOperation() {
            @Override
            public int operate(int a, int b) {
                return a + b;
            }
        };

        // NEW WAY: lambda expression
        // (a, b) -> a + b  is a shorthand for the anonymous class above
        // The compiler knows that MathOperation needs an operate(int, int) method,
        // so it wires (a, b) -> a + b into that method automatically.
        MathOperation add = (a, b) -> a + b;
        MathOperation subtract = (a, b) -> a - b;
        MathOperation multiply = (a, b) -> a * b;

        // Call operate() on each lambda
        System.out.println("add(10, 5)      = " + add.operate(10, 5));       // 15
        System.out.println("subtract(10, 5) = " + subtract.operate(10, 5));  // 5
        System.out.println("multiply(10, 5) = " + multiply.operate(10, 5));  // 50

        // Using the default method (it's already implemented in the interface)
        System.out.println("describe: " + add.describe());

        // Using the static factory method from the interface
        MathOperation addViaStatic = MathOperation.addition();
        System.out.println("addViaStatic(3, 7) = " + addViaStatic.operate(3, 7)); // 10

        // You can also pass a lambda to a method that expects a MathOperation
        System.out.println("applyOp(add, 20, 4)      = " + applyOp(add, 20, 4));
        System.out.println("applyOp(subtract, 20, 4) = " + applyOp(subtract, 20, 4));
    }

    // A method that accepts a MathOperation and two integers, then applies the operation
    static int applyOp(MathOperation op, int a, int b) {
        return op.operate(a, b); // calls whatever logic the lambda holds
    }
}
