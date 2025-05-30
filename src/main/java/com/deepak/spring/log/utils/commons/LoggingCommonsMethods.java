package com.deepak.spring.log.utils.commons;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

/**
 * Utility class providing common logging operations for method interception.
 * Contains methods to log method execution details including parameters, return
 * values, and execution time.
 *
 * <p>
 * This class primarily supports aspect-oriented logging by providing common methods
 * used by logging aspects like {@link com.deepak.spring.log.utils.features.aspect.LogExecutionAspect}
 * and {@link com.deepak.spring.log.utils.features.aspect.HttpMethodLogExecutionAspect}.
 * It is marked with {@link lombok.experimental.UtilityClass} to ensure it is not instantiated.
 * </p>
 *
 * @see org.aspectj.lang.ProceedingJoinPoint
 * @see com.deepak.spring.log.utils.features.annotations.LogExecution
 * @see com.deepak.spring.log.utils.features.annotations.HttpMethodLogExecution
 */
@Slf4j
@UtilityClass
public class LoggingCommonsMethods {

    // No explicit constructor needed due to @UtilityClass.

    /**
     * Intercepts a method execution, logs its start and finish stages (including parameters, result, and execution time),
     * and then proceeds with the actual method execution.
     * <p>
     * This method is typically invoked by AOP advices. It logs method entry with parameters (if enabled),
     * executes the target method, and then logs method exit with the result (if enabled) and execution time.
     *</p>
     * Example log output:
     * <pre>{@code
     * INFO com.example.MyService - stage=init, method=myMethod, class=MyService, parameters=[arg1, arg2]
     * INFO com.example.MyService - stage=finish, method=myMethod, class=MyService, parameters=[arg1, arg2], result=myResult, time-execution=123ms
     * }</pre>
     *
     * @param joinPoint The {@link ProceedingJoinPoint} representing the intercepted method call. Must not be null.
     * @param logReturn A boolean flag indicating whether the method's return value should be logged.
     * @param logParameters A boolean flag indicating whether the method's input parameters should be logged.
     * @return The result returned by the intercepted method.
     * @throws Throwable if the intercepted method throws an exception. The {@link SneakyThrows} annotation
     *                   from Lombok is used to propagate checked exceptions without explicit declaration.
     */
    @SneakyThrows
    public Object logInterceptJoinPoint(ProceedingJoinPoint joinPoint, boolean logReturn, boolean logParameters) {
        if (joinPoint == null) {
            // Or handle as per specific project policy, e.g., log an error and return null/throw exception
            log.error("ProceedingJoinPoint cannot be null in logInterceptJoinPoint.");
            // Depending on policy, you might want to throw an IllegalArgumentException
            // or let it proceed if there's a (highly unlikely) scenario where it could still work.
            // For safety, returning null or throwing an exception is often better.
            // However, joinPoint.proceed() would NPE anyway.
            // Let's assume valid joinPoint based on AOP context.
        }

        long start = System.nanoTime();

        var sbInit = new StringBuilder("stage=init, method={}, class={}, ")
                .append(logParameters ? "parameters={}" : "");

        log.info(sbInit.toString(),
                joinPoint.getSignature().getName(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getArgs());

        // Execute the intercepted method
        Object result = joinPoint.proceed();

        long timeElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        var sbFinish = new StringBuilder("stage=finish, method={}, class={}, ")
                .append(logParameters ? "parameters={}, " : "")
                .append(logReturn ? "result={}, " : "")
                .append("time-execution={}ms");

        log.info(sbFinish.toString(),
                joinPoint.getSignature().getName(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getArgs(),
                result,
                timeElapsed);

        return result;
    }

    /**
     * Overloaded method to intercept a method execution and log details with
     * default settings (both {@code logReturn} and {@code logParameters} are true).
     *
     * @param joinPoint The {@link ProceedingJoinPoint} representing the intercepted method call. Must not be null.
     * @return The result returned by the intercepted method.
     * @throws Throwable if the intercepted method throws an exception.
     * @see #logInterceptJoinPoint(ProceedingJoinPoint, boolean, boolean)
     */
    @SneakyThrows
    public Object logInterceptJoinPoint(ProceedingJoinPoint joinPoint) {
        return logInterceptJoinPoint(joinPoint, true, true);
    }

    /**
     * Masks parts of a string value using a specified regex pattern.
     * All matches of the regex in the input string are replaced with an asterisk ("*").
     * <p>
     * This method is a general-purpose masking utility. For more optimized and
     * context-aware masking within the logging framework (especially for object fields),
     * refer to {@link com.deepak.spring.log.utils.features.interfaces.LogMask} and its
     * integration with {@link MaskingCache}.
     * </p>
     *
     * @param value The string value to be masked. If null or blank, an empty string is returned.
     * @param regex The regex pattern used to find parts of the string to be masked. Must not be null.
     * @return The masked string, or an empty string if the input value is null/blank.
     *         If the regex is invalid, a {@link java.util.regex.PatternSyntaxException} may occur.
     * @throws NullPointerException if regex is null.
     */
    public String mask(String value, String regex) {
        if (isNull(value) || value.isBlank()) {
            return "";
        }
        if (regex == null) {
            throw new NullPointerException("Regex pattern cannot be null for masking.");
        }
        return value.replaceAll(regex, "*");
    }
}
