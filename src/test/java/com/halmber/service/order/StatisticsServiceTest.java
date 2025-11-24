package com.halmber.service.order;

import com.halmber.config.ApplicationConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticsServiceTest {
    private ApplicationConfig config;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testProcessStatistics_CreatesOutputFile(@TempDir Path inputDir, @TempDir Path outputDir) throws Exception {
        // Prepare test data
        String json = """
                [
                  {
                    "id": "ord-001",
                    "customer": {"id": "cust-101", "fullName": "John Doe", "email": "john@ex.com", "phone": "+123", "city": "Lviv"},
                    "status": "NEW",
                    "tags": "gift",
                    "paymentMethod": "card",
                    "amount": 499.99,
                    "createdAt": 1731600000
                  }
                ]
                """;

        Files.writeString(inputDir.resolve("orders.json"), json);

        config = new ApplicationConfig(
                inputDir + "/",
                outputDir.toString(),
                "status",
                2
        );

        StatisticsService service = new StatisticsService(config);
        service.processStatistics();

        Path outputFile = outputDir.resolve("statistics_by_status.xml");
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("<statistics>"));
        assertTrue(content.contains("NEW"));
        assertTrue(outContent.toString().contains("Statistics processing completed successfully"));
    }

    @Test
    void testProcessStatistics_ProcessesMultipleOrders(@TempDir Path inputDir, @TempDir Path outputDir) throws Exception {
        String json = """
                [
                  {"id": "ord-001", "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"}, "status": "NEW", "tags": "gift", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000},
                  {"id": "ord-002", "customer": {"id": "c2", "fullName": "B", "email": "b@ex.com", "phone": "+2", "city": "Kyiv"}, "status": "DONE", "tags": "urgent", "paymentMethod": "cash", "amount": 200, "createdAt": 1731600000},
                  {"id": "ord-003", "customer": {"id": "c3", "fullName": "C", "email": "c@ex.com", "phone": "+3", "city": "Lviv"}, "status": "NEW", "tags": "promo", "paymentMethod": "card", "amount": 150, "createdAt": 1731600000}
                ]
                """;

        Files.writeString(inputDir.resolve("orders.json"), json);

        config = new ApplicationConfig(
                inputDir + "/",
                outputDir.toString(),
                "city",
                2
        );

        StatisticsService service = new StatisticsService(config);
        service.processStatistics();

        Path outputFile = outputDir.resolve("statistics_by_city.xml");
        String content = Files.readString(outputFile);

        assertTrue(content.contains("Lviv"));
        assertTrue(content.contains("Kyiv"));
        assertTrue(content.contains("<count>2</count>")); // Lviv appears twice

        // Verify sorting: Lviv (2) should appear before Kyiv (1)
        int lvivIndex = content.indexOf("<value>Lviv</value>");
        int kyivIndex = content.indexOf("<value>Kyiv</value>");
        assertTrue(lvivIndex < kyivIndex, "Lviv with count 2 should appear before Kyiv with count 1");
    }

    @Test
    void testProcessStatistics_WithTagsAttribute(@TempDir Path inputDir, @TempDir Path outputDir) throws Exception {
        String json = """
                [
                  {"id": "ord-001", "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"}, "status": "NEW", "tags": "gift, urgent, promo", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000}
                ]
                """;

        Files.writeString(inputDir.resolve("orders.json"), json);

        config = new ApplicationConfig(
                inputDir + "/",
                outputDir.toString(),
                "tags",
                2
        );

        StatisticsService service = new StatisticsService(config);
        service.processStatistics();

        Path outputFile = outputDir.resolve("statistics_by_tags.xml");
        String content = Files.readString(outputFile);

        assertTrue(content.contains("gift"));
        assertTrue(content.contains("urgent"));
        assertTrue(content.contains("promo"));
    }

    @Test
    void testProcessStatistics_CreatesOutputDirectory(@TempDir Path inputDir, @TempDir Path outputDir) throws Exception {
        String json = """
                [{"id": "ord-001", "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"}, "status": "NEW", "tags": "gift", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000}]
                """;

        Files.writeString(inputDir.resolve("orders.json"), json);

        Path newOutputDir = outputDir.resolve("new/nested/directory");
        config = new ApplicationConfig(
                inputDir + "/",
                newOutputDir.toString(),
                "status",
                2
        );

        StatisticsService service = new StatisticsService(config);
        service.processStatistics();

        assertTrue(Files.exists(newOutputDir));
        assertTrue(Files.exists(newOutputDir.resolve("statistics_by_status.xml")));
    }

    @Test
    void testProcessStatistics_SortsResultsByCountDescending(@TempDir Path inputDir, @TempDir Path outputDir) throws Exception {
        String json = """
                [
                  {"id": "ord-001", "customer": {"id": "c1", "fullName": "A", "email": "a@ex.com", "phone": "+1", "city": "Lviv"}, "status": "NEW", "tags": "gift", "paymentMethod": "card", "amount": 100, "createdAt": 1731600000},
                  {"id": "ord-002", "customer": {"id": "c2", "fullName": "B", "email": "b@ex.com", "phone": "+2", "city": "Kyiv"}, "status": "NEW", "tags": "urgent", "paymentMethod": "cash", "amount": 200, "createdAt": 1731600000},
                  {"id": "ord-003", "customer": {"id": "c3", "fullName": "C", "email": "c@ex.com", "phone": "+3", "city": "Lviv"}, "status": "DONE", "tags": "promo", "paymentMethod": "card", "amount": 150, "createdAt": 1731600000},
                  {"id": "ord-004", "customer": {"id": "c4", "fullName": "D", "email": "d@ex.com", "phone": "+4", "city": "Odesa"}, "status": "NEW", "tags": "sale", "paymentMethod": "card", "amount": 175, "createdAt": 1731600000},
                  {"id": "ord-005", "customer": {"id": "c5", "fullName": "E", "email": "e@ex.com", "phone": "+5", "city": "Kyiv"}, "status": "CANCELED", "tags": "return", "paymentMethod": "cash", "amount": 50, "createdAt": 1731600000}
                ]
                """;

        Files.writeString(inputDir.resolve("orders.json"), json);

        config = new ApplicationConfig(
                inputDir + "/",
                outputDir.toString(),
                "status",
                2
        );

        StatisticsService service = new StatisticsService(config);
        service.processStatistics();

        Path outputFile = outputDir.resolve("statistics_by_status.xml");
        String content = Files.readString(outputFile);

        // NEW (3), DONE (1), CANCELED (1)
        // Verify NEW appears first (highest count)
        int newIndex = content.indexOf("<value>NEW</value>");
        int doneIndex = content.indexOf("<value>DONE</value>");
        int canceledIndex = content.indexOf("<value>CANCELED</value>");

        assertTrue(newIndex > 0, "NEW should be present");
        assertTrue(newIndex < doneIndex, "NEW (3) should appear before DONE (1)");
        assertTrue(newIndex < canceledIndex, "NEW (3) should appear before CANCELED (1)");

        // Verify count for NEW is 3
        assertTrue(content.contains("<count>3</count>"));
    }
}

