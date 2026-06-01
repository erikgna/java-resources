package poc04;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * IMPL 10 — Everything Together: Real-World Stream Pipelines
 *
 * No new operations here. This impl combines everything from 01–09
 * to build realistic, multi-step pipelines.
 *
 * Focus on:
 *   - Reading a pipeline from top to bottom as a story.
 *   - Knowing WHICH operation is intermediate vs terminal.
 *   - Choosing the right collector for the job.
 *   - Using named Predicates to keep code readable.
 *
 * Data model: a simple Product record (name, category, price).
 * All operations run on a List<Product>.
 */
public class Impl10 {

    // A plain data holder for a product.
    // In modern Java (14+) you'd use a record. In Java 11, use a regular class.
    static class Product {
        final String name;
        final String category;
        final double price;

        Product(String name, String category, double price) {
            this.name = name;
            this.category = category;
            this.price = price;
        }

        @Override
        public String toString() {
            return name + "($" + price + ")";
        }
    }

    public static void main(String[] args) {

        // --- Dataset ---
        List<Product> catalog = List.of(
            new Product("Laptop",     "Electronics", 999.99),
            new Product("Phone",      "Electronics", 599.99),
            new Product("Headset",    "Electronics", 79.99),
            new Product("Desk",       "Furniture",   349.00),
            new Product("Chair",      "Furniture",   249.00),
            new Product("Lamp",       "Furniture",   39.99),
            new Product("Java Book",  "Books",       49.99),
            new Product("Clean Code", "Books",       35.00),
            new Product("Notebook",   "Books",       12.00)
        );

        // ── 1. Named Predicates for clarity ────────────────────────────────
        Predicate<Product> isElectronics = p -> p.category.equals("Electronics");
        Predicate<Product> isExpensive   = p -> p.price > 100.0;
        Predicate<Product> isCheap       = isExpensive.negate();

        // ── 2. Filter + collect: all electronics ───────────────────────────
        System.out.println("=== Electronics ===");
        catalog.stream()
            .filter(isElectronics)
            .forEach(p -> System.out.println("  " + p));

        // ── 3. Filter + map + collect: names of cheap products ─────────────
        System.out.println("\n=== Cheap product names (price <= 100) ===");
        List<String> cheapNames = catalog.stream()
            .filter(isCheap)                     // keep only low-cost items
            .map(p -> p.name)                    // extract the name
            .sorted()                            // alphabetical order
            .collect(Collectors.toList());
        System.out.println(cheapNames);

        // ── 4. Total price of all electronics ──────────────────────────────
        System.out.println("\n=== Total electronics cost ===");
        double electronicsTotal = catalog.stream()
            .filter(isElectronics)
            .mapToDouble(p -> p.price)  // Stream<Product> → DoubleStream
            .sum();                     // DoubleStream terminal: numeric sum
        System.out.printf("  $%.2f%n", electronicsTotal);

        // ── 5. Most expensive product overall ──────────────────────────────
        System.out.println("\n=== Most expensive product ===");
        Optional<Product> priciest = catalog.stream()
            .max(Comparator.comparingDouble(p -> p.price)); // terminal: max by price
        priciest.ifPresent(p -> System.out.println("  " + p));

        // ── 6. Group by category ────────────────────────────────────────────
        System.out.println("\n=== Products by category ===");
        Map<String, List<Product>> byCategory = catalog.stream()
            .collect(Collectors.groupingBy(p -> p.category));
        byCategory.forEach((cat, products) -> {
            System.out.println("  " + cat + ": " + products);
        });

        // ── 7. Average price per category ──────────────────────────────────
        System.out.println("\n=== Average price per category ===");
        Map<String, Double> avgPriceByCategory = catalog.stream()
            .collect(Collectors.groupingBy(
                p -> p.category,               // key: category name
                Collectors.averagingDouble(p -> p.price) // value: average price in that group
            ));
        avgPriceByCategory.forEach((cat, avg) ->
            System.out.printf("  %-15s $%.2f%n", cat, avg));

        // ── 8. Top 3 cheapest products ──────────────────────────────────────
        System.out.println("\n=== Top 3 cheapest ===");
        catalog.stream()
            .sorted(Comparator.comparingDouble(p -> p.price)) // cheapest first
            .limit(3)                                          // take only 3
            .forEach(p -> System.out.println("  " + p));

        // ── 9. Count products under $50 ─────────────────────────────────────
        long underFifty = catalog.stream()
            .filter(p -> p.price < 50.0)
            .count();
        System.out.println("\n=== Products under $50: " + underFifty + " ===");

        // ── 10. Comma-separated names of books ──────────────────────────────
        System.out.println("\n=== Books (joined) ===");
        String bookNames = catalog.stream()
            .filter(p -> p.category.equals("Books"))
            .map(p -> p.name)
            .collect(Collectors.joining(", "));
        System.out.println("  " + bookNames);

        // ── 11. anyMatch / allMatch / noneMatch ─────────────────────────────
        // These are terminal operations that return boolean.
        boolean hasExpensiveElectronics = catalog.stream()
            .filter(isElectronics)
            .anyMatch(p -> p.price > 800); // is there at least one?
        System.out.println("\n=== anyMatch: electronics > $800? " + hasExpensiveElectronics);

        boolean allBooksAffordable = catalog.stream()
            .filter(p -> p.category.equals("Books"))
            .allMatch(p -> p.price < 100); // are ALL books under $100?
        System.out.println("=== allMatch: all books < $100? " + allBooksAffordable);

        boolean noFreeItems = catalog.stream()
            .noneMatch(p -> p.price == 0); // are there NO free items?
        System.out.println("=== noneMatch: no free items? " + noFreeItems);
    }
}
