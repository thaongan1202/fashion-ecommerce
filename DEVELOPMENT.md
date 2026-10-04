# 🛠️ Development & Running Guide

Tài liệu này hướng dẫn cách thiết lập môi trường phát triển và khởi chạy dự án **UTE Fashion Hub**.

---

## 🛠️ Yêu Cầu Cài Đặt (Prerequisites)

Để chạy dự án, bạn cần cài đặt các công cụ sau:
*   [Java JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
*   [Node.js 20+](https://nodejs.org/) (Khuyến nghị bản LTS)
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/)
*   [Git](https://git-scm.com/)

---

## 📥 Hướng Dẫn Cài Đặt & Chạy (Installation)

### 1. Clone Project
```bash
git clone https://github.com/darktheDE/ute-phonehub.git
cd ute-phonehub
```

### 2. Cấu Hình & Chạy Backend (Docker)
Chúng tôi khuyến khích chạy Backend và Database bằng Docker Compose để đảm bảo môi trường đồng nhất.

1.  Di chuyển vào thư mục backend:
    ```bash
    cd backend
    ```

2.  Tạo file cấu hình môi trường:
    ```bash
    # Trên Windows (PowerShell)
    copy .env.example .env
    # Trên Linux/Mac
    cp .env.example .env
    ```

3.  Cập nhật file `.env` với thông tin của bạn (Google Client ID, VNPay, Mail, etc.). *Nếu chỉ chạy test local cơ bản, bạn có thể giữ nguyên các cấu hình Database/Redis mặc định.*

4.  Khởi chạy hệ thống (Database + Redis + Backend):
    ```bash
    docker-compose up -d --build
    ```
    *Lệnh này sẽ tự động khởi tạo database, chạy migration và start server tại port `8081`.*

### 3. Cấu Hình & Chạy Frontend
1.  Mở một terminal mới, di chuyển vào thư mục frontend:
    ```bash
    cd frontend
    ```

2.  Tạo file môi trường (Nếu chưa có):
    Tạo file `.env.local` với nội dung sau:
    ```properties
    NEXT_PUBLIC_API_URL=http://localhost:8081/api/v1
    ```

3.  Cài đặt dependencies:
    ```bash
    npm install
    ```

4.  Chạy server development:
    ```bash
    npm run dev
    ```

🚀 **Frontend sẽ chạy tại:** [http://localhost:3000](http://localhost:3000)

---

## 📚 Tài Liệu API (Documentation)

Sau khi chạy Backend thành công, bạn có thể truy cập tài liệu API đầy đủ (Swagger UI) tại:

👉 **[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)**

---

## 📂 Cấu Trúc Dự Án (Project Structure)

```
ute-phonehub/
├── backend/                # Mã nguồn Backend (Spring Boot)
│   ├── src/main/java       # Source code Java
│   ├── src/main/resources  # Configs, SQL Migrations
│   ├── Dockerfile          # Config Docker Backend
│   └── docker-compose.yml  # Config Docker Compose (Full Stack infra)
├── frontend/               # Mã nguồn Frontend (Next.js)
│   ├── app/                # Next.js App Router (Pages & Layouts)
│   ├── components/         # React Components (Shadcn/UI)
│   ├── services/           # API Services (Axios)
│   └── store/              # Zustand State Management
├── docs/                   # Tài liệu dự án (SRS, Diagrams, Convention)
└── .cursor/                # Config & Docs cho AI Agent (Cursor)
```

---

## 🤝 Đóng Góp (Contributing)

1.  **Fork** dự án.
2.  Tạo branch feature mới (`git checkout -b feature/AmazingFeature`).
3.  Commit thay đổi (`git commit -m 'Add some AmazingFeature'`).
4.  Push lên branch (`git push origin feature/AmazingFeature`).
5.  Tạo **Pull Request**.

---

## 📏 Quy Chuẩn Lập Trình (Conventions)

Vui lòng tham khảo tài liệu chi tiết tại: [docs/CONVENTIONS.md](docs/CONVENTIONS.md)
