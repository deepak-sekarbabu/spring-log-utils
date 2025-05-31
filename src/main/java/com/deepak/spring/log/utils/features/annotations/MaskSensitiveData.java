package com.deepak.spring.log.utils.features.annotations;

import com.deepak.spring.log.utils.features.enums.MaskedType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field whose value should be masked when its containing object is processed by
 * features like {@link com.deepak.spring.log.utils.features.interfaces.LogMask#mask(Object)}.
 * <p>
 * This annotation allows developers to indicate that a field contains sensitive information
 * (e.g., personal data, financial details) that should not appear in plain text in logs or
 * other string representations of the object.
 * <p>
 * The actual masking is performed by replacing characters matched by a regex pattern with an asterisk ('*').
 * The regex pattern can be selected from a predefined set using {@link #maskedType()} or
 * specified as a custom pattern using {@link #customMaskRegex()}.
 * <p>
 * If {@link #customMaskRegex()} is provided and is not blank, it will be used. Otherwise,
 * the regex pattern associated with the specified {@link #maskedType()} will be used.
 *
 * @see com.deepak.spring.log.utils.features.enums.MaskedType
 * @see com.deepak.spring.log.utils.features.interfaces.LogMask
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskSensitiveData {
    /**
     * Specifies the predefined {@link MaskedType} that dictates the regex pattern for masking.
     * This type is used if {@link #customMaskRegex()} is not provided or is blank.
     *
     * @return The predefined masking strategy. Defaults to {@link MaskedType#ALL},
     *         which masks all non-whitespace characters.
     */
    MaskedType maskedType() default MaskedType.ALL;

    /**
     * Defines a custom Java regular expression (regex) for masking the annotated field's value.
     * <p>
     * If this attribute is set to a non-blank string, it takes precedence over the
     * regex pattern derived from {@link #maskedType()}. This allows for fine-grained,
     * field-specific masking rules when the predefined types are insufficient.
     * <p>
     * The regex should be written to match the characters that need to be replaced by an asterisk.
     *
     * @return A custom regex string. Defaults to an empty string, indicating that
     *         {@link #maskedType()} should be used.
     */
    String customMaskRegex() default "";
}
