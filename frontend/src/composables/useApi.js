const BASE = 'http://localhost:8080/api'

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
  const res = await fetch(`${BASE}${path}`, {
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
    uploadVayAnh: async (id, file) => {
      const fd = new FormData()
      fd.append('file', file)
      const headers = {}
      const token = getToken()
      if (token) headers['Authorization'] = `Bearer ${token}`
      const res = await fetch(`${BASE}/vay/${id}/anh`, { method: 'POST', headers, body: fd })
      if (!res.ok) throw await res.json().catch(() => ({ error: 'Upload thất bại' }))
      return res.json()
    },
    deleteVayAnh: (anhId) => request(`/vay/anh/${anhId}`, { method: 'DELETE' }),

    // Orders
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

    // Customers
    getKhachHang: () => request('/khach-hang'),

    // Dashboard
    getDashboardStats: () => request('/dashboard/stats'),

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
    getMyOrders: () => request('/payment/my-orders'),
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
