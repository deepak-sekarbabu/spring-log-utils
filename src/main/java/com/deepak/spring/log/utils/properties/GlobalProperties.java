package com.deepak.spring.log.utils.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for global logging settings.
 * <p>
 * Binds properties prefixed with 'spring.log-utils.global'.
 * </p>
 */
@ConfigurationProperties(prefix = "spring.log-utils.global")
public record GlobalProperties(
                HttpMethodProperties httpMethod) {
}
