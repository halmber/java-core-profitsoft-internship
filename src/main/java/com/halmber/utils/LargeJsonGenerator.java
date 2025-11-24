package com.halmber.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LargeJsonGenerator {

    private static final Random random = new Random();

    private static final List<String> names = List.of(
            "Bohdan Roh", "Ivan Petrov", "Petro Poroh", "Olena Kvitka",
            "Stepan Mazur", "Ihor Bobyl", "Maryna Fox", "Katya Smile"
    );

    private static final List<String> cities = List.of(
            "Lviv", "Kyiv", "Odesa", "Kharkiv", "Kropyvnytskyi", "Dnipro"
    );

    private static final List<String> statuses = List.of(
            "NEW", "DONE", "CANCELLED", "PROCESSING"
    );

    private static List<String> tags = List.of(
            "gift", "urgent", "newCustomer", "vip", "wholesale"
    );

    private static <T> T randomItem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private static String generateTags() {
        int count = 1 + random.nextInt(3);
        ArrayList<String> shuffledTags = new ArrayList<>(List.copyOf(tags));
        Collections.shuffle(shuffledTags);
        return String.join(", ", shuffledTags.subList(0, count));
    }

    private static double randomAmount() {
        return 100 + random.nextInt(900) + random.nextDouble();
    }

    /**
     * Generates an extremely large JSON file using Jackson streaming API.
     *
     * @param filePath path to output file
     * @param count    number of JSON objects to write
     */
    public static void generateLargeJson(String filePath, long count) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        JsonFactory factory = new JsonFactory();

        try (FileOutputStream fos = new FileOutputStream(file);
             JsonGenerator gen = factory.createGenerator(fos)) {

            gen.useDefaultPrettyPrinter();

            gen.writeStartArray(); // [

            for (long i = 1; i <= count; i++) {

                gen.writeStartObject(); // {

                gen.writeStringField("id", "ord-" + i);

                // Customer object
                gen.writeObjectFieldStart("customer");
                gen.writeStringField("id", "cust-" + i);
                gen.writeStringField("fullName", randomItem(names));
                gen.writeStringField("email", "user" + i + "@example.com");
                gen.writeStringField("phone", "+38050" + (1000000 + random.nextInt(9000000)));
                gen.writeStringField("city", randomItem(cities));
                gen.writeEndObject();

                gen.writeStringField("status", randomItem(statuses));
                gen.writeStringField("tags", generateTags());
                gen.writeNumberField("amount", randomAmount());
                gen.writeStringField("paymentMethod", random.nextBoolean() ? "card" : "cash");
                gen.writeNumberField("createdAt", System.currentTimeMillis() / 1000);

                gen.writeEndObject(); // }

                if (i % 1_000_000 == 0) {
                    System.out.println("Generated " + i + " orders...");
                }
            }

            gen.writeEndArray(); // ]
        }

        System.out.println("Done! File generated: " + file.getAbsolutePath());
    }

    public static void main(String[] args) throws Exception {
        // Example: generate 1 million orders, +-340 MB
        generateLargeJson("src/main/resources/large_orders.json", 1_000_000L);
    }
}
