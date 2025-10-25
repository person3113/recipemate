# RecipeMate API - Agent Guidelines

## Build/Test Commands
- Build: `./gradlew build` (Windows: `gradlew.bat build`)
- Run: `./gradlew bootRun`
- Test all: `./gradlew test`
- Test single: `./gradlew test --tests com.recipemate.package.ClassName.methodName`
- Clean: `./gradlew clean`

## Technology Stack
**Backend**: Spring Boot 3.5.7, Java 21, JPA, Lombok, QueryDSL, Spring Security
**Database**: H2 (dev), PostgreSQL 16 (prod)
**Frontend**: Thymeleaf, Bootstrap (CDN), htmx (primary), Alpine.js (secondary), Vanilla JS
**Cache**: Redis (session, cache - when needed)
**Container**: Docker, Docker Compose
**Reverse Proxy**: Nginx

## Frontend Approach
- **htmx**: Primary tool for AJAX - server returns HTML fragments, htmx swaps them into DOM
- **Alpine.js**: Client-side interactivity without server calls (dropdowns, modals, toggles)
- **Thymeleaf**: Server-side rendering for initial page load
- **Bootstrap**: Styling via CDN
- **Vanilla JS**: Only when htmx/Alpine.js insufficient

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

## Development Methodology
- **TDD (Test-Driven Development)**:
  1. Red: Write failing test first
  2. Green: Write minimum code to pass test
  3. Refactor: Improve code while keeping tests passing
- **DDD (Domain-Driven Design)**: Design using domain language, evolve rich domain models
- **Agile**: 
  - Start simple, add complexity later
  - Core features first, additional features later
  - Avoid premature perfection - make it work, then improve
  - Iterative improvement: simple version → core features → details → optimization

## Testing Strategy
- Unit tests: Domain service level
- Integration tests: Repository, Controller level
- E2E tests: Main flows only (optional)
- When refactoring: update tests first, then refactor code

## Performance & Optimization
- Pagination: Offset-based (initial implementation)
- Caching: Redis for API responses, frequently accessed data
- Indexing: Add on FKs and search fields as needed
