import { ref } from 'vue'
import { api } from './useApi'

const letters = ['Z', 'e', 's', 't', 'i', 'a']
const bgs = [
  'linear-gradient(160deg,#F3E8E6,#D4A99E)',
  'linear-gradient(160deg,#E8DDD6,#C4A98E)',
  'linear-gradient(160deg,#E6E0DA,#A8A49E)',
  'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
  'linear-gradient(160deg,#E4DDD2,#C0B49E)',
  'linear-gradient(160deg,#F5EDE3,#E8CFC9)',
]

export function mapProduct(p, i) {
  const hasDiscount = p.giaBanGoc && Number(p.giaBanGoc) > Number(p.giaBan)
  return {
    id: p.id,
    code: p.maVay,
    name: p.tenVay,
    category: p.loaiVay || '',
    price: hasDiscount ? Number(p.giaBanGoc) : Number(p.giaBan),
    salePrice: hasDiscount ? Number(p.giaBan) : null,
    badge: hasDiscount ? 'Sale' : null,
    stock: p.tonKho || 0,
    active: p.trangThai === 1 || p.trangThai === true,
    image: p.anhUrl || null,
    images: p.danhSachAnh || [],
    letter: letters[i % letters.length],
    bg: bgs[i % bgs.length],
  }
}

export function fmtPrice(n) {
  if (!n && n !== 0) return '0đ'
  return Number(n).toLocaleString('vi-VN') + 'đ'
}

const _products = ref([])
let _loading = false

export async function loadProducts(force = false) {
  if ((!force && _products.value.length) || _loading) return
  _loading = true
  try {
    const data = await api().getVay()
    _products.value = data.map(mapProduct)
  } catch (e) {
    console.error('Failed to load products:', e)
  } finally {
    _loading = false
  }
}

export const products = _products
