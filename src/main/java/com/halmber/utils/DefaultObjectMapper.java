package com.halmber.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class DefaultObjectMapper {
    public static final ObjectMapper OBJECT_MAPPER;

    static {
        ObjectMapper mapper = new ObjectMapper();
        // Don't throw exception if json has extra fields without serialization.
        // This is useful when you want to use pojo to deserialize and only cares
        // about the json part
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Ignore null values when writing json.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Write the time as a string instead of long so that it is human-readable.
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Fail on invalid types
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);
        // Fail on null
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        OBJECT_MAPPER = mapper;
    }


}
