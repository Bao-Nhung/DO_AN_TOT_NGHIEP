import { ref, computed } from 'vue'

const currentLocale = ref(localStorage.getItem('zestia_lang') || 'vi')

const translations = {
  vi: {
    // Navigation
    home: 'Trang Chủ',
    products: 'Sản Phẩm',
    lookbook: 'Lookbook',
    reviews: 'Đánh Giá',
    tracking: 'Tra Cứu Đơn',
    myOrders: 'Đơn Hàng',
    wishlist: 'Yêu Thích',
    about: 'Thông Tin',
    account: 'Tài Khoản',
    cart: 'Giỏ Hàng',
    notifications: 'Thông Báo',
    posTerminal: 'Bán Tại Quầy',

    // Search
    searchPlaceholder: 'Tìm kiếm sản phẩm, danh mục...',
    searchBtn: 'Tìm kiếm',

    // Notification dropdown
    notifTitle: 'Thông báo',
    notifMarkAll: 'Đọc hết',
    notifEmpty: 'Không có thông báo mới',
    notifViewAll: 'Xem tất cả thông báo',

    // Footer
    footerAboutTitle: 'Về Zestia Fashion',
    footerAboutText: 'Thương hiệu thời trang nữ cao cấp mang phong cách hiện đại, thanh lịch và dẫn đầu xu hướng.',
    footerShowrooms: 'Hệ thống Showroom',
    footerPolicies: 'Chính sách cửa hàng',
    footerContact: 'Thông tin liên hệ',
    footerRights: 'Bản quyền thuộc về Zestia Fashion. Tất cả các quyền được bảo lưu.',
    showroomAddr1: 'Showroom 1: 123 Nguyễn Trãi, Quận 1, TP. Hồ Chí Minh',
    showroomAddr2: 'Showroom 2: 456 Cầu Giấy, Quận Cầu Giấy, Hà Nội',
    showroomHours: 'Giờ mở cửa: 08:00 - 22:00 hàng ngày',

    // Common Actions
    checkout: 'Thanh toán',
    addToCart: 'Thêm vào giỏ',
    buyNow: 'Mua ngay',
    cancel: 'Hủy',
    confirm: 'Xác nhận',
    save: 'Lưu',
    delete: 'Xóa',
    edit: 'Sửa',
    viewDetail: 'Xem chi tiết',
    filter: 'Bộ lọc',
    sort: 'Sắp xếp',
    contactUs: 'Liên hệ với chúng tôi',

    // Lang toggle button label
    langSwitchText: 'EN 🇬🇧',
    currentLangName: 'Tiếng Việt',
  },
  en: {
    // Navigation
    home: 'Home',
    products: 'Products',
    lookbook: 'Lookbook',
    reviews: 'Reviews',
    tracking: 'Track Order',
    myOrders: 'My Orders',
    wishlist: 'Wishlist',
    about: 'About Us',
    account: 'Account',
    cart: 'Shopping Cart',
    notifications: 'Notifications',
    posTerminal: 'POS Terminal',

    // Search
    searchPlaceholder: 'Search products, categories...',
    searchBtn: 'Search',

    // Notification dropdown
    notifTitle: 'Notifications',
    notifMarkAll: 'Mark all as read',
    notifEmpty: 'No new notifications',
    notifViewAll: 'View all notifications',

    // Footer
    footerAboutTitle: 'About Zestia Fashion',
    footerAboutText: 'High-end womens fashion brand offering modern, elegant, and trend-leading styles.',
    footerShowrooms: 'Store Showrooms',
    footerPolicies: 'Store Policies',
    footerContact: 'Contact Info',
    footerRights: 'Copyright Zestia Fashion. All rights reserved.',
    showroomAddr1: 'Showroom 1: 123 Nguyen Trai, District 1, Ho Chi Minh City',
    showroomAddr2: 'Showroom 2: 456 Cau Giay, Cau Giay District, Hanoi',
    showroomHours: 'Operating hours: 08:00 AM - 10:00 PM Daily',

    // Common Actions
    checkout: 'Checkout',
    addToCart: 'Add to Cart',
    buyNow: 'Buy Now',
    cancel: 'Cancel',
    confirm: 'Confirm',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    viewDetail: 'View Details',
    filter: 'Filter',
    sort: 'Sort',
    contactUs: 'Contact Us',

    // Lang toggle button label
    langSwitchText: 'VI 🇻🇳',
    currentLangName: 'English',
  }
}

export function useI18n() {
  function t(key, fallback = '') {
    const dict = translations[currentLocale.value] || translations.vi
    return dict[key] || fallback || key
  }

  function setLocale(lang) {
    if (translations[lang]) {
      currentLocale.value = lang
      localStorage.setItem('zestia_lang', lang)
    }
  }

  function toggleLocale() {
    setLocale(currentLocale.value === 'vi' ? 'en' : 'vi')
  }

  const locale = computed(() => currentLocale.value)

  return {
    locale,
    t,
    setLocale,
    toggleLocale,
    isEn: computed(() => currentLocale.value === 'en')
  }
}
