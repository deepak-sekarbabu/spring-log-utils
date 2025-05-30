package com.deepak.spring.log.utils.commons;

import com.deepak.spring.log.utils.features.annotations.MaskSensitiveData;
import com.deepak.spring.log.utils.features.enums.MaskedType;
import com.deepak.spring.log.utils.features.interfaces.LogMask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaskingCache Tests")
class MaskingCacheTest {

    @BeforeEach
    @AfterEach
    void clearCache() {
        // Clear cache before and after each test for isolation
        MaskingCache.clearCache();
    }

    static class TestClassNoAnnotations {
        private String fieldA;
        private int fieldB;
    }

    static class TestClassWithAnnotations {
        @MaskSensitiveData(maskedType = MaskedType.NAME)
        private String name;

        @MaskSensitiveData(customMaskRegex = "\\d") // Mask all digits
        private String accountNumber;

        private String nonSensitiveField;

        @MaskSensitiveData(maskedType = MaskedType.ALL)
        protected Object genericField; // Type is Object, but annotation is present
    }

    static class TestClassForFieldTypeCheck implements LogMask { // Implement LogMask to access classesMaskEnabled
        @MaskSensitiveData(maskedType = MaskedType.ALL)
        String maskableStringField; // Type is in classesMaskEnabled

        @MaskSensitiveData(maskedType = MaskedType.ALL)
        Integer maskableIntegerField; // Type is in classesMaskEnabled

        @MaskSensitiveData(maskedType = MaskedType.ALL)
        int primitiveIntField; // Primitive type

        @MaskSensitiveData(maskedType = MaskedType.ALL)
        CustomNonMaskableType customField; // Type not in classesMaskEnabled
    }

    static class CustomNonMaskableType {
        String data;
    }


    @Test
    @DisplayName("Cache retrieves same FieldMaskingData map for same class")
    void testCacheRetrieval() {
        Map<String, FieldMaskingData> data1 = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        Map<String, FieldMaskingData> data2 = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        assertSame(data1, data2, "Should return the same map instance from cache");
    }

    @Test
    @DisplayName("Cache computes different FieldMaskingData maps for different classes")
    void testDifferentClassesGetDifferentCache() {
        Map<String, FieldMaskingData> data1 = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        Map<String, FieldMaskingData> data2 = MaskingCache.getFieldsToMask(TestClassNoAnnotations.class);
        assertNotSame(data1, data2, "Should return different map instances for different classes");
    }

    @Test
    @DisplayName("Cache clear removes entries")
    void testCacheClear() {
        Map<String, FieldMaskingData> data1 = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        assertFalse(data1.isEmpty(), "Cache should have data before clear");

        MaskingCache.clearCacheForClass(TestClassWithAnnotations.class);
        Map<String, FieldMaskingData> data2 = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        assertNotSame(data1, data2, "Should be a new map instance after clearing for that class and re-fetching");

        // Re-populate for global clear test
        MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        MaskingCache.getFieldsToMask(TestClassNoAnnotations.class);

        MaskingCache.clearCache();
        // This is tricky, as computeIfAbsent might return a new empty map if we query after clear.
        // The real test is that if we add it again, it's a new instance.
        Map<String, FieldMaskingData> data3 = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        assertNotSame(data2, data3, "Cache should be new after global clear and re-fetch");
        // And the cache for TestClassNoAnnotations should also be new or empty
        Map<String, FieldMaskingData> data4 = MaskingCache.getFieldsToMask(TestClassNoAnnotations.class);
        assertNotNull(data4); // It will be an empty map if no fields, or a map for its fields.
    }

    @Test
    @DisplayName("FieldMaskingData correctly built for class with annotations")
    void testFieldMaskingDataContent_WithAnnotations() {
        Map<String, FieldMaskingData> fieldDataMap = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);

        assertEquals(4, fieldDataMap.size(), "Should have entries for all declared fields");

        FieldMaskingData nameData = fieldDataMap.get("name");
        assertNotNull(nameData);
        assertTrue(nameData.hasMaskingAnnotation());
        assertEquals(MaskedType.NAME.getRegex(), nameData.getCompiledPattern().pattern());
        assertTrue(nameData.isFieldTypePotentiallyMaskable(), "String type should be potentially maskable");

        FieldMaskingData accountData = fieldDataMap.get("accountNumber");
        assertNotNull(accountData);
        assertTrue(accountData.hasMaskingAnnotation());
        assertEquals("\\d", accountData.getCompiledPattern().pattern());
        assertTrue(accountData.isFieldTypePotentiallyMaskable(), "String type should be potentially maskable");


        FieldMaskingData nonSensitiveData = fieldDataMap.get("nonSensitiveField");
        assertNotNull(nonSensitiveData);
        assertFalse(nonSensitiveData.hasMaskingAnnotation());
        assertNull(nonSensitiveData.getCompiledPattern());
        assertTrue(nonSensitiveData.isFieldTypePotentiallyMaskable(), "String type should be potentially maskable");

        FieldMaskingData genericFieldData = fieldDataMap.get("genericField");
        assertNotNull(genericFieldData);
        assertTrue(genericFieldData.hasMaskingAnnotation());
        assertEquals(MaskedType.ALL.getRegex(), genericFieldData.getCompiledPattern().pattern());
        assertFalse(genericFieldData.isFieldTypePotentiallyMaskable(), "Object type should not be in default classesMaskEnabled");

    }

    @Test
    @DisplayName("FieldMaskingData correctly identifies fieldTypePotentiallyMaskable")
    void testFieldTypePotentiallyMaskable() {
        Map<String, FieldMaskingData> fieldDataMap = MaskingCache.getFieldsToMask(TestClassForFieldTypeCheck.class);

        FieldMaskingData stringFieldData = fieldDataMap.get("maskableStringField");
        assertNotNull(stringFieldData);
        assertTrue(stringFieldData.hasMaskingAnnotation());
        assertTrue(stringFieldData.isFieldTypePotentiallyMaskable(), "String field should be potentially maskable");

        FieldMaskingData integerFieldData = fieldDataMap.get("maskableIntegerField");
        assertNotNull(integerFieldData);
        assertTrue(integerFieldData.hasMaskingAnnotation());
        assertTrue(integerFieldData.isFieldTypePotentiallyMaskable(), "Integer field should be potentially maskable");

        FieldMaskingData intFieldData = fieldDataMap.get("primitiveIntField");
        assertNotNull(intFieldData);
        assertTrue(intFieldData.hasMaskingAnnotation());
        assertTrue(intFieldData.isFieldTypePotentiallyMaskable(), "primitive int field should be potentially maskable");

        FieldMaskingData customFieldData = fieldDataMap.get("customField");
        assertNotNull(customFieldData);
        assertTrue(customFieldData.hasMaskingAnnotation()); // Annotation is present
        assertFalse(customFieldData.isFieldTypePotentiallyMaskable(), "CustomNonMaskableType field should not be classified as fieldTypePotentiallyMaskable by default");
    }


    @Test
    @DisplayName("FieldMaskingData correctly built for class with no annotations")
    void testFieldMaskingDataContent_NoAnnotations() {
        Map<String, FieldMaskingData> fieldDataMap = MaskingCache.getFieldsToMask(TestClassNoAnnotations.class);

        assertEquals(2, fieldDataMap.size(), "Should have entries for all fields");

        FieldMaskingData fieldAData = fieldDataMap.get("fieldA");
        assertNotNull(fieldAData);
        assertFalse(fieldAData.hasMaskingAnnotation());
        assertNull(fieldAData.getCompiledPattern());
        assertTrue(fieldAData.isFieldTypePotentiallyMaskable(), "String should be maskable type");


        FieldMaskingData fieldBData = fieldDataMap.get("fieldB");
        assertNotNull(fieldBData);
        assertFalse(fieldBData.hasMaskingAnnotation());
        assertNull(fieldBData.getCompiledPattern());
        assertTrue(fieldBData.isFieldTypePotentiallyMaskable(), "int should be maskable type");
    }

    @Test
    @DisplayName("Field objects in FieldMaskingData are accessible")
    void testFieldAccessibility() throws NoSuchFieldException {
        Map<String, FieldMaskingData> fieldDataMap = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        FieldMaskingData nameData = fieldDataMap.get("name");
        assertNotNull(nameData);
        Field reflectField = TestClassWithAnnotations.class.getDeclaredField("name");
        // FieldMaskingData constructor calls setAccessible(true)
        assertTrue(nameData.getField().canAccess(new TestClassWithAnnotations()), "Field should be accessible");
        assertTrue(reflectField.canAccess(new TestClassWithAnnotations()) || nameData.getField().isAccessible(),
                   "Field from FieldMaskingData should be explicitly made accessible");
    }

    @Test
    @DisplayName("Basic concurrency test for cache access")
    void testCacheConcurrency() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Define a list of classes to be accessed by different threads
        Class<?>[] classesToTest = {
                TestClassNoAnnotations.class,
                TestClassWithAnnotations.class,
                TestClassForFieldTypeCheck.class,
                AnotherTestClass1.class, // Add more diverse classes
                AnotherTestClass2.class
        };

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            service.submit(() -> {
                try {
                    // Each thread accesses a specific class from the array
                    Class<?> clazz = classesToTest[threadIndex % classesToTest.length];
                    Map<String, FieldMaskingData> fieldData = MaskingCache.getFieldsToMask(clazz);
                    assertNotNull(fieldData, "Field data map should not be null for " + clazz.getSimpleName());

                    // Perform some basic validation on one of the fields if possible
                    // This part depends on knowing field names, which might make the test brittle
                    // For now, just ensure the map itself is consistent.
                    if (clazz == TestClassWithAnnotations.class) {
                        assertTrue(fieldData.containsKey("name"));
                        assertEquals(MaskedType.NAME.getRegex(), fieldData.get("name").getCompiledPattern().pattern());
                    }

                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Threads did not complete in time");
        service.shutdown();

        // Post-concurrency check: ensure cache integrity for one of the classes
        Map<String, FieldMaskingData> data = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        assertNotNull(data.get("name"));
        assertEquals(MaskedType.NAME.getRegex(), data.get("name").getCompiledPattern().pattern());

        // Verify that distinct classes still have distinct cache entries after concurrent access
        Map<String, FieldMaskingData> dataAnnotations = MaskingCache.getFieldsToMask(TestClassWithAnnotations.class);
        Map<String, FieldMaskingData> dataNoAnnotations = MaskingCache.getFieldsToMask(TestClassNoAnnotations.class);
        assertNotSame(dataAnnotations, dataNoAnnotations, "Cache entries for different classes should be distinct.");
        assertSame(dataAnnotations, MaskingCache.getFieldsToMask(TestClassWithAnnotations.class), "Cache should still return same instance for TestClassWithAnnotations");

    }

    // Additional classes for concurrency test
    static class AnotherTestClass1 { String data1; }
    static class AnotherTestClass2 { String data2; @MaskSensitiveData(maskedType = MaskedType.DOCUMENT) String doc; }

}
