package com.deepak.spring.log.utils.commons;

import com.deepak.spring.log.utils.features.annotations.MaskSensitiveData;
import com.deepak.spring.log.utils.features.enums.MaskedType;
import com.deepak.spring.log.utils.features.interfaces.LogMask; // Required to access LogMask.classesMaskEnabled

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

/**
 * A thread-safe cache for storing masking metadata associated with classes.
 * This class significantly optimizes the masking process by caching reflection data
 * (like {@link Field} objects and their annotations) and pre-compiled regex {@link Pattern}s.
 * <p>
 * When a class is processed for masking for the first time, its fields are introspected,
 * and relevant information for masking (such as which fields to mask, what regex to use, etc.)
 * is stored in {@link FieldMaskingData} objects. These are then stored in a map associated
 * with the class. Subsequent requests to mask objects of the same class will retrieve this
 * cached information, avoiding costly reflection and regex compilation operations.
 * <p>
 * The cache is implemented using a {@link ConcurrentHashMap} to ensure thread safety.
 * The values (maps of field data) are immutable after creation.
 *
 * @see FieldMaskingData
 * @see com.deepak.spring.log.utils.features.interfaces.LogMask#mask(Object)
 */
public class MaskingCache {

    // Cache: Class -> Map<FieldName, FieldMaskingData>
    // This map stores the cached masking information for each class.
    // The key is the Class object, and the value is an unmodifiable Map where keys are field names
    // and values are FieldMaskingData objects containing all necessary info for masking that field.
    private static final ConcurrentHashMap<Class<?>, Map<String, FieldMaskingData>> CACHE = new ConcurrentHashMap<>();

    /**
     * Retrieves a map of {@link FieldMaskingData} for the given class, containing all
     * necessary information to perform masking operations on its fields.
     * <p>
     * If the class's masking data is not already in the cache, this method will trigger
     * the building of this data (by calling {@link #buildCacheForClass(Class)}) and
     * store it in the cache for future use.
     *
     * @param clazz The class for which to retrieve masking field data. Must not be null.
     * @return An unmodifiable {@link Map} where keys are field names and values are {@link FieldMaskingData} objects.
     *         Returns an empty map if the class has no fields.
     * @throws IllegalArgumentException if clazz is null.
     */
    public static Map<String, FieldMaskingData> getFieldsToMask(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null.");
        }
        return CACHE.computeIfAbsent(clazz, MaskingCache::buildCacheForClass);
    }

    /**
     * Builds the masking metadata for a given class.
     * This method iterates over all declared fields of the class, determines if they
     * need masking based on the presence of {@link MaskSensitiveData} annotation,
     * chooses the appropriate regex (custom or from {@link MaskedType}), compiles it,
     * and stores all this information in {@link FieldMaskingData} objects.
     * <p>
     * This method is called by {@link #getFieldsToMask(Class)} when a class's metadata
     * is not found in the cache (i.e., on a cache miss).
     *
     * @param clazz The class for which to build the masking field data.
     * @return An unmodifiable {@link Map} of field names to their {@link FieldMaskingData}.
     */
    private static Map<String, FieldMaskingData> buildCacheForClass(Class<?> clazz) {
        Map<String, FieldMaskingData> fieldCache = new HashMap<>();
        // clazz.getDeclaredFields() does not return fields from superclasses.
        // This is consistent with the original behavior of LogMask.mask().
        // If masking fields from superclasses is desired, the logic here would need to change
        // to walk up the class hierarchy (e.g., using getFields() or iterating clazz.getSuperclass()).
        for (Field field : clazz.getDeclaredFields()) {
            MaskSensitiveData annotation = field.getAnnotation(MaskSensitiveData.class);

            // Check if the field's declared type is eligible for masking (primitive or in classesMaskEnabled)
            // This is a static check based on field type.
            boolean fieldTypePotentiallyMaskable = field.getType().isPrimitive() ||
                                                   LogMask.classesMaskEnabled.contains(field.getType()) ||
                                                   (field.getType().isArray() && field.getType().getComponentType().isPrimitive()) ||
                                                   (field.getType().isArray() && LogMask.classesMaskEnabled.contains(field.getType().getComponentType()));


            if (annotation != null) {
                String regex = chooseRegexToMask(annotation);
                Pattern compiledPattern = Pattern.compile(regex);
                // Field is made accessible within FieldMaskingData constructor
                fieldCache.put(field.getName(), new FieldMaskingData(field, true, compiledPattern, fieldTypePotentiallyMaskable));
            } else {
                // Store even non-masked fields to avoid re-checking for annotations later,
                // and to have the Field object readily available and accessible.
                fieldCache.put(field.getName(), new FieldMaskingData(field, false, null, fieldTypePotentiallyMaskable));
            }
        }
        // Return an unmodifiable map as the cache structure for a class should not change after creation.
        return Collections.unmodifiableMap(fieldCache);
    }

    private static String chooseRegexToMask(MaskSensitiveData maskSensitiveData) {
        if (nonNull(maskSensitiveData.customMaskRegex()) && !maskSensitiveData.customMaskRegex().isBlank()) {
            return maskSensitiveData.customMaskRegex();
        }
        // Ensure maskedType is not null, though it shouldn't be based on annotation definition
        Objects.requireNonNull(maskSensitiveData.maskedType(), "MaskedType cannot be null in @MaskSensitiveData");
        return maskSensitiveData.maskedType().getRegex();
    }

    /**
     * Clears the entire cache. Potentially useful for testing or specific lifecycle events.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Clears the cache for a specific class.
     * @param clazz The class for which to clear the cache.
     */
    public static void clearCacheForClass(Class<?> clazz) {
        CACHE.remove(clazz);
    }
}
