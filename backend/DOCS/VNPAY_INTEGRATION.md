# HƯỚNG DẪN TÍCH HỢP VNPAY - MODULE 6

## 📋 CÁC FILE ĐÃ TẠO

### 1. Configuration
- `config/VNPayConfig.java` - Cấu hình VNPay từ application.yaml

### 2. Utilities
- `util/VNPayUtil.java` - HMAC SHA512, URL encoding, query builder

### 3. DTOs
- `dto/request/payment/CreatePaymentRequest.java`
- `dto/response/payment/PaymentResponse.java`
- `dto/response/payment/VNPayPaymentResponse.java`

### 4. Service Layer
- `service/IPaymentService.java`
- `service/impl/VNPayService.java`

### 5. Controller
- `controller/PaymentController.java`

### 6. Repository
- `repository/PaymentRepository.java` - Đã thêm `findByOrderId()`

---

## 🔧 CẤU HÌNH VNPAY

### Bước 1: Cập nhật `application.yaml` hoặc biến môi trường

```yaml
vnpay:
  url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  tmn-code: YOUR_TMN_CODE        # Thay bằng mã của bạn
  hash-secret: YOUR_HASH_SECRET  # Thay bằng secret của bạn
  return-url: http://localhost:8081/api/v1/payments/vnpay/return
```

**HOẶC** set biến môi trường:
```bash
export VNPAY_TMN_CODE="W0MI1ZMG"
export VNPAY_HASH_SECRET="W5AF1T7ITXWOP1PC960RXCWYW0UWBBYZ"
```

### Bước 2: Cập nhật Docker Compose (nếu dùng)

Thêm vào `backend/docker-compose.yml`:
```yaml
services:
  backend:
    environment:
      - VNPAY_TMN_CODE=YOUR_TMN_CODE
      - VNPAY_HASH_SECRET=YOUR_HASH_SECRET
      - VNPAY_RETURN_URL=http://localhost:8081/api/v1/payments/vnpay/return
```

---

## 🚀 API ENDPOINTS

### 1. Tạo Payment URL (Frontend call)
```http
POST /api/v1/payments/vnpay/create
Content-Type: application/json
Authorization: Bearer <token>

{
  "orderId": 123,
  "amount": 32990000,
  "orderInfo": "Thanh toan don hang ORD_251207093853",
  "locale": "vn"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Payment URL created successfully",
  "data": {
    "code": "00",
    "message": "Success",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
  }
}
```

### 2. VNPay Callback (VNPay server call)
```http
GET /api/v1/payments/vnpay/callback?vnp_Amount=...&vnp_ResponseCode=00&...
```

### 3. VNPay Return (User redirect)
```http
GET /api/v1/payments/vnpay/return?vnp_Amount=...&vnp_ResponseCode=00&...
```
→ Redirect về frontend: `http://localhost:3000/payment-result?orderId=123&status=SUCCESS`

### 4. Lấy thông tin Payment
```http
GET /api/v1/payments/order/{orderId}
GET /api/v1/payments/{paymentId}
```

---

## 🔄 FLOW THANH TOÁN

### Flow hoàn chỉnh:

```
1. Customer tạo Order
   POST /api/orders
   → Order status = WAITING_PAYMENT

2. Customer request payment URL
   POST /api/v1/payments/vnpay/create
   → Nhận paymentUrl

3. Frontend redirect user đến VNPay
   window.location.href = paymentUrl

4. User thanh toán trên VNPay

5. VNPay callback (IPN)
   GET /api/v1/payments/vnpay/callback
   → Lưu Payment record
   → Update Order status = CONFIRMED (nếu success)

6. VNPay redirect user về
   GET /api/v1/payments/vnpay/return
   → Redirect về frontend với kết quả

7. Frontend hiển thị kết quả
   GET /payment-result?orderId=123&status=SUCCESS
```

---

## 🧪 TESTING

### Test với Postman:

1. **Tạo Order:**
```json
POST /api/orders
{
  "userId": 6,
  "email": "test@example.com",
  "recipientName": "Nguyen Van A",
  "phoneNumber": "0912345678",
  "shippingAddress": "123 ABC Street",
  "paymentMethod": "VNPAY",
  "items": [
    {
      "productId": 1,
      "quantity": 1
    }
  ]
}
```

2. **Tạo Payment URL:**
```json
POST /api/v1/payments/vnpay/create
{
  "orderId": 1,
  "amount": 32990000,
  "orderInfo": "Test payment",
  "locale": "vn"
}
```

3. **Copy `paymentUrl` và mở trong browser**

4. **Thanh toán trên VNPay Sandbox:**
- Ngân hàng: NCB
- Số thẻ: 9704198526191432198
- Tên chủ thẻ: NGUYEN VAN A
- Ngày phát hành: 07/15
- Mật khẩu OTP: 123456

---

## ⚙️ CẤU HÌNH FRONTEND

### Frontend cần update:

```typescript
// 1. Sau khi tạo order thành công, gọi create payment
const createPayment = async (orderId: number, amount: number) => {
  const response = await fetch('/api/v1/payments/vnpay/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      orderId,
      amount,
      orderInfo: `Thanh toan don hang ${orderId}`,
      locale: 'vn'
    })
  });
  
  const data = await response.json();
  
  // Redirect to VNPay
  window.location.href = data.data.paymentUrl;
};

// 2. Tạo page payment-result để nhận kết quả
// /payment-result?orderId=123&status=SUCCESS&transactionId=xxx
```

---

## 🔐 BẢO MẬT

1. **NEVER commit** `tmn-code` và `hash-secret` vào Git
2. Luôn dùng biến môi trường trong production
3. Verify VNPay signature trong callback
4. Check order status trước khi process payment
5. Log tất cả payment transactions

---

## 📝 GHI CHÚ

- VNPay Sandbox URL: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- Production URL: `https://vnpayment.vn/paymentv2/vpcpay.html`
- Payment expires sau 15 phút
- Amount phải nhân 100 khi gửi cho VNPay (VNPay format)
- Return URL phải public và accessible từ internet (production)

---

## 🐛 TROUBLESHOOTING

### Lỗi "Invalid signature"
→ Check `hash-secret` có đúng không
→ Check thứ tự params khi hash (phải sort)

### Payment callback không nhận được
→ Check return URL có đúng không
→ Check server có accessible từ internet không (production)

### Order status không update
→ Check logs trong VNPayService
→ Verify transaction trong database

---

## 📚 TÀI LIỆU THAM KHẢO

- VNPay Documentation: https://sandbox.vnpayment.vn/apis/docs/
- Project mẫu: `VNPay-integration/`
