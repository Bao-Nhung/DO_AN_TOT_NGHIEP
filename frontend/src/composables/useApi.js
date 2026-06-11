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
  return res.json()
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

    // Products
    getVay: () => request('/vay'),
    getVayById: (id) => request(`/vay/${id}`),
    searchVay: (q) => request(`/vay/search?q=${encodeURIComponent(q)}`),

    // Orders
    getHoaDon: () => request('/hoa-don'),
    getHoaDonById: (id) => request(`/hoa-don/${id}`),
    updateOrderStatus: (id, trangThai) =>
      request(`/hoa-don/${id}/trang-thai`, {
        method: 'PUT',
        body: JSON.stringify({ trangThai })
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

    // Promotions
    getKhuyenMai: () => request('/khuyen-mai'),
    getGiamGia: () => request('/khuyen-mai/giam-gia'),
    getAllKhuyenMai: () => request('/khuyen-mai/all'),
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
