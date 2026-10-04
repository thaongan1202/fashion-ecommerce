# Tài khoản Admin dùng thử

Khi chạy backend với profile `dev`, ứng dụng tự tạo tài khoản này nếu username và email chưa được dùng:

- Username: `testadmin`
- Email: `testadmin@utefashionhub.local`
- Password: `Admin123!`
- Quyền: `ADMIN`

Mở trang đăng nhập tại `http://localhost:3000/login` hoặc gửi request `POST /api/v1/auth/login`:

```json
{
  "usernameOrEmail": "testadmin",
  "password": "Admin123!"
}
```

Mật khẩu được mã hóa bằng BCrypt trước khi lưu. Tài khoản chỉ được seed ở profile `dev`; có thể đổi thông tin mặc định bằng các biến môi trường `APP_TEST_ADMIN_USERNAME`, `APP_TEST_ADMIN_EMAIL` và `APP_TEST_ADMIN_PASSWORD`.
