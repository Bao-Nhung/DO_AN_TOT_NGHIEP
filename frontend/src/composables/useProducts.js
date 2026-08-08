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

export const MOCK_PRODUCTS = [
  {
    id: 1,
    code: 'ASM001',
    name: 'Áo Sơ Mi Lụa Cổ Điển',
    category: 'Áo thời trang',
    material: 'Lụa tơ tằm',
    fit: 'Dáng vừa vặn, thanh lịch',
    price: 2890000,
    promotionActive: false,
    badge: null,
    stock: 25,
    active: true,
    image: '/images/products/shirt1.jpg',
    images: ['/images/products/shirt1.jpg', '/images/products/shirt2.jpg', '/images/products/shirt3.jpg'],
    rating: 4.8,
    reviewCount: 12,
    letter: 'Z',
    bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)',
    bienThe: [
      { id: 101, mauSac: 'Trắng Ngà', maHex: '#FFF8F0', kichThuoc: 'S', soLuong: 10, giaBan: 2890000, trangThai: 1, anhUrl: '/images/products/shirt1.jpg' },
      { id: 102, mauSac: 'Trắng Ngà', maHex: '#FFF8F0', kichThuoc: 'M', soLuong: 15, giaBan: 2890000, trangThai: 1, anhUrl: '/images/products/shirt1.jpg' },
      { id: 103, mauSac: 'Hồng Nude', maHex: '#E8CFC9', kichThuoc: 'M', soLuong: 12, giaBan: 2890000, trangThai: 1, anhUrl: '/images/products/shirt2.jpg' }
    ]
  },
  {
    id: 2,
    code: 'QJN001',
    name: 'Quần Jeans Wide Leg Thời Trang',
    category: 'Quần & Jeans',
    material: 'Denim cao cấp',
    fit: 'Ống rộng tôn dáng',
    price: 1590000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 45,
    active: true,
    image: '/images/products/pants1.jpg',
    images: ['/images/products/pants1.jpg', '/images/products/pants2.jpg', '/images/products/pants3.jpg'],
    rating: 4.9,
    reviewCount: 28,
    letter: 'e',
    bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)',
    bienThe: [
      { id: 201, mauSac: 'Xanh Vintage', maHex: '#4A6B82', kichThuoc: 'S', soLuong: 20, giaBan: 1590000, trangThai: 1, anhUrl: '/images/products/pants1.jpg' },
      { id: 202, mauSac: 'Xanh Vintage', maHex: '#4A6B82', kichThuoc: 'M', soLuong: 25, giaBan: 1590000, trangThai: 1, anhUrl: '/images/products/pants1.jpg' }
    ]
  },
  {
    id: 3,
    code: 'VDH001',
    name: 'Váy Dạ Hội Gấm Hoàng Gia',
    category: 'Váy & Đầm',
    material: 'Gấm thêu tay',
    fit: 'Dáng xòe công chúa',
    price: 4290000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 15,
    active: true,
    image: '/images/products/dress1.jpg',
    images: ['/images/products/dress1.jpg', '/images/products/dress2.jpg', '/images/products/dress3.jpg'],
    rating: 5.0,
    reviewCount: 34,
    letter: 's',
    bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)',
    bienThe: [
      { id: 301, mauSac: 'Đỏ Đô', maHex: '#800020', kichThuoc: 'S', soLuong: 5, giaBan: 4290000, trangThai: 1, anhUrl: '/images/products/dress1.jpg' },
      { id: 302, mauSac: 'Đỏ Đô', maHex: '#800020', kichThuoc: 'M', soLuong: 10, giaBan: 4290000, trangThai: 1, anhUrl: '/images/products/dress1.jpg' }
    ]
  },
  {
    id: 4,
    code: 'PKT001',
    name: 'Túi Xách Da Nữ Zestia Premium',
    category: 'Phụ kiện thời trang',
    material: 'Da thật cao cấp',
    fit: 'Thiết kế tối giản sang trọng',
    price: 1290000,
    promotionActive: false,
    badge: null,
    stock: 30,
    active: true,
    image: '/images/products/accessories1.jpg',
    images: ['/images/products/accessories1.jpg', '/images/products/accessories2.jpg', '/images/products/accessories3.jpg'],
    rating: 4.7,
    reviewCount: 19,
    letter: 't',
    bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
    bienThe: [
      { id: 401, mauSac: 'Đen Tuyền', maHex: '#1A1A1A', kichThuoc: 'Freesize', soLuong: 15, giaBan: 1290000, trangThai: 1, anhUrl: '/images/products/accessories1.jpg' },
      { id: 402, mauSac: 'Nâu Kem', maHex: '#C4A98E', kichThuoc: 'Freesize', soLuong: 15, giaBan: 1290000, trangThai: 1, anhUrl: '/images/products/accessories2.jpg' }
    ]
  },
  {
    id: 5,
    code: 'TCS001',
    name: 'Set Áo Blazer & Quần Tây Công Sở',
    category: 'Trang phục công sở',
    material: 'Đũi & Cotton',
    fit: 'Phom suông hiện đại',
    price: 1390000,
    promotionActive: false,
    badge: null,
    stock: 20,
    active: true,
    image: '/images/products/shirt14.jpg',
    images: ['/images/products/shirt14.jpg', '/images/products/pants8.jpg', '/images/products/shirt15.jpg'],
    rating: 4.8,
    reviewCount: 15,
    letter: 'i',
    bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)',
    bienThe: [
      { id: 501, mauSac: 'Xám Be', maHex: '#B0A8A0', kichThuoc: 'S', soLuong: 10, giaBan: 1390000, trangThai: 1, anhUrl: '/images/products/shirt14.jpg' },
      { id: 502, mauSac: 'Xám Be', maHex: '#B0A8A0', kichThuoc: 'M', soLuong: 10, giaBan: 1390000, trangThai: 1, anhUrl: '/images/products/shirt14.jpg' }
    ]
  },
  {
    id: 6,
    code: 'DTP001',
    name: 'Đầm Dự Tiệc Lụa Trắng Tinh Khôi',
    category: 'Trang phục dự tiệc',
    material: 'Lụa tơ tằm',
    fit: 'Cổ V quyến rũ',
    price: 8990000,
    promotionActive: false,
    badge: null,
    stock: 5,
    active: true,
    image: '/images/products/dress15.jpg',
    images: ['/images/products/dress15.jpg', '/images/products/dress16.jpg', '/images/products/dress17.jpg'],
    rating: 5.0,
    reviewCount: 42,
    letter: 'a',
    bg: 'linear-gradient(160deg,#F5EDE3,#E8CFC9)',
    bienThe: [
      { id: 601, mauSac: 'Trắng Tinh', maHex: '#FFFFFF', kichThuoc: 'S', soLuong: 2, giaBan: 8990000, trangThai: 1, anhUrl: '/images/products/dress15.jpg' },
      { id: 602, mauSac: 'Trắng Tinh', maHex: '#FFFFFF', kichThuoc: 'M', soLuong: 3, giaBan: 8990000, trangThai: 1, anhUrl: '/images/products/dress15.jpg' }
    ]
  },
  {
    id: 9,
    code: 'ASM002',
    name: 'Áo Kiểu Lụa Tơ Tằm Hoàng Gia',
    category: 'Áo thời trang',
    material: 'Lụa thêu hoa',
    fit: 'Dáng xòe nhẹ',
    price: 3890000,
    promotionActive: false,
    badge: null,
    stock: 21,
    active: true,
    image: '/images/products/shirt2.jpg',
    images: ['/images/products/shirt2.jpg', '/images/products/shirt4.jpg'],
    rating: 4.6,
    reviewCount: 8,
    letter: 'Z',
    bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)',
    bienThe: [
      { id: 901, mauSac: 'Vàng Hoàng Gia', maHex: '#FFD700', kichThuoc: 'M', soLuong: 21, giaBan: 3890000, trangThai: 1, anhUrl: '/images/products/shirt2.jpg' }
    ]
  },
  {
    id: 10,
    code: 'ASM003',
    name: 'Áo Kiểu Voan Cách Điệu Nữ Tính',
    category: 'Áo thời trang',
    material: 'Voan xếp nếp',
    fit: 'Dáng nữ tính',
    price: 1890000,
    promotionActive: false,
    badge: null,
    stock: 15,
    active: true,
    image: '/images/products/shirt3.jpg',
    images: ['/images/products/shirt3.jpg', '/images/products/shirt5.jpg'],
    rating: 4.7,
    reviewCount: 14,
    letter: 'e',
    bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)',
    bienThe: [
      { id: 1001, mauSac: 'Hồng Pastel', maHex: '#FFC0CB', kichThuoc: 'S', soLuong: 15, giaBan: 1890000, trangThai: 1, anhUrl: '/images/products/shirt3.jpg' }
    ]
  },
  {
    id: 11,
    code: 'AKH001',
    name: 'Áo Khoác Blazer Gấm Đỏ Nổi Bật',
    category: 'Áo khoác & Blazer',
    material: 'Gấm thêu',
    fit: 'Form rộng cá tính',
    price: 2490000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 18,
    active: true,
    image: '/images/products/shirt5.jpg',
    images: ['/images/products/shirt5.jpg', '/images/products/shirt6.jpg'],
    rating: 4.9,
    reviewCount: 22,
    letter: 'e',
    bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)',
    bienThe: [
      { id: 1101, mauSac: 'Đỏ Nổi Bật', maHex: '#D32F2F', kichThuoc: 'L', soLuong: 18, giaBan: 2490000, trangThai: 1, anhUrl: '/images/products/shirt5.jpg' }
    ]
  },
  {
    id: 12,
    code: 'ASM005',
    name: 'Áo Thun Cotton Form Rộng Premium',
    category: 'Áo thời trang',
    material: 'Cotton 100%',
    fit: 'Form rộng mát mẻ',
    price: 690000,
    promotionActive: false,
    badge: null,
    stock: 50,
    active: true,
    image: '/images/products/shirt4.jpg',
    images: ['/images/products/shirt4.jpg', '/images/products/shirt7.jpg'],
    rating: 4.8,
    reviewCount: 36,
    letter: 's',
    bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)',
    bienThe: [
      { id: 1201, mauSac: 'Trắng', maHex: '#FFFFFF', kichThuoc: 'M', soLuong: 25, giaBan: 690000, trangThai: 1, anhUrl: '/images/products/shirt4.jpg' },
      { id: 1202, mauSac: 'Đen', maHex: '#000000', kichThuoc: 'L', soLuong: 25, giaBan: 690000, trangThai: 1, anhUrl: '/images/products/shirt7.jpg' }
    ]
  },
  {
    id: 13,
    code: 'QTY001',
    name: 'Quần Tây Ôm Dáng Công Sở',
    category: 'Quần & Jeans',
    material: 'Tuyết mưa cao cấp',
    fit: 'Ống đứng thanh lịch',
    price: 990000,
    promotionActive: false,
    badge: null,
    stock: 35,
    active: true,
    image: '/images/products/pants2.jpg',
    images: ['/images/products/pants2.jpg', '/images/products/pants4.jpg'],
    rating: 4.7,
    reviewCount: 14,
    letter: 's',
    bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)',
    bienThe: [
      { id: 1301, mauSac: 'Đen', maHex: '#000000', kichThuoc: 'S', soLuong: 15, giaBan: 990000, trangThai: 1, anhUrl: '/images/products/pants2.jpg' },
      { id: 1302, mauSac: 'Đen', maHex: '#000000', kichThuoc: 'M', soLuong: 20, giaBan: 990000, trangThai: 1, anhUrl: '/images/products/pants2.jpg' }
    ]
  },
  {
    id: 14,
    code: 'QTY002',
    name: 'Quần Culottes Lụa Xòe Nhẹ',
    category: 'Quần & Jeans',
    material: 'Lụa cao cấp',
    fit: 'Dáng xòe thanh thoát',
    price: 1250000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 22,
    active: true,
    image: '/images/products/pants3.jpg',
    images: ['/images/products/pants3.jpg', '/images/products/pants5.jpg'],
    rating: 4.8,
    reviewCount: 18,
    letter: 't',
    bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
    bienThe: [
      { id: 1401, mauSac: 'Kem Nude', maHex: '#F5F5DC', kichThuoc: 'M', soLuong: 22, giaBan: 1250000, trangThai: 1, anhUrl: '/images/products/pants3.jpg' }
    ]
  },
  {
    id: 15,
    code: 'QTY003',
    name: 'Quần Short Đũi Mùa Hè',
    category: 'Quần & Jeans',
    material: 'Đũi tự nhiên',
    fit: 'Năng động dạo phố',
    price: 590000,
    promotionActive: false,
    badge: null,
    stock: 40,
    active: true,
    image: '/images/products/pants4.jpg',
    images: ['/images/products/pants4.jpg', '/images/products/pants6.jpg'],
    rating: 4.6,
    reviewCount: 20,
    letter: 'i',
    bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)',
    bienThe: [
      { id: 1501, mauSac: 'Trắng', maHex: '#FFFFFF', kichThuoc: 'S', soLuong: 20, giaBan: 590000, trangThai: 1, anhUrl: '/images/products/pants4.jpg' }
    ]
  },
  {
    id: 16,
    code: 'QTY004',
    name: 'Quần Tây Dáng Tối Giản Minimalist',
    category: 'Quần & Jeans',
    material: 'Kaki cao cấp',
    fit: 'Tối giản sang trọng',
    price: 1190000,
    promotionActive: false,
    badge: null,
    stock: 18,
    active: true,
    image: '/images/products/pants5.jpg',
    images: ['/images/products/pants5.jpg', '/images/products/pants7.jpg'],
    rating: 4.9,
    reviewCount: 25,
    letter: 'a',
    bg: 'linear-gradient(160deg,#F5EDE3,#E8CFC9)',
    bienThe: [
      { id: 1601, mauSac: 'Nâu Đất', maHex: '#8B4513', kichThuoc: 'L', soLuong: 18, giaBan: 1190000, trangThai: 1, anhUrl: '/images/products/pants5.jpg' }
    ]
  },
  {
    id: 17,
    code: 'VDH002',
    name: 'Đầm Dạ Hội Sequin Vàng Kim',
    category: 'Váy & Đầm',
    material: 'Sequin cao cấp',
    fit: 'Dáng ôm lấp lánh',
    price: 3990000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 10,
    active: true,
    image: '/images/products/dress2.jpg',
    images: ['/images/products/dress2.jpg', '/images/products/dress4.jpg'],
    rating: 5.0,
    reviewCount: 30,
    letter: 'Z',
    bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)',
    bienThe: [
      { id: 1701, mauSac: 'Vàng Kim', maHex: '#FFD700', kichThuoc: 'S', soLuong: 10, giaBan: 3990000, trangThai: 1, anhUrl: '/images/products/dress2.jpg' }
    ]
  },
  {
    id: 18,
    code: 'VDH003',
    name: 'Váy Dạ Hội Đen Huyền Bí',
    category: 'Váy & Đầm',
    material: 'Lụa Satin',
    fit: 'Bí ẩn quyến rũ',
    price: 4500000,
    promotionActive: false,
    badge: null,
    stock: 8,
    active: true,
    image: '/images/products/dress3.jpg',
    images: ['/images/products/dress3.jpg', '/images/products/dress5.jpg'],
    rating: 4.9,
    reviewCount: 16,
    letter: 'e',
    bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)',
    bienThe: [
      { id: 1801, mauSac: 'Đen Huyền', maHex: '#000000', kichThuoc: 'M', soLuong: 8, giaBan: 4500000, trangThai: 1, anhUrl: '/images/products/dress3.jpg' }
    ]
  },
  {
    id: 19,
    code: 'VDH004',
    name: 'Váy Dạ Hội Xẻ Đùi Sang Trọng',
    category: 'Váy & Đầm',
    material: 'Lụa thun',
    fit: 'Xẻ tà tôn dáng',
    price: 4890000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 12,
    active: true,
    image: '/images/products/dress4.jpg',
    images: ['/images/products/dress4.jpg', '/images/products/dress6.jpg'],
    rating: 5.0,
    reviewCount: 24,
    letter: 's',
    bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)',
    bienThe: [
      { id: 1901, mauSac: 'Đỏ Rượu', maHex: '#722F37', kichThuoc: 'S', soLuong: 12, giaBan: 4890000, trangThai: 1, anhUrl: '/images/products/dress4.jpg' }
    ]
  },
  {
    id: 21,
    code: 'TCS002',
    name: 'Set Đầm Công Sở Thanh Lịch',
    category: 'Trang phục công sở',
    material: 'Lụa pha',
    fit: 'Thanh lịch hiện đại',
    price: 1790000,
    promotionActive: false,
    badge: null,
    stock: 25,
    active: true,
    image: '/images/products/dress12.jpg',
    images: ['/images/products/dress12.jpg', '/images/products/shirt15.jpg'],
    rating: 4.8,
    reviewCount: 19,
    letter: 't',
    bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
    bienThe: [
      { id: 2101, mauSac: 'Xanh Navy', maHex: '#000080', kichThuoc: 'M', soLuong: 25, giaBan: 1790000, trangThai: 1, anhUrl: '/images/products/dress12.jpg' }
    ]
  },
  {
    id: 22,
    code: 'TCS003',
    name: 'Set Áo Kiểu & Quần Tây Công Sở',
    category: 'Trang phục công sở',
    material: 'Cotton & Tuyết mưa',
    fit: 'Tôn dáng nhẹ nhàng',
    price: 1690000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 18,
    active: true,
    image: '/images/products/shirt17.jpg',
    images: ['/images/products/shirt17.jpg', '/images/products/pants9.jpg'],
    rating: 4.7,
    reviewCount: 13,
    letter: 'i',
    bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)',
    bienThe: [
      { id: 2201, mauSac: 'Trắng Kẻ', maHex: '#F0F0F0', kichThuoc: 'S', soLuong: 18, giaBan: 1690000, trangThai: 1, anhUrl: '/images/products/shirt17.jpg' }
    ]
  },
  {
    id: 25,
    code: 'PKT002',
    name: 'Túi Xách Lụa Thêu Hoa',
    category: 'Phụ kiện thời trang',
    material: 'Lụa thêu thủ công',
    fit: 'Dáng túi xách tay',
    price: 1850000,
    promotionActive: false,
    badge: null,
    stock: 12,
    active: true,
    image: '/images/products/accessories2.jpg',
    images: ['/images/products/accessories2.jpg', '/images/products/accessories4.jpg'],
    rating: 4.8,
    reviewCount: 11,
    letter: 't',
    bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
    bienThe: [
      { id: 2501, mauSac: 'Hồng Phấn', maHex: '#FFB6C1', kichThuoc: 'Freesize', soLuong: 12, giaBan: 1850000, trangThai: 1, anhUrl: '/images/products/accessories2.jpg' }
    ]
  },
  {
    id: 26,
    code: 'PKT003',
    name: 'Thắt Lưng Da Nữ Zestia Gold',
    category: 'Phụ kiện thời trang',
    material: 'Da bò khóa mạ vàng',
    fit: 'Bản 2.5cm',
    price: 650000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 40,
    active: true,
    image: '/images/products/accessories3.jpg',
    images: ['/images/products/accessories3.jpg', '/images/products/accessories5.jpg'],
    rating: 4.9,
    reviewCount: 31,
    letter: 'i',
    bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)',
    bienThe: [
      { id: 2601, mauSac: 'Đen Vàng', maHex: '#000000', kichThuoc: 'Freesize', soLuong: 40, giaBan: 650000, trangThai: 1, anhUrl: '/images/products/accessories3.jpg' }
    ]
  },
  {
    id: 27,
    code: 'PKT004',
    name: 'Khăn Lụa Vuông Zestia Signature',
    category: 'Phụ kiện thời trang',
    material: 'Lụa tơ tằm 100%',
    fit: '70x70 cm',
    price: 490000,
    promotionActive: false,
    badge: null,
    stock: 35,
    active: true,
    image: '/images/products/accessories4.jpg',
    images: ['/images/products/accessories4.jpg', '/images/products/accessories6.jpg'],
    rating: 4.7,
    reviewCount: 22,
    letter: 'a',
    bg: 'linear-gradient(160deg,#F5EDE3,#E8CFC9)',
    bienThe: [
      { id: 2701, mauSac: 'Họa Tiết Độc Quyền', maHex: '#D4A99E', kichThuoc: 'Freesize', soLuong: 35, giaBan: 490000, trangThai: 1, anhUrl: '/images/products/accessories4.jpg' }
    ]
  },
  {
    id: 28,
    code: 'PKT005',
    name: 'Mũ Vành Rộng Zestia Bohemian',
    category: 'Phụ kiện thời trang',
    material: 'Cói tự nhiên',
    fit: 'Phong cách du lịch',
    price: 550000,
    promotionActive: false,
    badge: null,
    stock: 20,
    active: true,
    image: '/images/products/accessories5.jpg',
    images: ['/images/products/accessories5.jpg', '/images/products/accessories7.jpg'],
    rating: 4.8,
    reviewCount: 17,
    letter: 'Z',
    bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E)',
    bienThe: [
      { id: 2801, mauSac: 'Màu Cói', maHex: '#E5D3B3', kichThuoc: 'Freesize', soLuong: 20, giaBan: 550000, trangThai: 1, anhUrl: '/images/products/accessories5.jpg' }
    ]
  },
  {
    id: 29,
    code: 'DTP002',
    name: 'Đầm Đi Tiệc Ngắn Trẻ Trung',
    category: 'Trang phục dự tiệc',
    material: 'Voan nhung',
    fit: 'Ngắn trẻ trung tiệc tối',
    price: 2890000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 14,
    active: true,
    image: '/images/products/dress16.jpg',
    images: ['/images/products/dress16.jpg', '/images/products/dress17.jpg'],
    rating: 4.9,
    reviewCount: 20,
    letter: 'e',
    bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)',
    bienThe: [
      { id: 2901, mauSac: 'Hồng Đô', maHex: '#C71585', kichThuoc: 'S', soLuong: 14, giaBan: 2890000, trangThai: 1, anhUrl: '/images/products/dress16.jpg' }
    ]
  },
  {
    id: 33,
    code: 'ASM006',
    name: 'Áo Sơ Mi Lụa Hồng Pastel',
    category: 'Áo thời trang',
    material: 'Lụa tơ tằm',
    fit: 'Nhẹ nhàng thanh lịch',
    price: 2190000,
    promotionActive: false,
    badge: null,
    stock: 25,
    active: true,
    image: '/images/products/shirt5.jpg',
    images: ['/images/products/shirt5.jpg', '/images/products/shirt8.jpg'],
    rating: 4.8,
    reviewCount: 16,
    letter: 's',
    bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)',
    bienThe: [
      { id: 3301, mauSac: 'Hồng Pastel', maHex: '#FFD1DC', kichThuoc: 'M', soLuong: 25, giaBan: 2190000, trangThai: 1, anhUrl: '/images/products/shirt5.jpg' }
    ]
  },
  {
    id: 35,
    code: 'QTY005',
    name: 'Quần Tây Đũi Tự Nhiên',
    category: 'Quần & Jeans',
    material: 'Đũi tự nhiên',
    fit: 'Mộc mạc thoáng mát',
    price: 890000,
    promotionActive: false,
    badge: null,
    stock: 30,
    active: true,
    image: '/images/products/pants6.jpg',
    images: ['/images/products/pants6.jpg', '/images/products/pants8.jpg'],
    rating: 4.7,
    reviewCount: 15,
    letter: 't',
    bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
    bienThe: [
      { id: 3501, mauSac: 'Màu Đũi Nude', maHex: '#E4DDD2', kichThuoc: 'L', soLuong: 30, giaBan: 890000, trangThai: 1, anhUrl: '/images/products/pants6.jpg' }
    ]
  },
  {
    id: 48,
    code: 'PKT006',
    name: 'Ví Cầm Tay Voan Đính Đá',
    category: 'Phụ kiện thời trang',
    material: 'Voan đính đá 3D',
    fit: 'Bảng vừa sang trọng',
    price: 1590000,
    promotionActive: true,
    badge: 'Ưu đãi',
    stock: 16,
    active: true,
    image: '/images/products/accessories6.jpg',
    images: ['/images/products/accessories6.jpg', '/images/products/accessories8.jpg'],
    rating: 4.9,
    reviewCount: 23,
    letter: 'i',
    bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)',
    bienThe: [
      { id: 4801, mauSac: 'Bạc Kim', maHex: '#C0C0C0', kichThuoc: 'Freesize', soLuong: 16, giaBan: 1590000, trangThai: 1, anhUrl: '/images/products/accessories6.jpg' }
    ]
  }
]

export function mapProduct(p, i) {
  return {
    id: p.id,
    code: p.maVay,
    name: p.tenVay,
    category: p.loaiVay || '',
    material: p.chatLieu || '',
    fit: p.moTaPhom || '',
    price: Number(p.giaBan),
    promotionActive: Boolean(p.coKhuyenMai),
    badge: p.coKhuyenMai ? 'Ưu đãi' : null,
    campaign: p.dotKhuyenMai || null,
    stock: p.tonKho || 0,
    active: p.trangThai === 1 || p.trangThai === true,
    image: p.anhUrl || null,
    images: p.danhSachAnh || [],
    rating: Number(p.diemDanhGia || 0),
    reviewCount: Number(p.soDanhGia || 0),
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
    if (Array.isArray(data) && data.length > 0) {
      _products.value = data
        .filter(p => p.trangThai === 1 || p.trangThai === true)
        .map(mapProduct)
    } else {
      _products.value = MOCK_PRODUCTS
    }
  } catch (e) {
    console.warn('Backend API unavailable, using fallback mock products for testing:', e)
    _products.value = MOCK_PRODUCTS
  } finally {
    _loading = false
  }
}

export const products = _products
