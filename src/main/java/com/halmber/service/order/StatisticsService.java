package com.halmber.service.order;

import com.halmber.config.ApplicationConfig;
import com.halmber.factory.statistics.StatisticItemFactoryImpl;
import com.halmber.factory.statistics.StatisticsWrapperFactoryImpl;
import com.halmber.model.statistics.StatisticItem;
import com.halmber.model.statistics.StatisticsWrapper;
import com.halmber.service.FileService;
import com.halmber.service.XmlFileWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for orchestrating the entire statistics processing workflow.
 * Follows Single Responsibility Principle by delegating specific tasks to specialized services.
 */
public class StatisticsService {
    private final ApplicationConfig config;
    private final ProcessingService processingService;
    private final XmlFileWriter<StatisticsWrapper, StatisticItem> writer;
    private final Map<String, Integer> statistics;

    public StatisticsService(ApplicationConfig config) {
        this.config = config;
        this.processingService = new ProcessingService(config);
        this.writer = new XmlFileWriter<>(
                new StatisticsWrapperFactoryImpl(),
                new StatisticItemFactoryImpl()
        );
        this.statistics = new ConcurrentHashMap<>();
    }

    /**
     * Processes all JSON files and generates statistics XML output.
     */
    public void processStatistics() {
        try {
            processingService.processAllFiles(statistics);
            writeResults();
        } catch (IOException e) {
            System.err.printf("Failed to process statistics: %s%n", e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.printf("Processing interrupted: %s%n", e.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\n=== Unexpected Error ===\n");
            System.err.printf("An unexpected error occurred: %s%n", e.getMessage());
            System.exit(1);
        }
    }

    private void writeResults() throws IOException {
        Path outputPath = FileService.createFile(
                config.getOutputDirectory(),
                config.getOutputFileName()
        );
        writer.writeStatistics(outputPath.toFile(), statistics);
    }
}