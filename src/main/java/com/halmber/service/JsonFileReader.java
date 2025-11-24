package com.halmber.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static com.halmber.utils.DefaultObjectMapper.OBJECT_MAPPER;

/**
 * Utility class for reading JSON files containing an array of objects.
 * Provides a generic method to process each element as a specific type.
 */
public class JsonFileReader {
    private static final JsonFactory jsonFactory = new JsonFactory();

    /**
     * Reads a JSON file expected to contain a top-level array of objects.
     * Each object is deserialized to the specified class type and passed
     * to the provided {@link Consumer} for processing.
     * <p>
     * The method validates that each deserialized object is an instance of {@code clazz}.
     * If any object fails the type check, an {@link IllegalArgumentException} is thrown
     * after logging the invalid entry.
     * </p>
     *
     * @param <T>      the type of objects to deserialize
     * @param file     the JSON file to read
     * @param clazz    the target class of deserialized objects
     * @param consumer a consumer to process each deserialized object
     * @throws IOException              if an I/O error occurs during reading
     * @throws IllegalArgumentException if the JSON does not start with an array
     *                                  or an object is not an instance of {@code clazz}
     */
    public static <T> void readFile(File file, Class<T> clazz, Consumer<T> consumer) throws IOException, IllegalArgumentException {
        try (JsonParser jsonParser = jsonFactory.createParser(file)) {
            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("JSON must start with array.");
            }

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                T obj;
                try {
                    obj = OBJECT_MAPPER.readValue(jsonParser, clazz);

                    if (!clazz.isInstance(obj)) {
                        throw new IllegalArgumentException(String.format("Object %s is not of type %s", obj, clazz.getName()));
                    } else {
                        consumer.accept(obj);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format("Failed to deserialize element. %s%n", e.getMessage()));
                }
            }
        }
    }
}
