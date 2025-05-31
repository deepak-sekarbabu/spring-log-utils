package com.deepak.spring.log.utils.features.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable automated logging of method execution details.
 * <p>
 * When applied to a method, this annotation, in conjunction with an appropriate aspect
 * (e.g., {@link com.deepak.spring.log.utils.features.aspect.LogExecutionAspect}),
 * enables automated logging of method execution details.
 * <p>
 * It logs:
 * <ul>
 *   <li>Method entry: including the method name, class name, and optionally, the input parameters.</li>
 *   <li>Method exit: including the method name, class name, optionally, the return value, and the execution time in milliseconds.</li>
 * </ul>
 * The logging behavior can be customized using the attributes of this annotation.
 *
 * @see com.deepak.spring.log.utils.features.aspect.LogExecutionAspect
 * @see com.deepak.spring.log.utils.commons.LoggingCommonsMethods#logInterceptJoinPoint(org.aspectj.lang.ProceedingJoinPoint, boolean, boolean)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {
    /**
     * Determines whether the return value of the annotated method should be logged upon method exit.
     *
     * @return {@code true} (default) if the return value should be logged, {@code false} otherwise.
     */
    boolean logReturn() default true;

    /**
     * Determines whether the input parameters of the annotated method should be logged upon method entry and exit.
     *
     * @return {@code true} (default) if the method parameters should be logged, {@code false} otherwise.
     */
    boolean logParameters() default true;
}
