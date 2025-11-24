package com.halmber.config;

import lombok.Getter;

/**
 * Configuration holder for application settings.
 * Encapsulates all configuration parameters in one place.
 */
@Getter
public class ApplicationConfig {
    private static final int DEFAULT_THREAD_POOL_SIZE = 8;
    private static final String DEFAULT_INPUT_DIRECTORY = "src/main/resources/";
    private static final String DEFAULT_OUTPUT_DIRECTORY = "src/main/resources/outputFiles";
    private static final String DEFAULT_ATTRIBUTE = "id";
    private static final String XML_FILE_NAME_PREFIX = "statistics_by_";
    private static final String XML_FILE_TYPE = "xml";
    private static final String JSON_FILE_TYPE = "json";
    private static final String[] AVAILABLE_ATTRIBUTES = {"id", "status", "tags", "paymentMethod", "fullName", "email", "phone", "city"};

    private final String inputDirectory;
    private final String outputDirectory;
    private final String attribute;
    private final int threadPoolSize;

    public ApplicationConfig() {
        this(DEFAULT_INPUT_DIRECTORY, DEFAULT_OUTPUT_DIRECTORY, DEFAULT_ATTRIBUTE, DEFAULT_THREAD_POOL_SIZE);
    }

    public ApplicationConfig(String inputDirectory, String outputDirectory, String attribute, int threadPoolSize) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.attribute = attribute;
        this.threadPoolSize = threadPoolSize;
    }

    public String getOutputFileName() {
        return XML_FILE_NAME_PREFIX + attribute + "." + XML_FILE_TYPE;
    }

    public String getJsonFileType() {
        return JSON_FILE_TYPE;
    }

    public static String getDefaultInputDirectory() {
        return DEFAULT_INPUT_DIRECTORY;
    }

    public static String getDefaultOutputDirectory() {
        return DEFAULT_OUTPUT_DIRECTORY;
    }

    public static String getDefaultAttribute() {
        return DEFAULT_ATTRIBUTE;
    }

    public static int getDefaultThreadPoolSize() {
        return DEFAULT_THREAD_POOL_SIZE;
    }

    public static String[] getAvailableAttributes() {
        return AVAILABLE_ATTRIBUTES;
    }
}