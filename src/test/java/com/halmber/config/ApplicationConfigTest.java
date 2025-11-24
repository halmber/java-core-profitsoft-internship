package com.halmber.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigTest {

    @Test
    void testDefaultConstructor_CreatesConfigWithDefaultValues() {
        ApplicationConfig config = new ApplicationConfig();

        assertEquals("src/main/resources/", config.getInputDirectory());
        assertEquals("src/main/resources/outputFiles", config.getOutputDirectory());
        assertEquals("id", config.getAttribute());
        assertEquals(8, config.getThreadPoolSize());
    }

    @Test
    void testParameterizedConstructor_CreatesConfigWithCustomValues() {
        ApplicationConfig config = new ApplicationConfig(
                "custom/input/",
                "custom/output/",
                "status",
                16
        );

        assertEquals("custom/input/", config.getInputDirectory());
        assertEquals("custom/output/", config.getOutputDirectory());
        assertEquals("status", config.getAttribute());
        assertEquals(16, config.getThreadPoolSize());
    }

    @Test
    void testGetOutputFileName_GeneratesCorrectFileName() {
        ApplicationConfig config = new ApplicationConfig(
                "input/",
                "output/",
                "city",
                4
        );

        assertEquals("statistics_by_city.xml", config.getOutputFileName());
    }

    @Test
    void testGetJsonFileType_ReturnsJson() {
        ApplicationConfig config = new ApplicationConfig();
        assertEquals("json", config.getJsonFileType());
    }

    @Test
    void testStaticGetters_ReturnDefaultValues() {
        assertEquals("src/main/resources/", ApplicationConfig.getDefaultInputDirectory());
        assertEquals("src/main/resources/outputFiles", ApplicationConfig.getDefaultOutputDirectory());
        assertEquals("id", ApplicationConfig.getDefaultAttribute());
        assertEquals(8, ApplicationConfig.getDefaultThreadPoolSize());
    }

    @Test
    void testGetAvailableAttributes_ReturnsAllAttributes() {
        String[] attributes = ApplicationConfig.getAvailableAttributes();

        assertNotNull(attributes);
        assertEquals(8, attributes.length);
        assertArrayEquals(
                new String[]{"id", "status", "tags", "paymentMethod", "fullName", "email", "phone", "city"},
                attributes
        );
    }

    @Test
    void testGetOutputFileName_WithDifferentAttributes() {
        String[] attributes = {"id", "status", "tags", "email"};

        for (String attr : attributes) {
            ApplicationConfig config = new ApplicationConfig("in/", "out/", attr, 4);
            assertEquals("statistics_by_" + attr + ".xml", config.getOutputFileName());
        }
    }
}

