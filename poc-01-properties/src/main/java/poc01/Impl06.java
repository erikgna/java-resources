package poc01;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Impl06 — STORE + XML round-trip from memory.
 *
 * Rules: write this WITHOUT looking at Impl05.
 * Must successfully:
 *   1. Create properties, store to byte array, reload, verify values survive round-trip.
 *   2. Do the same with XML.
 *   3. Show that keys are alphabetically sorted in the stored output.
 *
 * If you needed to look at Impl05 for method names: repeat this file until you don't.
 */
public class Impl06 {

    public static void main(String[] args) throws IOException {

        // ── Build properties ──────────────────────────────────────────────────

        Properties original = new Properties();
        original.setProperty("zebra", "last letter");
        original.setProperty("apple", "first letter");
        original.setProperty("mango", "middle letter");
        original.setProperty("db.url", "jdbc:postgresql://localhost/mydb");


        // ── Round-trip 1: .properties format (ISO-8859-1 OutputStream) ───────

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        original.store(bos, "Round-trip test via OutputStream");

        // Print raw output — notice keys are ALPHABETICALLY SORTED (apple, db.url, mango, zebra).
        System.out.println("── Stored .properties (OutputStream) ──");
        System.out.println(bos.toString("ISO-8859-1"));

        // Reload from the byte array.
        Properties reloaded1 = new Properties();
        reloaded1.load(new ByteArrayInputStream(bos.toByteArray()));

        System.out.println("── Reloaded from .properties ──");
        System.out.println("apple = " + reloaded1.getProperty("apple"));    // first letter
        System.out.println("zebra = " + reloaded1.getProperty("zebra"));    // last letter
        System.out.println("db.url = " + reloaded1.getProperty("db.url"));  // jdbc:...


        // ── Round-trip 2: .properties format (UTF-8 Writer) ──────────────────

        // Add a Unicode value to prove the Writer path handles it without escaping.
        original.setProperty("emoji", "Olá mundo");

        ByteArrayOutputStream utf8Bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(utf8Bos, StandardCharsets.UTF_8);
        original.store(writer, "Round-trip test via UTF-8 Writer");
        writer.close();  // Flush + close

        System.out.println("── Stored .properties (UTF-8 Writer) ──");
        String utf8Output = utf8Bos.toString("UTF-8");
        System.out.println(utf8Output);
        // "Olá" should appear as "Olá" (not escaped) because the Writer is UTF-8.

        Properties reloaded2 = new Properties();
        // Load with a Reader to match the UTF-8 encoding used on store.
        reloaded2.load(new java.io.StringReader(utf8Output));
        System.out.println("emoji after UTF-8 round-trip: " + reloaded2.getProperty("emoji"));  // Olá mundo


        // ── Round-trip 3: XML format ──────────────────────────────────────────

        ByteArrayOutputStream xmlBos = new ByteArrayOutputStream();
        original.storeToXML(xmlBos, "XML round-trip test");

        System.out.println("── Stored XML ──");
        System.out.println(xmlBos.toString("UTF-8"));

        // Load back from XML.
        Properties reloaded3 = new Properties();
        reloaded3.loadFromXML(new ByteArrayInputStream(xmlBos.toByteArray()));

        System.out.println("── Reloaded from XML ──");
        System.out.println("mango = " + reloaded3.getProperty("mango"));   // middle letter
        System.out.println("emoji = " + reloaded3.getProperty("emoji"));   // Olá mundo
    }
}
