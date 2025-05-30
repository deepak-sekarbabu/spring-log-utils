package com.deepak.spring.log.utils.properties;

/**
 * Configuration properties specifically for customizing the behavior of HTTP method logging,
 * such as logging of request/response bodies and headers.
 * <p>
 * These properties are typically nested under {@link GlobalProperties} and are bound from
 * external configuration, for example, under {@code spring.log-utils.global.http-method}.
 * </p>
 * <p>Example configuration in {@code application.yml}:</p>
 * <pre>{@code
 * spring:
 *   log-utils:
 *     global:
 *       http-method:
 *         enabled: true
 *         log-request-body: true       # Example of a potential property
 *         log-response-body: true      # Example of a potential property
 *         included-request-headers: "Content-Type,Authorization" # Example
 *         excluded-response-headers: "Set-Cookie"                 # Example
 * }</pre>
 * <p>
 * Note: While this record currently only defines an 'enabled' flag, it is designed
 * to be extensible with more specific properties to control HTTP logging details
 * as demonstrated in the example (e.g., logRequestBody, logResponseBody, header filtering).
 * The actual processing of these additional properties would be handled by the relevant aspects.
 * </p>
 *
 * @param enabled A boolean flag to enable or disable all HTTP method logging governed by these properties.
 *                If {@code false}, HTTP method logging might be skipped entirely by aspects
 *                that check this flag. Defaults to {@code true} if not specified (aspects should define this default behavior).
 *
 * @see GlobalProperties
 * @see com.deepak.spring.log.utils.features.annotations.HttpMethodLogExecution
 * @see com.deepak.spring.log.utils.features.aspect.GlobalHttpMethodLogAspect
 */
public record HttpMethodProperties(
                Boolean enabled
    // TODO: Consider adding more specific properties as needed, for example:
    // Boolean logRequestBody,
    // Boolean logResponseBody,
    // Set<String> includedRequestHeaders,
    // Set<String> excludedRequestHeaders,
    // Set<String> includedResponseHeaders,
    // Set<String> excludedResponseHeaders,
    // int maxBodySizeLog // Max characters of body to log
) {

    /**
     * Default value for the 'enabled' property if not specified in the configuration.
     * Aspects or services using these properties should refer to this for default behavior.
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * Canonical constructor for {@link HttpMethodProperties}.
     *
     * @param enabled Flag to enable/disable HTTP method logging.
     *                It's recommended that consuming aspects treat a null value here as {@link #DEFAULT_ENABLED}.
     */
    public HttpMethodProperties(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether HTTP method logging is enabled.
     * If the 'enabled' property was not explicitly set in the configuration (i.e., is null),
     * this method returns {@link #DEFAULT_ENABLED}.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled == null ? DEFAULT_ENABLED : enabled;
    }
}
