package com.deepak.spring.log.utils.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Represents global configuration properties for the logging utilities,
 * typically loaded from application properties files.
 * <p>
 * These properties are bound from the external configuration under the prefix {@code spring.log-utils.global}.
 * This class acts as a container for various module-specific properties, such as those for HTTP method logging.
 * </p>
 * <p>Example configuration in {@code application.yml}:</p>
 * <pre>{@code
 * spring:
 *   log-utils:
 *     global:
 *       http-method:
 *         log-request-body: true
 *         log-response-body: true
 *         # ... other HttpMethodProperties
 * }</pre>
 *
 * @param httpMethod Nested configuration properties for HTTP method logging. See {@link HttpMethodProperties}.
 *
 * @see HttpMethodProperties
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "spring.log-utils.global")
public record GlobalProperties(
                HttpMethodProperties httpMethod) {

    /**
     * Default constructor for {@link GlobalProperties}.
     * Lombok will generate this for record classes, but explicitly defining it
     * can be useful for Javadoc or future initializations if needed.
     *
     * @param httpMethod The HTTP method properties.
     */
    public GlobalProperties {
        // Record constructor body, can be used for validation or initialization if necessary.
    }
}
