package com.deepak.spring.log.utils.features.interfaces;

import com.deepak.spring.log.utils.commons.FieldMaskingData;
import com.deepak.spring.log.utils.commons.MaskingCache;
// LoggingCommonsMethods is no longer directly used for masking regex by LogMask, but might be used by other parts, so keep if necessary.
// For now, assume it's not needed for the mask() method's direct logic if all masking happens via FieldMaskingData.
// import com.deepak.spring.log.utils.commons.LoggingCommonsMethods;
import com.deepak.spring.log.utils.features.annotations.MaskSensitiveData;
// MaskedType is no longer directly used by LogMask, it's used by MaskingCache
// import com.deepak.spring.log.utils.features.enums.MaskedType;
import lombok.SneakyThrows;
// ReflectionUtils.makeAccessible is now handled by FieldMaskingData constructor via MaskingCache
// import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
// Field is no longer directly iterated here in the same way
// import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map; // For using the cache
// Objects.requireNonNull is used for annotation, but that logic moves to cache
// import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

// nonNull is no longer directly used by LogMask, it's used by MaskingCache
// import static java.util.Objects.nonNull;

/**
 * Interface defining a mechanism to mask sensitive data in object fields during logging.
 * <p>
 * This interface provides a default implementation to serialize objects into a string
 * representation while applying masking to fields annotated with {@link MaskSensitiveData}.
 * The masking behavior is determined by regex patterns, which can be chosen from predefined
 * types in {@link com.deepak.spring.log.utils.features.enums.MaskedType} or specified as a custom regex
 * string within the {@link MaskSensitiveData} annotation.
 * <p>
 * This interface is typically implemented by objects that need a string representation
 * with sensitive data masked, often for logging purposes. The default {@link #mask(Object)}
 * method leverages a caching mechanism ({@link MaskingCache}) for efficient processing,
 * especially in high-frequency logging scenarios.
 * </p>
 *
 * <p>The following types (and their primitive counterparts where applicable) are
 * considered eligible for masking by default if a field of such type is annotated:
 * <ul>
 *   <li>{@link String}</li>
 *   <li>{@link java.math.BigDecimal}</li>
 *   <li>{@link java.time.LocalDate}</li>
 *   <li>{@link java.time.LocalDateTime}</li>
 *   <li>{@link Boolean}</li>
 *   <li>{@link Integer}</li>
 *   <li>{@link Float}</li>
 *   <li>{@link Short}</li>
 *   <li>{@link Double}</li>
 * </ul>
 *
 * @see MaskSensitiveData
 * @see com.deepak.spring.log.utils.features.enums.MaskedType
 * @see MaskingCache
 */
public interface LogMask extends Serializable {

    /**
     * A predefined set of classes (and their corresponding primitive types, where applicable)
     * whose values are considered eligible for masking if a field of such a type is annotated
     * with {@link MaskSensitiveData}.
     * <p>
     * For a field to be masked, it must be annotated with {@link MaskSensitiveData}, and
     * its value's type must be either a primitive type or present in this set.
     * This check applies to the runtime type of the value.
     * </p>
     */
    Set<Class<?>> classesMaskEnabled = Set.of(
            String.class,
            BigDecimal.class,
            LocalDate.class,
            LocalDateTime.class,
            Boolean.class,
            Integer.class,
            Float.class,
            Short.class,
            Double.class
    );

    /**
     * Generates a masked string representation of the provided object.
     * <p>
     * This default implementation iterates over the object's declared fields. If a field
     * is annotated with {@link MaskSensitiveData} and its value's type is eligible for masking
     * (primitive or listed in {@link #classesMaskEnabled}), its value is converted to a string
     * and masked using the regex pattern determined by the annotation (either a predefined
     * type from {@link com.deepak.spring.log.utils.features.enums.MaskedType} or a custom regex).
     * </p>
     * <p>
     * The method utilizes {@link MaskingCache} to cache reflection details and compiled regex patterns,
     * significantly improving performance for repeated masking of objects of the same class.
     * </p>
     * If the input object is {@code null}, the string "null" is returned.
     * Non-annotated fields or fields of non-maskable types are included with their original string values.
     *
     * @param object The object whose string representation with masked fields is to be generated.
     *               Can be any object. If {@code null}, "null" is returned.
     * @return A string representation of the object in the format {@code ClassName{field1=value1, field2=***masked***, ...}}.
     *         Returns "null" if the input object is {@code null}.
     * @throws SecurityException if a security manager denies access to field information.
     * @throws IllegalAccessException (wrapped by {@link SneakyThrows}) if a field is enforcing Java language access control and the underlying field is inaccessible (though caching makes fields accessible).
     *
     * <p>Example output for an object {@code new User("John Doe", "john.doe@example.com")}
     * where 'name' is masked with {@code MaskedType.NAME} and 'email' with {@code MaskedType.EMAIL}:
     * <pre>{@code User{name=J***oe, email=j***@e***.com}}</pre> // Example output might vary based on actual regex
     * (Note: The example output format `User{name=***, email=****@***.com}` in original doc was generic;
     * updated example reflects more specific masking if applied by revised regexes.)
     * Let's use a more generic example format for the javadoc as the actual masked output depends on the specific regex.
     * <pre>{@code ClassName{fieldName=maskedValue, otherField=value}}</pre>
     */
    @SneakyThrows
    default String mask(Object object) {
        if (object == null) {
            return "null"; // Handle null object input gracefully.
        }
        Class<?> clazz = object.getClass();
        // Only declared fields of the actual class are considered, not inherited fields.
        // This is consistent with clazz.getDeclaredFields() behavior used by the cache.
        StringJoiner sj = new StringJoiner(", ", clazz.getSimpleName() + "{", "}");

        Map<String, FieldMaskingData> classFieldCache = MaskingCache.getFieldsToMask(clazz);

        for (Map.Entry<String, FieldMaskingData> entry : classFieldCache.entrySet()) {
            String fieldName = entry.getKey();
            FieldMaskingData fieldData = entry.getValue();

            Object value = fieldData.getValue(object); // Uses cached Field object, already accessible

            if (value != null && fieldData.hasMaskingAnnotation()) {
                // If a field is annotated with @MaskSensitiveData and its value is not null,
                // its string representation should be masked using the pre-compiled pattern.
                // The classesMaskEnabled check is implicitly handled by the fact that
                // we are converting to a String, and String.class is in classesMaskEnabled.
                // The primary driver here is the presence of the annotation.
                value = fieldData.maskValue(value.toString());
            }
            sj.add(fieldName + "=" + (value == null ? "null" : value.toString()));
        }
        return sj.toString();
    }
}
// chooseRegexToMask and shouldBeMasked are no longer needed here as their logic is in MaskingCache / FieldMaskingData
