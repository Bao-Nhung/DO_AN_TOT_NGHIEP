<template>
  <div class="z-customer-ai">
    <!-- Nút bấm mở AI Assistant nổi có hiệu ứng Pulse & Badge -->
    <button
      v-if="!open"
      type="button"
      class="z-ai-toggle-btn"
      title="Zestia AI Fashion Assistant & Stylist"
      aria-label="Mở Trợ lý AI Zestia"
      @click="toggleOpen"
    >
      <div class="z-ai-pulse"></div>
      <i class="bi bi-stars z-ai-icon-stars"></i>
      <span class="z-ai-badge-text">Zestia AI</span>
    </button>

    <!-- Khung Chat AI Assistant chính -->
    <transition name="z-slide-up">
      <section v-if="open" class="z-ai-window" aria-label="Zestia AI Fashion Assistant">
        <!-- Header -->
        <header class="z-ai-header">
          <div class="z-ai-header-info">
            <div class="z-ai-avatar">
              <i class="bi bi-robot"></i>
              <span class="z-ai-status-dot"></span>
            </div>
            <div>
              <div class="d-flex align-items-center gap-2">
                <strong class="z-ai-title">{{ humanMode ? 'Nhân viên Zestia CSKH' : (activeTab === 'stylist' ? 'Zestia AI Stylist' : 'Zestia AI Assistant') }}</strong>
                <span class="badge bg-danger-subtle text-danger" style="font-size:10px;padding:2px 6px">PRO</span>
              </div>
              <span class="z-ai-subtitle" v-if="humanMode">{{ supportStatusText }}</span>
              <span class="z-ai-subtitle" v-else-if="activeTab === 'stylist'">Gợi ý outfit phối đồ chuẩn gu & dịp</span>
              <span class="z-ai-subtitle" v-else>Tư vấn sản phẩm, size, voucher & đơn hàng</span>
            </div>
          </div>
          <div class="z-ai-header-actions">
            <button type="button" class="z-ai-icon-btn" :title="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'" @click="soundEnabled = !soundEnabled">
              <i class="bi" :class="soundEnabled ? 'bi-volume-up' : 'bi-volume-mute'"></i>
            </button>
            <button type="button" class="z-ai-icon-btn" title="Xóa lịch sử chat" @click="clearHistory">
              <i class="bi bi-trash"></i>
            </button>
            <button type="button" class="z-ai-icon-btn close-btn" title="Đóng" @click="open = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </header>

        <!-- Thanh chuyển chế độ (Tabs) -->
        <div class="z-ai-tabs" v-if="!humanMode">
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'assistant' }" @click="activeTab = 'assistant'">
            <i class="bi bi-chat-left-dots"></i> Tư vấn Mua sắm
          </button>
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'stylist' }" @click="activeTab = 'stylist'">
            <i class="bi bi-magic"></i> AI Stylist Phối Đồ
          </button>
        </div>

        <!-- Body Khung Chat -->
        <div ref="bodyRef" class="z-ai-body">
          <div v-for="(msg, index) in displayMessages" :key="msg.id || index" class="z-ai-msg-group" :class="msg.role">
            <div class="z-ai-avatar-mini" v-if="msg.role === 'assistant'">
              <i class="bi bi-stars"></i>
            </div>
            
            <div class="z-ai-msg-content">
              <div class="z-ai-msg-bubble">
                <small v-if="humanMode && msg.senderName" class="d-block text-muted mb-1">{{ msg.senderName }}</small>
                <div class="z-ai-text" v-html="formatMarkdown(msg.content)"></div>

                <!-- Action copy cho tin nhắn AI -->
                <button v-if="msg.role === 'assistant' && msg.content" class="z-ai-copy-btn" title="Sao chép câu trả lời" @click="copyText(msg.content)">
                  <i class="bi bi-clipboard"></i>
                </button>
              </div>

              <!-- THẺ DỮ LIỆU SẢN PHẨM TRỰC QUAN (PRODUCT CARDS) -->
              <div v-if="msg.cards && msg.cards.length" class="z-ai-cards-container">
                <div class="z-ai-cards-scroll">
                  <div
                    v-for="(card, cIdx) in msg.cards"
                    :key="cIdx"
                    class="z-ai-card-item"
                    :class="card.type"
                  >
                    <!-- THẺ SẢN PHẨM -->
                    <template v-if="card.type === 'product'">
                      <div class="z-card-img-wrapper" @click="goToProduct(card.id)">
                        <img :src="card.image || '/images/products/shirt1.jpg'" :alt="card.name" @error="onCardImgError" />
                        <span v-if="card.category" class="z-card-cat-badge">{{ card.category }}</span>
                      </div>
                      <div class="z-card-info">
                        <div class="z-card-name" :title="card.name" @click="goToProduct(card.id)">{{ card.name }}</div>
                        <div class="d-flex align-items-center justify-content-between mt-1">
                          <span class="z-card-price">{{ formatPrice(card.price) }}</span>
                          <span v-if="card.stock" class="z-card-stock">Còn {{ card.stock }}</span>
                        </div>
                        <div class="d-flex gap-1 mt-2">
                          <button type="button" class="btn btn-sm btn-dark flex-grow-1" style="font-size:11px" @click="goToProduct(card.id)">
                            <i class="bi bi-eye"></i> Xem ngay
                          </button>
                          <button type="button" class="btn btn-sm btn-outline-dark" style="font-size:11px" title="Thêm vào giỏ" @click="quickAddToCart(card)">
                            <i class="bi bi-cart-plus"></i>
                          </button>
                        </div>
                      </div>
                    </template>

                    <!-- THẺ VOUCHER -->
                    <template v-else-if="card.type === 'voucher'">
                      <div class="z-voucher-card">
                        <div class="z-voucher-icon"><i class="bi bi-ticket-perforated-fill"></i></div>
                        <div class="z-voucher-details">
                          <strong class="z-voucher-code">{{ card.code }}</strong>
                          <div class="z-voucher-desc">{{ card.description || ('Giảm ' + formatPrice(card.discount) + ' cho đơn từ ' + formatPrice(card.minOrder)) }}</div>
                        </div>
                        <button type="button" class="z-voucher-apply-btn" @click="applyVoucherCode(card.code)">
                          <i class="bi bi-check2-circle"></i> Áp dụng
                        </button>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Typing Indicator Animation -->
          <div v-if="loading" class="z-ai-msg-group assistant">
            <div class="z-ai-avatar-mini"><i class="bi bi-stars"></i></div>
            <div class="z-ai-msg-content">
              <div class="z-ai-msg-bubble z-ai-typing-bubble">
                <span class="z-dot"></span>
                <span class="z-dot"></span>
                <span class="z-dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- THẺ GỢI Ý NHANH (QUICK CHIPS) -->
        <div v-if="!humanMode" class="z-ai-quick-section">
          <div class="z-ai-chips-scroll">
            <button
              v-for="chip in currentChips"
              :key="chip.label"
              type="button"
              class="z-ai-chip-btn"
              :disabled="loading"
              @click="sendSuggestedQuestion(chip.question)"
            >
              <i class="bi" :class="chip.icon"></i>
              {{ chip.label }}
            </button>
          </div>
        </div>

        <!-- Handoff Chat với Nhân Viên -->
        <div v-if="!humanMode && hasAskedAi && !loading" class="z-ai-handoff-bar">
          <span>Cần gặp nhân viên tư vấn trực tiếp?</span>
          <button type="button" class="z-handoff-btn" :disabled="handoffLoading" @click="requestEmployee">
            <i class="bi bi-headset"></i> {{ handoffLoading ? 'Đang kết nối...' : 'Gặp NV CSKH' }}
          </button>
        </div>

        <div v-if="humanMode && supportStatus === 'CLOSED'" class="z-ai-handoff-bar">
          <span>Phiên hỗ trợ nhân viên đã kết thúc.</span>
          <button type="button" class="z-handoff-btn" @click="backToAi">
            <i class="bi bi-stars"></i> Quay lại AI
          </button>
        </div>

        <!-- Form nhập tin nhắn -->
        <form v-if="!humanMode || supportStatus !== 'CLOSED'" class="z-ai-form" @submit.prevent="send">
          <input
            v-model="draft"
            ref="inputRef"
            class="z-ai-input"
            :placeholder="humanMode ? 'Nhắn cho nhân viên đang trực...' : (activeTab === 'stylist' ? 'Nhập dịp đi chơi, chiều cao, cân nặng...' : 'Hỏi về mẫu váy, size, voucher...')"
            :disabled="loading"
            maxlength="1000"
          />
          <button class="z-ai-send-btn" title="Gửi" aria-label="Gửi tin nhắn" :disabled="loading || draft.trim().length < 2">
            <i class="bi bi-send-fill"></i>
          </button>
        </form>
      </section>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/composables/useApi'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'
import { MOCK_PRODUCTS } from '@/composables/useProducts'

const router = useRouter()
const { formatPrice, addItem } = useCart()
const { showToast } = useToast()

const open = ref(false)
const loading = ref(false)
const handoffLoading = ref(false)
const soundEnabled = ref(true)
const activeTab = ref('assistant') // 'assistant' | 'stylist'
const draft = ref('')
const bodyRef = ref(null)
const inputRef = ref(null)

const humanMode = ref(false)
const supportToken = ref('')
const supportStatus = ref('')
const supportEmployee = ref('')
const humanMessages = ref([])
let supportPoll = null

// Danh sách gợi ý theo tab Assistant
const assistantChips = [
  { label: '💃 Váy đi tiệc', icon: 'bi-stars', question: 'Gợi ý cho tôi các mẫu váy dự tiệc sang trọng và tôn dáng nhất.' },
  { label: '💼 Đồ công sở', icon: 'bi-briefcase', question: 'Tìm giúp tôi các mẫu áo sơ mi và quần tây công sở thanh lịch.' },
  { label: '📏 Tư vấn size', icon: 'bi-rulers', question: 'Tôi cao 1m62 nặng 48kg thì mặc size váy nào vừa đẹp?' },
  { label: '🎟️ Mã giảm giá', icon: 'bi-ticket-perforated', question: 'Hiện tại cửa hàng có những voucher giảm giá nào đang áp dụng?' },
  { label: '📦 Trạng thái đơn', icon: 'bi-box-seam', question: 'Tôi muốn tra cứu thông tin đơn hàng vừa đặt.' }
]

// Danh sách gợi ý theo tab AI Stylist
const stylistChips = [
  { label: '✨ Phối set đi tiệc tối', icon: 'bi-gem', question: 'Hãy phối cho tôi một set đồ đi tiệc đêm sang trọng kèm phụ kiện.' },
  { label: '☕ Phối set hẹn hò cafe', icon: 'bi-cup-hot', question: 'Gợi ý phối đồ nhẹ nhàng nữ tính cho buổi hẹn hò cuối tuần.' },
  { label: '🏖️ Phối set du lịch', icon: 'bi-sun', question: 'Phối giúp tôi set đồ đũi thoáng mát năng động đi du lịch.' },
  { label: '💰 Set đồ dưới 1.5tr', icon: 'bi-wallet2', question: 'Gợi ý một set outfit hoàn chỉnh giá dưới 1.500.000đ.' }
]

const currentChips = computed(() => activeTab.value === 'stylist' ? stylistChips : assistantChips)

const messages = ref([
  {
    role: 'assistant',
    content: '✨ **Xin chào! Mình là Zestia AI Fashion Assistant & Stylist.**\n\nMình có thể giúp bạn chọn mẫu váy tôn dáng, tư vấn chuẩn size số đo, gợi ý phối đồ outfit theo dịp hoặc áp dụng voucher ưu đãi hot nhất hôm nay!',
    cards: []
  }
])

const displayMessages = computed(() => humanMode.value ? humanMessages.value : messages.value)
const hasAskedAi = computed(() => messages.value.some(m => m.role === 'user'))
const supportStatusText = computed(() => {
  if (supportStatus.value === 'CLOSED') return 'Phiên hỗ trợ đã kết thúc'
  if (supportStatus.value === 'WAITING') return 'Đã vào hàng chờ hỗ trợ'
  if (supportEmployee.value) return `${supportEmployee.value} đang trực tuyến`
  return 'Đang chờ nhân viên hỗ trợ'
})

onMounted(async () => {
  const savedToken = localStorage.getItem('zestia_support_chat_token') || ''
  if (!savedToken) return
  try {
    const snapshot = await api().getCustomerSupportChat(savedToken)
    applySupportSnapshot(snapshot)
    if (supportStatus.value !== 'CLOSED') startSupportPoll()
  } catch (_) {
    localStorage.removeItem('zestia_support_chat_token')
  }
})

onBeforeUnmount(stopSupportPoll)

function toggleOpen() {
  open.value = !open.value
  if (open.value) {
    scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
}

function clearHistory() {
  messages.value = [
    {
      role: 'assistant',
      content: '🧹 Đã làm sạch lịch sử hội thoại. Mình có thể hỗ trợ gì khác cho bạn?',
      cards: []
    }
  ]
  showToast('Đã làm sạch lịch sử hội thoại')
}

// Âm thanh phản hồi Web Audio Synthesizer
function playChimeSound() {
  if (!soundEnabled.value) return
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext
    if (!AudioCtx) return
    const ctx = new AudioCtx()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'sine'
    osc.frequency.setValueAtTime(587.33, ctx.currentTime) // D5
    osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.15) // A5
    gain.gain.setValueAtTime(0.08, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.3)
  } catch (_) {}
}

function formatMarkdown(text) {
  if (!text) return ''
  let html = text
    // Code blocks
    .replace(/```([\s\S]*?)```/g, '<pre class="z-ai-code"><code>$1</code></pre>')
    // Inline code
    .replace(/`([^`]+)`/g, '<code class="z-ai-inline-code">$1</code>')
    // Bold
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // Italic
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    // Bullet lists (• or - or *)
    .replace(/^[•\-\*]\s+(.+)$/gm, '<li>$1</li>')
    // Numbered lists
    .replace(/^(\d+)\.\s+(.+)$/gm, '<li><span class="z-list-num">$1.</span> $2</li>')
    // Newlines to <br>, but not inside <pre>
    .replace(/\n/g, '<br>')
  // Wrap consecutive <li> items in <ul>
  html = html.replace(/((?:<li>.*?<\/li><br>?)+)/g, (match) => {
    const items = match.replace(/<br>/g, '')
    return `<ul class="z-ai-list">${items}</ul>`
  })
  return html
}

function copyText(text) {
  const plainText = text.replace(/<[^>]*>/g, '')
  navigator.clipboard.writeText(plainText)
  showToast('Đã sao chép nội dung!')
}

function onCardImgError(e) {
  e.target.src = '/images/products/dress1.jpg'
}

function goToProduct(id) {
  open.value = false
  router.push('/product/' + id)
}

function quickAddToCart(card) {
  addItem({
    id: card.id,
    name: card.name,
    price: Number(card.price || 0),
    image: card.image || '/images/products/dress1.jpg',
    qty: 1,
    selectedSize: 'M',
    selectedColor: 'Tiêu chuẩn'
  })
  showToast(`Đã thêm "${card.name}" vào giỏ hàng!`)
}

function applyVoucherCode(code) {
  navigator.clipboard.writeText(code)
  showToast(`Đã sao chép mã voucher "${code}"!`)
}

async function send() {
  const text = draft.value.trim()
  if (text.length < 2 || loading.value) return
  if (humanMode.value) return sendHumanMessage(text)

  messages.value.push({ role: 'user', content: text, cards: [] })
  draft.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const history = messages.value
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .slice(-8)
      .map(m => ({ role: m.role, content: m.content }))

    const res = await api().sendAiChat({
      message: text,
      history,
      mode: activeTab.value // 'assistant' | 'stylist'
    }).catch(() => null)

    let replyText = res?.reply
    let cards = res?.cards || []

    // Nếu offline hoặc không có reply từ API -> Dùng client smart fallback AI engine
    if (!replyText) {
      const localRes = generateClientAiResponse(text)
      replyText = localRes.reply
      cards = localRes.cards || []
    }

    // Thêm tin nhắn với streaming reveal effect
    const newMsg = { role: 'assistant', content: '', cards: [] }
    messages.value.push(newMsg)
    playChimeSound()
    loading.value = false
    await scrollToBottom()
    // Typewriter-like reveal
    await streamText(newMsg, replyText)
    newMsg.cards = cards
    await scrollToBottom()
  } catch (e) {
    const localRes = generateClientAiResponse(text)
    const newMsg = { role: 'assistant', content: '', cards: [] }
    messages.value.push(newMsg)
    playChimeSound()
    loading.value = false
    await streamText(newMsg, localRes.reply)
    newMsg.cards = localRes.cards || []
    await scrollToBottom()
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function sendSuggestedQuestion(question) {
  if (humanMode.value || loading.value) return
  draft.value = question
  await send()
}

// BỘ ĐỘNG CƠ PHẢN HỒI THÔNG MINH CLIENT ENGINE (KHI OFFLINE / PREVIEW)
function generateClientAiResponse(query) {
  const q = query.toLowerCase()
  
  if (q.includes('dự tiệc') || q.includes('đi tiệc') || q.includes('dạ hội')) {
    const dresses = MOCK_PRODUCTS.filter(p => p.category?.includes('Váy') || p.name?.includes('Đầm')).slice(0, 3)
    const cards = dresses.map(p => ({
      type: 'product',
      id: p.id,
      name: p.name,
      price: p.price,
      image: p.image,
      category: p.category,
      stock: 12
    }))
    return {
      reply: '👗 **GỢI Ý VÁY DỰ TIỆC SANG TRỌNG & TÔN DÁNG**\n\nDưới đây là 3 mẫu váy dạ hội & dự tiệc thiết kế cao cấp nhất tại Zestia dành cho bạn:',
      cards: cards
    }
  }

  if (q.includes('công sở') || q.includes('đi làm') || q.includes('sơ mi')) {
    const officeItems = MOCK_PRODUCTS.filter(p => p.category?.includes('Áo') || p.category?.includes('Quần') || p.category?.includes('Công sở')).slice(0, 3)
    const cards = officeItems.map(p => ({
      type: 'product',
      id: p.id,
      name: p.name,
      price: p.price,
      image: p.image,
      category: p.category,
      stock: 15
    }))
    return {
      reply: '💼 **OUTFIT CÔNG SỞ THANH LỊCH & CHUYÊN NGHIỆP**\n\nZestia đề xuất set áo sơ mi lụa mềm kết hợp quần tây phom đứng chuẩn Hàn Quốc:',
      cards: cards
    }
  }

  if (q.includes('voucher') || q.includes('mã') || q.includes('giảm giá') || q.includes('khuyến mãi')) {
    return {
      reply: '🎟️ **DANH SÁCH VOUCHER HOT ĐANG ÁP DỤNG TẠI ZESTIA**\n\nBấm nút **"Áp dụng"** để sao chép mã và dùng ngay khi thanh toán nhé:',
      cards: [
        { type: 'voucher', code: 'ZESTIA10', discount: 100000, minOrder: 500000, description: 'Giảm 100.000đ cho đơn hàng từ 500.000đ' },
        { type: 'voucher', code: 'VIPMEM20', discount: 200000, minOrder: 1000000, description: 'Giảm 200.000đ cho đơn hàng từ 1.000.000đ' },
        { type: 'voucher', code: 'FREESHIP', discount: 30000, minOrder: 300000, description: 'Miễn phí vận chuyển toàn quốc' }
      ]
    }
  }

  if (q.includes('size') || q.includes('số đo') || q.includes('cân nặng') || q.includes('kg') || q.includes('m6')) {
    return {
      reply: '📏 **BẢNG TƯ VẤN SIZE CHUẨN ZESTIA**\n\n' +
             '• **Size S**: 40kg - 48kg | Ngực 82-85cm, Eo 62-66cm\n' +
             '• **Size M**: 49kg - 55kg | Ngực 86-90cm, Eo 67-71cm\n' +
             '• **Size L**: 56kg - 62kg | Ngực 91-95cm, Eo 72-76cm\n\n' +
             '💡 *Mẹo nhỏ: Nếu bạn muốn mặc ôm dáng vừa vặn hãy chọn đúng size, nếu muốn thoải mái nhẹ nhàng hãy chọn nhích 1 size nhé!*',
      cards: []
    }
  }

  if (q.includes('đơn') || q.includes('trạng thái') || q.includes('tra cứu')) {
    return {
      reply: '📦 **TRA CỨU ĐƠN HÀNG**\n\n' +
             'Bạn có thể tra cứu nhanh trạng thái đơn hàng của mình bằng cách vào mục **"Đơn hàng của tôi"** ở trang cá nhân hoặc nhập mã đơn hàng (ví dụ: `HD001234`) kèm số điện thoại mua hàng tại đây!',
      cards: []
    }
  }

  // Mặc định gợi ý sản phẩm bán chạy
  const featured = MOCK_PRODUCTS.slice(0, 3)
  return {
    reply: '✨ **SẢN PHẨM NỔI BẬT ĐƯỢC YÊU THÍCH NHẤT**\n\nXem ngay các thiết kế mới cập bến tuần này tại Zestia:',
    cards: featured.map(p => ({ type: 'product', id: p.id, name: p.name, price: p.price, image: p.image, category: p.category, stock: 20 }))
  }
}

async function requestEmployee() {
  if (handoffLoading.value) return
  const lastQuestion = [...messages.value].reverse().find(m => m.role === 'user')?.content
  if (!lastQuestion) return
  handoffLoading.value = true
  try {
    const snapshot = await api().requestHumanSupport(lastQuestion)
    applySupportSnapshot(snapshot)
    localStorage.setItem('zestia_support_chat_token', supportToken.value)
    startSupportPoll()
    await scrollToBottom()
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: error.error || 'Hiện chưa thể kết nối với nhân viên. Bạn vui lòng thử lại sau.',
      cards: []
    })
  } finally {
    handoffLoading.value = false
  }
}

async function sendHumanMessage(text) {
  if (!supportToken.value) return
  loading.value = true
  draft.value = ''
  try {
    applySupportSnapshot(await api().sendCustomerSupportMessage(supportToken.value, text))
  } catch (error) {
    humanMessages.value.push({ role: 'assistant', content: error.error || 'Không thể gửi tin nhắn lúc này.', cards: [] })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

function applySupportSnapshot(snapshot) {
  if (!snapshot?.token) return
  humanMode.value = true
  supportToken.value = snapshot.token
  supportStatus.value = snapshot.status || 'WAITING'
  supportEmployee.value = snapshot.employeeName || ''
  humanMessages.value = (snapshot.messages || []).map(m => ({
    id: m.id,
    role: m.senderType === 'CUSTOMER' ? 'user' : m.senderType === 'SYSTEM' ? 'system' : 'assistant',
    senderName: m.senderName,
    content: m.content
  }))
}

function startSupportPoll() {
  stopSupportPoll()
  supportPoll = window.setInterval(async () => {
    if (!supportToken.value || supportStatus.value === 'CLOSED') {
      stopSupportPoll() // Tự dừng khi phiên đóng
      return
    }
    try {
      applySupportSnapshot(await api().getCustomerSupportChat(supportToken.value))
      await scrollToBottom()
    } catch (_) {}
  }, 4000)
}

function stopSupportPoll() {
  if (supportPoll) window.clearInterval(supportPoll)
  supportPoll = null
}

function backToAi() {
  stopSupportPoll()
  humanMode.value = false
  supportToken.value = ''
  supportStatus.value = ''
  supportEmployee.value = ''
  humanMessages.value = []
  localStorage.removeItem('zestia_support_chat_token')
}

// Streaming typewriter reveal – hi\u1ec7n text t\u1eebng chunk nh\u1ecf
async function streamText(msgObj, fullText, chunkSize = 6, delayMs = 18) {
  if (!fullText) return
  msgObj.content = ''
  let i = 0
  while (i < fullText.length) {
    msgObj.content += fullText.slice(i, i + chunkSize)
    i += chunkSize
    await new Promise(resolve => setTimeout(resolve, delayMs))
    await scrollToBottom()
  }
  msgObj.content = fullText // ensure exact final
}

async function scrollToBottom() {
  await nextTick()
  if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
}
</script>

<style scoped>
.z-customer-ai {
  position: fixed;
  right: 22px;
  bottom: 24px;
  z-index: 1200;
  font-family: var(--z-font-body);
}

/* NÚT TOGGLE NỔI BẤM MỞ AI */
.z-ai-toggle-btn {
  position: relative;
  height: 52px;
  padding: 0 20px 0 16px;
  border: none;
  border-radius: 30px;
  background: linear-gradient(135deg, #1A1A1A 0%, #333333 50%, #D4564E 100%);
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 12px 32px rgba(212, 86, 78, 0.35);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.z-ai-toggle-btn:hover {
  transform: translateY(-4px) scale(1.03);
  box-shadow: 0 16px 40px rgba(212, 86, 78, 0.45);
}

.z-ai-icon-stars {
  font-size: 20px;
  color: #FFD700;
  animation: z-spin-slow 6s linear infinite;
}

@keyframes z-spin-slow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.z-ai-badge-text {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.3px;
}

.z-ai-pulse {
  position: absolute;
  top: -2px; left: -2px; right: -2px; bottom: -2px;
  border-radius: 32px;
  border: 2px solid rgba(212, 86, 78, 0.6);
  animation: z-pulse-ring 2s cubic-bezier(0.455, 0.03, 0.515, 0.955) infinite;
}

@keyframes z-pulse-ring {
  0% { transform: scale(0.95); opacity: 0.8; }
  50% { transform: scale(1.08); opacity: 0; }
  100% { transform: scale(0.95); opacity: 0; }
}

/* KHUNG CHAT AI PANEL */
.z-ai-window {
  width: min(390px, calc(100vw - 28px));
  height: min(600px, calc(100vh - 100px));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* HEADER */
.z-ai-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, #1A1A1A 0%, #2A2A2A 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.z-ai-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.z-ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #D4564E 0%, #B8433C 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  position: relative;
}

.z-ai-status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2ECC71;
  border: 2px solid #1A1A1A;
}

.z-ai-title { font-size: 14px; font-weight: 700; color: #fff; }
.z-ai-subtitle { display: block; font-size: 11px; color: rgba(255,255,255,0.7); margin-top: 1px; }

.z-ai-header-actions { display: flex; align-items: center; gap: 6px; }
.z-ai-icon-btn {
  width: 32px; height: 32px; border: none; border-radius: 50%;
  background: rgba(255,255,255,0.1); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; cursor: pointer; transition: background 0.2s;
}
.z-ai-icon-btn:hover { background: rgba(255,255,255,0.2); }
.z-ai-icon-btn.close-btn:hover { background: #D4564E; }

/* TABS MODE SWITCHER */
.z-ai-tabs {
  display: flex;
  background: #F4F4F5;
  padding: 4px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}
.z-ai-tab {
  flex: 1;
  border: none;
  padding: 8px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #666;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.z-ai-tab.active {
  background: #fff;
  color: var(--z-dark);
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}

/* CHAT BODY */
.z-ai-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.z-ai-msg-group {
  display: flex;
  gap: 10px;
  max-width: 92%;
}

.z-ai-msg-group.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.z-ai-avatar-mini {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1A1A1A, #D4564E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.z-ai-msg-bubble {
  position: relative;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.5;
}

.z-ai-msg-group.assistant .z-ai-msg-bubble {
  background: #F4F4F6;
  color: #1F2937;
  border-top-left-radius: 4px;
}

.z-ai-msg-group.user .z-ai-msg-bubble {
  background: var(--z-dark);
  color: #fff;
  border-top-right-radius: 4px;
}

.z-ai-copy-btn {
  position: absolute;
  bottom: 4px; right: 6px;
  border: none; background: transparent;
  font-size: 11px; color: #9CA3AF;
  opacity: 0; transition: opacity 0.2s;
}
.z-ai-msg-bubble:hover .z-ai-copy-btn { opacity: 1; }
.z-ai-copy-btn:hover { color: var(--z-accent); }

/* CARDS STYLING */
.z-ai-cards-container {
  margin-top: 10px;
  width: 100%;
}
.z-ai-cards-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
}
.z-ai-card-item {
  flex: 0 0 200px;
  background: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  scroll-snap-align: start;
}
.z-card-img-wrapper {
  height: 140px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  background: #f9f9f9;
}
.z-card-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.z-card-img-wrapper:hover img { transform: scale(1.05); }
.z-card-cat-badge {
  position: absolute; top: 6px; left: 6px;
  background: rgba(0,0,0,0.6); color: #fff;
  font-size: 10px; padding: 2px 8px; border-radius: 10px;
}
.z-card-info { padding: 10px; }
.z-card-name {
  font-size: 12px; font-weight: 700; color: #111;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  cursor: pointer;
}
.z-card-price { font-size: 13px; font-weight: 700; color: var(--z-accent); }
.z-card-stock { font-size: 10px; color: #6B7280; }

/* VOUCHER CARD STYLING */
.z-voucher-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #FFF8F6;
  border: 1px dashed var(--z-accent);
  border-radius: 10px;
  margin-bottom: 8px;
}
.z-voucher-icon { font-size: 20px; color: var(--z-accent); }
.z-voucher-details { flex: 1; }
.z-voucher-code { font-size: 13px; font-weight: 800; color: var(--z-dark); }
.z-voucher-desc { font-size: 10px; color: #666; margin-top: 2px; }
.z-voucher-apply-btn {
  border: none; background: var(--z-accent); color: #fff;
  font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 6px;
  cursor: pointer; transition: background 0.2s;
}
.z-voucher-apply-btn:hover { background: #B8433C; }

/* TYPING INDICATOR */
.z-ai-typing-bubble {
  display: flex; align-items: center; gap: 4px; padding: 10px 16px;
}
.z-dot {
  width: 6px; height: 6px; border-radius: 50%; background: #9CA3AF;
  animation: z-bounce 1.4s infinite ease-in-out both;
}
.z-dot:nth-child(1) { animation-delay: -0.32s; }
.z-dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes z-bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* QUICK CHIPS */
.z-ai-quick-section {
  padding: 8px 12px;
  background: #FAFAFA;
  border-top: 1px solid #F0F0F0;
}
.z-ai-chips-scroll {
  display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px;
}
.z-ai-chip-btn {
  white-space: nowrap; border: 1px solid #E4E4E7; border-radius: 16px;
  background: #fff; color: #3F3F46; font-size: 11px; font-weight: 600;
  padding: 6px 12px; cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; gap: 6px;
}
.z-ai-chip-btn:hover {
  background: var(--z-dark); color: #fff; border-color: var(--z-dark);
}

/* HANDOFF BAR */
.z-ai-handoff-bar {
  padding: 8px 14px; background: #FFF7ED; border-top: 1px solid #FFEDD5;
  display: flex; align-items: center; justify-content: space-between;
  font-size: 11px; color: #C2410C;
}
.z-handoff-btn {
  border: none; background: #EA580C; color: #fff;
  font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 6px;
  cursor: pointer;
}

/* FORM INPUT */
.z-ai-form {
  padding: 12px 14px;
  background: #fff;
  border-top: 1px solid #E5E7EB;
  display: flex;
  gap: 10px;
}
.z-ai-input {
  flex: 1; border: 1px solid #E5E7EB; border-radius: 20px;
  padding: 8px 16px; font-size: 13px; outline: none; transition: border-color 0.2s;
}
.z-ai-input:focus { border-color: var(--z-dark); }
.z-ai-send-btn {
  width: 36px; height: 36px; border: none; border-radius: 50%;
  background: var(--z-dark); color: #fff; font-size: 14px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: background 0.2s;
}
.z-ai-send-btn:disabled { background: #D1D5DB; cursor: not-allowed; }
.z-ai-send-btn:not(:disabled):hover { background: var(--z-accent); }

/* ANIMATION SLIDE UP */
.z-slide-up-enter-active, .z-slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.z-slide-up-enter-from, .z-slide-up-leave-to {
  opacity: 0; transform: translateY(20px) scale(0.95);
}
/* MARKDOWN ELEMENTS */
.z-ai-list {
  margin: 4px 0;
  padding-left: 18px;
  list-style: disc;
}
.z-ai-list li {
  margin: 3px 0;
  font-size: 13px;
  line-height: 1.5;
}
.z-list-num {
  font-weight: 700;
  color: var(--z-accent);
}
.z-ai-code {
  background: #1e1e2e;
  color: #cdd6f4;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 12px;
  font-family: 'Fira Code', 'Consolas', monospace;
  overflow-x: auto;
  margin: 6px 0;
  white-space: pre;
}
.z-ai-inline-code {
  background: #F1F5F9;
  color: #D4564E;
  border-radius: 4px;
  padding: 1px 5px;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
}
</style>
