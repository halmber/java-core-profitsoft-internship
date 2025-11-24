package com.halmber.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Service class for common file operations such as listing files in a directory
 * and creating new files. Provides utilities with proper error handling and
 * ensures directories exist when creating files.
 */
public class FileService {
    /**
     * Returns a {@link Stream} of files located inside the given directory,
     * filtered by the specified extension. The filter is applied in a
     * case-insensitive manner.
     * <p>
     * The returned stream must be closed after use, either manually or using
     * try-with-resources, to release underlying system resources.
     * </p>
     *
     * @param directoryPath the path to the directory to scan
     * @param extension     the file extension to filter by (e.g., ".json")
     * @return a {@link Stream} of {@link Path} objects representing the matching files
     * @throws IllegalArgumentException if the directory does not exist or the path
     *                                  is not a directory
     * @throws IOException              if an I/O error occurs while reading the directory
     */
    public static DirectoryStream<Path> listFilesAsStream(String directoryPath, String extension) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
        }

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path is not a directory: " + directoryPath);
        }

        return Files.newDirectoryStream(dir, "*" + extension.toLowerCase());
    }

    /**
     * Creates a new file with the specified name in the given directory.
     * <p>
     * If the parent directories do not exist, they are created automatically.
     * If a file with the same name already exists, it is deleted and replaced
     * with a new empty file.
     * </p>
     *
     * @param directoryPath the path to the directory where the file should be created
     * @param fileName      the name of the file to create
     * @return the {@link Path} to the newly created file
     * @throws IllegalArgumentException if {@code fileName} is null, empty, or blank
     * @throws IOException              if an error occurs while creating directories
     *                                  or the file itself
     */
    public static Path createFile(String directoryPath, String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty!");
        }

        Path dir = Paths.get(directoryPath);

        try {
            if (!Files.exists(dir)) {
                Path ok = Files.createDirectories(dir);
                if (!ok.toFile().exists()) {
                    throw new IOException("Failed to create directory: " + directoryPath);
                }
            }
        } catch (IOException e) {
            throw new IOException("Error occurred while creating the directory: " + directoryPath, e);
        }

        Path file = dir.resolve(fileName);

        if (Files.exists(file) && !file.toFile().canWrite()) {
            throw new IOException("File is read-only and cannot be deleted or modified: " + file);
        }

        try {
            Files.deleteIfExists(file);
            return Files.createFile(file);
        } catch (IOException e) {
            throw new IOException("Error creating or deleting existing file: " + file, e);
        }
    }
}