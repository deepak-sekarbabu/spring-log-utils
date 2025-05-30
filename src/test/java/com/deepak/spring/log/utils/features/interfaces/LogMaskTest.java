package com.deepak.spring.log.utils.features.interfaces;

import com.deepak.spring.log.utils.commons.MaskingCache;
import com.deepak.spring.log.utils.features.annotations.MaskSensitiveData;
import com.deepak.spring.log.utils.features.enums.MaskedType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map; // Added import for Map
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame; // Added import for assertSame

@DisplayName("LogMask Interface Tests")
class LogMaskTest {

    // Helper class implementing LogMask for testing
    static class TestUser implements LogMask {
        @MaskSensitiveData(maskedType = MaskedType.NAME)
        private String name;

        @MaskSensitiveData(maskedType = MaskedType.EMAIL)
        private String email;

        @MaskSensitiveData(customMaskRegex = "\\d(?=\\d{2})") // Mask digits except last two
        private String phoneNumber;

        private int age; // Not annotated

        @MaskSensitiveData(maskedType = MaskedType.ALL)
        private String address;

        @MaskSensitiveData(maskedType = MaskedType.NUMBER) // Will mask all digits
        private Integer accountBalance;

        @MaskSensitiveData(maskedType = MaskedType.DATE)
        private LocalDate joinDate;

        // Field of a type not in classesMaskEnabled by default in LogMask, but annotated
        @MaskSensitiveData(maskedType = MaskedType.ALL)
        private CustomType customObject;

        // Field of a maskable type (String) but value is null
        @MaskSensitiveData(maskedType = MaskedType.ALL)
        private String nullField;


        public TestUser(String name, String email, String phoneNumber, int age, String address, Integer balance, LocalDate joinDate, CustomType customObject) {
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.age = age;
            this.address = address;
            this.accountBalance = balance;
            this.joinDate = joinDate;
            this.customObject = customObject;
            this.nullField = null;
        }

        @Override
        public String toString() {
            return mask(this); // Uses the default method from LogMask
        }
    }

    static class CustomType {
        String value = "customData";
        @Override
        public String toString() { return value; }
    }

    static class TestSimple implements LogMask {
        @MaskSensitiveData(maskedType = MaskedType.ALL)
        String simpleField;

        public TestSimple(String simpleField) {
            this.simpleField = simpleField;
        }
         @Override
        public String toString() {
            return mask(this);
        }
    }


    @BeforeEach
    @AfterEach
    void clearCacheBetweenTests() {
        MaskingCache.clearCache();
    }

    @Test
    @DisplayName("TestUser object fields are masked correctly")
    void testUserMasking() {
        LocalDate date = LocalDate.of(2024, 7, 31);
        TestUser user = new TestUser("Test User", "test.user@example.com", "1234567890", 30, "123 Main St", 1000, date, new CustomType());
        String maskedOutput = user.toString();

        // Expected values based on MaskedTypeTest and custom regex
        // Name: T*******r (first, last two: T..t Use.r -> T*******r)
        // Email: t*********@e*********.com
        // Phone (custom \\d(?=\\d{2})): 1234567890 -> ********90
        // Address (ALL): "***********" (11 chars for "123 Main St") -> No, ALL is \S, so "*** **** **"
        // AccountBalance (NUMBER): 1000 -> "****"
        // Expected values based on current regexes:
        // Name: "Test User" with NAME -> "T***t U*er" (Mistake in previous trace, NAME is first and last TWO)
        // "Test User" -> T e s t   U s e r -> T * * t   U * r. This is "T**t U*r"
        // Email: "test.user@example.com" with EMAIL -> "t***********@e*********.com"
        // Phone (custom \\d(?=\\d{2})): "1234567890" -> "********90"
        // Address (ALL \S): "123 Main St" (11 non-whitespace) -> "*** **** **"
        // AccountBalance (NUMBER \d): 1000 -> "****"
        // JoinDate (DATE): "2024-07-31" -> "****-**-31"
        // CustomObject (ALL \S): "customData" (10 non-whitespace) -> "**********"
        // NullField: null

        assertTrue(maskedOutput.contains("name=T**t U*r"), "Name masking failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("email=t***********@e*********.com"), "Email masking failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("phoneNumber=********90"), "Phone masking failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("age=30"), "Non-sensitive field 'age' should not be masked. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("address=*** **** **"), "Address masking (ALL) failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("accountBalance=****"), "Account Balance (NUMBER) failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("joinDate=****-**-31"), "Join Date (DATE) failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("customObject=**********"), "CustomObject (ALL) masking with LogMask fix. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("nullField=null"), "Null field should be 'null'. Output: " + maskedOutput);

        // Check class name part
        assertTrue(maskedOutput.startsWith("TestUser{"), "Output should start with class name. Output: " + maskedOutput);
        assertTrue(maskedOutput.endsWith("}"), "Output should end with '}'. Output: " + maskedOutput);
    }

    @Test
    @DisplayName("Masking a null object should return 'null'")
    void testNullObjectMasking() {
        TestUser user = null;
        // Directly call the default method via an anonymous implementation or a concrete one
        LogMask masker = new TestSimple(""); // or (LogMask) obj -> default mask(obj) if Java allows
        String output = masker.mask(user);
        assertEquals("null", output, "Masking a null object should return 'null'");
    }

    @Test
    @DisplayName("Object with no maskable fields but implements LogMask")
    void testNoMaskableFields() {
        class NoMaskFields implements LogMask {
            String field1 = "value1";
            int count = 10;
            public String toString() { return mask(this); }
        }
        NoMaskFields obj = new NoMaskFields();
        String output = obj.toString();
        assertTrue(output.contains("field1=value1"), "field1 should be unmasked. Output: " + output);
        assertTrue(output.contains("count=10"), "count should be unmasked. Output: " + output);
        assertTrue(output.startsWith("NoMaskFields{"), "Output format error. Output: " + output);
    }

    @Test
    @DisplayName("Object with fields of types not in classesMaskEnabled (some annotated, some not)")
    void testFieldsWithNonDefaultMaskableTypes() {
        class ComplexObject implements LogMask {
            @MaskSensitiveData(maskedType = MaskedType.ALL)
            CustomType annotatedCustom = new CustomType(); // Annotated, should mask via toString

            CustomType nonAnnotatedCustom = new CustomType(); // Not annotated, toString

            @MaskSensitiveData(maskedType = MaskedType.ALL)
            String alwaysMaskable = "maskMe";

            public String toString() { return mask(this); }
        }
        ComplexObject obj = new ComplexObject();
        String output = obj.toString();

        assertTrue(output.contains("annotatedCustom=**********"), "Annotated custom type should be masked. Output: " + output);
        assertTrue(output.contains("nonAnnotatedCustom=customData"), "Non-annotated custom type should use default toString. Output: " + output);
        assertTrue(output.contains("alwaysMaskable=******"), "Standard maskable type (String) should be masked. Output: " + output);
    }

    @Test
    @DisplayName("Test caching interaction by masking same type of object multiple times")
    void testCachingInteraction() {
        TestSimple obj1 = new TestSimple("firstValue");
        String masked1 = obj1.toString();
        assertEquals("TestSimple{simpleField=**********}", masked1);

        // MaskingCache should now have TestSimple.class data
        int cacheSizeBeforeClearForClass = MaskingCache.getFieldsToMask(TestSimple.class).size();
        assertTrue(cacheSizeBeforeClearForClass > 0, "Cache should contain data for TestSimple");

        TestSimple obj2 = new TestSimple("anotherValue");
        String masked2 = obj2.toString();
        assertEquals("TestSimple{simpleField=************}", masked2); // ALL masks based on length

        // Verify that the cache for TestSimple.class was indeed used (not easily done by checking output alone)
        // but we can ensure that getFieldsToMask returns the same instance
        Map firstCallMap = MaskingCache.getFieldsToMask(TestSimple.class);
        Map secondCallMap = MaskingCache.getFieldsToMask(TestSimple.class);
        assertSame(firstCallMap, secondCallMap, "MaskingCache should return same map instance for the same class");
    }

     // Test with various eligible types from LogMask.classesMaskEnabled
    static class AllTypesMaskableTest implements LogMask {
        @MaskSensitiveData(maskedType = MaskedType.ALL) String strField = "string";
        @MaskSensitiveData(maskedType = MaskedType.ALL) BigDecimal bigDecimalField = new BigDecimal("123.45");
        @MaskSensitiveData(maskedType = MaskedType.ALL) LocalDate localDateField = LocalDate.of(2023,1,1);
        @MaskSensitiveData(maskedType = MaskedType.ALL) LocalDateTime localDateTimeField = LocalDateTime.of(2023,1,1,10,30);
        @MaskSensitiveData(maskedType = MaskedType.ALL) Boolean boolField = true; // toString is "true"
        @MaskSensitiveData(maskedType = MaskedType.ALL) Integer intField = 12345;
        @MaskSensitiveData(maskedType = MaskedType.ALL) Float floatField = 12.3f;
        @MaskSensitiveData(maskedType = MaskedType.ALL) Short shortField = 12;
        @MaskSensitiveData(maskedType = MaskedType.ALL) Double doubleField = 1.23;
        @MaskSensitiveData(maskedType = MaskedType.ALL) int primitiveIntField = 789;

        public String toString() { return mask(this); }
    }

    @Test
    @DisplayName("Test masking for all default eligible types")
    void testAllDefaultEligibleTypes() {
        AllTypesMaskableTest obj = new AllTypesMaskableTest();
        String maskedOutput = obj.toString();

        assertTrue(maskedOutput.contains("strField=******"), "String field masking failed. Output: " + maskedOutput);
        assertTrue(maskedOutput.contains("bigDecimalField=******"), "BigDecimal field masking failed. Output: " + maskedOutput); // "123.45" -> 6 chars
        assertTrue(maskedOutput.contains("localDateField=**********"), "LocalDate field masking failed. Output: " + maskedOutput); // "2023-01-01" -> 10 chars
        assertTrue(maskedOutput.contains("localDateTimeField=*******************"), "LocalDateTime field masking failed. Output: " + maskedOutput); // "2023-01-01T10:30" -> 19 non-whitespace chars
        assertTrue(maskedOutput.contains("boolField=****"), "Boolean field masking failed. Output: " + maskedOutput); // "true" -> 4 non-whitespace chars
        assertTrue(maskedOutput.contains("intField=*****"), "Integer field masking failed. Output: " + maskedOutput); // "12345" -> 5 non-whitespace chars
        assertTrue(maskedOutput.contains("floatField=****"), "Float field masking failed. Output: " + maskedOutput); // "12.3" -> 4 non-whitespace chars
        assertTrue(maskedOutput.contains("shortField=**"), "Short field masking failed. Output: " + maskedOutput); // "12" -> 2 non-whitespace chars
        assertTrue(maskedOutput.contains("doubleField=****"), "Double field masking failed. Output: " + maskedOutput); // "1.23" -> 4 non-whitespace chars
        assertTrue(maskedOutput.contains("primitiveIntField=***"), "primitive int field masking failed. Output: " + maskedOutput); // "789" -> 3 non-whitespace chars
    }
}
