# Kế hoạch chạy nước rút — Đồ án Fashion E-commerce

**Hạn nộp:** Chủ nhật, 04/10/2026
**Múi giờ:** Việt Nam (UTC+7)
**Mục tiêu:** hoàn thành luồng MUST HAVE sớm nhất có thể, chừa ngày 03/10 để tích hợp, sửa lỗi và tập demo.

> Các mốc dưới đây cố ý sớm hơn hạn nộp. Nếu nhóm đã có source ở máy thành viên, hãy kiểm tra và đưa vào nhánh riêng; repository hiện tại mới có README và quy tắc Git. Không push thẳng lên `dev` hoặc `main`.

## Stack chốt đề xuất cho nhóm

- **Backend:** Java 17 + Spring Boot 3.5.x + Maven Wrapper; Spring Web, Spring Data JPA, Spring Security, Validation, PostgreSQL Driver, Flyway, BCrypt và thư viện JWT.
- **Frontend:** React + TypeScript + Vite; Node.js 22; React Router cho trang; dùng `fetch` trước, CSS thường; chưa thêm Next.js, Tailwind, Zustand hay UI framework.
- **Database:** PostgreSQL 17, database local trên cùng port/schema theo hướng dẫn nhóm. Một người điều phối migration Flyway; các owner gửi thay đổi schema cho người đó.
- **IDE đề xuất:** VS Code cho frontend và backend; cài Extension Pack for Java và Spring Tools cho VS Code. Spring Tool Suite/STS là một IDE khác cũng dùng được, nhưng không phải framework hay thành phần của stack.
- **Chưa dùng:** Redis, microservices/BFF, VNPay, chatbot, Docker toàn bộ ứng dụng.

Lý do: blueprint cho phép kiến trúc SPA + REST + relational database; Spring Boot hợp với lựa chọn Java nhóm đã nhắc và hỗ trợ phân tầng. Spring Boot 3.5.x chạy với Java 17; Vite có template React-TypeScript; PostgreSQL 17 đang được hỗ trợ. Chốt cùng một phiên bản/toolchain trong README để tránh máy mỗi người chạy khác nhau.

## Mốc thời gian chung

| Hạn | Việc phải xong | Điều kiện nghiệm thu |
|---|---|---|
| **30/09 — 17:15** | Xác nhận chính xác thứ phải nộp ngày 04/10; chốt stack và người phụ trách setup. | Cả nhóm biết deadline áp dụng cho báo cáo, demo hay source hoàn chỉnh; dùng stack đã chốt ở trên trừ khi có thành viên chỉ ra blocker cụ thể ngay hôm nay. |
| **30/09 — 18:30** | Đóng băng schema/API tối thiểu và các hợp đồng liên module. | Có tài liệu/issue ghi rõ endpoint, payload chính, role, tên status và Voucher–Checkout handoff. Những điểm chưa rõ được giao người quyết định và giờ chốt. |
| **30/09 — 21:00** | Project skeleton chạy được; mỗi người đã nhận module/nhánh. | Backend khởi động, frontend khởi động, database kết nối được; có hướng dẫn chạy ngắn; mỗi module có nhánh feature riêng. |
| **01/10 — 12:00** | Bản đầu theo module được đẩy lên nhánh cá nhân. | Có commit chạy được, API/UI stub hoặc luồng đang làm, migration/entity đã thống nhất; không để đến cuối ngày mới báo tình hình. |
| **01/10 — 21:00** | Mỗi người demo tiến độ riêng và nêu blocker. | Có thể khởi chạy module, thấy API hoặc màn hình thật; blocker có người xử lý và giờ hoàn tất. |
| **02/10 — 12:00** | Gửi PR các phần MUST để review/integration. | PR nhỏ, mô tả rõ, có bằng chứng chạy/thử; owner khác review. Member 4 và 5 đã tích hợp thử hợp đồng Voucher. |
| **02/10 — 18:00** | **Code-complete mục MUST** trên `dev`. | Đăng nhập, xem sản phẩm, giỏ hàng, checkout COD, đơn hàng/hủy đủ điều kiện và Voucher cơ bản ghép được; không còn mock trong luồng chính. |
| **02/10 — 21:00** | Chạy end-to-end lần đầu trên môi trường tích hợp. | Từ tài khoản mới đến đặt COD và xem đơn chạy được; có danh sách lỗi ưu tiên P0/P1/P2. |
| **03/10 — 10:00** | Hoàn tất 14 test case bắt buộc và sửa lỗi nghiêm trọng. | Có bảng pass/fail; P0/P1 được xử lý hoặc có phương án demo an toàn. |
| **03/10 — 13:00** | **Feature freeze.** | Không thêm chức năng mới. Chỉ sửa lỗi, ổn định dữ liệu, tài liệu và demo. |
| **03/10 — 18:00** | Regression test, hướng dẫn chạy, dữ liệu demo và kịch bản đã sẵn sàng. | Một thành viên khác chạy theo README; Customer và Admin demo được trên cùng môi trường. |
| **04/10 — trước giờ nộp ít nhất 2 giờ** | Bản nộp cuối. | Không merge tính năng mới vào phút cuối; xác nhận source, báo cáo và dữ liệu nộp đúng yêu cầu môn học. |

## Kết quả cần có theo thành viên

### Member 1 — Auth, hồ sơ, địa chỉ

**Hạn gửi PR:** 02/10 — 12:00. **Hoàn tất tích hợp:** 02/10 — 18:00.

- Đăng ký email hợp lệ; chuẩn hóa email; từ chối email trùng.
- Đăng nhập trả JWT; password được hash; token hết hạn theo cấu hình đã chốt.
- Role CUSTOMER/ADMIN được kiểm tra phía backend; Customer gọi `/admin/**` bị từ chối.
- Hồ sơ/địa chỉ tối thiểu chạy được nếu thuộc phần MUST của bản nộp.
- Cung cấp tài khoản demo Customer/Admin và cách khởi tạo an toàn; không commit mật khẩu bí mật.
- Gửi cho Member 5: giá trị `User.status`, `User.role`, cách nhận biết Admin, API khóa/mở user.

### Member 2 — Catalog

**Hạn gửi PR:** 02/10 — 12:00. **Hoàn tất tích hợp:** 02/10 — 18:00.

- Migration/seed cho Category, Brand, Product, Variant và ProductImage theo schema nhóm đã duyệt.
- Admin CRUD catalog; Product có trạng thái ẩn/hiện hoặc soft-delete theo blueprint.
- Variant lưu SKU, giá, tồn kho; size/color cho phép null; SKU trùng bị từ chối.
- Không xóa Category/Brand khi còn Product liên kết.
- Có dữ liệu mẫu đủ để Member 3 duyệt và Member 4 thử giỏ/stock.
- Chốt với nhóm API/đường dẫn ảnh upload; Product list/filter phía khách phối hợp Member 3.

### Member 3 — Duyệt sản phẩm và Review

**Hạn gửi PR:** 02/10 — 12:00. **Hoàn tất tích hợp:** 02/10 — 18:00.

- Khách xem danh sách và chi tiết sản phẩm; filter/search tối thiểu theo hợp đồng đã chốt.
- Customer gửi review rating/comment chỉ khi đã có đơn hoàn thành chứa sản phẩm; một review/user/product.
- Review có status và quy tắc hiển thị đã thống nhất với Member 5.
- Gửi sớm cho Member 5 API lấy danh sách review cần kiểm duyệt và payload đổi status.
- Tạo data hoặc hướng dẫn để kiểm thử review hợp lệ/không hợp lệ.

### Member 4 — Cart, Checkout, Order

**Hạn gửi PR đầu:** 02/10 — 12:00. **Voucher handoff phải chốt:** 30/09 — 18:00. **Tích hợp checkout:** 02/10 — 18:00.

- Customer thêm/sửa/xóa/xem cart; số lượng không vượt tồn kho tại checkout.
- Checkout COD tạo Order/OrderItem và snapshot thông tin giao hàng; trừ kho trong transaction.
- Checkout thất bại giữa chừng không để lại Order hoặc thay đổi tồn kho một phần.
- Customer chỉ hủy Pending; Admin chuyển trạng thái theo state machine và hủy đúng điều kiện.
- Lịch sử/chi tiết đơn hiển thị dữ liệu snapshot.
- Gửi cho Member 5 ngay hôm nay: payload checkout, thời điểm validate Voucher, thời điểm tăng `used_count`, interface/service cần gọi.

### Member 5 — Voucher, Admin User, Review moderation, Dashboard

**Hạn gửi hợp đồng Voucher:** 30/09 — 18:00. **Voucher API bản đầu:** 01/10 — 18:00. **Voucher tích hợp checkout:** 02/10 — 12:00 (trước hạn tích hợp chung).

- Voucher CRUD Admin tạo/sửa/tắt mã; có code, loại giảm, giá trị, hạn, giới hạn lượt, lượt đã dùng và trạng thái hoạt động theo schema nhóm chốt.
- API áp Voucher kiểm tra hết hạn/hết lượt; một đơn tối đa một mã; discount không vượt tổng tiền.
- Voucher tăng lượt đúng trong transaction checkout; hủy đơn không hoàn lại lượt.
- Có test hết hạn/hết lượt và phối hợp test checkout thành công/rollback.
- Admin User, moderation, Dashboard chỉ nhận thêm sau khi luồng MUST đã xanh; đây là SHOULD theo scope freeze.
- Cập nhật phần SRS Admin và checklist bàn giao; ghi rõ tính năng nào chưa làm thay vì tuyên bố hoàn tất.

## Ưu tiên lỗi khi thời gian thiếu

- **P0 — dừng demo/nộp:** backend/frontend không khởi động; không đăng nhập; checkout tạo dữ liệu sai; stock âm; Customer truy cập Admin; dữ liệu bị mất/hỏng.
- **P1 — sửa trước feature freeze:** Voucher sai hạn/lượt; hủy sai trạng thái; role sai; lịch sử Order hiển thị sai snapshot; luồng MUST bị chặn.
- **P2 — chỉ sửa nếu còn thời gian:** căn chỉnh giao diện, biểu đồ, chi tiết UX, SHOULD/COULD chưa cần cho luồng demo.

Nếu tới **02/10 — 18:00** mà MUST chưa chạy end-to-end, ngừng làm SHOULD/COULD và dồn người vào điểm nghẽn. Không giảm kiểm thử luồng checkout để lấy thêm tính năng phụ.

## Việc bạn cần làm ngay bây giờ (Member 5)

1. **Gửi tin nhắn chốt nhóm ngay**, yêu cầu mỗi người trả lời trước 17:15: deadline 04/10 là nộp gì, stack đang dùng, source hiện có ở đâu, việc nào đã chạy được, và blocker lớn nhất.
2. **Gọi nhanh Member 4** để chốt voucher handoff trước 18:00: checkout gửi `voucher_code` hay voucher ID; service nào kiểm tra; chỉ tăng lượt sau khi transaction tạo đơn thành công.
3. **Gọi Member 1 và 3** chốt User status/role và Review status/API list để không phải sửa schema/UI về sau.
4. **Đề xuất thêm `is_active` hoặc `status` cho Voucher**: requirement nói Admin có thể tắt voucher nhưng entity list chưa có field này. Xin cả nhóm duyệt cùng schema.
5. **Chọn phần mình làm hôm nay:** tạo branch cho Voucher theo Git rules, dựng entity/service/controller và ít nhất một test hết hạn hoặc hết lượt; chưa dành thời gian cho Dashboard.
6. **Tạo bảng theo dõi đơn giản** với cột: việc, owner, deadline, link PR, cách nghiệm thu, blocker. Cập nhật lúc 21:00 mỗi ngày.
7. **Gửi báo cáo ngắn cuối ngày:** đã merge gì, test nào chạy, blocker nào cần quyết định. Nếu blocker nằm ở hợp đồng liên module, gọi trực tiếp người phụ trách thay vì chờ tới ngày tích hợp.

### Tin nhắn có thể gửi cho nhóm

> Nhóm mình còn đến 4/10 nên mình đề xuất chốt sớm để dành 3/10 sửa lỗi và tập demo. Mọi người xác nhận giúp trước 17:15 hôm nay: ngày 4/10 phải nộp chính xác những gì; stack nhóm đã chọn; source hiện có ở đâu; phần nào chạy được; blocker lớn nhất là gì. Mốc chung đề xuất: chốt API/schema tối thiểu 18:30 hôm nay; bản đầu từng module 1/10 21:00; gửi PR phần MUST 2/10 12:00; MUST chạy trên dev 2/10 18:00; feature freeze 3/10 13:00. Mình phụ trách Voucher; Member 4 gửi checkout payload và điểm gọi Voucher trước 18:00 hôm nay. Member 1 gửi User role/status; Member 3 gửi Review status và API list moderation. Mình sẽ gửi API Voucher bản đầu trước 18:00 ngày mai và hoàn tất tích hợp trước trưa 2/10. Admin User, moderation, Dashboard làm sau nếu MUST đã ổn.

## Giới hạn phạm vi và cách báo tiến độ

Mốc này rất gấp, nhất là khi repository chung hiện chưa có source. Nếu thành viên có code trên máy/nhánh riêng, kiểm kê và tích hợp ngay hôm nay. Đừng báo “xong” nếu chưa khởi chạy được hoặc chưa chỉ ra cách kiểm tra. Ghi rõ mỗi tính năng là **Done**, **In progress**, hay **Not started**; không che phần chưa hoàn thành bằng demo mock.
