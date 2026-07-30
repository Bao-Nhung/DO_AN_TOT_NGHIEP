import autoEnglish from './auto-en.json'
import { currentLocale } from './index'

const SKIPPED_TAGS = new Set(['SCRIPT', 'STYLE', 'CODE', 'PRE', 'TEXTAREA'])
const TRANSLATED_ATTRIBUTES = ['placeholder', 'title', 'aria-label', 'alt']
const sourceText = new WeakMap()
const renderedText = new WeakMap()
const sourceAttributes = new WeakMap()
const renderedAttributes = new WeakMap()
let observer
let writing = false

const phraseOverrides = {
  'Tất cả': 'All',
  'Xem': 'View',
  'Mã SP:': 'Product code:',
  'Mã:': 'Code:',
  'Tồn:': 'Stock:',
  'Màu:': 'Color:',
  'Màu sắc': 'Color',
  'Kích thước': 'Size',
  'Số lượng': 'Quantity',
  'Giá': 'Price',
  'Giá bán': 'Price',
  'Tạm tính': 'Subtotal',
  'Tổng cộng': 'Total',
  'Giảm giá': 'Discount',
  'Khách đưa': 'Customer paid',
  'Tiền thối': 'Change',
  'Thối lại': 'Change due',
  'Còn thiếu': 'Amount due',
  'Đã giữ tồn kho': 'Stock reserved',
  'Hết thời gian giữ': 'Reservation expired',
  'Ngày tạo': 'Created',
  'Trạng thái': 'Status',
  'Thao tác': 'Actions',
  'Loại': 'Category',
  'Vai trò': 'Role',
  'Ca làm': 'Shift',
  'Ghi chú': 'Notes',
  'Ngày làm': 'Working day',
  'Xuất Excel': 'Export Excel',
  'Chưa có SĐT': 'No phone number',
  'Đang xử lý': 'Processing',
  'Đang hiển thị': 'Visible',
  'Chờ xử lý': 'Pending',
  'Đã xác nhận': 'Confirmed',
  'Hoàn thành': 'Completed',
  'Đang giao': 'Shipping',
  'Giao thành công': 'Delivered',
  'Giao thất bại': 'Delivery failed',
  'Đã hủy': 'Cancelled',
  'Đã thanh toán': 'Paid',
  'Chưa thanh toán': 'Unpaid',
  'Thanh toán thất bại': 'Payment failed',
  'Mua trực tiếp tại cửa hàng': 'Purchased directly at the store',
  'Nhân viên': 'Employee',
  'Khách hàng': 'Customer',
  'Sản phẩm': 'Product',
  'Đơn hàng': 'Order',
  'Voucher': 'Voucher',
  'Thông báo': 'Notification',
  'Doanh thu': 'Revenue',
  'Đã bán:': 'Sold:',
  'Còn': 'Remaining',
  'Tại quầy': 'At the counter',
  'Hẹn gửi:': 'Scheduled:',
  'SL': 'Qty',
  'Thứ 2': 'Monday',
  'Thứ 3': 'Tuesday',
  'Thứ 4': 'Wednesday',
  'Thứ 5': 'Thursday',
  'Thứ 6': 'Friday',
  'Thứ 7': 'Saturday',
  'Chủ nhật': 'Sunday',
  'Thứ Hai': 'Monday',
  'Thứ Ba': 'Tuesday',
  'Thứ Tư': 'Wednesday',
  'Thứ Năm': 'Thursday',
  'Thứ Sáu': 'Friday',
  'Thứ Bảy': 'Saturday',
  'Chủ Nhật': 'Sunday',
  'Tên đăng nhập hoặc email': 'Username or email',
  'Hiện mật khẩu': 'Show password',
  'Mở trong thẻ mới': 'Open in a new tab',
  'Đen': 'Black',
  'Trắng': 'White',
  'Đỏ': 'Red',
  'Hồng': 'Pink',
  'Vàng': 'Yellow',
  'Tím': 'Purple',
  'Xanh Navy': 'Navy',
  'Xanh Lá': 'Green',
  'Lụa tơ tằm': 'Mulberry silk',
  'Gấm': 'Brocade',
  'Voan': 'Chiffon',
  'Đũi': 'Linen',
  'Nhung': 'Velvet',
  'Váy truyền thống': 'Traditional dresses',
  'Váy cách tân': 'Modern dresses',
  'Váy dạ hội': 'Evening dresses',
  'Váy cưới': 'Wedding dresses',
  'Váy học sinh': 'School dresses',
  'Váy công sở': 'Office dresses',
  'Váy dự tiệc': 'Party dresses',
  'Voucher thành viên': 'Member vouchers',
  'Kênh Bán Hàng': 'Sales Channels',
  'Đăng ký': 'Subscribe',
  'Thêm vào yêu thích': 'Add to wishlist',
  'Xóa khỏi yêu thích': 'Remove from wishlist',
  'Chính sách giao hàng': 'Shipping policy',
  'Đổi size': 'Size exchanges',
  'Đổi trả': 'Returns and exchanges',
  'Hoàn tiền': 'Refunds',
  'Đi làm': 'Work',
  '24h cho sản phẩm đủ điều kiện.': 'Within 24 hours for eligible products.',
  'Sản phẩm đang bán': 'Active products',
  'Hỗ trợ đổi size': 'Size exchange support',
  'Tư vấn chọn size': 'Size advice',
  'Ưu đãi': 'Special offer',
  'Chọn váy theo khoảnh khắc': 'Choose a dress for every occasion',
  'Được chọn nhiều nhất': 'Most-loved styles',
  'Hàng mới tháng này': 'New arrivals this month',
  'Trải nghiệm đã xác minh': 'Verified customer experiences',
  'Nhận ưu đãi độc quyền': 'Get exclusive offers',
  'THÔNG TIN': 'INFORMATION',
  'Thông Tin': 'Information',
  'THÔNG TIN MUA HÀNG': 'SHOPPING INFORMATION',
  'Thông tin mua hàng': 'Shopping information',
  'Cập nhật:': 'Updated:',
  'Tiếp tục sử dụng dịch vụ bằng Google': 'Continue with Google',
  'Lễ kỷ niệm': 'Celebrations',
  'Váy Dự Lễ': 'Ceremony Dresses',
  'Hẹn hò': 'Date Night',
  'Váy Hẹn Hò': 'Date-night Dresses',
  'Váy Dự Tiệc': 'Party Dresses',
  'Váy Cưới': 'Wedding Dresses',
  'Váy Đi Làm': 'Work Dresses',
  'Tiêu chí': 'Criteria',
}

const dictionary = Object.freeze({ ...autoEnglish, ...phraseOverrides })
const replacementEntries = Object.entries(phraseOverrides)
  .sort(([left], [right]) => right.length - left.length)
const templateEntries = Object.entries(dictionary)
  .filter(([source]) => /\{\d+}/.test(source))
  .sort(([left], [right]) => right.length - left.length)
  .map(([source, target]) => {
    const indexes = []
    const escaped = source
      .replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      .replace(/\\\{(\d+)\\\}/g, (_, index) => {
        indexes.push(Number(index))
        return '(.+?)'
      })
    return {
      expression: new RegExp(`^${escaped}$`, 'i'),
      indexes,
      target,
    }
  })

const dynamicRules = [
  [/^Mở danh sách\s+(\d+)\s+việc cần xử lý$/i, 'Open $1 pending tasks'],
  [/^Đã chọn\s+(\d+)\s*\/\s*(\d+)\s+sản phẩm$/i, 'Selected $1/$2 products'],
  [/^(\d+)\s+đánh giá đã xác minh$/i, '$1 verified reviews'],
  [/^Đã mua hàng\s*·\s*(.+)$/i, 'Verified purchase · $1'],
  [/^(\d+)\s+đánh giá đã mua$/i, '$1 verified-purchase reviews'],
  [/^(\d+)\s+sản phẩm đã giao$/i, '$1 items delivered'],
  [/^(\d+)\s+trên\s+5\s+sao$/i, '$1 out of 5 stars'],
  [/^\((\d+)\s+đánh giá\)$/i, '($1 reviews)'],
  [/^(\d+)\s+sản phẩm$/i, '$1 products'],
  [/^(\d+)\s+đơn hàng$/i, '$1 orders'],
  [/^(\d+)\s+yêu cầu$/i, '$1 requests'],
  [/^(\d+)\s+đợt đã tạo$/i, '$1 campaigns created'],
  [/^(\d+)\s+biến thể$/i, '$1 variants'],
  [/^(\d+)\s+giờ$/i, '$1 hours'],
  [/^Tuần\s+(.+)$/i, 'Week $1'],
  [/^Hẹn gửi:\s*(.+)$/i, 'Scheduled: $1'],
  [/^đến\s+(.+)$/i, 'until $1'],
  [/^Đã bán:\s*(\d+)$/i, 'Sold: $1'],
  [/^Còn\s+(\d+)$/i, '$1 remaining'],
  [/^Hiển thị từ\s+(\d+)\s+đến\s+(\d+)\s+trong tổng số\s+(\d+)\s+sản phẩm$/i, 'Showing $1-$2 of $3 products'],
  [/^Hiển thị từ\s+(\d+)\s+đến\s+(\d+)\s+trong tổng số\s+(\d+)\s+đơn hàng$/i, 'Showing $1-$2 of $3 orders'],
  [/^Hiển thị từ\s+(\d+)\s+đến\s+(\d+)\s+trong tổng số\s+(\d+)\s+khách hàng$/i, 'Showing $1-$2 of $3 customers'],
  [/^Hiển thị từ\s+(\d+)\s+đến\s+(\d+)\s+trong tổng số\s+(\d+)\s+nhân viên$/i, 'Showing $1-$2 of $3 employees'],
  [/^Hiển thị từ\s+(\d+)\s+đến\s+(\d+)\s+trong tổng số\s+(\d+)\s+ca làm$/i, 'Showing $1-$2 of $3 shifts'],
  [/^Hiển thị từ\s+(\d+)\s+đến\s+(\d+)\s+trong tổng số\s+(\d+)\s+thông báo$/i, 'Showing $1-$2 of $3 notifications'],
  [/^Hiển thị\s+(\d+)-(\d+)\s*\/\s*(\d+)\s+yêu cầu$/i, 'Showing $1-$2 of $3 requests'],
  [/^Hiển thị\s+(\d+)-(\d+)\s*\/\s*(\d+)\s+voucher$/i, 'Showing $1-$2 of $3 vouchers'],
  [/^Hiển thị\s+(\d+)-(\d+)\s*\/\s*(\d+)\s+đợt$/i, 'Showing $1-$2 of $3 campaigns'],
  [/^Trang\s+(\d+)\s*\/\s*(\d+)$/i, 'Page $1 of $2'],
]

function normalize(value) {
  return String(value || '').replace(/\\n/g, ' ').replace(/\s+/g, ' ').trim()
}

export function translateUiText(value, locale = currentLocale()) {
  if (locale !== 'en' || value == null) return value
  const raw = String(value)
  const normalized = normalize(raw)
  if (!normalized) return value
  const exact = dictionary[normalized]
  if (exact) {
    const leading = raw.match(/^\s*/)?.[0] || ''
    const trailing = raw.match(/\s*$/)?.[0] || ''
    return `${leading}${exact}${trailing}`
  }

  const quoted = normalized.match(/^([“"‘'])(.+)([”"’'])$/)
  if (quoted) {
    const translatedQuote = dictionary[quoted[2]]
    if (translatedQuote) return `${quoted[1]}${translatedQuote}${quoted[3]}`
  }

  const viewLabel = normalized.match(/^Xem\s+(.+)$/i)
  if (viewLabel) return `View ${translateUiText(viewLabel[1], 'en').trim()}`

  const productCommand = normalized.match(/^(Add to comparison|Remove from comparison)\s+(.+)$/i)
  if (productCommand) {
    return `${productCommand[1]} ${translateUiText(productCommand[2], 'en').trim()}`
  }

  for (const template of templateEntries) {
    const match = normalized.match(template.expression)
    if (!match) continue
    let result = template.target
    template.indexes.forEach((index, captureIndex) => {
      result = result.replaceAll(`{${index}}`, match[captureIndex + 1])
    })
    return result
  }

  for (const [expression, replacement] of dynamicRules) {
    if (expression.test(normalized)) return normalized.replace(expression, replacement)
  }

  let translated = raw
  for (const [source, target] of replacementEntries) {
    if (translated.includes(source)) translated = translated.split(source).join(target)
  }
  return translated
}

function shouldSkip(node) {
  const parent = node.nodeType === Node.ELEMENT_NODE ? node : node.parentElement
  return !parent || SKIPPED_TAGS.has(parent.tagName) || parent.closest('[data-no-i18n]')
}

function processTextNode(node) {
  if (shouldSkip(node) || !normalize(node.nodeValue)) return
  const previousRendered = renderedText.get(node)
  if (!sourceText.has(node) || (node.nodeValue !== previousRendered && currentLocale() === 'vi')) {
    sourceText.set(node, node.nodeValue)
  } else if (node.nodeValue !== previousRendered && currentLocale() === 'en') {
    sourceText.set(node, node.nodeValue)
  }
  const source = sourceText.get(node)
  const next = currentLocale() === 'vi' ? source : translateUiText(source, 'en')
  renderedText.set(node, next)
  if (node.nodeValue !== next) node.nodeValue = next
}

function processElementAttributes(element) {
  if (shouldSkip(element)) return
  const sources = sourceAttributes.get(element) || {}
  const rendered = renderedAttributes.get(element) || {}
  for (const attribute of TRANSLATED_ATTRIBUTES) {
    if (!element.hasAttribute(attribute)) continue
    const current = element.getAttribute(attribute)
    if (!(attribute in sources) || current !== rendered[attribute]) sources[attribute] = current
    const next = currentLocale() === 'vi'
      ? sources[attribute]
      : translateUiText(sources[attribute], 'en')
    rendered[attribute] = next
    if (current !== next) element.setAttribute(attribute, next)
  }
  sourceAttributes.set(element, sources)
  renderedAttributes.set(element, rendered)
}

function processNode(node) {
  if (node.nodeType === Node.TEXT_NODE) {
    processTextNode(node)
    return
  }
  if (node.nodeType !== Node.ELEMENT_NODE || shouldSkip(node)) return
  processElementAttributes(node)
  node.childNodes.forEach(processNode)
}

export function refreshDomTranslations(root = document.body) {
  if (!root || typeof document === 'undefined') return
  writing = true
  try {
    processNode(root)
  } finally {
    writing = false
  }
}

export function installDomTranslator(app) {
  if (typeof document === 'undefined') return
  app.mixin({
    mounted() {
      queueMicrotask(() => refreshDomTranslations(this.$el))
    },
    updated() {
      queueMicrotask(() => refreshDomTranslations(this.$el))
    },
  })

  observer = new MutationObserver(mutations => {
    if (writing) return
    writing = true
    try {
      for (const mutation of mutations) {
        if (mutation.type === 'characterData') {
          processTextNode(mutation.target)
        } else if (mutation.type === 'attributes') {
          processElementAttributes(mutation.target)
        } else {
          mutation.addedNodes.forEach(processNode)
        }
      }
    } finally {
      writing = false
    }
  })
  observer.observe(document.body, {
    subtree: true,
    childList: true,
    characterData: true,
    attributes: true,
    attributeFilter: TRANSLATED_ATTRIBUTES,
  })
  refreshDomTranslations()
}

export function stopDomTranslator() {
  observer?.disconnect()
  observer = undefined
}
