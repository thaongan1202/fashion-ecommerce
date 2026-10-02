# Fashion E-commerce — HCMUTE Software Engineering

Five-member course project. The agreed starting stack is Java 17 / Spring Boot 3.5.x, React / TypeScript / Vite, and PostgreSQL 17. Scope and API decisions are documented in [`docs/decisions/STACK-API-DATABASE.md`](docs/decisions/STACK-API-DATABASE.md); team deadlines are in [`Ke-hoach-deadline-va-checklist-nhom.md`](Ke-hoach-deadline-va-checklist-nhom.md).

## Requirements

- JDK 17 (Spring Boot 3.5 needs Java 17 or newer; make sure `JAVA_HOME` points to JDK 17, not Java 8).
- Node.js 22.12 or newer in the 22.x line, with npm.
- Docker Desktop / Docker Engine with Compose, for the local PostgreSQL container.

## Start locally (Windows PowerShell)

Run these commands from the repository root. Docker Compose starts the local database on host port `5433`.

1. Start PostgreSQL:

   ```powershell
   docker compose up -d database
   ```

2. In **Terminal 1**, go to the backend folder and configure its local environment:

   ```powershell
   cd backend
   $env:DATABASE_URL = "jdbc:postgresql://localhost:5433/fashion_ecommerce"

   $bytes = New-Object byte[] 32
   $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
   $rng.Dispose()

   $env:MAIL_USERNAME = "sender@gmail.com"
   $secureMailPassword = Read-Host "Google App Password" -AsSecureString
   $env:MAIL_PASSWORD = [System.Net.NetworkCredential]::new("", $secureMailPassword).Password.Replace(" ", "")

   .\mvnw.cmd spring-boot:run
   ```

   Keep this terminal open while using the app. These environment variables apply only to this PowerShell session. Generate a new `JWT_SECRET` for local development; changing it invalidates tokens created with the previous secret.

3. In **Terminal 2**, start the frontend:

   ```powershell
   cd frontend
   npm.cmd install
   npm.cmd run dev
   ```

   Run `npm.cmd install` once per checkout; afterwards use `npm.cmd run dev`.
4. Open the URL printed by Vite, usually `http://localhost:5173`. The API health endpoint is `http://localhost:8080/api/health`.

### Configure Gmail for registration OTP

The app sends a six-digit registration OTP, valid for 60 seconds, from the Gmail address in `MAIL_USERNAME` to the customer's email address submitted in the registration form.

1. Sign in to the Gmail account that will send the OTP and enable 2-Step Verification.
2. Create a Google App Password for the application. Google requires 2-Step Verification for App Passwords; see [Google's official instructions](https://support.google.com/accounts/answer/185833?hl=en).
3. Set `MAIL_USERNAME` to that sender Gmail address. When the PowerShell command above prompts for `Google App Password`, enter the generated App Password. The input is hidden and spaces are removed automatically.

Use the App Password, not the Gmail account's normal password. Do not put real passwords, App Passwords, or JWT secrets in source files, README examples, screenshots, or Git. If Google does not offer App Passwords for the account, check Google's eligibility notes in the linked instructions.

For macOS/Linux, start the database with `docker compose up -d database`, set `DATABASE_URL` to `jdbc:postgresql://localhost:5433/fashion_ecommerce`, provide `JWT_SECRET`, `MAIL_USERNAME`, and `MAIL_PASSWORD` as environment variables, then run `./mvnw spring-boot:run` in `backend` and `npm run dev` in `frontend`.

The frontend development server proxies `/api` requests to port 8080. Flyway applies database migrations when the backend starts. The database container is configured for local development only and binds its port to localhost; do not reuse that authentication configuration in a deployed environment. If `java -version` shows Java 8, set `JAVA_HOME` to JDK 17 before running the Maven Wrapper.

## Current scaffold boundary

The project includes the shared API/database contract and module work on authentication, user profiles, and customer addresses. Spring Security is stateless, uses JWT bearer tokens, and restricts `/api/admin/**` to the `ADMIN` role. Public registration always creates a `CUSTOMER`; never accept a role from registration input.

## Module owners

- Member 1: Auth, user profile, addresses, shared authentication configuration.
- Member 2: Category, Brand, Product, variants, images, catalog schema coordination.
- Member 3: Customer product browsing and reviews.
- Member 4: Cart, checkout, orders, order state and stock transactions.
- Member 5: Voucher, Admin user management, review moderation, dashboard, project coordination.

Use module feature branches and PRs into `dev` as described in [`Quy_tac_git.md`](Quy_tac_git.md). Do not commit local passwords or secrets.
