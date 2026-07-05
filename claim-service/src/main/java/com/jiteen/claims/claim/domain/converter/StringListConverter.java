package com.jiteen.claims.claim.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

/**
 * JPA {@link AttributeConverter} that maps a {@code List<String>} in the Java domain
 * model to a JSON-encoded {@code TEXT} column in the PostgreSQL database.
 *
 * <p>
 * This converter is automatically applied to any entity field annotated with
 * {@link jakarta.persistence.Convert @Convert(converter = StringListConverter.class)}.
 * It serializes the list as a compact JSON array string on write and deserializes
 * it back on read, preserving all list contents without requiring a join table.
 * </p>
 *
 * <p>
 * Used by {@code AiAnalysisResult} to persist {@code fraudIndicators} and
 * {@code missingDocuments} lists within a single TEXT column.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Serializes a {@code List<String>} into a JSON array string for database storage.
     *
     * @param attribute the list to serialize; may be {@code null} or empty
     * @return a JSON array string, or {@code "[]"} if the attribute is null or empty
     */
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * Deserializes a JSON array string from the database back into a {@code List<String>}.
     *
     * @param dbData the raw JSON string from the database column; may be {@code null}
     * @return the deserialized list, or an empty list if the data is null or malformed
     */
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
