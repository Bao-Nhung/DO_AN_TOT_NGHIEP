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
      <span class="z-ai-badge-text">Zestia AI 2.0</span>
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
                <strong class="z-ai-title">
                  {{ humanMode ? 'Nhân viên Zestia CSKH' : (activeTab === 'stylist' ? 'Zestia AI Stylist' : (activeTab === 'size' ? 'Zestia Size Advisor' : 'Zestia AI Assistant')) }}
                </strong>
                <span class="badge bg-danger-subtle text-danger fw-bold" style="font-size:10px;padding:2px 6px">PRO 2.0</span>
              </div>
              <span class="z-ai-subtitle" v-if="humanMode">{{ supportStatusText }}</span>
              <span class="z-ai-subtitle" v-else-if="activeTab === 'stylist'">Gợi ý Outfit phối đồ chuẩn gu & dịp</span>
              <span class="z-ai-subtitle" v-else-if="activeTab === 'size'">Tính Size chuẩn vóc dáng theo chiều cao/cân nặng</span>
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
            <i class="bi bi-chat-left-dots"></i> Tư vấn
          </button>
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'stylist' }" @click="activeTab = 'stylist'">
            <i class="bi bi-magic"></i> AI Stylist
          </button>
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'size' }" @click="activeTab = 'size'">
            <i class="bi bi-ruler"></i> Tính Size
          </button>
        </div>

        <!-- BỘ TÍNH SIZE CHUẨN KHI Ở TAB SIZE -->
        <div v-if="activeTab === 'size' && !humanMode" class="z-size-calculator-panel">
          <div class="z-size-calc-header">
            <i class="bi bi-stars text-danger me-1"></i>
            <strong>Tính Size Chuẩn Theo Vóc Dáng</strong>
          </div>
          <div class="row g-2 mt-1">
            <div class="col-6">
              <label class="z-calc-label">Chiều cao: <strong class="text-danger">{{ sizeHeight }} cm</strong></label>
              <input type="range" v-model.number="sizeHeight" min="145" max="185" class="form-range custom-range" />
            </div>
            <div class="col-6">
              <label class="z-calc-label">Cân nặng: <strong class="text-danger">{{ sizeWeight }} kg</strong></label>
              <input type="range" v-model.number="sizeWeight" min="38" max="85" class="form-range custom-range" />
            </div>
          </div>
          <div class="z-size-result-bar mt-2">
            <div class="z-size-badge-val">
              Size khuyên dùng: <span class="z-size-tag">{{ calculatedSizeInfo.size }}</span>
              <small class="ms-2 text-success">Vừa vặn {{ calculatedSizeInfo.score }}%</small>
            </div>
            <button type="button" class="btn btn-sm btn-dark z-calc-submit-btn" @click="askAiForSize">
              <i class="bi bi-send-fill me-1"></i> Tư vấn sản phẩm
            </button>
          </div>
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

                <!-- Các nút hỗ trợ phụ (Copy & Text-to-Speech) -->
                <div v-if="msg.role === 'assistant' && msg.content" class="z-ai-msg-actions">
                  <button type="button" class="z-ai-action-sub-btn" title="Đọc câu trả lời" @click="speakText(msg.content)">
                    <i class="bi bi-volume-up"></i>
                  </button>
                  <button type="button" class="z-ai-action-sub-btn" title="Sao chép câu trả lời" @click="copyText(msg.content)">
                    <i class="bi bi-clipboard"></i>
                  </button>
                </div>
              </div>

              <!-- THẺ DỮ LIỆU TRỰC QUAN (CARDS) -->
              <div v-if="msg.cards && msg.cards.length" class="z-ai-cards-container">
                <div class="z-ai-cards-scroll">
                  <div
                    v-for="(card, cIdx) in msg.cards"
                    :key="cIdx"
                    class="z-ai-card-item"
                    :class="card.type"
                  >
                    <!-- THẺ OUTFIT LOOKBOOK (Full Set Match) -->
                    <template v-if="card.type === 'outfit'">
                      <div class="z-outfit-card">
                        <div class="z-outfit-header">
                          <span class="z-outfit-badge"><i class="bi bi-stars me-1"></i> LOOKBOOK OUTFIT</span>
                          <strong class="z-outfit-title">{{ card.title }}</strong>
                          <span v-if="card.occasion" class="z-outfit-sub">{{ card.occasion }}</span>
                        </div>
                        <div class="z-outfit-items-grid">
                          <div v-for="(item, iIdx) in card.items" :key="iIdx" class="z-outfit-mini-item" @click="goToProduct(item.id)">
                            <img :src="item.image || '/images/products/shirt1.jpg'" :alt="item.name" @error="onCardImgError" />
                            <div class="z-outfit-mini-info">
                              <span class="z-outfit-mini-name">{{ item.name }}</span>
                              <span class="z-outfit-mini-price">{{ formatPrice(item.price) }}</span>
                            </div>
                          </div>
                        </div>
                        <div class="z-outfit-footer">
                          <div class="d-flex align-items-center justify-content-between">
                            <div class="z-outfit-pricing">
                              <span class="z-outfit-combo-price">{{ formatPrice(card.comboPrice || card.totalPrice) }}</span>
                              <span class="z-outfit-old-price ms-1" v-if="card.totalPrice && card.totalPrice > card.comboPrice">{{ formatPrice(card.totalPrice) }}</span>
                            </div>
                            <span class="badge bg-danger-subtle text-danger font-monospace" v-if="card.discountPercent">-{{ card.discountPercent }}% Set</span>
                          </div>
                          <button type="button" class="btn btn-sm btn-danger w-100 mt-2 z-outfit-add-btn" @click="addOutfitToCart(card)">
                            <i class="bi bi-bag-check-fill me-1"></i> Thêm cả Set vào Giỏ hàng
                          </button>
                        </div>
                      </div>
                    </template>

                    <!-- THẺ SẢN PHẨM -->
                    <template v-else-if="card.type === 'product'">
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

        <!-- Input ẩn cho Visual Search Tải Ảnh -->
        <input type="file" ref="fileInputRef" accept="image/*" class="d-none" @change="handleImageUpload" />

        <!-- Form nhập tin nhắn -->
        <form v-if="!humanMode || supportStatus !== 'CLOSED'" class="z-ai-form" @submit.prevent="send">
          <!-- Nút Tải ảnh / Visual Search -->
          <button
            type="button"
            class="z-ai-media-btn"
            title="Tìm sản phẩm bằng hình ảnh (AI Visual Search)"
            :disabled="loading"
            @click="triggerImageUpload"
          >
            <i class="bi bi-camera-fill"></i>
          </button>

          <!-- Nút Nói / Voice Input -->
          <button
            type="button"
            class="z-ai-mic-btn"
            :class="{ listening: isListening }"
            :title="isListening ? 'Đang lắng nghe...' : 'Nói với AI bằng giọng nói'"
            :disabled="loading"
            @click="toggleVoiceInput"
          >
            <i class="bi" :class="isListening ? 'bi-mic-fill text-danger' : 'bi-mic'"></i>
          </button>

          <input
            v-model="draft"
            ref="inputRef"
            class="z-ai-input"
            :placeholder="humanMode ? 'Nhắn cho nhân viên đang trực...' : (activeTab === 'stylist' ? 'Nhập dịp đi chơi, phong cách...' : (activeTab === 'size' ? 'Hỏi chi tiết về size vóc dáng...' : 'Hỏi về mẫu áo, váy, size, voucher...'))"
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
const activeTab = ref('assistant') // 'assistant' | 'stylist' | 'size'
const draft = ref('')
const bodyRef = ref(null)
const inputRef = ref(null)
const fileInputRef = ref(null)

const sizeHeight = ref(160)
const sizeWeight = ref(50)

const isListening = ref(false)
let recognition = null

const humanMode = ref(false)
const supportToken = ref('')
const supportStatus = ref('')
const supportEmployee = ref('')
const humanMessages = ref([])
let supportPoll = null

const hasAskedAi = ref(false)

const messages = ref([
  {
    id: 1,
    role: 'assistant',
    content: '✨ **Xin chào! Mình là Zestia AI Fashion Assistant & Stylist 2.0.**\n\nMình có thể giúp bạn chọn trang phục tôn dáng, phối đồ Outfit trọn bộ theo dịp, tìm đồ qua hình ảnh, hoặc tính Size chuẩn vóc dáng!',
    cards: []
  }
])

const displayMessages = computed(() => humanMode.value ? humanMessages.value : messages.value)

const assistantChips = [
  { label: '🔥 Mẫu hot nhất', question: 'Mẫu sản phẩm nào đang bán chạy nhất?', icon: 'bi-fire' },
  { label: '🎟️ Mã giảm giá', question: 'Có mã giảm giá hoặc voucher nào hôm nay?', icon: 'bi-ticket-perforated' },
  { label: '📐 Tư vấn chọn size', question: 'Tư vấn cho mình cách chọn size chuẩn', icon: 'bi-ruler' },
  { label: '🚚 Tra cứu đơn hàng', question: 'Cho mình tra cứu trạng thái đơn hàng', icon: 'bi-truck' }
]

const stylistChips = [
  { label: '🥂 Outfit đi tiệc', question: 'Gợi ý cho mình set đồ đi tiệc sang trọng tôn dáng', icon: 'bi-balloon-heart' },
  { label: '💼 Set đồ công sở', question: 'Tư vấn outfit công sở thanh lịch lịch sự', icon: 'bi-briefcase' },
  { label: '☕ Cafe dạo phố', question: 'Gợi ý set đồ dạo phố nhẹ nhàng cá tính', icon: 'bi-cup-hot' },
  { label: '🏖️ Outfit du lịch', question: 'Gợi ý set đồ du lịch thoáng mát trẻ trung', icon: 'bi-sun' }
]

const sizeChips = [
  { label: '✨ Cao 1m55 - 45kg', question: 'Mình cao 1m55 nặng 45kg mặc size gì vừa vặn?', icon: 'bi-person' },
  { label: '✨ Cao 1m62 - 52kg', question: 'Mình cao 1m62 nặng 52kg mặc size gì?', icon: 'bi-person' },
  { label: '✨ Cao 1m68 - 60kg', question: 'Mình cao 1m68 nặng 60kg chọn size nào chuẩn?', icon: 'bi-person' }
]

const currentChips = computed(() => {
  if (activeTab.value === 'stylist') return stylistChips
  if (activeTab.value === 'size') return sizeChips
  return assistantChips
})

const calculatedSizeInfo = computed(() => {
  const h = sizeHeight.value
  const w = sizeWeight.value
  let size = 'M'
  let score = 96
  if (h < 155 && w < 48) { size = 'S'; score = 98; }
  else if (h >= 155 && h <= 165 && w >= 48 && w <= 56) { size = 'M'; score = 97; }
  else if (w > 56 && w <= 65) { size = 'L'; score = 95; }
  else if (w > 65) { size = 'XL'; score = 92; }
  return { size, score }
})

const supportStatusText = computed(() => {
  if (supportStatus.value === 'PENDING') return 'Đang chờ nhân viên phản hồi...'
  if (supportStatus.value === 'CLAIMED') return `Đang trò chuyện với NV ${supportEmployee.value || ''}`
  if (supportStatus.value === 'CLOSED') return 'Phiên hỗ trợ đã kết thúc'
  return 'Hỗ trợ trực tiếp'
})

function playChimeSound() {
  if (!soundEnabled.value) return
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'sine'
    osc.frequency.setValueAtTime(587.33, ctx.currentTime) // D5
    osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.15) // A5
    gain.gain.setValueAtTime(0.08, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.2)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.2)
  } catch (e) {
    // Ignore audio play errors
  }
}

function speakText(text) {
  if (!('speechSynthesis' in window)) {
    showToast('Trình duyệt không hỗ trợ phát âm thanh văn bản.')
    return
  }
  window.speechSynthesis.cancel()
  const clean = text.replace(/[*_#`~]/g, '')
  const utterance = new SpeechSynthesisUtterance(clean)
  utterance.lang = 'vi-VN'
  utterance.rate = 1.0
  window.speechSynthesis.speak(utterance)
}

function toggleVoiceInput() {
  if (isListening.value) {
    if (recognition) recognition.stop()
    isListening.value = false
    return
  }

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    showToast('Trình duyệt không hỗ trợ nhận diện giọng nói Voice Input.')
    return
  }

  recognition = new SpeechRecognition()
  recognition.lang = 'vi-VN'
  recognition.continuous = false
  recognition.interimResults = false

  recognition.onstart = () => {
    isListening.value = true
    showToast('Đang lắng nghe giọng nói...')
  }

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript
    if (transcript) {
      draft.value = transcript
    }
    isListening.value = false
  }

  recognition.onerror = () => {
    isListening.value = false
    showToast('Không nghe rõ, vui lòng thử lại!')
  }

  recognition.onend = () => {
    isListening.value = false
  }

  recognition.start()
}

function triggerImageUpload() {
  fileInputRef.value?.click()
}

async function handleImageUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  e.target.value = ''

  if (!file.type.startsWith('image/')) {
    showToast('Vui lòng chọn tệp hình ảnh valid.')
    return
  }

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: '📷 [Đã gửi 1 hình ảnh để AI phân tích trang phục]'
  })
  scrollToBottom()
  loading.value = true

  try {
    const res = await api().visualSearch(file)
    playChimeSound()
    const found = res.data || []
    let reply = `🔍 **AI Visual Search đã phân tích hình ảnh!**\n\nMình tìm thấy ${found.length} mẫu trang phục phong cách tương tự tại Zestia:`
    if (!found.length) {
      reply = '🔍 **AI Visual Search**: Không tìm thấy sản phẩm hoàn toàn giống hệt, nhưng đây là các mẫu gợi ý thời trang mới nhất:'
    }
    const cards = (found.length ? found : MOCK_PRODUCTS.slice(0, 3)).map(p => ({
      type: 'product',
      id: p.id,
      name: p.tenSanPham || p.tenVay || p.name,
      price: p.giaBan || p.price,
      image: p.anhUrl || p.image,
      category: p.loaiSanPham || p.category || 'Thời trang'
    }))

    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: reply,
      cards
    })
  } catch (err) {
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: 'Không thể phân tích ảnh lúc này. Bạn có thể tả bằng lời mẫu áo/váy bạn đang muốn tìm nhé!',
      cards: []
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function askAiForSize() {
  const q = `Mình cao ${sizeHeight.value}cm nặng ${sizeWeight.value}kg. Cho mình xin tư vấn chọn size và sản phẩm vừa vặn nhé!`
  sendSuggestedQuestion(q)
}

function toggleOpen() {
  open.value = !open.value
  if (open.value) {
    scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  })
}

function onCardImgError(e) {
  e.target.src = '/images/products/shirt1.jpg'
}

function goToProduct(id) {
  if (!id) return
  open.value = false
  router.push(`/products/${id}`)
}

function quickAddToCart(card) {
  addItem({
    id: card.id,
    name: card.name,
    price: card.price,
    image: card.image
  })
  showToast(`Đã thêm "${card.name}" vào giỏ hàng!`)
}

function addOutfitToCart(outfitCard) {
  if (!outfitCard || !outfitCard.items || !outfitCard.items.length) return
  outfitCard.items.forEach(item => {
    addItem({
      id: item.id,
      name: item.name,
      price: item.price,
      image: item.image
    })
  })
  showToast(`🎉 Đã thêm trọn bộ Set Outfit (${outfitCard.items.length} món) vào giỏ hàng!`)
}

function applyVoucherCode(code) {
  if (!code) return
  navigator.clipboard.writeText(code)
  showToast(`Đã sao chép mã "${code}"! Nhập mã ở bước thanh toán nhé.`)
}

function copyText(text) {
  if (!text) return
  navigator.clipboard.writeText(text.replace(/[*_#`~]/g, ''))
  showToast('Đã sao chép câu trả lời AI!')
}

function clearHistory() {
  messages.value = [
    {
      id: Date.now(),
      role: 'assistant',
      content: '✨ **Lịch sử chat đã được làm mới.**\n\nMình có thể giúp gì cho bạn hôm nay?',
      cards: []
    }
  ]
  hasAskedAi.value = false
}

function sendSuggestedQuestion(questionText) {
  draft.value = questionText
  send()
}

async function send() {
  const text = draft.value.trim()
  if (!text || loading.value) return

  draft.value = ''
  hasAskedAi.value = true

  if (humanMode.value) {
    await sendHumanMessage(text)
    return
  }

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: text
  })
  scrollToBottom()
  loading.value = true

  try {
    const historyPayload = messages.value.slice(-6).map(m => ({
      role: m.role,
      content: m.content
    }))

    const res = await api().sendAiChat({
      message: text,
      history: historyPayload,
      mode: activeTab.value
    })

    playChimeSound()

    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: res.reply || 'Zestia AI đã ghi nhận.',
      cards: res.cards || []
    })
  } catch (err) {
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: 'Rất tiếc, AI tạm thời không phản hồi. Bạn có thể nhấn nút "Gặp NV CSKH" bên dưới để trò chuyện trực tiếp nhé!',
      cards: []
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function requestEmployee() {
  handoffLoading.value = true
  try {
    const firstMsg = messages.value.find(m => m.role === 'user')?.content || 'Cần hỗ trợ tư vấn trực tiếp'
    const res = await api().requestHumanSupport(firstMsg)
    supportToken.value = res.token
    supportStatus.value = res.status
    supportEmployee.value = res.employeeName || ''
    humanMessages.value = (res.messages || []).map(m => ({
      id: m.id,
      role: m.senderRole === 'CUSTOMER' ? 'user' : 'assistant',
      senderName: m.senderName,
      content: m.content
    }))
    humanMode.value = true
    startSupportPolling()
    showToast('Đã tạo kết nối với nhân viên CSKH!')
  } catch (err) {
    showToast(err.error || 'Không thể kết nối nhân viên lúc này.')
  } finally {
    handoffLoading.value = false
  }
}

async function sendHumanMessage(text) {
  if (!supportToken.value) return
  try {
    const res = await api().sendCustomerSupportMessage(supportToken.value, text)
    humanMessages.value = (res.messages || []).map(m => ({
      id: m.id,
      role: m.senderRole === 'CUSTOMER' ? 'user' : 'assistant',
      senderName: m.senderName,
      content: m.content
    }))
    scrollToBottom()
  } catch (err) {
    showToast('Gửi tin nhắn thất bại.')
  }
}

function startSupportPolling() {
  stopSupportPolling()
  supportPoll = setInterval(async () => {
    if (!supportToken.value || !humanMode.value) return
    try {
      const data = await api().getCustomerSupportChat(supportToken.value)
      supportStatus.value = data.status
      supportEmployee.value = data.employeeName || ''
      humanMessages.value = (data.messages || []).map(m => ({
        id: m.id,
        role: m.senderRole === 'CUSTOMER' ? 'user' : 'assistant',
        senderName: m.senderName,
        content: m.content
      }))
    } catch {
      // Ignore poll error
    }
  }, 4000)
}

function stopSupportPolling() {
  if (supportPoll) {
    clearInterval(supportPoll)
    supportPoll = null
  }
}

function backToAi() {
  humanMode.value = false
  supportToken.value = ''
  stopSupportPolling()
}

function formatMarkdown(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  html = html.replace(/`(.*?)`/g, '<code>$1</code>')
  html = html.replace(/\n/g, '<br>')
  return html
}

onBeforeUnmount(() => {
  stopSupportPolling()
  if (recognition) recognition.stop()
})
</script>

<style scoped>
.z-customer-ai {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  font-family: var(--z-font-base, system-ui, -apple-system, sans-serif);
}

.z-ai-toggle-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 32px;
  background: linear-gradient(135deg, #1A1A1A 0%, #333333 100%);
  color: #fff;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.z-ai-toggle-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.z-ai-icon-stars {
  font-size: 20px;
  color: #FFD700;
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

/* KHUNG CHAT AI PANEL GLASSMORPHISM */
.z-ai-window {
  width: min(420px, calc(100vw - 28px));
  height: min(640px, calc(100vh - 100px));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* HEADER */
.z-ai-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, #1A1A1A 0%, #2D2D2D 100%);
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
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}

/* SIZE CALCULATOR PANEL */
.z-size-calculator-panel {
  background: #FFF9F9;
  padding: 12px 16px;
  border-bottom: 1px solid #FFEBEB;
}
.z-size-calc-header {
  font-size: 12px;
  color: #333;
  display: flex;
  align-items: center;
}
.z-calc-label {
  font-size: 11px;
  color: #555;
}
.custom-range {
  height: 4px;
}
.z-size-result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #FFD6D6;
}
.z-size-badge-val {
  font-size: 12px;
}
.z-size-tag {
  background: #D4564E;
  color: #fff;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 13px;
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
  max-width: 94%;
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
  padding: 12px 14px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.5;
}

.z-ai-msg-group.assistant .z-ai-msg-bubble {
  background: #F4F4F6;
  color: #1A1A1A;
  border-top-left-radius: 4px;
}

.z-ai-msg-group.user .z-ai-msg-bubble {
  background: linear-gradient(135deg, #1A1A1A, #333333);
  color: #fff;
  border-top-right-radius: 4px;
}

.z-ai-msg-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.z-ai-action-sub-btn {
  border: none;
  background: rgba(0,0,0,0.05);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 11px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.z-ai-action-sub-btn:hover { background: rgba(0,0,0,0.1); color: #000; }

/* CARDS SCROLL */
.z-ai-cards-container {
  margin-top: 10px;
  width: 100%;
}
.z-ai-cards-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 6px;
}

/* THẺ OUTFIT SET LOOKBOOK */
.z-outfit-card {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #EAEAEA;
  border-radius: 14px;
  padding: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.z-outfit-badge {
  font-size: 10px;
  font-weight: 800;
  color: #D4564E;
  letter-spacing: 0.5px;
}
.z-outfit-title {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #1A1A1A;
}
.z-outfit-sub {
  font-size: 11px;
  color: #777;
}
.z-outfit-items-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin: 10px 0;
}
.z-outfit-mini-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #F9F9FB;
  padding: 6px;
  border-radius: 8px;
  cursor: pointer;
}
.z-outfit-mini-item img {
  width: 36px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}
.z-outfit-mini-name {
  font-size: 10px;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.z-outfit-mini-price {
  font-size: 10px;
  color: #D4564E;
  font-weight: 700;
}
.z-outfit-combo-price {
  font-size: 14px;
  font-weight: 800;
  color: #D4564E;
}
.z-outfit-old-price {
  font-size: 11px;
  text-decoration: line-through;
  color: #999;
}

/* THẺ SẢN PHẨM SINGLE */
.z-ai-card-item {
  width: 170px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #EAEAEA;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.z-card-img-wrapper {
  position: relative;
  aspect-ratio: 3/4;
  cursor: pointer;
  overflow: hidden;
}
.z-card-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.z-card-img-wrapper:hover img {
  transform: scale(1.05);
}
.z-card-cat-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 4px;
}
.z-card-info {
  padding: 8px 10px;
}
.z-card-name {
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}
.z-card-price {
  font-size: 13px;
  font-weight: 700;
  color: #D4564E;
}
.z-card-stock {
  font-size: 10px;
  color: #888;
}

/* THẺ VOUCHER */
.z-voucher-card {
  width: 220px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #FFF9F9, #FFEBEB);
  border: 1px dashed #D4564E;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.z-voucher-code {
  font-size: 14px;
  color: #D4564E;
}
.z-voucher-desc {
  font-size: 11px;
  color: #555;
}
.z-voucher-apply-btn {
  align-self: flex-end;
  border: none;
  background: #D4564E;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

/* TYPING INDICATOR */
.z-ai-typing-bubble {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}
.z-dot {
  width: 6px;
  height: 6px;
  background: #888;
  border-radius: 50%;
  animation: z-typing 1.4s infinite ease-in-out;
}
.z-dot:nth-child(1) { animation-delay: 0s; }
.z-dot:nth-child(2) { animation-delay: 0.2s; }
.z-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes z-typing {
  0%, 100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-4px); opacity: 1; }
}

/* QUICK CHIPS */
.z-ai-quick-section {
  padding: 6px 12px;
  background: #FAFAFA;
  border-top: 1px solid #F0F0F0;
}
.z-ai-chips-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 4px 0;
}
.z-ai-chip-btn {
  flex-shrink: 0;
  border: 1px solid #E0E0E0;
  background: #fff;
  color: #444;
  font-size: 11px;
  font-weight: 600;
  padding: 6px 10px;
  border-radius: 16px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.z-ai-chip-btn:hover {
  background: #1A1A1A;
  color: #fff;
  border-color: #1A1A1A;
}

/* HANDOFF BAR */
.z-ai-handoff-bar {
  padding: 8px 14px;
  background: #FFF5F5;
  border-top: 1px solid #FFEBEB;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: #666;
}
.z-handoff-btn {
  border: none;
  background: #1A1A1A;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

/* FORM INPUT */
.z-ai-form {
  padding: 10px 14px;
  background: #fff;
  border-top: 1px solid #EAEAEA;
  display: flex;
  align-items: center;
  gap: 6px;
}
.z-ai-media-btn, .z-ai-mic-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: #F4F4F6;
  color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}
.z-ai-media-btn:hover, .z-ai-mic-btn:hover {
  background: #EAEAEA;
  color: #1A1A1A;
}
.z-ai-mic-btn.listening {
  background: #FFEBEB;
  animation: z-mic-pulse 1.2s infinite;
}
@keyframes z-mic-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.z-ai-input {
  flex: 1;
  border: 1px solid #E5E5E5;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}
.z-ai-input:focus {
  border-color: #1A1A1A;
}

.z-ai-send-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #1A1A1A, #333333);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
}
.z-ai-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* TRANSITIONS */
.z-slide-up-enter-active, .z-slide-up-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.z-slide-up-enter-from, .z-slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}
</style>
