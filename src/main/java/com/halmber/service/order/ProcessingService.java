package com.halmber.service.order;

import com.halmber.config.ApplicationConfig;
import com.halmber.exception.InvalidAttributeException;
import com.halmber.model.Order;
import com.halmber.service.FileService;
import com.halmber.service.JsonFileReader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for processing order files in parallel.
 * Manages thread pool and coordinates file processing tasks.
 */
public class ProcessingService {
    private final ApplicationConfig config;
    private final StatisticProcessor statisticProcessor;
    private final ExecutorService executorService;

    public ProcessingService(ApplicationConfig config) {
        this.config = config;
        this.statisticProcessor = new StatisticProcessor();
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
    }

    /**
     * Processes all JSON files from the input directory and aggregates statistics.
     */
    public void processAllFiles(Map<String, Integer> statistics) throws IOException, InterruptedException {
        validateInputDirectory();

        try (DirectoryStream<Path> jsonFiles = getJsonFiles()) {
            submitProcessingTasks(jsonFiles, statistics);
        }
        awaitCompletion();
    }

    /**
     * Validates that input directory contains JSON files.
     * Opens {@link DirectoryStream} and checks if it has elements.
     * <p>Reopening the stream consumes negligible resources.
     *
     * @throws IOException if directory is empty or inaccessible
     */
    private void validateInputDirectory() throws IOException {
        try (DirectoryStream<Path> stream = getJsonFiles()) {

            if (!stream.iterator().hasNext()) {
                throw new IOException(String.format("No JSON files found in input directory: %s", config.getInputDirectory()));
            }
        }
    }

    private DirectoryStream<Path> getJsonFiles() throws IOException {
        return FileService.listFilesAsStream(
                config.getInputDirectory(),
                config.getJsonFileType()
        );
    }

    // test 'execute' method (my default was 'submit')
    private void submitProcessingTasks(DirectoryStream<Path> files, Map<String, Integer> statistics) {
        files.forEach(path -> executorService.execute(() -> processFile(path, statistics)));
    }

    /**
     * Catching some errors from threads.
     * Ignore files with invalid JSON and log error.
     */
    private void processFile(Path path, Map<String, Integer> statistics) {
        try {
            JsonFileReader.readFile(
                    path.toFile(),
                    Order.class,
                    order -> statisticProcessor.processStatistic(
                            order,
                            statistics,
                            config.getAttribute()
                    )
            );
            System.out.printf("Processed by '%s': %s%n",
                    config.getAttribute(),
                    path.getFileName());
        } catch (IOException e) {
            System.err.printf("Error reading file %s: %s%n",
                    path.getFileName(),
                    e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.printf("Error processing file %s: %s%n%s%n", path.getFileName(), e.getMessage(), e);
        } catch (InvalidAttributeException e) {
            executorService.shutdownNow();
            System.err.printf("InvalidAttributeException: reading invalid attribute: %s%n", e.getMessage());
        }
    }

    private void awaitCompletion() throws InterruptedException {
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        if (terminated) {
            System.out.println("\nAll files processed successfully");
        }
    }
}
