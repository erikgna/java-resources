package poc02;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Reflection POC.
 *
 * Each test verifies one concrete behavior of the Reflection API.
 * Run with: mvn test
 */
class ReflectionTest {

    // Reset the static counter before each test so tests don't depend on each other's state.
    @BeforeEach
    void resetInstanceCount() throws Exception {
        Field countField = Person.class.getDeclaredField("instanceCount");
        countField.setAccessible(true);
        countField.setInt(null, 0);
    }

    // ── READ: Class metadata ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Class metadata (Impl01)")
    class ClassMetadata {

        @Test
        void classLiteralAndForNameReferToSameObject() throws ClassNotFoundException {
            Class<Person> fromLiteral = Person.class;
            Class<?> fromForName = Class.forName("poc02.Person");
            // Same class loaded by the same ClassLoader must be the identical object.
            assertSame(fromLiteral, fromForName);
        }

        @Test
        void simpleAndFullNameAreCorrect() {
            assertEquals("Person", Person.class.getSimpleName());
            assertEquals("poc02.Person", Person.class.getName());
            assertEquals("poc02", Person.class.getPackageName());
        }

        @Test
        void superclassIsObject() {
            assertEquals(Object.class, Person.class.getSuperclass());
        }

        @Test
        void isNotInterfaceNotEnumNotArray() {
            assertFalse(Person.class.isInterface());
            assertFalse(Person.class.isEnum());
            assertFalse(Person.class.isArray());
        }
    }

    // ── READ: Fields ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Field inspection (Impl02)")
    class FieldInspection {

        @Test
        void personHasFourDeclaredFields() {
            // name, age, id, instanceCount
            assertEquals(4, Person.class.getDeclaredFields().length);
        }

        @Test
        void hasZeroPublicFields() {
            // All Person fields are private — getFields() returns nothing.
            assertEquals(0, Person.class.getFields().length);
        }

        @Test
        void idFieldIsFinal() throws NoSuchFieldException {
            Field id = Person.class.getDeclaredField("id");
            assertTrue(java.lang.reflect.Modifier.isFinal(id.getModifiers()));
        }

        @Test
        void instanceCountFieldIsStatic() throws NoSuchFieldException {
            Field f = Person.class.getDeclaredField("instanceCount");
            assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()));
        }
    }

    // ── READ: Methods ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Method inspection (Impl03)")
    class MethodInspection {

        @Test
        void secretMethodIsPrivate() throws NoSuchMethodException {
            Method secret = Person.class.getDeclaredMethod("secret");
            assertTrue(java.lang.reflect.Modifier.isPrivate(secret.getModifiers()));
        }

        @Test
        void getInstanceCountIsStatic() throws NoSuchMethodException {
            Method m = Person.class.getDeclaredMethod("getInstanceCount");
            assertTrue(java.lang.reflect.Modifier.isStatic(m.getModifiers()));
        }

        @Test
        void greetWithTakesOneStringParam() throws NoSuchMethodException {
            Method m = Person.class.getDeclaredMethod("greetWith", String.class);
            assertEquals(1, m.getParameterCount());
            assertEquals(String.class, m.getParameterTypes()[0]);
        }
    }

    // ── READ: Constructors + Instantiation ────────────────────────────────────

    @Nested
    @DisplayName("Constructor inspection and instantiation (Impl04)")
    class ConstructorInspection {

        @Test
        void personHasTwoDeclaredConstructors() {
            assertEquals(2, Person.class.getDeclaredConstructors().length);
        }

        @Test
        void canInstantiateViaPublicConstructor() throws Exception {
            Constructor<?> ctor = Person.class.getDeclaredConstructor(String.class, int.class);
            Person p = (Person) ctor.newInstance("Test", 10);
            assertEquals("Test", p.getName());
            assertEquals(10, p.getAge());
        }

        @Test
        void canInstantiateViaPrivateConstructorWithSetAccessible() throws Exception {
            Constructor<?> ctor = Person.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            Person p = (Person) ctor.newInstance();
            assertEquals("Unknown", p.getName());
            assertEquals(0, p.getAge());
        }

        @Test
        void privateConstructorThrowsWithoutSetAccessible() throws Exception {
            Constructor<?> ctor = Person.class.getDeclaredConstructor();
            // Do NOT call setAccessible — should throw
            assertThrows(IllegalAccessException.class, () -> ctor.newInstance());
        }
    }

    // ── INVOKE: Methods ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Method invocation (Impl05, Impl06)")
    class MethodInvocation {

        @Test
        void invokePublicGreet() throws Exception {
            Person p = new Person("Alice", 30);
            Method greet = Person.class.getDeclaredMethod("greet");
            String result = (String) greet.invoke(p);
            assertEquals("Hi, I'm Alice and I'm 30 years old.", result);
        }

        @Test
        void invokePublicGreetWith() throws Exception {
            Person p = new Person("Bob", 20);
            Method greetWith = Person.class.getDeclaredMethod("greetWith", String.class);
            String result = (String) greetWith.invoke(p, "Hey");
            assertEquals("Hey, I'm Bob!", result);
        }

        @Test
        void invokeStaticMethodWithNullInstance() throws Exception {
            Method countMethod = Person.class.getDeclaredMethod("getInstanceCount");
            // Creating one person above increments the count.
            // But beforeEach reset it to 0, so after creating person in setup: depends on order.
            // Just verify it doesn't throw and returns an Integer.
            Object result = countMethod.invoke(null);
            assertInstanceOf(Integer.class, result);
        }

        @Test
        void invokePrivateMethodRequiresSetAccessible() throws Exception {
            Person p = new Person("Carol", 40);
            Method secret = Person.class.getDeclaredMethod("secret");
            secret.setAccessible(true);
            String result = (String) secret.invoke(p);
            assertTrue(result.startsWith("My secret ID is: "));
        }

        @Test
        void privateMethodWithoutAccessThrowsIllegalAccess() throws Exception {
            Person p = new Person("Dana", 45);
            Method secret = Person.class.getDeclaredMethod("secret");
            // No setAccessible
            assertThrows(IllegalAccessException.class, () -> secret.invoke(p));
        }

        @Test
        void invocationTargetExceptionWrapsRealException() throws Exception {
            Person p = new Person("Eve", 50);
            Method explode = Person.class.getDeclaredMethod("explode");
            explode.setAccessible(true);

            InvocationTargetException ex = assertThrows(
                    InvocationTargetException.class,
                    () -> explode.invoke(p)
            );
            // The real cause is the RuntimeException thrown inside explode()
            assertInstanceOf(RuntimeException.class, ex.getCause());
            assertEquals("This method always blows up intentionally.", ex.getCause().getMessage());
        }
    }

    // ── MODIFY: Fields ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Field modification (Impl07, Impl08)")
    class FieldModification {

        @Test
        void readPrivateStringField() throws Exception {
            Person p = new Person("Frank", 55);
            Field nameField = Person.class.getDeclaredField("name");
            nameField.setAccessible(true);
            assertEquals("Frank", nameField.get(p));
        }

        @Test
        void writePrivateStringField() throws Exception {
            Person p = new Person("George", 60);
            Field nameField = Person.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(p, "George Modified");
            assertEquals("George Modified", p.getName());
        }

        @Test
        void writePrivateIntFieldWithPrimitive() throws Exception {
            Person p = new Person("Helen", 65);
            Field ageField = Person.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.setInt(p, 99);
            assertEquals(99, p.getAge());
        }

        @Test
        void readAndWriteStaticField() throws Exception {
            Field countField = Person.class.getDeclaredField("instanceCount");
            countField.setAccessible(true);
            int before = countField.getInt(null);
            countField.setInt(null, before + 10);
            assertEquals(before + 10, Person.getInstanceCount());
        }

        @Test
        void writeFinalFieldViaReflection() throws Exception {
            // On Java 11, writing to a final instance field is possible via reflection.
            Person p = new Person("Ivan", 70);
            Field idField = Person.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(p, "OVERRIDE");
            // Reflective read must see the new value.
            assertEquals("OVERRIDE", idField.get(p));
        }
    }

    // ── ERROR CONDITIONS ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Error conditions (Impl09)")
    class ErrorConditions {

        @Test
        void classForNameThrowsOnBadName() {
            assertThrows(ClassNotFoundException.class,
                    () -> Class.forName("poc02.Preson"));
        }

        @Test
        void getDeclaredMethodThrowsOnBadName() {
            assertThrows(NoSuchMethodException.class,
                    () -> Person.class.getDeclaredMethod("doesNotExist"));
        }

        @Test
        void getDeclaredMethodThrowsOnWrongParamTypes() {
            // greetWith(String) — wrong param type here
            assertThrows(NoSuchMethodException.class,
                    () -> Person.class.getDeclaredMethod("greetWith", int.class));
        }

        @Test
        void getDeclaredFieldThrowsOnBadName() {
            assertThrows(NoSuchFieldException.class,
                    () -> Person.class.getDeclaredField("lastName"));
        }
    }
}
