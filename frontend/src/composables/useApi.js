import { currentLocale } from '@/i18n'

export const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '')

function getToken() {
  return localStorage.getItem('zestia_token')
}

function getHeaders() {
  const h = {
    'Content-Type': 'application/json',
    'Accept-Language': currentLocale()
  }
  const token = getToken()
  if (token) h['Authorization'] = `Bearer ${token}`
  return h
}

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: getHeaders(),
    ...options
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }))
    if (res.status === 401 && getToken()) {
      clearAuthStorage()
      window.dispatchEvent(new Event('zestia-auth-changed'))
    }
    if (res.status === 403 && err.code === 'SHIFT_REQUIRED') {
      window.dispatchEvent(new CustomEvent('zestia-shift-required', { detail: err }))
    }
    throw { status: res.status, ...err }
  }
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

async function downloadFile(path, fallbackName) {
  const headers = { 'Accept-Language': currentLocale() }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(`${API_BASE}${path}`, { headers })
  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: 'Không thể tải file' }))
    throw { status: res.status, ...error }
  }
  const disposition = res.headers.get('Content-Disposition') || ''
  const matchedName = disposition.match(/filename="?([^";]+)"?/i)?.[1]
  const url = URL.createObjectURL(await res.blob())
  const link = document.createElement('a')
  link.href = url
  link.download = matchedName || fallbackName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

function clearAuthStorage() {
  localStorage.removeItem('zestia_token')
  localStorage.removeItem('zestia_user')
}

function toQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.append(key, String(value))
  })
  const encoded = query.toString()
  return encoded ? `?${encoded}` : ''
}

function tokenIsCurrent(token) {
  if (!token) return false
  try {
    const segment = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = segment.padEnd(Math.ceil(segment.length / 4) * 4, '=')
    const payload = JSON.parse(atob(padded))
    return !payload.exp || payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

export function api() {
  return {
    // Auth
    login: (username, password) =>
      request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      }),
    googleLogin: (credential) =>
      request('/auth/google', { method: 'POST', body: JSON.stringify({ credential }) }),
    me: () => request('/auth/me'),
    register: (data) =>
      request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
    forgotPassword: (identifier) =>
      request('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ identifier }) }),
    resetPassword: (token, newPassword) =>
      request('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword }) }),
    changePassword: (currentPassword, newPassword) =>
      request('/auth/change-password', {
        method: 'POST',
        body: JSON.stringify({ currentPassword, newPassword })
      }),

    // Customer AI support
    sendAiChat: (data) =>
      request('/ai-chat', { method: 'POST', body: JSON.stringify(data) }),

    // Newsletter
    subscribeNewsletter: (email) =>
      request('/newsletter/subscribe', { method: 'POST', body: JSON.stringify({ email }) }),

    // Lucky wheel: reads eligible completed orders but stores outcomes separately.
    getLuckyWheelCampaign: () => request('/lucky-wheel/campaign'),
    checkLuckyWheelEligibility: (orderCode, phone) => request('/lucky-wheel/check', {
      method: 'POST', body: JSON.stringify({ orderCode, phone })
    }),
    spinLuckyWheel: (orderCode, phone) => request('/lucky-wheel/spin', {
      method: 'POST', body: JSON.stringify({ orderCode, phone })
    }),
    getLuckyWheelCampaignsAdmin: () => request('/lucky-wheel/admin/campaigns'),
    createLuckyWheelCampaign: (data) => request('/lucky-wheel/admin/campaigns', {
      method: 'POST', body: JSON.stringify(data)
    }),
    updateLuckyWheelCampaign: (id, data) => request(`/lucky-wheel/admin/campaigns/${id}`, {
      method: 'PUT', body: JSON.stringify(data)
    }),
    createLuckyWheelPrize: (campaignId, data) => request(`/lucky-wheel/admin/campaigns/${campaignId}/prizes`, {
      method: 'POST', body: JSON.stringify(data)
    }),
    updateLuckyWheelPrize: (campaignId, prizeId, data) => request(`/lucky-wheel/admin/campaigns/${campaignId}/prizes/${prizeId}`, {
      method: 'PUT', body: JSON.stringify(data)
    }),
    uploadLuckyWheelIcon: async (file) => {
      const form = new FormData()
      form.append('file', file)
      const headers = { 'Accept-Language': currentLocale() }
      const token = getToken()
      if (token) headers.Authorization = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/lucky-wheel/admin/icons`, { method: 'POST', headers, body: form })
      if (!res.ok) throw await res.json().catch(() => ({ error: 'Không thể tải ảnh biểu tượng' }))
      return res.json()
    },
    getLuckyWheelSpinsAdmin: (params = {}) => request(`/lucky-wheel/admin/spins${toQuery(params)}`),
    deliverLuckyWheelPrize: (spinId) => request(`/lucky-wheel/admin/spins/${spinId}/deliver`, { method: 'PUT' }),

    // Products (San Pham)
    getSanPham: () => request('/san-pham'),
    getVay: () => request('/san-pham'),
    getSanPhamPage: (params) => request(`/san-pham/paged${toQuery(params)}`),
    getVayPage: (params) => request(`/san-pham/paged${toQuery(params)}`),
    getStockMovements: (params) => request(`/san-pham/stock-movements${toQuery(params)}`),
    getSanPhamById: (id) => request(`/san-pham/${id}`),
    getVayById: (id) => request(`/san-pham/${id}`),
    createSanPham: (data) => request('/san-pham', { method: 'POST', body: JSON.stringify(data) }),
    createVay: (data) => request('/san-pham', { method: 'POST', body: JSON.stringify(data) }),
    updateSanPham: (id, data) => request(`/san-pham/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    updateVay: (id, data) => request(`/san-pham/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    uploadVayAnh: async (id, file) => {
      const fd = new FormData()
      fd.append('file', file)
      const headers = { 'Accept-Language': currentLocale() }
      const token = getToken()
      if (token) headers['Authorization'] = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/san-pham/${id}/anh`, { method: 'POST', headers, body: fd })
      if (!res.ok) throw await res.json().catch(() => ({ error: 'Upload thất bại' }))
      return res.json()
    },
    uploadVayColorImage: async (id, colorId, file) => {
      const fd = new FormData()
      fd.append('file', file)
      const headers = { 'Accept-Language': currentLocale() }
      const token = getToken()
      if (token) headers.Authorization = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/san-pham/${id}/mau/${colorId}/anh`, { method: 'POST', headers, body: fd })
      if (!res.ok) throw await res.json().catch(() => ({ error: 'Upload ảnh màu thất bại' }))
      return res.json()
    },
    deleteVayAnh: (anhId) => request(`/san-pham/anh/${anhId}`, { method: 'DELETE' }),

    // Orders & Tracking
    getHoaDon: () => request('/hoa-don'),
    getHoaDonPage: (params) => request(`/hoa-don/paged${toQuery(params)}`),
    getHoaDonById: (id) => request(`/hoa-don/${id}`),
    updateOrderStatus: (id, trangThai, ghiChu) =>
      request(`/hoa-don/${id}/trang-thai`, {
        method: 'PUT',
        body: JSON.stringify({
          trangThai,
          ghiChu: ghiChu || null
        })
      }),
      
    cancelMyOrder: (id, ghiChu) => request(`/hoa-don/${id}/cancel`, {
      method: 'PUT',
      body: JSON.stringify({ ghiChu: ghiChu || null })
    }),
    requestGuestCancelOtp: (id, maHoaDon, soDienThoai) => request(`/hoa-don/${id}/cancel-guest/request-otp`, {
      method: 'POST',
      body: JSON.stringify({ maHoaDon, soDienThoai })
    }),
    cancelGuestOrder: (id, maHoaDon, soDienThoai, otp, ghiChu) => request(`/hoa-don/${id}/cancel-guest`, {
      method: 'PUT',
      body: JSON.stringify({ maHoaDon, soDienThoai, otp, ghiChu: ghiChu || null })
    }),
    createOnlineReturnRequest: async (data) => {
      const form = new FormData()
      form.append('orderId', data.orderId)
      form.append('orderDetailId', data.orderDetailId)
      form.append('type', data.type)
      form.append('quantity', data.quantity)
      form.append('reason', data.reason)
      form.append('condition', data.condition)
      if (data.refundInfo) form.append('refundInfo', data.refundInfo)
      if (data.replacementVariantId) form.append('replacementVariantId', data.replacementVariantId)
      ;(data.images || []).forEach(file => form.append('images', file))
      const headers = { 'Accept-Language': currentLocale() }
      const token = getToken()
      if (token) headers.Authorization = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/returns/online`, { method: 'POST', headers, body: form })
      if (!res.ok) {
        const err = await res.json().catch(() => ({ error: 'Không thể gửi yêu cầu đổi trả' }))
        throw { status: res.status, ...err }
      }
      return res.json()
    },
    getMyReturnRequests: () => request('/returns/mine'),
    getReturnRequests: (params = {}) => request(`/returns${toQuery(params)}`),
    createOfflineReturnRequest: (data) => request('/returns/offline', {
      method: 'POST', body: JSON.stringify(data)
    }),
    reviewReturnRequest: (id, approved, reason) => request(`/returns/${id}/review`, {
      method: 'PUT', body: JSON.stringify({ approved, reason: reason || null })
    }),
    receiveReturnRequest: (id, accepted, reason) => request(`/returns/${id}/receive`, {
      method: 'PUT', body: JSON.stringify({ accepted, reason: reason || null })
    }),
    completeReturnRequest: (id, note) => request(`/returns/${id}/complete`, {
      method: 'PUT', body: JSON.stringify({ note: note || null })
    }),
    visualSearch: async (file) => {
      const form = new FormData()
      form.append('file', file)
      const headers = { 'Accept-Language': currentLocale() }
      const token = getToken()
      if (token) headers.Authorization = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/ai/visual-search`, { method: 'POST', headers, body: form })
      if (!res.ok) {
        const err = await res.json().catch(() => ({ error: 'Không thể phân tích hình ảnh AI' }))
        throw { status: res.status, ...err }
      }
      return res.json()
    },
    getFrequentlyBoughtTogether: (productId) => request(`/ai/frequently-bought-together/${productId}`),
    // E-Invoice VAT
    issueEInvoice: (orderId) => request(`/orders/${orderId}/issue-e-invoice`, { method: 'POST' }),
    getEInvoice: (orderId) => request(`/orders/${orderId}/e-invoice`),
    lookupEInvoice: (lookupCode) => request(`/e-invoice/lookup/${lookupCode}`),
    // Tracking
    searchOrder: (params) => {
      const query = new URLSearchParams()
      if (params.maHoaDon) query.append('maHoaDon', params.maHoaDon)
      if (params.soDienThoai) query.append('soDienThoai', params.soDienThoai)
      return request(`/hoa-don/search?${query.toString()}`)
    },
    searchOrderByPhone: (soDienThoai) => request(`/hoa-don/search-by-phone?soDienThoai=${soDienThoai}`),
    getMyOrders: () => request('/hoa-don/my-orders'),
    getOrderTracking: (orderId) => request(`/hoa-don/${orderId}/tracking`),
    // Customers
    getKhachHangPage: (params) => request(`/khach-hang/paged${toQuery(params)}`),
    getKhachHangAddresses: (id) => request(`/khach-hang/${id}/addresses`),
    getKhachHangHistory: (id) => request(`/khach-hang/${id}/history`),
    searchKhachHang: (q) => request(`/khach-hang/search?q=${encodeURIComponent(q)}`),
    quickCreateKhachHang: (data) => request('/khach-hang/quick', {
      method: 'POST', body: JSON.stringify(data)
    }),

    // Employees
    getNhanVien: () => request('/nhan-vien'),
    getNhanVienHieuSuat: (id) => request(`/nhan-vien/${id}/hieu-suat`),
    getVaiTroNhanVien: () => request('/nhan-vien/vai-tro'),
    addNhanVien: (data) => request('/nhan-vien', { method: 'POST', body: JSON.stringify(data) }),
    updateNhanVien: (id, data) => request(`/nhan-vien/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    updateNhanVienStatus: (id, tinhTrangLamViec) =>
      request(`/nhan-vien/${id}/trang-thai`, { method: 'PUT', body: JSON.stringify({ tinhTrangLamViec }) }),

    // Work schedule
    getLichLamViec: ({ startDate, endDate, nhanVienId } = {}) => {
      const params = new URLSearchParams()
      if (startDate) params.append('startDate', startDate)
      if (endDate) params.append('endDate', endDate)
      if (nhanVienId) params.append('nhanVienId', nhanVienId)
      const query = params.toString()
      return request(`/lich-lam-viec${query ? '?' + query : ''}`)
    },
    getNhanVienLamViec: () => request('/lich-lam-viec/nhan-vien'),
    getWorkShiftStatus: () => request('/lich-lam-viec/work-status'),
    getShiftHistory: (params = {}) => request(`/lich-lam-viec/history${toQuery(params)}`),
    getShiftReport: (id) => request(`/lich-lam-viec/${id}/report`),
    exportShiftHistory: (params = {}) =>
      downloadFile(`/lich-lam-viec/history/export${toQuery(params)}`, 'lich-su-ca-lam.xlsx'),
    addLichLamViec: (data) => request('/lich-lam-viec', { method: 'POST', body: JSON.stringify(data) }),
    updateLichLamViec: (id, data) => request(`/lich-lam-viec/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteLichLamViec: (id) => request(`/lich-lam-viec/${id}`, { method: 'DELETE' }),
    confirmShift: (id) => request(`/lich-lam-viec/${id}/confirm`, { method: 'POST' }),
    reportShiftUnavailable: (id, lyDo) => request(`/lich-lam-viec/${id}/unavailable`, {
      method: 'POST', body: JSON.stringify({ lyDo })
    }),
    reviewShiftUnavailable: (id, approved, phanHoi) => request(`/lich-lam-viec/${id}/review-unavailable`, {
      method: 'POST', body: JSON.stringify({ approved, phanHoi: phanHoi || null })
    }),
    checkInShift: (id) => request(`/lich-lam-viec/${id}/check-in`, { method: 'POST' }),
    checkOutShift: (id) => request(`/lich-lam-viec/${id}/check-out`, { method: 'POST' }),

    // Human support handoff
    requestHumanSupport: (message) => request('/support-chat/customer/request', {
      method: 'POST', body: JSON.stringify({ message })
    }),
    getCustomerSupportChat: (token) => request(`/support-chat/customer/${encodeURIComponent(token)}`),
    sendCustomerSupportMessage: (token, message) => request(`/support-chat/customer/${encodeURIComponent(token)}/messages`, {
      method: 'POST', body: JSON.stringify({ message })
    }),
    getStaffSupportChats: () => request('/support-chat/staff/conversations'),
    getStaffSupportChat: (id) => request(`/support-chat/staff/conversations/${id}`),
    claimStaffSupportChat: (id) => request(`/support-chat/staff/conversations/${id}/claim`, { method: 'POST' }),
    sendStaffSupportMessage: (id, message) => request(`/support-chat/staff/conversations/${id}/messages`, {
      method: 'POST', body: JSON.stringify({ message })
    }),
    closeStaffSupportChat: (id) => request(`/support-chat/staff/conversations/${id}/close`, { method: 'POST' }),
    getStaffTasks: () => request('/staff/tasks'),
    getStaffDashboard: () => request('/staff/dashboard'),

    // Dashboard
    getDashboardStats: () => request('/dashboard/stats'),

    // Thống kê (báo cáo admin)
    getThongKeTongHop: ({ startDate, endDate, timeType = 'ngay' }) => {
      const params = new URLSearchParams()
      if (startDate) params.append('startDate', startDate)
      if (endDate) params.append('endDate', endDate)
      if (timeType) params.append('timeType', timeType)
      return request(`/admin/thong-ke/tong-hop?${params.toString()}`)
    },

    // Attributes
    getThuocTinh: () => request('/thuoc-tinh'),

    // Attributes CRUD
    addMauSac: (data) => request('/thuoc-tinh/mau-sac', { method: 'POST', body: JSON.stringify(data) }),
    deleteMauSac: (id) => request(`/thuoc-tinh/mau-sac/${id}`, { method: 'DELETE' }),

    addKichThuoc: (data) => request('/thuoc-tinh/kich-thuoc', { method: 'POST', body: JSON.stringify(data) }),
    deleteKichThuoc: (id) => request(`/thuoc-tinh/kich-thuoc/${id}`, { method: 'DELETE' }),

    addChatLieu: (data) => request('/thuoc-tinh/chat-lieu', { method: 'POST', body: JSON.stringify(data) }),
    deleteChatLieu: (id) => request(`/thuoc-tinh/chat-lieu/${id}`, { method: 'DELETE' }),

    addLoaiSanPham: (data) => request('/thuoc-tinh/loai-san-pham', { method: 'POST', body: JSON.stringify(data) }),
    addLoaiVay: (data) => request('/thuoc-tinh/loai-san-pham', { method: 'POST', body: JSON.stringify(data) }),
    deleteLoaiSanPham: (id) => request(`/thuoc-tinh/loai-san-pham/${id}`, { method: 'DELETE' }),
    deleteLoaiVay: (id) => request(`/thuoc-tinh/loai-san-pham/${id}`, { method: 'DELETE' }),

    addNhaCungCap: (data) => request('/thuoc-tinh/nha-cung-cap', { method: 'POST', body: JSON.stringify(data) }),
    deleteNhaCungCap: (id) => request(`/thuoc-tinh/nha-cung-cap/${id}`, { method: 'DELETE' }),

    // Payment
    createOrder: (data) =>
      request('/payment/create-order', { method: 'POST', body: JSON.stringify(data) }),
    createMomoPayment: (orderId, maHoaDon, soDienThoai) =>
      request('/payment/momo/create', { method: 'POST', body: JSON.stringify({ orderId, maHoaDon, soDienThoai }) }),
    createZaloPayment: (orderId, maHoaDon, soDienThoai) =>
      request('/payment/zalopay/create', { method: 'POST', body: JSON.stringify({ orderId, maHoaDon, soDienThoai }) }),
    momoQr: (amount) =>
      request('/payment/momo/qr', { method: 'POST', body: JSON.stringify({ amount }) }),
    zaloQr: (amount) =>
      request('/payment/zalopay/qr', { method: 'POST', body: JSON.stringify({ amount }) }),
    applyVoucher: (maGiamGia, tongTien) =>
      request('/payment/apply-voucher', { method: 'POST', body: JSON.stringify({ maGiamGia, tongTien }) }),
    getBestVoucher: (tongTien) =>
      request('/payment/best-voucher', { method: 'POST', body: JSON.stringify({ tongTien }) }),
    getPosReservation: (token) => request(`/pos-reservations/${encodeURIComponent(token)}`),
    setPosReservationItem: (token, variantId, quantity) =>
      request('/pos-reservations/items', {
        method: 'POST',
        body: JSON.stringify({ token: token || null, variantId, quantity })
      }),
    setPosReservationVoucher: (token, maGiamGia) =>
      request(`/pos-reservations/${encodeURIComponent(token)}/voucher`, {
        method: 'PUT',
        body: JSON.stringify({ maGiamGia: maGiamGia || null })
      }),
    reserveBestPosVoucher: (token) =>
      request(`/pos-reservations/${encodeURIComponent(token)}/voucher/best`, { method: 'POST' }),
    clearPosReservationVoucher: (token) =>
      request(`/pos-reservations/${encodeURIComponent(token)}/voucher`, { method: 'DELETE' }),
    releasePosReservation: (token) =>
      request(`/pos-reservations/${encodeURIComponent(token)}`, { method: 'DELETE' }),

    // Storefront and verified customer content
    getStorefrontSummary: () => request('/storefront/summary'),
    getStorePolicies: () => request('/storefront/policies'),
    getStoreReviews: (page = 0, size = 9, stars = '') =>
      request(`/reviews${toQuery({ page, size, stars })}`),
    getProductReviews: (productId, page = 0, size = 6) =>
      request(`/reviews/product/${productId}?page=${page}&size=${size}`),
    getReviewEligibility: (productId) => request(`/reviews/product/${productId}/eligibility`),
    createProductReview: async (productId, { orderId, stars, content, images = [] }) => {
      const form = new FormData()
      form.append('orderId', orderId)
      form.append('stars', stars)
      form.append('content', content)
      images.forEach(image => form.append('images', image))
      const headers = { 'Accept-Language': currentLocale() }
      const token = getToken()
      if (token) headers.Authorization = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/reviews/product/${productId}`, { method: 'POST', headers, body: form })
      if (!res.ok) {
        const err = await res.json().catch(() => ({ error: 'Không thể gửi đánh giá' }))
        throw { status: res.status, ...err }
      }
      return res.json()
    },

    // Account-synchronized cart, wishlist and recently viewed products
    getCustomerData: () => request('/customer-data'),
    replaceCustomerCart: (items) => request('/customer-data/cart', {
      method: 'PUT',
      body: JSON.stringify({ items })
    }),
    addCustomerWishlist: (productId) => request(`/customer-data/wishlist/${productId}`, { method: 'POST' }),
    removeCustomerWishlist: (productId) => request(`/customer-data/wishlist/${productId}`, { method: 'DELETE' }),
    recordCustomerView: (productId) => request(`/customer-data/recent/${productId}`, { method: 'POST' }),
    
    updateProfile: (data) =>
      request('/auth/profile', { method: 'PUT', body: JSON.stringify(data) }),

    getVouchers: () => request('/voucher'),

    // Voucher CRUD
    addVoucher: (data) => request('/voucher', { method: 'POST', body: JSON.stringify(data) }),
    updateVoucher: (id, data) => request(`/voucher/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteVoucher: (id) => request(`/voucher/${id}`, { method: 'DELETE' }),

    // Promotion campaigns (product price remains a single base selling price)
    getPromotions: () => request('/promotions'),
    createPromotion: (data) => request('/promotions', { method: 'POST', body: JSON.stringify(data) }),
    updatePromotion: (id, data) => request(`/promotions/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deletePromotion: (id) => request(`/promotions/${id}`, { method: 'DELETE' }),

    getProfileAddresses: () => request('/auth/profile/addresses'),
    createProfileAddress: (data) => request('/auth/profile/addresses', {
      method: 'POST', body: JSON.stringify(data)
    }),
    updateProfileAddressById: (id, data) => request(`/auth/profile/addresses/${id}`, {
      method: 'PUT', body: JSON.stringify(data)
    }),
    setDefaultProfileAddress: (id) => request(`/auth/profile/addresses/${id}/default`, { method: 'PUT' }),
    deleteProfileAddress: (id) => request(`/auth/profile/addresses/${id}`, { method: 'DELETE' }),
    getThongBao: () => request('/thong-bao'),
    getThongBaoActive: () => request('/thong-bao/active'),
    getCustomerNotifications: () => request('/customer-notifications'),
    markCustomerNotificationRead: (id) => request(`/customer-notifications/${id}/read`, { method: 'PUT' }),
    markAllCustomerNotificationsRead: () => request('/customer-notifications/read-all', { method: 'PUT' }),
    addThongBao: (data) => request('/thong-bao', { method: 'POST', body: JSON.stringify(data) }),
    updateThongBao: (id, data) => request(`/thong-bao/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteThongBao: (id) => request(`/thong-bao/${id}`, { method: 'DELETE' }),
    postAiChat: (message, history = [], mode = null) =>
      request('/ai-chat', { method: 'POST', body: JSON.stringify({ message, history, ...(mode ? { mode } : {}) }) })
  }
}

export function useAuth() {
  function saveLogin(data) {
    localStorage.setItem('zestia_token', data.token)
    localStorage.setItem('zestia_user', JSON.stringify(data))
    window.dispatchEvent(new Event('zestia-auth-changed'))
  }

  function getUser() {
    const raw = localStorage.getItem('zestia_user')
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  }

  function isLoggedIn() {
    const token = localStorage.getItem('zestia_token')
    if (tokenIsCurrent(token)) return true
    if (token) {
      clearAuthStorage()
      window.dispatchEvent(new Event('zestia-auth-changed'))
    }
    return false
  }

  function logout() {
    clearAuthStorage()
    window.dispatchEvent(new Event('zestia-auth-changed'))
  }

  return { saveLogin, getUser, isLoggedIn, logout }
}
