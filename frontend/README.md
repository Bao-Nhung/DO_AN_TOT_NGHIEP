# LUMIÈRE — Vue 3 + Bootstrap 5

Chuyển đổi từ HTML thuần sang Vue 3 với Bootstrap 5, Vite, Vue Router.

## 🚀 Cài đặt & chạy

```bash
npm install
npm run dev       # Dev server: http://localhost:5173
npm run build     # Build production
npm run preview   # Preview build
```

## 📁 Cấu trúc dự án

```
lumiere/
├── index.html                      # Entry HTML
├── vite.config.js                  # Vite config (alias @→src)
├── package.json
│
└── src/
    ├── main.js                     # App entry — mount Vue, router
    ├── App.vue                     # Root: cursor, navbar, cart drawer, transitions
    │
    ├── assets/
    │   └── main.css                # Design tokens + global utility classes
    │
    ├── composables/                # Reactive logic (không phụ thuộc component)
    │   ├── useCart.js              # Cart state (items, open/close, qty, format)
    │   ├── useToast.js             # Toast notification state
    │   ├── useReveal.js            # Scroll-reveal IntersectionObserver
    │   └── useProducts.js          # Dữ liệu sản phẩm dùng chung
    │
    ├── components/
    │   ├── layout/                 # Khung sườn toàn trang
    │   │   ├── AppNavbar.vue       # Navbar cố định + scroll effect
    │   │   ├── AppFooter.vue       # Footer 4 cột
    │   │   ├── CartDrawer.vue      # Slide-in cart từ phải
    │   │   └── ToastNotification.vue # Toast góc dưới phải
    │   │
    │   └── ui/                     # Component tái sử dụng
    │       ├── ProductCard.vue     # Card sản phẩm (badge, wish, quick-add)
    │       ├── CollectionCard.vue  # Card danh mục (hover overlay)
    │       └── MarqueeStrip.vue    # Dải chạy chữ đen-vàng
    │
    └── pages/                      # Một file = một route
        ├── HomePage.vue            # /           → Hero + collections + products
        ├── ProductsPage.vue        # /collections → Grid lọc
        ├── ProductDetail.vue       # /product/:id → Gallery + thông tin + mua
        ├── WishlistPage.vue        # /wishlist   → Danh sách yêu thích
        ├── ProfilePage.vue         # /profile    → Đơn hàng / cài đặt / địa chỉ
        └── LoginPage.vue           # /login      → 2 cột visual + form
```

## 🧩 Routing (Vue Router 4 – Hash mode)

| Route              | Component          | Mô tả               |
|--------------------|--------------------|---------------------|
| `/`                | HomePage           | Trang chủ           |
| `/collections`     | ProductsPage       | Bộ sưu tập + lọc   |
| `/product/:id`     | ProductDetail      | Chi tiết sản phẩm   |
| `/wishlist`        | WishlistPage       | Yêu thích           |
| `/profile`         | ProfilePage        | Tài khoản           |
| `/login`           | LoginPage          | Đăng nhập           |

## 🗄️ State (Composables)

- **useCart** — singleton reactive state, không cần Pinia
- **useToast** — singleton, gọi `showToast(msg)` từ bất kỳ đâu
- **useReveal** — gọi trong `onMounted` của mỗi page để kích hoạt scroll reveal
- **useProducts** — mảng sản phẩm tĩnh, thay bằng API call khi cần

## 🎨 Design System

Tất cả màu sắc và font định nghĩa trong `src/assets/main.css` dưới `:root`:

```css
--lm-cream, --lm-beige, --lm-black, --lm-gold   /* palette */
--lm-font-display  /* Cormorant Garamond */
--lm-font-body     /* Inter */
```

Các class tiện ích bắt đầu bằng `.lm-` để không xung đột với Bootstrap.

## 📦 Dependencies

| Package           | Phiên bản | Dùng cho              |
|-------------------|-----------|-----------------------|
| vue               | ^3.4      | Framework             |
| vue-router        | ^4.3      | SPA routing           |
| bootstrap         | ^5.3      | Grid, utilities       |
| bootstrap-icons   | ^1.11     | Icon set              |
| vite              | ^5.2      | Build tool            |
| @vitejs/plugin-vue| ^5.0      | Vue SFC support       |
