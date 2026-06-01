package poc01;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Impl05 — STORE: OutputStream, Writer, XML, and round-trip.
 *
 * Concepts covered:
 *   - store(OutputStream, comment) → writes using ISO-8859-1 encoding
 *   - store(Writer, comment)       → writes using whatever encoding the Writer has
 *   - storeToXML(OutputStream, comment) → writes XML format (UTF-8 by default)
 *   - loadFromXML(InputStream)     → loads the XML format back
 *   - full round-trip: store to file, reload, verify values
 *
 * KEY INSIGHT — Encoding difference (this is a common real-world bug):
 *   store(OutputStream) uses ISO-8859-1 internally.
 *   Any character outside ISO-8859-1 (e.g., "é", "中", "€") is escaped to \\uXXXX.
 *   store(Writer) trusts the encoding you gave to the Writer.
 *   If you wrap the OutputStream with OutputStreamWriter(stream, UTF-8),
 *   the characters are written as-is without escaping.
 *
 * KEY INSIGHT — Key ordering:
 *   store() key order depends on the JDK version.
 *   Java 11 writes keys in ConcurrentHashMap order (not sorted).
 *   JDK 26 sorts keys alphabetically. Do NOT rely on order across JVM versions.
 *
 * KEY INSIGHT — Defaults are NOT written:
 *   If you created Properties with a defaults chain, store() only writes
 *   the properties in the top-level object — NOT the defaults entries.
 *
 * Reference allowed: only the Javadoc for store() / storeToXML().
 */
public class Impl05 {

    public static void main(String[] args) throws IOException {

        // ── Set up properties with Unicode ────────────────────────────────────

        Properties props = new Properties();
        props.setProperty("app.name", "Café App");      // "é" is non-ASCII
        props.setProperty("greeting", "こんにちは");       // Japanese — well outside ISO-8859-1
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "5432");


        // ── 1. store(OutputStream) → escapes non-ASCII to \\uXXXX ──────────────

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // The comment argument is written as "# comment" on the first line.
        // A date line is always written on the second line — you cannot suppress it.
        props.store(bos, "Stored with OutputStream (ISO-8859-1)");
        String outputStreamResult = bos.toString("ISO-8859-1");  // match the encoding used

        System.out.println("── store(OutputStream) output ──────────────────────────────────");
        System.out.println(outputStreamResult);
        // Observation: "é" appears as é and Japanese chars appear as \\uXXXX escape sequences.


        // ── 2. store(Writer) → respects the Writer's encoding ─────────────────

        StringWriter sw = new StringWriter();

        // StringWriter uses Java's in-memory UTF-16 string — no encoding issue.
        props.store(sw, "Stored with Writer (no escaping)");
        System.out.println("── store(Writer) output ────────────────────────────────────────");
        System.out.println(sw.toString());
        // Observation: "é" and Japanese characters appear as actual characters, NOT escaped.


        // ── 3. store(Writer) with explicit UTF-8 OutputStreamWriter ───────────

        ByteArrayOutputStream utf8Bos = new ByteArrayOutputStream();
        // Wrapping with OutputStreamWriter(stream, UTF-8) tells the Writer to use UTF-8.
        Writer utf8Writer = new OutputStreamWriter(utf8Bos, StandardCharsets.UTF_8);
        props.store(utf8Writer, "UTF-8 store");
        utf8Writer.close();  // Must close or flush the Writer to ensure all bytes are written.

        // Read back as UTF-8 to verify.
        String utf8Result = utf8Bos.toString("UTF-8");
        System.out.println("── store with OutputStreamWriter(UTF-8) ────────────────────────");
        System.out.println(utf8Result);


        // ── 4. storeToXML + loadFromXML round-trip ─────────────────────────────

        ByteArrayOutputStream xmlBos = new ByteArrayOutputStream();
        // storeToXML writes a well-formed XML document.
        // The XML declaration specifies UTF-8 by default, so Unicode characters are NOT escaped.
        props.storeToXML(xmlBos, "XML round-trip test");

        System.out.println("── storeToXML output ────────────────────────────────────────────");
        System.out.println(xmlBos.toString("UTF-8"));

        // loadFromXML reads back the XML into a new Properties object.
        Properties reloaded = new Properties();
        reloaded.loadFromXML(new ByteArrayInputStream(xmlBos.toByteArray()));

        System.out.println("── After loadFromXML ────────────────────────────────────────────");
        System.out.println("app.name  = " + reloaded.getProperty("app.name"));   // Café App
        System.out.println("greeting  = " + reloaded.getProperty("greeting"));   // こんにちは


        // ── 5. store to real file + reload (full round-trip) ──────────────────

        // Create a temporary file in the system temp directory (cleaned up on restart).
        Path tempFile = Files.createTempFile("poc-01-", ".properties");

        // Write to file using UTF-8 via OutputStreamWriter.
        try (OutputStream fileOut = Files.newOutputStream(tempFile);
             Writer fileWriter = new OutputStreamWriter(fileOut, StandardCharsets.UTF_8)) {
            props.store(fileWriter, "File round-trip test");
        }

        System.out.println("── Written to: " + tempFile);

        // Read back from the same file.
        Properties fromFile = new Properties();
        try (var fileIn = Files.newInputStream(tempFile)) {
            // load(InputStream) uses ISO-8859-1, so using newBufferedReader with UTF-8 instead.
        }
        // Correct way: use a Reader with explicit UTF-8 when loading a UTF-8 file.
        try (var fileReader = Files.newBufferedReader(tempFile, StandardCharsets.UTF_8)) {
            fromFile.load(fileReader);
        }

        System.out.println("── After file round-trip ────────────────────────────────────────");
        System.out.println("app.name  = " + fromFile.getProperty("app.name"));  // Café App
        System.out.println("db.port   = " + fromFile.getProperty("db.port"));   // 5432

        // Clean up temp file.
        Files.delete(tempFile);
        System.out.println("Temp file deleted.");
    }
}
