package com.deepak.spring.log.utils.features.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for logging execution details of HTTP-related methods.
 * <p>
 * When applied to methods, particularly those handling HTTP requests (e.g., in Spring MVC controllers),
 * this annotation, in conjunction with an appropriate aspect (e.g.,
 * {@link com.deepak.spring.log.utils.features.aspect.HttpMethodLogExecutionAspect} or
 * {@link com.deepak.spring.log.utils.features.aspect.GlobalHttpMethodLogAspect}),
 * enables automated logging of HTTP request and response details, along with method execution timing.
 * <p>
 * It typically logs:
 * <ul>
 *   <li>Method entry: including method name, class name, and optionally, HTTP request parameters or body.</li>
 *   <li>Method exit: including method name, class name, optionally, HTTP response or return value, and execution time.</li>
 * </ul>
 * The specifics of what is logged (e.g., full request/response bodies, headers) might be further
 * configured by properties in {@link com.deepak.spring.log.utils.properties.HttpMethodProperties}
 * and {@link com.deepak.spring.log.utils.properties.GlobalProperties}.
 * <p>
 * The {@link #logReturn()} and {@link #logParameters()} attributes control the logging of the
 * method's direct return value and parameters, respectively, similar to {@link LogExecution}.
 *
 * @see com.deepak.spring.log.utils.features.aspect.HttpMethodLogExecutionAspect
 * @see com.deepak.spring.log.utils.features.aspect.GlobalHttpMethodLogAspect
 * @see com.deepak.spring.log.utils.commons.LoggingCommonsMethods#logInterceptJoinPoint(org.aspectj.lang.ProceedingJoinPoint, boolean, boolean)
 * @see com.deepak.spring.log.utils.properties.HttpMethodProperties
 * @see com.deepak.spring.log.utils.properties.GlobalProperties
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethodLogExecution {
    /**
     * Determines whether the return value of the annotated method (often an HTTP response object or body)
     * should be logged upon method exit.
     *
     * @return {@code true} (default) if the return value should be logged, {@code false} otherwise.
     */
    boolean logReturn() default true;

    /**
     * Determines whether the input parameters of the annotated method (often representing HTTP request
     * parameters or body) should be logged upon method entry and exit.
     *
     * @return {@code true} (default) if the method parameters should be logged, {@code false} otherwise.
     */
    boolean logParameters() default true;
}
