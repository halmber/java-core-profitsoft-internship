package com.halmber.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileReaderTest {
    static class Person {
        @JsonProperty
        public String name;

        @JsonProperty
        public int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    private File writeJson(Path dir, String fileName, String content) throws IOException {
        File file = dir.resolve(fileName).toFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }
        return file;
    }

    // Success test
    @Test
    void testReadFile_SuccessfullyReadsArray(@TempDir Path tempDir) throws IOException {
        String json = """
                    [
                      {"name": "John", "age": 30},
                      {"name": "Alice", "age": 25}
                    ]
                """;

        File file = writeJson(tempDir, "people.json", json);

        List<Person> result = new ArrayList<>();

        JsonFileReader.readFile(file, Person.class, result::add);

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).name);
        assertEquals(30, result.get(0).age);
        assertEquals("Alice", result.get(1).name);
        assertEquals(25, result.get(1).age);
    }

    // Invalid JSON: not an array
    @Test
    void testReadFile_ThrowsIfNotArray(@TempDir Path tempDir) throws IOException {
        String json = """
                    {"name": "Not", "age": 10}
                """;

        File file = writeJson(tempDir, "wrong.json", json);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                JsonFileReader.readFile(file, Person.class, p -> {
                })
        );

        assertTrue(ex.getMessage().contains("JSON must start with array"));
    }

    // Unknown fields should be ignored (FAIL_ON_UNKNOWN_PROPERTIES = false)
    @Test
    void testReadFile_IgnoresUnknownFieldsInObject(@TempDir Path tempDir) throws IOException {
        String json = """
                    [
                      {"name": "John", "age": 30},
                      {"name": "Alice", "age": 25, "unknownField": "ignored"},
                      {"unknownField": "ignored"}
                    ]
                """;

        File file = writeJson(tempDir, "ignoredFields.json", json);

        List<Person> people = new ArrayList<>();

        // Should NOT throw — unknown fields must be ignored
        JsonFileReader.readFile(file, Person.class, people::add);

        assertEquals(3, people.size());

        // First element
        assertEquals("John", people.get(0).name);
        assertEquals(30, people.get(0).age);

        // Second element
        assertEquals("Alice", people.get(1).name);
        assertEquals(25, people.get(1).age);

        // Third element
        assertNull(people.get(2).name);
        assertEquals(0, people.get(2).age);
    }

    // Bad JSON (malformed)
    @Test
    void testReadFile_ThrowsOnMalformedJson(@TempDir Path tempDir) throws IOException {
        String badJson = """
                    [
                      {"name": "John", "age": 30},
                      {BAD JSON HERE}
                    ]
                """;

        File file = writeJson(tempDir, "malformed.json", badJson);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                JsonFileReader.readFile(file, Person.class, p -> {
                })
        );

        assertTrue(ex.getMessage().contains("Failed to deserialize element"));
    }

    // Consumer is invoked correctly
    @Test
    void testReadFile_ConsumerCalledCorrectly(@TempDir Path tempDir) throws IOException {
        String json = """
                    [
                      {"name": "John", "age": 30},
                      {"name": "Bob", "age": 40},
                      {"name": "Alice", "age": 22}
                    ]
                """;

        File file = writeJson(tempDir, "people3.json", json);

        List<String> names = new ArrayList<>();

        JsonFileReader.readFile(file, Person.class, p -> names.add(p.name));

        assertEquals(List.of("John", "Bob", "Alice"), names);
    }
}
