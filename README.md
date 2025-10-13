# Surest Member Management

Java 17 Spring Boot project scaffold for member management.

Features:
- PostgreSQL scripts in `db/init/`.
- JWT authentication.
- Role-based access: ROLE_ADMIN and ROLE_USER.
- REST APIs for Member CRUD.
- Redis cache for GET /members/{id}.
- Pagination, sorting, filtering on GET /members.
- Unit and integration test examples.
- JaCoCo code coverage configured.

Run:
1. Create postgres database and run SQL scripts in `db/init/`.
2. Configure `src/main/resources/application.yml`.
3. Build: `./gradlew clean build`
4. Run: `./gradlew bootRun`

Notes:
- Replace JWT secret in application.yml with a strong secret.
- Seeded SQL uses placeholder bcrypt hashes. Replace with real bcrypt hashes or insert with your own.
