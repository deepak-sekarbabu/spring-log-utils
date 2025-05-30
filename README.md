# Spring Log Utils

**Note:** The blog post link (https://technologytrendsinjava.blogspot.com/2025/05/enhance-your-spring-application-logging.html) is currently a placeholder for a future article.

`spring-log-utils` is a library designed to enhance logging capabilities in Spring-based applications. It provides features for logging HTTP methods, specific endpoints, and methods, as well as efficiently masking sensitive data in logs. The library leverages Spring Boot's autoconfiguration mechanism to simplify integration and usage.

This library simplifies logging in Spring applications, making it easier to monitor and debug while ensuring sensitive data is protected. It's optimized for performance, especially in high-frequency logging scenarios involving data masking, thanks to an internal caching mechanism for reflection and regex processing.

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
2025-05-13 15:59:15 app=example-secret-app [http-nio-8080-exec-3] [:] commons.com.deepak.spring.log.utils.LoggingCommonsMethods INFO  M=logInterceptJoinPoint, stage=finish, method=getUsers, class=GreetingController, parameters=[], result=<200 OK OK,[User{email=d*******@g*****.com, name=D*****bu, address=** ****o*45, age=**, birthDate=****-**-13, phone=** *****99, document=******911}],[]>, time-execution=5ms
```
*(Note: The exact masked output for `User` will depend on the specific regex rules applied by `MaskedType` enum values, which have been refined for better masking behavior. The example above is illustrative.)*

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
2025-05-13 16:22:08 app=example-secret-app [http-nio-8080-exec-1] [:] commons.com.deepak.spring.log.utils.LoggingCommonsMethods INFO  M=logInterceptJoinPoint, stage=finish, method=getUser, class=GreetingService, parameters=[1], result=User{email=d*******@g*****.com, name=D*****bu, address=** ****o*45, age=**, birthDate=****-**-13, phone=** *****99, document=******911}, time-execution=2ms
```
*(Note: The exact masked output for `User` will depend on the specific regex rules applied by `MaskedType` enum values.)*

### `@MaskSensitiveData` Annotation

Use this annotation to mask sensitive fields in logs. The masking process is optimized for performance using a caching mechanism for field metadata and compiled regex patterns.

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

### Masking Behavior with `MaskedType`

The `MaskedType` enum provides predefined strategies for common data types. Here's a summary of their behavior (refer to `MaskedType.java` Javadoc for precise regex and more examples):

1.  **`ALL`**: Masks all non-whitespace characters.
    *   Example: `"abc def"` becomes `"*** ***"`
2.  **`EMAIL`**: Shows the first character of the local part and the first character of the domain name.
    *   Example: `"john.doe@example.com"` becomes `"j*******@e******.com"`
3.  **`DOCUMENT`**: Leaves only the last three characters visible.
    *   Example: `"123456789"` becomes `"******789"`
4.  **`NAME`**: Leaves the first character and the last two characters visible.
    *   Example: `"Leonardo"` becomes `"L******do"`
5.  **`DATE`**: Masks digits, typically leaving the last component (e.g., day or last two year digits) unmasked.
    *   Example: `"2023-10-25"` becomes `"****-**-25"`
6.  **`ADDRESS`**: Masks alphanumeric characters, leaving approximately the last word or part of it unmasked.
    *   Example: `"123 Main Street"` becomes `"*** **** ******t"`
7.  **`ZIP_CODE`**: Masks digits, typically leaving the last two digits of any segment unmasked.
    *   Example: `"90210-1234"` becomes `"***10-**34"`
8.  **`NUMBER`**: Masks all digits. Non-digit characters are preserved.
    *   Example: `"ID: 123-456"` becomes `"ID: ***-***"`
9.  **`TELEPHONE`**: Masks digits, typically leaving the last two digits of any segment unmasked.
    *   Example: `"(123) 456-7890"` becomes `"(*23) *56-**90"`
10. **`PASSWORD`**: Displays only the first character and masks the rest.
    *   Example: `"secret123"` becomes `"s********"`

### Usage Example for `customMaskRegex`

The `customMaskRegex` attribute allows you to define a custom Java regular expression for masking sensitive data. This takes precedence over the predefined `maskedType`. The regex should match the characters you want to replace with an asterisk (`*`).

#### Example

Suppose you have a field `employeeId` with format `EMP12345XYZ` and you want to mask the middle digits `12345`, showing `EMP*****XYZ`.

```java
public class Employee {
    // Regex to find digits between "EMP" and three trailing uppercase letters
    @MaskSensitiveData(customMaskRegex = "(?<=EMP)\\d{5}(?=[A-Z]{3})")
    private String employeeId; // Example: "EMP12345XYZ" would become "EMP*****XYZ"

    @MaskSensitiveData(maskedType = MaskedType.EMAIL)
    private String email;

    // Constructor, Getters and setters
    public Employee(String employeeId, String email) {
        this.employeeId = employeeId;
        this.email = email;
    }

    @Override
    public String toString() {
        // Assuming Employee implements LogMask or uses a utility that calls LogMask.mask()
        // For demonstration, let's assume it has a LogMask instance or is one:
        // return new com.deepak.spring.log.utils.features.interfaces.LogMask(){}.mask(this);
        // A more typical usage if Employee itself implements LogMask:
        // return mask(this);
        return "Employee(employeeId=" + employeeId + ", email=" + email + ")"; // Placeholder if not using LogMask directly for toString
    }
}
```
*(To see the custom masking in action, ensure the object's `toString()` method (or the logging framework) uses a mechanism that processes `@MaskSensitiveData`, like the `LogMask.mask(this)` pattern shown in the `User` record example.)*

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
