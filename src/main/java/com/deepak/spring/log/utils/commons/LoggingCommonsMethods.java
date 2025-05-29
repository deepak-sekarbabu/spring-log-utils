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
 * This class uses Aspect-Oriented Programming (AOP) constructs to intercept and
 * log method executions.
 * </p>
 *
 * @see org.aspectj.lang.ProceedingJoinPoint
 */
@Slf4j
@UtilityClass
public class LoggingCommonsMethods {

    /**
     * Default constructor for LoggingCommonsMethods.
     */
    // UtilityClass from Lombok makes the constructor private and throws exception,
    // so explicit constructor is not needed.

    /**
     * Intercepts a method execution and logs its start/finish stages, parameters,
     * result, and execution time.
     * Logging of parameters and return values can be configured via method
     * arguments.
     *
     * @param joinPoint     The intercepted method execution context
     * @param logReturn     Whether to log the method's return value
     * @param logParameters Whether to log the method's input parameters
     * @return The result of the intercepted method execution
     *
     *         <p>
     *         Logs include:
     *         <ul>
     *         <li>Stage (start/finish)</li>
     *         <li>Method name and class</li>
     *         <li>Input parameters (if enabled)</li>
     *         <li>Return value (if enabled)</li>
     *         <li>Execution time in milliseconds</li>
     *         </ul>
     */
    @SneakyThrows
    public Object logInterceptJoinPoint(ProceedingJoinPoint joinPoint, boolean logReturn, boolean logParameters) {
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
     * default settings (logReturn and logParameters enabled).
     *
     * @param joinPoint The intercepted method execution context
     * @return The result of the intercepted method execution
     */
    @SneakyThrows
    public Object logInterceptJoinPoint(ProceedingJoinPoint joinPoint) {
        return logInterceptJoinPoint(joinPoint, true, true);
    }

    /**
     * Masks parts of a string using the provided regex.
     *
     * @param value The string to mask
     * @param regex The regex pattern to match parts to mask
     * @return The masked string
     */
    public String mask(String value, String regex) {
        if (isNull(value) || value.isBlank())
            return "";
        return value.replaceAll(regex, "*");
    }
}
