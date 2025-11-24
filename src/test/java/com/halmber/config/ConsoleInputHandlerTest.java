package com.halmber.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleInputHandlerTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testGetConfiguration_WithAllDefaults() {
        String input = "\n\n\n"; // All empty inputs = defaults
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals("src/main/resources/", config.getInputDirectory());
        assertEquals("id", config.getAttribute());
        assertEquals(8, config.getThreadPoolSize());
        assertTrue(outContent.toString().contains("Order Statistics Configuration"));
    }

    @Test
    void testGetConfiguration_WithCustomValues() {
        String input = "custom/path/\nstatus\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals("custom/path/", config.getInputDirectory());
        assertEquals("status", config.getAttribute());
        assertEquals(4, config.getThreadPoolSize());
    }

    @Test
    void testPromptForInputDirectory_AddsTrailingSlash() {
        String input = "my/custom/path\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals("my/custom/path/", config.getInputDirectory());
    }

    @Test
    void testPromptForInputDirectory_KeepsTrailingSlash() {
        String input = "my/path/\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals("my/path/", config.getInputDirectory());
    }

    @Test
    void testPromptForThread_RejectsZero() {
        String input = "\n\n0\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals(4, config.getThreadPoolSize());
        assertTrue(errContent.toString().contains("must be greater than 0"));
    }

    @Test
    void testPromptForThread_RejectsNegative() {
        String input = "\n\n-5\n8\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals(8, config.getThreadPoolSize());
        assertTrue(errContent.toString().contains("must be greater than 0"));
    }

    @Test
    void testPromptForThread_RejectsInvalidNumber() {
        String input = "\n\nabc\n8\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals(8, config.getThreadPoolSize());
        assertTrue(errContent.toString().contains("is not a valid number"));
    }

    @Test
    void testPromptForThread_WarnsForLargeValue_UserConfirms() {
        String input = "\n\n150\ny\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals(150, config.getThreadPoolSize());
        assertTrue(outContent.toString().contains("is very large"));
    }

    @Test
    void testPromptForThread_WarnsForLargeValue_UserDeclinesAndEntersNew() {
        String input = "\n\n150\nn\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        ApplicationConfig config = handler.getConfiguration();

        assertEquals(4, config.getThreadPoolSize());
        assertTrue(outContent.toString().contains("is very large"));
    }

    @Test
    void testGetConfiguration_DisplaysAvailableAttributes() {
        String input = "\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ConsoleInputHandler handler = new ConsoleInputHandler();
        handler.getConfiguration();

        String output = outContent.toString();
        assertTrue(output.contains("Available attributes:"));
        assertTrue(output.contains("id"));
        assertTrue(output.contains("status"));
        assertTrue(output.contains("tags"));
    }
}
