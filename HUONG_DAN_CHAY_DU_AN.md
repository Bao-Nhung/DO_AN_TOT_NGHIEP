# Hướng Dẫn Chạy Dự Án Zestia

## 1. Yêu cầu cài đặt

Máy tính cần cài sẵn các phần mềm sau:

| Phần mềm | Phiên bản | Link tải |
|-----------|-----------|----------|
| **Java JDK** | 21 trở lên | https://adoptium.net/ (chọn Temurin JDK 21) |
| **Node.js** | 22 LTS (hoặc 20.19 trở lên) | https://nodejs.org/ (chọn bản LTS) |
| **SQL Server** | 2019 trở lên | https://www.microsoft.com/en-us/sql-server/sql-server-downloads (chọn Express miễn phí) |
| **SQL Server Management Studio (SSMS)** | Bản mới nhất | https://learn.microsoft.com/en-us/ssms/download-sql-server-management-studio-ssms |
| **Git** | Bản mới nhất | https://git-scm.com/downloads |

### Kiểm tra đã cài chưa

Mở **PowerShell** hoặc **Command Prompt** và chạy:

```
java -version
node -v
npm -v
git --version
```

Nếu mỗi lệnh đều hiện ra phiên bản thì OK.

---

## 2. Clone dự án

```
git clone https://github.com/<tên-tài-khoản>/DO_AN_TOT_NGHIEP.git
cd DO_AN_TOT_NGHIEP
```

Thư mục dự án gồm:
```
DO_AN_TOT_NGHIEP/
├── backend/        ← Spring Boot (Java)
├── frontend/       ← Vue 3 + Vite
├── database/       ← File SQL tạo dữ liệu
```

---

## 3. Cấu hình SQL Server

### Bước 1: Bật xác thực SQL Server (sa account)

1. Mở **SSMS** → kết nối vào SQL Server
2. Chuột phải vào tên server → **Properties** → **Security**
3. Chọn **SQL Server and Windows Authentication mode**
4. Nhấn OK, khởi động lại SQL Server

### Bước 2: Đặt mật khẩu cho tài khoản `sa`

1. Trong SSMS, mở **Security** → **Logins** → chuột phải **sa** → **Properties**
2. Đặt mật khẩu: `123456`
3. Bỏ tích **Enforce password policy**
4. Tab **Status** → Login: **Enabled**
5. Nhấn OK

### Bước 3: Bật TCP/IP

1. Mở **SQL Server Configuration Manager**
2. Chọn **SQL Server Network Configuration** → **Protocols for MSSQLSERVER**
3. Chuột phải **TCP/IP** → **Enable**
4. Chuột phải **TCP/IP** → **Properties** → tab **IP Addresses**
5. Kéo xuống **IPAll** → đặt **TCP Port** = `1433`
6. Khởi động lại SQL Server service

### Bước 4: Tạo database và nạp dữ liệu (QUAN TRỌNG)

Toàn bộ dữ liệu mẫu (sản phẩm, **giá tiền**, **hình ảnh**, biến thể màu/size, đơn hàng, tài khoản...) nằm trong **một file SQL duy nhất**: `database/fashion_shop.sql`.

> ⚠️ **Đây là bước quan trọng nhất để dự án chạy giống nhau trên mọi máy.** Nếu bỏ qua, sản phẩm sẽ hiện giá `0đ` và không có ảnh, vì giá lấy từ bảng `san_pham_chi_tiet` còn ảnh lấy từ bảng `Anh` — cả hai đều nằm trong file SQL này.

**Cách 1 — Dùng SSMS (khuyên dùng):**

1. Mở **SSMS** → kết nối vào SQL Server
2. Vào menu **File → Open → File...** → chọn `database/fashion_shop.sql`
3. Nhấn **Execute** (hoặc phím `F5`)
4. Chờ chạy xong. File tự tạo database `fashion_shop`, tạo tất cả bảng và nạp đầy đủ dữ liệu.

**Cách 2 — Dùng dòng lệnh (sqlcmd):**

```
sqlcmd -S localhost,1433 -U sa -P 123456 -C -f 65001 -i database/fashion_shop.sql
```

> 💡 File này **an toàn khi chạy lại nhiều lần**: dữ liệu chuẩn được thêm/cập nhật theo khóa nghiệp vụ, dữ liệu trùng được hợp nhất và các bất biến được kiểm tra trước khi báo thành công.
>
> 💡 Sau khi nạp xong, Spring Boot dùng `ddl-auto=validate` để kiểm tra schema khớp entity và không tự ý thay đổi dữ liệu.

---

## 4. Chạy Backend (Spring Boot)

### Bước 1: Mở terminal, di chuyển vào thư mục backend

```
cd backend
```

### Bước 2: Kiểm tra cấu hình database

Mở file `backend/src/main/resources/application.properties` và kiểm tra:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=fashion_shop;encrypt=true;trustServerCertificate=true;
spring.datasource.username=sa
spring.datasource.password=123456
```

Nếu mật khẩu sa khác `123456` thì sửa lại cho đúng.

### Bước 3: Chạy backend

**Windows PowerShell:**
```
.\mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```
./mvnw spring-boot:run
```

Để bật đầy đủ email và ChatAI trong phiên PowerShell hiện tại, đặt biến môi trường trước khi chạy:

```powershell
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_gmail_app_password"
$env:OPENAI_API_KEY="your_openai_api_key"
.\mvnw.cmd spring-boot:run
```

`MAIL_PASSWORD` phải là App Password của Gmail, không phải mật khẩu đăng nhập Gmail.
Lần đầu chạy sẽ tải dependencies (mất 3-5 phút tùy mạng). Chờ đến khi thấy:

```
Started BackendApplication in X.XX seconds
```

Backend chạy tại: **http://localhost:8080**

> **Mẹo:** Để tắt backend, nhấn `Ctrl + C` trong terminal.

---

## 5. Chạy Frontend (Vue 3 + Vite)

### Bước 1: Mở terminal MỚI (giữ terminal backend đang chạy), di chuyển vào thư mục frontend

```
cd frontend
```

### Bước 2: Cài dependencies (chỉ cần lần đầu)

```
npm install
```

### Bước 3: Cấu hình VietQR nếu muốn đổi tài khoản nhận tiền

Frontend đã có sẵn tài khoản nhận tiền demo trong code để cả nhóm clone về chạy ngay. Nếu muốn đổi tài khoản trên máy local, tạo file `frontend/.env.local` và đặt các biến:

```properties
VITE_VIETQR_BANK_ID=VCB
VITE_VIETQR_ACCOUNT_NO=9869167207
VITE_VIETQR_ACCOUNT_NAME=NGUYEN TIEN THANH
VITE_MOMO_ACCOUNT=0869167207
VITE_ZALOPAY_ACCOUNT=0869167207
VITE_PAYMENT_ACCOUNT_NAME=NGUYEN TIEN THANH
```

Nếu không tạo `frontend/.env.local`, website sẽ dùng các giá trị mặc định ở trên.

### Bước 4: Chạy frontend

```
npm run dev
```

Khi thấy:

```
  VITE vX.X.X  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

Frontend chạy tại: **http://localhost:5173**

Mở trình duyệt và truy cập địa chỉ trên.

---

## 6. Tóm tắt nhanh

Mỗi lần muốn chạy dự án, cần **2 terminal chạy song song**:

| Terminal | Thư mục | Lệnh |
|----------|---------|-------|
| Terminal 1 (Backend) | `backend/` | `.\mvnw.cmd spring-boot:run` |
| Terminal 2 (Frontend) | `frontend/` | `npm run dev` |

Sau đó mở trình duyệt tại **http://localhost:5173**

---

## 7. Tài khoản mặc định

Các tài khoản này có sẵn sau khi nạp file `database/fashion_shop.sql`:

| Vai trò | Tên đăng nhập | Mật khẩu |
|---------|---------------|----------|
| Admin | `admin` | `123456` |
| Nhân viên | `tuannv` | `123456` |
| Nhân viên POS | `nv_pos` | `123456` |
| Khách hàng | `khach.demo@zestia.vn` | `123456` |

> Có thể đăng nhập bằng **tên đăng nhập** hoặc **email** (`admin@zestia.vn`).
> Nhân viên chỉ dùng được nghiệp vụ cửa hàng trong ca đã xác nhận và check-in.

---

## 8. Lỗi thường gặp

### Backend không khởi động được

- **Nguyên nhân:** SQL Server chưa chạy hoặc chưa bật TCP/IP port 1433
- **Cách sửa:** Mở **Services** (gõ `services.msc`) → tìm **SQL Server** → Start. Kiểm tra lại TCP/IP ở bước 3.

### Lỗi `JAVA_HOME environment variable is not defined correctly`

- **Nguyên nhân:** Biến `JAVA_HOME` trỏ tới thư mục JDK không tồn tại (ví dụ máy đã gỡ JDK cũ).
- **Cách sửa (tạm thời, cho 1 phiên terminal):**

  **Windows (PowerShell):**
  ```
  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
  ```
  Sửa lại đường dẫn cho đúng thư mục JDK thực tế trên máy bạn (xem trong `C:\Program Files\Eclipse Adoptium\`).
- **Cách sửa (vĩnh viễn):** Vào **Settings → System → About → Advanced system settings → Environment Variables**, sửa `JAVA_HOME` trỏ đúng thư mục JDK đã cài.

### Không kết nối được database

- **Nguyên nhân:** Sai mật khẩu sa hoặc chưa tạo database `fashion_shop`
- **Cách sửa:** Kiểm tra lại bước 2 và bước 4 ở phần SQL Server.

### Frontend báo lỗi khi `npm install`

- **Nguyên nhân:** Node.js chưa cài hoặc phiên bản quá cũ
- **Cách sửa:** Cài Node.js 22 LTS (hoặc tối thiểu 20.19) rồi chạy lại `npm install`.

### Trang web trắng, không hiện sản phẩm

- **Nguyên nhân:** Backend chưa chạy
- **Cách sửa:** Đảm bảo terminal backend đang hiển thị `Started BackendApplication`. Thử reload trang (F5).

### Port 8080 bị chiếm

- **Cách kiểm tra:** Chạy `netstat -ano | findstr :8080`
- **Cách sửa:** Tắt process đang chiếm port, hoặc đổi port trong `application.properties`:
  ```properties
  server.port=8081
  ```
  (Nhớ cập nhật lại URL API trong frontend nếu đổi port)
