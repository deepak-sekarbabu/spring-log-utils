package com.deepak.spring.log.utils.commons;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Holds cached reflection and masking information for a specific field.
 * This object stores pre-computed data like the {@link Field} object itself (made accessible),
 * whether the field is annotated for masking, the compiled regex {@link Pattern} for masking,
 * and whether the field's declared type is generally eligible for masking.
 * <p>
 * Instances of this class are created and cached by {@link MaskingCache} to optimize
 * the logging and masking process by reducing redundant reflection and regex compilation.
 */
public class FieldMaskingData {
    private final Field field;
    private final boolean hasMaskingAnnotation;
    private final Pattern compiledPattern;
    private final boolean fieldTypePotentiallyMaskable;

    /**
     * Constructs a new {@code FieldMaskingData} instance.
     * The constructor also makes the provided {@link Field} accessible to improve performance
     * during value retrieval.
     *
     * @param field The {@link Field} object this instance represents. Must not be null.
     * @param hasMaskingAnnotation {@code true} if the field is annotated with {@link com.deepak.spring.log.utils.features.annotations.MaskSensitiveData}, {@code false} otherwise.
     * @param compiledPattern The pre-compiled regex {@link Pattern} to be used for masking this field's value. Can be {@code null} if {@code hasMaskingAnnotation} is false or no specific pattern is applicable.
     * @param fieldTypePotentiallyMaskable {@code true} if the declared type of the field is considered generally maskable (e.g., String, Number, or a primitive type listed in {@link com.deepak.spring.log.utils.features.interfaces.LogMask#classesMaskEnabled}).
     */
    public FieldMaskingData(Field field, boolean hasMaskingAnnotation, Pattern compiledPattern, boolean fieldTypePotentiallyMaskable) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null.");
        }
        this.field = field;
        this.hasMaskingAnnotation = hasMaskingAnnotation;
        this.compiledPattern = compiledPattern;
        this.fieldTypePotentiallyMaskable = fieldTypePotentiallyMaskable;
        // Ensure field is accessible, done once during cache building
        this.field.setAccessible(true);
    }

    /**
     * Gets the cached {@link Field} object.
     * The field has already been made accessible.
     *
     * @return The {@link Field} object.
     */
    public Field getField() {
        return field;
    }

    /**
     * Checks if the field was annotated with {@link com.deepak.spring.log.utils.features.annotations.MaskSensitiveData}.
     *
     * @return {@code true} if the field has the masking annotation, {@code false} otherwise.
     */
    public boolean hasMaskingAnnotation() {
        return hasMaskingAnnotation;
    }

    /**
     * Gets the pre-compiled regex {@link Pattern} for masking this field's value.
     *
     * @return The compiled {@link Pattern}, or {@code null} if no masking pattern is applicable for this field.
     */
    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    /**
     * Indicates if the declared type of the field is one that is generally considered eligible for masking
     * (e.g., String, Number, or a type included in {@link com.deepak.spring.log.utils.features.interfaces.LogMask#classesMaskEnabled}).
     * This does not consider the actual runtime type of the field's value if the field is declared as a generic type like {@code Object}.
     *
     * @return {@code true} if the field's declared type is potentially maskable, {@code false} otherwise.
     */
    public boolean isFieldTypePotentiallyMaskable() {
        return fieldTypePotentiallyMaskable;
    }

    /**
     * Retrieves the value of this field from the given object.
     * Uses the cached and accessible {@link Field} object for efficient value retrieval.
     *
     * @param obj The object from which to retrieve the field's value.
     * @return The value of the field in the specified object.
     * @throws IllegalAccessException if this {@code Field} object is enforcing Java language access control and the underlying field is inaccessible.
     */
    public Object getValue(Object obj) throws IllegalAccessException {
        return field.get(obj);
    }

    /**
     * Masks the given string value using the compiled pattern stored in this object.
     * If no compiled pattern is available, or if the input value is {@code null},
     * the original value string is returned.
     *
     * @param valueToString The string representation of the field's value to be masked.
     * @return The masked string, or the original string if no masking is applied.
     */
    public String maskValue(String valueToString) {
        if (compiledPattern == null || valueToString == null) {
            return valueToString;
        }
        return compiledPattern.matcher(valueToString).replaceAll("*");
    }
}
