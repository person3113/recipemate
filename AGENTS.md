# RecipeMate API - Agent Guidelines

## Build/Test Commands
- Build: `./gradlew build` (Windows: `gradlew.bat build`)
- Run: `./gradlew bootRun`
- Test all: `./gradlew test`
- Test single: `./gradlew test --tests com.recipemate.package.ClassName.methodName`
- Clean: `./gradlew clean`

## Technology Stack
Spring Boot 3.5.7, Java 21, JPA, Lombok, QueryDSL, H2 (dev), PostgreSQL (prod), Spring Security, Thymeleaf

## Code Style & Conventions
- Package structure: `com.recipemate.{domain}.{layer}` (e.g., `com.recipemate.user.service`)
- Global utilities in `com.recipemate.global.{common,exception,config}`
- Use Lombok annotations: `@Getter`, `@AllArgsConstructor(access = AccessLevel.PRIVATE)` for DTOs
- Entities extend `BaseEntity` for audit fields (createdAt, updatedAt, deletedAt)
- Soft delete via `deletedAt` field and `delete()` method
- Return `ApiResponse<T>` from controllers with factory methods: `ApiResponse.success(data)`, `ApiResponse.error(code, message)`
- Error handling: throw `CustomException(ErrorCode.ENUM_VALUE)` for business logic errors
- Add error codes to `ErrorCode` enum with HttpStatus, code (e.g., "USER-001"), and Korean message
- Use `@RestControllerAdvice` for global exception handling
- QueryDSL Q-classes generated in `build/generated/querydsl`
- Follow existing naming: enums in PascalCase (e.g., `UserRole.USER`), packages in lowercase
- Minimize comments; write self-documenting code
- Use Korean for error messages and validation messages
- Follow TDD: write tests before implementation (Red → Green → Refactor)
- Agile approach: start simple, iterate, improve incrementally
