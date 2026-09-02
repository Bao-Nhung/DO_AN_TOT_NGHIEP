const CATEGORY_DEFINITIONS = [
  {
    category: 'Áo thời trang',
    name: 'Áo Thời Trang',
    label: 'Thời trang nữ',
    letter: 'Z',
    fallbackImage: '/images/products/catalog-v2/sp001_main.webp',
    prefixes: ['ASM'],
    aliases: ['Fashion tops & blouses'],
    bg: 'linear-gradient(160deg,#F3E8E6,#D4A99E 40%,#C08B7E)',
  },
  {
    category: 'Quần & Jeans',
    name: 'Quần & Jeans',
    label: 'Cá tính',
    letter: 'e',
    fallbackImage: '/images/products/catalog-v2/sp002_main.webp',
    prefixes: ['QJN', 'QTY'],
    aliases: ['Pants & Denim'],
    bg: 'linear-gradient(160deg,#E8DDD6,#C4A98E)',
  },
  {
    category: 'Váy & Đầm',
    name: 'Váy & Đầm',
    label: 'Quyến rũ',
    letter: 's',
    fallbackImage: '/images/products/catalog-v2/sp003_main.webp',
    prefixes: ['VDH'],
    aliases: ['Dresses & Skirts'],
    bg: 'linear-gradient(160deg,#E6E0DA,#A8A49E)',
  },
  {
    category: 'Phụ kiện thời trang',
    name: 'Phụ Kiện',
    label: 'Điểm nhấn',
    letter: 't',
    fallbackImage: '/images/products/catalog-v2/sp004_main.webp',
    prefixes: ['PKT'],
    aliases: ['Fashion accessories'],
    bg: 'linear-gradient(160deg,#F0E8E0,#D4C0A8)',
  },
  {
    category: 'Trang phục công sở',
    name: 'Đồ Công Sở',
    label: 'Thanh lịch',
    letter: 'ia',
    fallbackImage: '/images/products/catalog-v2/sp005_main.webp',
    prefixes: ['TCS'],
    aliases: ['Workwear & tailoring'],
    bg: 'linear-gradient(160deg,#E4DDD2,#C0B49E)',
  },
  {
    category: 'Trang phục dự tiệc',
    fallbackImage: '/images/products/catalog-v2/sp006_main.webp',
    prefixes: ['DTP'],
    aliases: ['Eveningwear & cocktail dress'],
  },
  {
    category: 'Áo khoác & Blazer',
    fallbackImage: '/images/products/catalog-v2/sp011_main.webp',
    prefixes: ['AKH'],
    aliases: ['Jackets & Blazers'],
  },
]

export const productCategoryDefinitions = Object.freeze(
  CATEGORY_DEFINITIONS.map(definition => Object.freeze({ ...definition }))
)

export const homeCategoryCards = Object.freeze(productCategoryDefinitions.slice(0, 5))

export function normalizeCategoryName(value) {
  return String(value || '').normalize('NFC').trim().toLocaleLowerCase('vi-VN')
}

export function categoryNamesEqual(left, right) {
  return normalizeCategoryName(left) === normalizeCategoryName(right)
}

export function getCategoryDefinition(category) {
  return productCategoryDefinitions.find(definition =>
    categoryNamesEqual(definition.category, category)
      || definition.aliases?.some(alias => categoryNamesEqual(alias, category))
  ) || null
}

export function productBelongsToCategory(product, category) {
  const actualCategory = normalizeCategoryName(product?.category)
  const definition = getCategoryDefinition(category)
  if (actualCategory) {
    if (!definition) return actualCategory === normalizeCategoryName(category)
    if (categoryNamesEqual(actualCategory, definition.category)
      || definition.aliases?.some(alias => categoryNamesEqual(actualCategory, alias))) {
      return true
    }
  }

  const code = String(product?.code || '').trim().toUpperCase()
  return Boolean(definition?.prefixes?.some(prefix => code.startsWith(prefix)))
}

export function findCategoryImage(productList, category, fallbackImage = null) {
  const productWithImage = (Array.isArray(productList) ? productList : [])
    .find(product => productBelongsToCategory(product, category) && String(product?.image || '').trim())

  return productWithImage?.image
    || fallbackImage
    || getCategoryDefinition(category)?.fallbackImage
    || null
}
