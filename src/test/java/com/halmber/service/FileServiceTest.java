package com.halmber.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    @TempDir
    Path tempDir;

    // ListFilesAsStream tests
    @Test
    void testListFilesAsStream_ReturnsOnlyFilesWithGivenExtension() throws IOException {
        Path json1 = Files.createFile(tempDir.resolve("a.json"));
        Path json2 = Files.createFile(tempDir.resolve("b.json"));
        Path txt = Files.createFile(tempDir.resolve("c.txt"));

        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = FileService.listFilesAsStream(tempDir.toString(), "jSOn")) { // check case-insensitive
            stream.forEach(result::add);
        }

        assertEquals(2, result.size());
        assertTrue(result.contains(json1));
        assertTrue(result.contains(json2));
        assertFalse(result.contains(txt));
    }

    @Test
    void testListFilesAsStream_ThrowsIfDirectoryDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (DirectoryStream<Path> stream = FileService.listFilesAsStream("non-existent-dir-123", "json")) {
                assertNotNull(stream);
            }
        });
    }

    @Test
    void testListFilesAsStream_ThrowsIfPathIsNotDirectory() throws IOException {
        Path file = Files.createFile(tempDir.resolve("notDir.txt"));

        assertThrows(IllegalArgumentException.class, () -> {
            try (DirectoryStream<Path> stream = FileService.listFilesAsStream(file.toString(), "json")) {
                assertNotNull(stream);
            }
        });
    }

    // CreateFile tests
    @Test
    void testCreateFile_CreatesFileSuccessfully() throws IOException {
        Path newFile = FileService.createFile(tempDir.toString(), "test.json");

        assertTrue(Files.exists(newFile));
        assertTrue(Files.isRegularFile(newFile));
    }

    @Test
    void testCreateFile_CreatesParentDirectories() throws IOException {
        Path deepDir = tempDir.resolve("a/b/c");

        Path created = FileService.createFile(deepDir.toString(), "file.txt");

        assertTrue(Files.exists(created));
        assertTrue(Files.exists(deepDir));
    }

    @Test
    void testCreateFile_ReplacesExistingFile() throws IOException {
        Path file = tempDir.resolve("existing.json");

        Files.writeString(file, "OLD");

        Path created = FileService.createFile(tempDir.toString(), "existing.json");

        assertTrue(Files.exists(created));
        assertEquals(0, Files.size(created)); // file size equals 0, so file replaced
    }

    @Test
    void testCreateFile_ThrowsIfFileNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                FileService.createFile(tempDir.toString(), null));
    }

    @Test
    void testCreateFile_ThrowsIfFileNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                FileService.createFile(tempDir.toString(), "   "));
    }

    @Test
    void testCreateFile_ThrowsIfCannotCreateDirectory() {
        Path dirShouldBe = tempDir.resolve("notDir");
        try {
            Files.writeString(dirShouldBe, "I am a file");
        } catch (IOException e) {
            fail(e);
        }

        assertThrows(IOException.class, () ->
                FileService.createFile(dirShouldBe.toString(), "file.txt"));
    }

    @Test
    void testCreateFile_ThrowsIfFileCannotBeDeletedOrCreated() throws IOException {
        Path file = tempDir.resolve("locked.json");
        Files.createFile(file);

        // Set "read-only" so we cant delete/create
        boolean isReadOnly = file.toFile().setReadOnly();

        assertTrue(isReadOnly);
        assertThrows(IOException.class, () ->
                FileService.createFile(tempDir.toString(), file.getFileName().toString()));
    }
}