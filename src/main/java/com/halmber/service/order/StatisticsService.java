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
 * <p>
 * Delegates file processing to {@link ProcessingService} and writes aggregated results
 * to an XML file using {@link XmlFileWriter}.
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
     * Processes all JSON files in the input directory and generates an XML statistics report.
     * <p>
     * Handles IO errors, interruptions, and unexpected exceptions.
     */
    public void processStatistics() {
        try {
            processingService.processAllFiles(statistics);
            writeResults();
            System.out.println("\nStatistics processing completed successfully");
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

    /**
     * Writes the aggregated statistics to an XML file in the configured output directory.
     *
     * @throws IOException if the output file cannot be created or written
     */
    private void writeResults() throws IOException {
        Path outputPath = FileService.createFile(
                config.getOutputDirectory(),
                config.getOutputFileName()
        );
        writer.writeStatistics(outputPath.toFile(), statistics);
    }
}