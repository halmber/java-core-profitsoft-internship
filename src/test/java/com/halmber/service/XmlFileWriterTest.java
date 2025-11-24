package com.halmber.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.halmber.factory.statistics.StatisticItemFactory;
import com.halmber.factory.statistics.StatisticsWrapperFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XmlFileWriterTest {
    @Mock
    private StatisticItemFactory<TestItem> itemFactory;
    @Mock
    private StatisticsWrapperFactory<TestWrapper, TestItem> wrapperFactory;
    private XmlFileWriter<TestWrapper, TestItem> writer;

    @BeforeEach
    void setup() {
        writer = new XmlFileWriter<>(wrapperFactory, itemFactory);
    }

    // Test classes used for XML serialization
    static class TestItem {
        public String name;
        public int count;

        public TestItem() {
        }

        public TestItem(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    static class TestWrapper {
        public List<TestItem> items;

        public TestWrapper() {
        }

        public TestWrapper(List<TestItem> items) {
            this.items = items;
        }
    }

    // Should write valid XML using real factory implementations
    @Test
    void testWriteStatistics_WritesValidXml(@TempDir Path tempDir) throws Exception {
        File outputFile = tempDir.resolve("stats.xml").toFile();

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("apples", 10);
        stats.put("oranges", 20);

        // Mock factories
        when(itemFactory.create(any())).thenAnswer(inv -> {
            Map.Entry<String, Integer> e = inv.getArgument(0);
            return new TestItem(e.getKey(), e.getValue());
        });

        when(wrapperFactory.create(any())).thenAnswer(inv ->
                new TestWrapper(inv.getArgument(0))
        );

        writer.writeStatistics(outputFile, stats);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        // Parse XML to verify structure
        XmlMapper mapper = new XmlMapper();
        TestWrapper parsed = mapper.readValue(outputFile, TestWrapper.class);

        assertEquals(2, parsed.items.size());
        assertEquals("oranges", parsed.items.get(0).name);
        assertEquals(20, parsed.items.get(0).count);
        assertEquals("apples", parsed.items.get(1).name);
        assertEquals(10, parsed.items.get(1).count);
    }

    // Verify factories are called correctly
    @Test
    void testWriteStatistics_FactoriesAreUsed(@TempDir Path tempDir) throws Exception {
        File outputFile = tempDir.resolve("stats.xml").toFile();

        Map<String, Integer> stats = Map.of("a", 1, "b", 2);

        when(itemFactory.create(any())).thenAnswer(inv -> {
            Map.Entry<String, Integer> e = inv.getArgument(0);
            return new TestItem(e.getKey(), e.getValue());
        });

        when(wrapperFactory.create(any())).thenAnswer(inv -> {
            List<TestItem> items = inv.getArgument(0);
            return new TestWrapper(items);
        });

        writer.writeStatistics(outputFile, stats);

        // Verify item factory called twice
        verify(itemFactory, times(2)).create(any());

        // Capture wrapperFactory argument
        ArgumentCaptor<List<TestItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(wrapperFactory).create(captor.capture());

        List<TestItem> items = captor.getValue();
        assertEquals(2, items.size());
    }

    // Empty map = empty items list
    @Test
    void testWriteStatistics_EmptyMapProducesEmptyList(@TempDir Path tempDir) throws Exception {
        File outputFile = tempDir.resolve("empty.xml").toFile();
        Map<String, Integer> stats = new HashMap<>();

        when(wrapperFactory.create(any())).thenAnswer(inv ->
                new TestWrapper(inv.getArgument(0))
        );

        writer.writeStatistics(outputFile, stats);

        assertTrue(outputFile.exists());

        XmlMapper xmlMapper = new XmlMapper();
        TestWrapper parsed = xmlMapper.readValue(outputFile, TestWrapper.class);

        assertNotNull(parsed.items);
        assertEquals(0, parsed.items.size());
        assertTrue(parsed.items.isEmpty());
    }
}
