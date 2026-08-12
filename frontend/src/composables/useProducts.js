import { ref } from 'vue'
import { api } from './useApi'

const letters = ['Z', 'e', 's', 't', 'i', 'a']
const backgrounds = [
  'linear-gradient(160deg,#F3E8E6,#D4A99E)',
  'linear-gradient(160deg,#E8DDD6,#C4A98E)',
  'linear-gradient(160deg,#E6E0DA,#A8A49E)',
  'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
  'linear-gradient(160deg,#E4DDD2,#C0B49E)',
  'linear-gradient(160deg,#F5EDE3,#E8CFC9)',
]

export function mapProduct(product, index = 0) {
  return {
    id: product.id,
    code: product.maSanPham,
    name: product.tenSanPham,
    category: product.loaiSanPham || '',
    material: product.chatLieu || '',
    fit: product.moTaPhom || '',
    price: Number(product.giaBan || 0),
    basePrice: Number(product.giaBanCoSo || product.giaBan || 0),
    promotionActive: Boolean(product.coKhuyenMai),
    badge: product.coKhuyenMai ? 'Ưu đãi' : null,
    campaign: product.dotKhuyenMai || null,
    stock: Number(product.tonKho || 0),
    active: product.trangThai === 1 || product.trangThai === true,
    image: product.anhUrl || null,
    images: Array.isArray(product.danhSachAnh) ? product.danhSachAnh : [],
    rating: Number(product.diemDanhGia || 0),
    reviewCount: Number(product.soDanhGia || 0),
    letter: letters[index % letters.length],
    bg: backgrounds[index % backgrounds.length],
  }
}

export function fmtPrice(value) {
  const amount = Number(value)
  return `${Number.isFinite(amount) ? amount.toLocaleString('vi-VN') : '0'}đ`
}

const _products = ref([])
const _error = ref(null)
let loadingPromise = null
let loaded = false

export async function loadProducts(force = false) {
  if (!force && loaded) return _products.value
  if (loadingPromise) return loadingPromise

  loadingPromise = (async () => {
    try {
      const data = await api().getSanPham()
      _products.value = Array.isArray(data)
        ? data
            .filter(product => product.trangThai === 1 || product.trangThai === true)
            .map(mapProduct)
        : []
      _error.value = null
      loaded = true
      return _products.value
    } catch (error) {
      _products.value = []
      _error.value = error
      loaded = false
      throw error
    } finally {
      loadingPromise = null
    }
  })()

  return loadingPromise
}

export const products = _products
export const productsLoadError = _error
