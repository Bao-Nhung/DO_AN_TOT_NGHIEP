export const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '')

function getToken() {
  return localStorage.getItem('zestia_token')
}

function getHeaders() {
  const h = { 'Content-Type': 'application/json' }
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
    throw { status: res.status, ...err }
  }
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export function api() {
  return {
    // Auth
    login: (username, password) =>
      request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      }),
    me: () => request('/auth/me'),
    register: (data) =>
      request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
    forgotPassword: (identifier) =>
      request('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ identifier }) }),

    // Products
    getVay: () => request('/vay'),
    getVayById: (id) => request(`/vay/${id}`),
    searchVay: (q) => request(`/vay/search?q=${encodeURIComponent(q)}`),
    createVay: (data) => request('/vay', { method: 'POST', body: JSON.stringify(data) }),
    updateVay: (id, data) => request(`/vay/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteVay: (id) => request(`/vay/${id}`, { method: 'DELETE' }),
    seedVay: () => request('/vay/seed', { method: 'POST' }),
    uploadVayAnh: async (id, file) => {
      const fd = new FormData()
      fd.append('file', file)
      const headers = {}
      const token = getToken()
      if (token) headers['Authorization'] = `Bearer ${token}`
      const res = await fetch(`${API_BASE}/vay/${id}/anh`, { method: 'POST', headers, body: fd })
      if (!res.ok) throw await res.json().catch(() => ({ error: 'Upload thất bại' }))
      return res.json()
    },
    deleteVayAnh: (anhId) => request(`/vay/anh/${anhId}`, { method: 'DELETE' }),

    // Orders & Tracking
    getHoaDon: () => request('/hoa-don'),
    getHoaDonById: (id) => request(`/hoa-don/${id}`),
    updateOrderStatus: (id, trangThai, ghiChu, daThanhToan) =>
      request(`/hoa-don/${id}/trang-thai`, {
        method: 'PUT',
        body: JSON.stringify({
          trangThai,
          ghiChu: ghiChu || null,
          ...(daThanhToan !== undefined ? { daThanhToan } : {})
        })
      }),
      
    // --- BỔ SUNG HÀM NÀY CHO KHÁCH HÀNG TỰ HỦY ĐƠN ---
    cancelMyOrder: (id, ghiChu) => request(`/hoa-don/${id}/cancel`, {
      method: 'PUT',
      body: JSON.stringify({ ghiChu: ghiChu || null })
    }),
    cancelGuestOrder: (id, maHoaDon, soDienThoai, ghiChu) => request(`/hoa-don/${id}/cancel-guest`, {
      method: 'PUT',
      body: JSON.stringify({ maHoaDon, soDienThoai, ghiChu: ghiChu || null })
    }),
      
    // ====== API MỚI CHO TRACKING ======
    searchOrder: (params) => {
      const query = new URLSearchParams()
      if (params.maHoaDon) query.append('maHoaDon', params.maHoaDon)
      if (params.soDienThoai) query.append('soDienThoai', params.soDienThoai)
      return request(`/hoa-don/search?${query.toString()}`)
    },
    searchOrderByPhone: (soDienThoai) => request(`/hoa-don/search-by-phone?soDienThoai=${soDienThoai}`),
    getMyOrders: () => request('/hoa-don/my-orders'),
    getOrderTracking: (orderId) => request(`/hoa-don/${orderId}/tracking`),
    // ==================================

    // Customers
    getKhachHang: () => request('/khach-hang'),

    // Employees
    getNhanVien: () => request('/nhan-vien'),
    getNhanVienHieuSuat: (id) => request(`/nhan-vien/${id}/hieu-suat`),
    getVaiTroNhanVien: () => request('/nhan-vien/vai-tro'),
    addNhanVien: (data) => request('/nhan-vien', { method: 'POST', body: JSON.stringify(data) }),
    updateNhanVien: (id, data) => request(`/nhan-vien/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    updateNhanVienStatus: (id, tinhTrangLamViec) =>
      request(`/nhan-vien/${id}/trang-thai`, { method: 'PUT', body: JSON.stringify({ tinhTrangLamViec }) }),
    deleteNhanVien: (id) => request(`/nhan-vien/${id}`, { method: 'DELETE' }),

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
    addLichLamViec: (data) => request('/lich-lam-viec', { method: 'POST', body: JSON.stringify(data) }),
    updateLichLamViec: (id, data) => request(`/lich-lam-viec/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteLichLamViec: (id) => request(`/lich-lam-viec/${id}`, { method: 'DELETE' }),

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
    getMauSac: () => request('/thuoc-tinh/mau-sac'),
    getKichThuoc: () => request('/thuoc-tinh/kich-thuoc'),
    getChatLieu: () => request('/thuoc-tinh/chat-lieu'),
    getLoaiVay: () => request('/thuoc-tinh/loai-vay'),
    getNhaCungCap: () => request('/thuoc-tinh/nha-cung-cap'),

    // Attributes CRUD
    addMauSac: (data) => request('/thuoc-tinh/mau-sac', { method: 'POST', body: JSON.stringify(data) }),
    updateMauSac: (id, data) => request(`/thuoc-tinh/mau-sac/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteMauSac: (id) => request(`/thuoc-tinh/mau-sac/${id}`, { method: 'DELETE' }),

    addKichThuoc: (data) => request('/thuoc-tinh/kich-thuoc', { method: 'POST', body: JSON.stringify(data) }),
    updateKichThuoc: (id, data) => request(`/thuoc-tinh/kich-thuoc/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteKichThuoc: (id) => request(`/thuoc-tinh/kich-thuoc/${id}`, { method: 'DELETE' }),

    addChatLieu: (data) => request('/thuoc-tinh/chat-lieu', { method: 'POST', body: JSON.stringify(data) }),
    updateChatLieu: (id, data) => request(`/thuoc-tinh/chat-lieu/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteChatLieu: (id) => request(`/thuoc-tinh/chat-lieu/${id}`, { method: 'DELETE' }),

    addLoaiVay: (data) => request('/thuoc-tinh/loai-vay', { method: 'POST', body: JSON.stringify(data) }),
    updateLoaiVay: (id, data) => request(`/thuoc-tinh/loai-vay/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteLoaiVay: (id) => request(`/thuoc-tinh/loai-vay/${id}`, { method: 'DELETE' }),

    addNhaCungCap: (data) => request('/thuoc-tinh/nha-cung-cap', { method: 'POST', body: JSON.stringify(data) }),
    updateNhaCungCap: (id, data) => request(`/thuoc-tinh/nha-cung-cap/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteNhaCungCap: (id) => request(`/thuoc-tinh/nha-cung-cap/${id}`, { method: 'DELETE' }),

    // Payment
    createOrder: (data) =>
      request('/payment/create-order', { method: 'POST', body: JSON.stringify(data) }),
    createVNPayUrl: (orderId) =>
      request('/payment/vnpay/create', { method: 'POST', body: JSON.stringify({ orderId }) }),
    confirmPayment: (orderId, method, maHoaDon) =>
      request('/payment/confirm', { method: 'POST', body: JSON.stringify({ orderId, method, maHoaDon }) }),
    createMomoPayment: (orderId) =>
      request('/payment/momo/create', { method: 'POST', body: JSON.stringify({ orderId }) }),
    createZaloPayment: (orderId) =>
      request('/payment/zalopay/create', { method: 'POST', body: JSON.stringify({ orderId }) }),
    momoQr: (amount) =>
      request('/payment/momo/qr', { method: 'POST', body: JSON.stringify({ amount }) }),
    zaloQr: (amount) =>
      request('/payment/zalopay/qr', { method: 'POST', body: JSON.stringify({ amount }) }),
    applyVoucher: (maGiamGia, tongTien) =>
      request('/payment/apply-voucher', { method: 'POST', body: JSON.stringify({ maGiamGia, tongTien }) }),
    getOrder: (id) => request(`/payment/order/${id}`),
    
    updateProfile: (data) =>
      request('/auth/profile', { method: 'PUT', body: JSON.stringify(data) }),

    // Promotions
    getKhuyenMai: () => request('/khuyen-mai'),
    getGiamGia: () => request('/khuyen-mai/giam-gia'),
    getAllKhuyenMai: () => request('/khuyen-mai/all'),

    // Voucher CRUD
    addGiamGia: (data) => request('/khuyen-mai/giam-gia', { method: 'POST', body: JSON.stringify(data) }),
    updateGiamGia: (id, data) => request(`/khuyen-mai/giam-gia/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteGiamGia: (id) => request(`/khuyen-mai/giam-gia/${id}`, { method: 'DELETE' }),

    // Promotion CRUD
    addKhuyenMai: (data) => request('/khuyen-mai', { method: 'POST', body: JSON.stringify(data) }),
    updateKhuyenMai: (id, data) => request(`/khuyen-mai/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteKhuyenMai: (id) => request(`/khuyen-mai/${id}`, { method: 'DELETE' }),

    getProfileAddress: () => request('/auth/profile/address'),
    updateProfileAddress: (data) =>
      request('/auth/profile/address', { method: 'PUT', body: JSON.stringify(data) }),
    updateOrderCustomer: (id, data) =>
      request(`/hoa-don/${id}/khach-hang`, { method: 'PUT', body: JSON.stringify(data) }),
    getThongBao: () => request('/thong-bao'),
    getThongBaoActive: () => request('/thong-bao/active'),
    addThongBao: (data) => request('/thong-bao', { method: 'POST', body: JSON.stringify(data) }),
    updateThongBao: (id, data) => request(`/thong-bao/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteThongBao: (id) => request(`/thong-bao/${id}`, { method: 'DELETE' })
  }
}

export function useAuth() {
  function saveLogin(data) {
    localStorage.setItem('zestia_token', data.token)
    localStorage.setItem('zestia_user', JSON.stringify(data))
  }

  function getUser() {
    const raw = localStorage.getItem('zestia_user')
    return raw ? JSON.parse(raw) : null
  }

  function isLoggedIn() {
    return !!localStorage.getItem('zestia_token')
  }

  function logout() {
    localStorage.removeItem('zestia_token')
    localStorage.removeItem('zestia_user')
  }

  return { saveLogin, getUser, isLoggedIn, logout }
}
