# Surest Member Management

Java 17 Spring Boot project scaffold for member management.

Features:
- PostgreSQL database
- Used Flyway SQL Scripts for INSERTION AND CREATION
- JWT authentication.
- Role-based access: ROLE_ADMIN and ROLE_USER.
- REST APIs for Surest Member CRUD.
- Caffeine cache for GET /members/{id}.
- Pagination, sorting, filtering on GET /members.
- Unit and integration test 
- JaCoCo code coverage configured for more than 80%

Run:
1. Build: `./gradlew clean build`
2. Run: `./gradlew bootRun`
3. Access the api via swagger.ui
4. Role with ID must be created in DB
5. Create a User with role-id assign ROLE_USER or ROLE_ADMIN
6. Login with the User JWT token will be generated
7. Authorize the JWT TOKEN 
8. Access the API 

