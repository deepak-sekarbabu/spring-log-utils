package com.deepak.spring.log.utils.properties;

/**
 * Configuration properties for HTTP method logging.
 * <p>
 * Used to enable or disable HTTP method logging via configuration.
 * </p>
 */
public record HttpMethodProperties(
                Boolean enabled) {
}
