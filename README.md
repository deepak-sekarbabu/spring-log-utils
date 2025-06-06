# Spring Log Utils

[My Blog Post on this Repo](https://technologytrendsinjava.blogspot.com/2025/05/enhance-your-spring-application-logging.html)

`spring-log-utils` is a library designed to enhance logging capabilities in Spring-based applications. It provides features for logging HTTP methods, specific endpoints, and methods, as well as masking sensitive data in logs. The library leverages Spring Boot's autoconfiguration mechanism to simplify integration and usage.

This library simplifies logging in Spring applications, making it easier to monitor and debug while ensuring sensitive data is protected.

## Features

1. **Global HTTP Methods Logging**:
   - Automatically logs HTTP method executions based on configuration properties.

2. **`@HttpMethodLogExecution` Annotation**:
   - Enables logging for specific HTTP endpoints.

3. **`@LogExecution` Annotation**:
   - Logs the execution of specific methods.

4. **`@MaskSensitiveData` Annotation**:
   - Masks sensitive fields in logs to protect sensitive information.

## Integration

### Maven

To use this library in a Maven project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.deepak</groupId>
    <artifactId>spring-log-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Global HTTP Methods Logging

Configure logging for HTTP methods globally using Spring Boot configuration properties. Example:

```yaml
spring:
  log-utils:
    global:
      httpMethod:
        enabled: true
```

### `@HttpMethodLogExecution` Annotation

Use this annotation to log specific HTTP endpoints:

```java
@RestController
@RequestMapping("/greetings")
@RequiredArgsConstructor
public class GreetingController {

   private final GreetingService service;

   @HttpMethodLogExecution
   @GetMapping
   public ResponseEntity<List<User>> getUsers() {
      return ResponseEntity.ok(service.getUser());
   }

}
```

#### Output Log Example

```plaintext
2025-05-13 15:59:15 app=example-secret-app [http-nio-8080-exec-3] [:] commons.com.deepak.spring.log.utils.LoggingCommonsMethods INFO  M=logInterceptJoinPoint, stage=init, method=getUsers, class=GreetingController, parameters=[]
2025-05-13 15:59:15 app=example-secret-app [http-nio-8080-exec-3] [:] commons.com.deepak.spring.log.utils.LoggingCommonsMethods INFO  M=logInterceptJoinPoint, stage=finish, method=getUsers, class=GreetingController, parameters=[], result=<200 OK OK,[User{email=******des***.com, name=**s****es, address=*a****o*45, age=**, birthDate=***5-13, phone=***9**9, document=********911}],[]>, time-execution=5ms
```

### `@LogExecution` Annotation

Use this annotation to log the execution of specific methods:

```java
@Service
public class GreetingService {

   @LogExecution
   public User getUser(Long id) {
      return User.builder()
              .name("Deepak Sekarbabu")
              .email("deepak.sekarbabu@gmail.com")
              .document("12345678911")
              .birthDate(LocalDate.now())
              .age(34)
              .phone("11 9999-9999")
              .address("SE Test 745")
              .build();
   }
}

```

#### Output Log Example 2

```plaintext
2025-05-13 16:22:08 app=example-secret-app [http-nio-8080-exec-1] [:] commons.com.deepak.spring.log.utils.LoggingCommonsMethods INFO  M=logInterceptJoinPoint, stage=init, method=getUser, class=GreetingService, parameters=[1]
2025-05-13 16:22:08 app=example-secret-app [http-nio-8080-exec-1] [:] commons.com.deepak.spring.log.utils.LoggingCommonsMethods INFO  M=logInterceptJoinPoint, stage=finish, method=getUser, class=GreetingService, parameters=[1], result=User{email=******des***.com, name=**s****es, address=*a****o*45, age=**, birthDate=***5-13, phone=***9**9, document=********911}, time-execution=2ms
```

### `@MaskSensitiveData` Annotation

Use this annotation to mask sensitive fields in logs:

```java
public record User(
        @MaskSensitiveData(maskedType = MaskedType.EMAIL) String email,
        @MaskSensitiveData(maskedType = MaskedType.NAME) String name,
        @MaskSensitiveData(maskedType = MaskedType.ADDRESS) String address,
        @MaskSensitiveData(maskedType = MaskedType.NUMBER) Integer age,
        @MaskSensitiveData(maskedType = MaskedType.DATE) LocalDate birthDate,
        @MaskSensitiveData(maskedType = MaskedType.TELEPHONE) String phone,
        @MaskSensitiveData(maskedType = MaskedType.DOCUMENT) String document) implements LogMask {

    @Override
    public String toString() {
        return mask(this);
    }
}
```

### Expected Log Outputs

1. `ALL`: Masks all characters.
2. `EMAIL`: Masks email addresses except the last 4 characters and domain.
3. `DOCUMENT`: Masks all but the last 3 characters.
4. `NAME`: Masks all characters except the first and last two.
5. `DATE`: Masks all characters except the last 3, excluding separators.
6. `ADDRESS`: Masks all characters except the last 3, excluding commas and spaces.
7. `ZIP_CODE`: Masks all characters except the last 3, excluding hyphens.
8. `NUMBER`: Masks all numeric characters.
9. `TELEPHONE`: Masks all characters except the last 2, excluding hyphens.
10. `PASSWORD`: Displays only the first character and masks the rest with `*`.

### Usage Example for `customMaskRegex`

The `customMaskRegex` attribute allows you to define a custom regular expression for masking sensitive data. This takes precedence over the predefined `maskedType`.

#### Example

```java
public class User {
    @MaskSensitiveData(customMaskRegex = "\\d{4}-\\d{2}-\\d{2}")
    private String dateOfBirth;

    @MaskSensitiveData(maskedType = MaskedType.EMAIL)
    private String email;

    // Getters and setters
}
```

## MaskedType.PASSWORD

The `PASSWORD` type masks all characters except the first one. This is useful for logging sensitive information like passwords while ensuring security.

### PASSWORD Example

Input: `mypassword`

Output: `m********`

### PASSWORD Usage

```java
@MaskSensitiveData(maskedType = MaskedType.PASSWORD)
private String password;
```

### LoginRequest LogMask Usage Example

```java
public class LoginRequest implements LogMask {
    @MaskSensitiveData(maskedType = MaskedType.NAME)
    private String username;

    @MaskSensitiveData(maskedType = MaskedType.PASSWORD)
    private String password;

    // constructors, getters, setters...

    @Override
    public String toString() {
        return mask(this);
    }
}
```
