package com.halmber.service.order;

import com.halmber.config.ApplicationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingServiceTest {
    private ApplicationConfig config;
    private Map<String, Integer> statistics;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        statistics = new ConcurrentHashMap<>();
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    void testProcessAllFiles_WithValidJsonFiles(@TempDir Path tempDir) throws Exception {
        // Create test JSON files
        String json1 = """
                [
                  {
                    "id": "ord-001",
                    "customer": {"id": "cust-101", "fullName": "John", "email": "j@ex.com", "phone": "+123", "city": "Lviv"},
                    "status": "NEW",
                    "tags": "gift",
                    "paymentMethod": "card",
                    "amount": 100.0,
                    "createdAt": 1731600000
                  }
                ]
                """;

        Files.writeString(tempDir.resolve("orders1.json"), json1);

        config = new ApplicationConfig(tempDir + "/", "output/", "status", 2);
        ProcessingService service = new ProcessingService(config);

        service.processAllFiles(statistics);

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("NEW"));
        assertTrue(outContent.toString().contains("All files processed successfully"));
    }

    @Test
    void testProcessAllFiles_WithMultipleFiles(@TempDir Path tempDir) throws Exception {
        String json1 = """
                [{"id": "ord-001", "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"}, "status": "NEW", "tags": "gift", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000}]
                """;
        String json2 = """
                [{"id": "ord-002", "customer": {"id": "c2", "fullName": "B", "email": "b@ex.com", "phone": "+2", "city": "Kyiv"}, "status": "DONE", "tags": "urgent", "paymentMethod": "cash", "amount": 200, "createdAt": 1731600000}]
                """;

        Files.writeString(tempDir.resolve("orders1.json"), json1);
        Files.writeString(tempDir.resolve("orders2.json"), json2);

        config = new ApplicationConfig(tempDir + "/", "output/", "status", 2);
        ProcessingService service = new ProcessingService(config);

        service.processAllFiles(statistics);

        assertEquals(2, statistics.size());
        assertEquals(1, statistics.get("NEW"));
        assertEquals(1, statistics.get("DONE"));
    }

    @Test
    void testProcessAllFiles_EmptyDirectory_ThrowsException(@TempDir Path tempDir) {
        config = new ApplicationConfig(tempDir.toString() + "/", "output/", "status", 2);
        ProcessingService service = new ProcessingService(config);

        IOException exception = assertThrows(IOException.class, () -> {
            service.processAllFiles(statistics);
        });

        assertTrue(exception.getMessage().contains("No JSON files found"));
    }

    @Test
    void testProcessAllFiles_NonExistentDirectory_ThrowsException() {
        config = new ApplicationConfig("non/existent/path/", "output/", "status", 2);
        ProcessingService service = new ProcessingService(config);

        assertThrows(Exception.class, () -> {
            service.processAllFiles(statistics);
        });
    }

    @Test
    void testProcessAllFiles_InvalidJsonFile_LogsError(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("invalid.json"), "{ invalid json }");

        config = new ApplicationConfig(tempDir + "/", "output/", "status", 2);
        ProcessingService service = new ProcessingService(config);

        service.processAllFiles(statistics);

        assertTrue(errContent.toString().contains("Error"));
    }

    @Test
    void testProcessAllFiles_ProcessesByTags(@TempDir Path tempDir) throws Exception {
        String json = """
                [
                  {
                    "id": "ord-001",
                    "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"},
                    "status": "NEW",
                    "tags": "gift, urgent, promo",
                    "paymentMethod": "card",
                    "amount": 100,
                    "createdAt": 1731600000
                  }
                ]
                """;

        Files.writeString(tempDir.resolve("orders.json"), json);

        config = new ApplicationConfig(tempDir + "/", "output/", "tags", 2);
        ProcessingService service = new ProcessingService(config);

        service.processAllFiles(statistics);

        assertEquals(3, statistics.size());
        assertEquals(1, statistics.get("gift"));
        assertEquals(1, statistics.get("urgent"));
        assertEquals(1, statistics.get("promo"));
    }

    @Test
    void testProcessAllFiles_InvalidAttribute_LogsError(@TempDir Path tempDir) throws Exception {
        String json = """
                [{"id": "ord-001", "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"}, "status": "NEW", "tags": "gift", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000}]
                """;

        Files.writeString(tempDir.resolve("orders.json"), json);

        config = new ApplicationConfig(tempDir + "/", "output/", "invalidAttr", 2);
        ProcessingService service = new ProcessingService(config);

        service.processAllFiles(statistics);

        assertTrue(errContent.toString().contains("InvalidAttributeException"));
    }

    @Test
    void testProcessAllFiles_ConcurrentProcessing(@TempDir Path tempDir) throws Exception {
        // Create multiple files to test concurrent processing
        for (int i = 1; i <= 5; i++) {
            String json = String.format("""
                    [{"id": "ord-%03d", "customer": {"id": "c%d", "fullName": "User%d", "email": "u%d@ex.com", "phone": "+%d", "city": "City%d"}, "status": "NEW", "tags": "tag%d", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000}]
                    """, i, i, i, i, i, i, i);
            Files.writeString(tempDir.resolve("orders" + i + ".json"), json);
        }

        config = new ApplicationConfig(tempDir + "/", "output/", "status", 4);
        ProcessingService service = new ProcessingService(config);

        service.processAllFiles(statistics);

        assertEquals(1, statistics.size());
        assertEquals(5, statistics.get("NEW"));
    }
}
