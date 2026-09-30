# Fashion E-commerce — HCMUTE Software Engineering

Five-member course project. The agreed starting stack is Java 17 / Spring Boot 3.5.x, React / TypeScript / Vite, and PostgreSQL 17. Scope and API decisions are documented in [`docs/decisions/STACK-API-DATABASE.md`](docs/decisions/STACK-API-DATABASE.md); team deadlines are in [`Ke-hoach-deadline-va-checklist-nhom.md`](Ke-hoach-deadline-va-checklist-nhom.md).

## Requirements

- JDK 17 (Spring Boot 3.5 needs Java 17 or newer; make sure `JAVA_HOME` points to JDK 17, not Java 8).
- Node.js 22.12 or newer in the 22.x line, with npm.
- Docker Desktop / Docker Engine with Compose, for the local PostgreSQL container.

## Start locally

From the repository root (macOS):

1. Start PostgreSQL: `docker compose up -d database`
2. In **Terminal 1**, select JDK 17 and start the API:

   ```sh
   export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
   cd backend
   ./mvnw spring-boot:run
   ```

3. In **Terminal 2**, select Node 22 and start the frontend:

   ```sh
   cd frontend
   nvm use
   npm install
   npm run dev
   ```

   Run `npm install` once per checkout; afterwards use `npm run dev`.
4. Open `http://localhost:5173` and select **Check backend**. The API health endpoint is `http://localhost:8080/api/health`.

The frontend development server proxies `/api` requests to port 8080. Flyway creates the initial schema when the backend starts. The database container is configured for local development only and binds its port to localhost; do not reuse that authentication configuration in a deployed environment. If `java -version` shows Java 8, set `JAVA_HOME` to JDK 17 as above before running the Maven Wrapper.

## Current scaffold boundary

The initial scaffold contains the running backend/frontend shells, database migration for the 14 blueprint entities, and the shared API/database contract. Business endpoints remain module work. Spring Security currently permits only `/api/health` and denies all other requests until Member 1 implements authentication and role-based access.

## Module owners

- Member 1: Auth, user profile, addresses, shared authentication configuration.
- Member 2: Category, Brand, Product, variants, images, catalog schema coordination.
- Member 3: Customer product browsing and reviews.
- Member 4: Cart, checkout, orders, order state and stock transactions.
- Member 5: Voucher, Admin user management, review moderation, dashboard, project coordination.

Use module feature branches and PRs into `dev` as described in [`Quy_tac_git.md`](Quy_tac_git.md). Do not commit local passwords or secrets.
