# Fashion E-commerce — HCMUTE Software Engineering

Five-member course project. The agreed starting stack is Java 17 / Spring Boot 3.5.x, React / TypeScript / Vite, and PostgreSQL 17. Scope and API decisions are documented in [`docs/decisions/STACK-API-DATABASE.md`](docs/decisions/STACK-API-DATABASE.md); team deadlines are in [`Ke-hoach-deadline-va-checklist-nhom.md`](Ke-hoach-deadline-va-checklist-nhom.md).

## Requirements

- JDK 17 (Spring Boot 3.5 needs Java 17 or newer).
- Node.js 22.12 or newer.
- Docker Desktop / Docker Engine.

---

## Hướng dẫn tải và chạy Code (Dành cho thành viên nhóm) 🚀

Các nhánh đã được gộp thành công vào nhánh `dev`. Để chạy dự án hoàn chỉnh trên máy của bạn, hãy làm theo các bước sau:

### Bước 1: Kéo code mới nhất về
Mở Terminal tại thư mục dự án và chạy:
```bash
git pull origin dev
```

### Bước 2: Khởi động Database (PostgreSQL)
Đảm bảo bạn đã bật ứng dụng Docker Desktop. Chạy lệnh sau để bật Database:
```bash
docker compose up -d database
```
*(Database sẽ chạy ngầm ở cổng 5433).*

### Bước 3: Khởi động Backend (Spring Boot)
Mở một Tab Terminal mới (giữ nguyên thư mục gốc của dự án), sau đó cấu hình biến môi trường và chạy Backend:

**Dành cho macOS / Linux:**
```bash
cd backend
export DATABASE_URL="jdbc:postgresql://localhost:5433/fashion_ecommerce"
export JWT_SECRET=$(openssl rand -base64 32)
export MAIL_USERNAME="your_mail@gmail.com"
export MAIL_PASSWORD="password"

./mvnw clean spring-boot:run
```
> ⚠️ **Lưu ý riêng cho người dùng Macbook (iCloud Drive):**
> Nếu trong lúc chạy Backend gặp lỗi `java.io.IOException: Operation timed out`, đó là do thư mục code bị iCloud đồng bộ gây treo file. Lệnh `./mvnw clean` ở trên đã tự động khắc phục điều này. Cứ kiên nhẫn đợi nó build xong nhé!

**Dành cho Windows (PowerShell):**
```powershell
cd backend
$env:DATABASE_URL = "jdbc:postgresql://localhost:5433/fashion_ecommerce"
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:MAIL_USERNAME = "your_mail@gmail.com"
$env:MAIL_PASSWORD = "password"

.\mvnw.cmd clean spring-boot:run
```
*(Chờ đến khi Terminal hiện dòng `Started FashionEcommerceApplication` là Backend đã chạy thành công).*

### Bước 4: Khởi động Frontend (React / Vite)
Mở thêm một Tab Terminal mới nữa, quay lại thư mục gốc của dự án:
```bash
cd frontend
npm install
npm run dev
```

Sau khi chạy xong, hãy mở trình duyệt và truy cập vào **http://localhost:5173/** để trải nghiệm trang web hoàn chỉnh! 🎉

*(Tài khoản Demo Admin và Khách hàng đã được tích hợp sẵn, bạn chỉ cần bấm "Đăng nhập demo" ở góc phải màn hình để test).*

---

## Current scaffold boundary

The project includes the shared API/database contract and module work on authentication, user profiles, and customer addresses. Spring Security is stateless, uses JWT bearer tokens, and restricts `/api/admin/**` to the `ADMIN` role. Public registration always creates a `CUSTOMER`; never accept a role from registration input.

## Module owners

- Member 1: Auth, user profile, addresses, shared authentication configuration.
- Member 2: Category, Brand, Product, variants, images, catalog schema coordination.
- Member 3: Customer product browsing and reviews.
- Member 4: Cart, checkout, orders, order state and stock transactions.
- Member 5: Voucher, Admin user management, review moderation, dashboard, project coordination.

Use module feature branches and PRs into `dev` as described in [`Quy_tac_git.md`](Quy_tac_git.md).
