package com.halmber.config;

import java.util.Scanner;

/**
 * Handles user input from console for configuration parameters.
 */
public class ConsoleInputHandler {
    private final Scanner scanner;

    public ConsoleInputHandler() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Prompts user for configuration parameters and creates ApplicationConfig.
     * Uses default values if user presses Enter without input.
     */
    public ApplicationConfig getConfiguration() {
        System.out.println("====== Order Statistics Configuration ======\n");
        System.out.print("Available attributes: ");
        for (String availableAttribute : ApplicationConfig.getAvailableAttributes()) {
            System.out.printf(availableAttribute + " | ");
        }
        System.out.println();


        String inputDirectory = promptForInputDirectory();
        String attribute = promptForAttribute();
        int threads = promptForThread();

        System.out.println("\nConfiguration set:");
        System.out.printf("  Input directory: %s%n", inputDirectory);
        System.out.printf("  Attribute: %s%n", attribute);
        System.out.printf("  Output directory: %s%n", ApplicationConfig.getDefaultOutputDirectory());
        System.out.printf("  Threads count: %s%n%n", ApplicationConfig.getDefaultThreadPoolSize());

        System.out.println("====== END Order Statistics Configuration END ======\n");


        return new ApplicationConfig(
                inputDirectory,
                ApplicationConfig.getDefaultOutputDirectory(),
                attribute,
                threads
        );
    }

    private String promptForInputDirectory() {
        System.out.printf("Enter input directory path (default: %s): ",
                ApplicationConfig.getDefaultInputDirectory());
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return ApplicationConfig.getDefaultInputDirectory();
        }

        // Ensure path ends with separator
        return input.endsWith("/") ? input : input + "/";
    }

    private String promptForAttribute() {
        System.out.printf("Enter attribute name (default example: %s): ",
                ApplicationConfig.getDefaultAttribute());
        String input = scanner.nextLine().trim();

        return input.isEmpty() ? ApplicationConfig.getDefaultAttribute() : input;
    }

    private int promptForThread() {
        while (true) {
            System.out.printf("Enter threads pool size (default: %s): ",
                    ApplicationConfig.getDefaultThreadPoolSize());
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return ApplicationConfig.getDefaultThreadPoolSize();
            }

            try {
                int threadPoolSize = Integer.parseInt(input);

                if (threadPoolSize <= 0) {
                    System.err.println("\nError: Thread pool size must be greater than 0. Please try again.");
                    continue;
                }

                if (threadPoolSize > 100) {
                    System.out.printf("Warning: Thread pool size %d is very large. Are you sure? (y/n): ", threadPoolSize);
                    String confirmation = scanner.nextLine().trim().toLowerCase();
                    if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                        continue;
                    }
                }

                return threadPoolSize;

            } catch (NumberFormatException e) {
                System.err.printf("\nError: '%s' is not a valid number. Please enter a positive integer.%n", input);
            }
        }
    }
}