<template>
  <div class="z-customer-ai">
    <button
      v-if="!open"
      type="button"
      class="z-ai-toggle-btn"
      title="Trợ lý mua sắm Zestia"
      aria-label="Mở trợ lý mua sắm Zestia"
      @click="toggleOpen"
    >
      <i class="bi bi-chat-dots" aria-hidden="true"></i>
      <span class="z-ai-badge-text">Tư vấn cùng Zestia</span>
    </button>

    <transition name="z-slide-up">
      <section v-if="open" class="z-ai-window" aria-label="Trợ lý mua sắm Zestia">
        <header class="z-ai-header">
          <div class="z-ai-header-info">
            <div class="z-ai-avatar">
              <img src="/images/brand/zestia-mark.png" alt="" aria-hidden="true">
              <span class="z-ai-status-dot"></span>
            </div>
            <div>
              <div class="d-flex align-items-center gap-2">
                <strong class="z-ai-title">
                  {{ humanMode ? 'Nhân viên Zestia' : (activeTab === 'stylist' ? 'Phối đồ Zestia' : (activeTab === 'size' ? 'Tư vấn chọn size' : 'Trợ lý Zestia')) }}
                </strong>
                <span v-if="!humanMode" class="z-ai-mode-badge">Trực tuyến</span>
              </div>
              <span class="z-ai-subtitle" v-if="humanMode">{{ supportStatusText }}</span>
              <span class="z-ai-subtitle" v-else-if="activeTab === 'stylist'">Trang phục theo dịp và phong cách</span>
              <span class="z-ai-subtitle" v-else-if="activeTab === 'size'">Tham khảo theo số đo và từng phom</span>
              <span class="z-ai-subtitle" v-else>Sản phẩm, voucher và đơn hàng</span>
            </div>
          </div>
          <div class="z-ai-header-actions">
            <button type="button" class="z-ai-icon-btn" :title="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'" :aria-label="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'" @click="soundEnabled = !soundEnabled">
              <i class="bi" :class="soundEnabled ? 'bi-volume-up' : 'bi-volume-mute'"></i>
            </button>
            <button v-if="!humanMode" type="button" class="z-ai-icon-btn" title="Xóa lịch sử chat" aria-label="Xóa lịch sử chat" @click="clearHistory">
              <i class="bi bi-trash"></i>
            </button>
            <button type="button" class="z-ai-icon-btn close-btn" title="Đóng" aria-label="Đóng trợ lý" @click="open = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </header>

        <div class="z-ai-tabs" v-if="!humanMode">
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'assistant' }" @click="activeTab = 'assistant'">
            <i class="bi bi-chat-left-dots"></i> Tư vấn
          </button>
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'stylist' }" @click="activeTab = 'stylist'">
            <i class="bi bi-bag-heart"></i> Phối đồ
          </button>
          <button type="button" class="z-ai-tab" :class="{ active: activeTab === 'size' }" @click="activeTab = 'size'">
            <i class="bi bi-rulers"></i> Chọn size
          </button>
        </div>

        <!-- BỘ TÍNH SIZE CHUẨN KHI Ở TAB SIZE -->
        <div v-if="activeTab === 'size' && !humanMode" class="z-size-calculator-panel">
          <div class="z-size-calc-header">
            <i class="bi bi-rulers" aria-hidden="true"></i>
            <strong>Tham khảo size theo vóc dáng</strong>
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
              Size ước tính ban đầu: <span class="z-size-tag">{{ calculatedSizeInfo.size }}</span>
              <small class="ms-2 text-muted">Cần đối chiếu từng mẫu</small>
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
              <i class="bi bi-chat-quote"></i>
            </div>
            
            <div class="z-ai-msg-content">
              <div class="z-ai-msg-bubble">
                <small v-if="humanMode && msg.senderName" class="z-ai-sender-name">{{ msg.senderName }}</small>
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
                          <span class="z-outfit-badge"><i class="bi bi-bag-heart me-1"></i> Gợi ý phối đồ</span>
                          <strong class="z-outfit-title">{{ card.title }}</strong>
                          <span v-if="card.occasion" class="z-outfit-sub">{{ card.occasion }}</span>
                        </div>
                        <div class="z-outfit-items-grid">
                          <div v-for="(item, iIdx) in card.items" :key="iIdx" class="z-outfit-mini-item" @click="goToProduct(item.id)">
                            <img :src="item.image || '/images/products/catalog-v2/sp001_main.webp'" :alt="item.name" @error="onCardImgError" />
                            <div class="z-outfit-mini-info">
                              <span class="z-outfit-mini-name">{{ item.name }}</span>
                              <ProductPrice
                                class="z-outfit-mini-price"
                                :price="item.price"
                                :original-price="item.originalPrice"
                                size="compact"
                              />
                            </div>
                          </div>
                        </div>
                        <div class="z-outfit-footer">
                          <div class="d-flex align-items-center justify-content-between">
                            <div class="z-outfit-pricing">
                              <ProductPrice
                                class="z-outfit-combo-price"
                                :price="card.totalPrice"
                                :original-price="card.totalOriginalPrice"
                                size="compact"
                              />
                            </div>
                            <span class="text-muted" style="font-size:11px">Tổng giá hiện tại</span>
                          </div>
                          <button type="button" class="lm-btn-primary w-100 mt-2 z-outfit-add-btn" @click="addOutfitToCart(card)">
                            <i class="bi bi-bag-check me-1"></i> Thêm cả bộ vào giỏ
                          </button>
                        </div>
                      </div>
                    </template>

                    <!-- THẺ SẢN PHẨM -->
                    <template v-else-if="card.type === 'product'">
                      <div class="z-card-img-wrapper" @click="goToProduct(card.id)">
                        <img :src="card.image || '/images/products/catalog-v2/sp001_main.webp'" :alt="card.name" @error="onCardImgError" />
                        <span v-if="card.category" class="z-card-cat-badge">{{ card.category }}</span>
                      </div>
                      <div class="z-card-info">
                        <div class="z-card-name" :title="card.name" @click="goToProduct(card.id)">{{ card.name }}</div>
                        <div class="d-flex align-items-center justify-content-between mt-1">
                          <ProductPrice
                            class="z-card-price"
                            :price="card.price"
                            :original-price="card.originalPrice"
                            size="compact"
                          />
                          <span v-if="card.stock" class="z-card-stock">Còn {{ card.stock }}</span>
                        </div>
                        <div class="d-flex gap-1 mt-2">
                          <button type="button" class="lm-btn-primary z-card-view-btn flex-grow-1" @click="goToProduct(card.id)">
                            <i class="bi bi-eye"></i> Xem ngay
                          </button>
                          <button type="button" class="z-icon-btn z-card-cart-btn" title="Thêm vào giỏ" aria-label="Thêm sản phẩm vào giỏ" @click="quickAddToCart(card)">
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
            <div class="z-ai-avatar-mini"><i class="bi bi-chat-quote"></i></div>
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
            <i class="bi bi-headset"></i> {{ handoffLoading ? 'Đang kết nối...' : 'Chat với nhân viên' }}
          </button>
        </div>

        <div v-if="humanMode && supportStatus === 'CLOSED'" class="z-ai-handoff-bar">
          <span>Phiên hỗ trợ nhân viên đã kết thúc.</span>
          <button type="button" class="z-handoff-btn" @click="backToAi">
            <i class="bi bi-chat-dots"></i> Quay lại trợ lý
          </button>
        </div>

        <!-- Input ảnh dùng để tìm biến thể có màu gần nhất. -->
        <input type="file" ref="fileInputRef" accept="image/*" class="d-none" @change="handleImageUpload" />

        <!-- Form nhập tin nhắn -->
        <form v-if="!humanMode || supportStatus !== 'CLOSED'" class="z-ai-form" @submit.prevent="send">
          <!-- Tìm sản phẩm theo màu từ ảnh. -->
          <button
            type="button"
            class="z-ai-media-btn"
            title="Tìm sản phẩm theo màu từ ảnh"
            aria-label="Tìm sản phẩm theo màu từ ảnh"
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
            :title="isListening ? 'Đang lắng nghe...' : 'Nhập câu hỏi bằng giọng nói'"
            :aria-label="isListening ? 'Dừng ghi âm' : 'Nhập câu hỏi bằng giọng nói'"
            :disabled="loading"
            @click="toggleVoiceInput"
          >
            <i class="bi" :class="isListening ? 'bi-mic-fill text-danger' : 'bi-mic'"></i>
          </button>

          <input
            v-model="draft"
            ref="inputRef"
            class="z-ai-input"
            :placeholder="humanMode ? 'Nhắn cho nhân viên đang trực...' : (activeTab === 'stylist' ? 'Bạn cần trang phục cho dịp nào?' : (activeTab === 'size' ? 'Nhập chiều cao, cân nặng hoặc số đo...' : 'Bạn đang cần Zestia tư vấn gì?'))"
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
import ProductPrice from '@/components/ui/ProductPrice.vue'
import { useCart } from '@/composables/useCart'
import { useToast } from '@/composables/useToast'

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
const SUPPORT_SESSION_KEY = 'zestia_support_token'

const hasAskedAi = ref(false)

const messages = ref([
  {
    id: 1,
    role: 'assistant',
    content: '**Xin chào! Mình là trợ lý mua sắm Zestia.**\n\nMình có thể giúp bạn tìm sản phẩm đang bán, phối trang phục theo dịp, tìm màu gần với ảnh mẫu và tham khảo size theo dữ liệu của từng sản phẩm.',
    cards: []
  }
])

const displayMessages = computed(() => humanMode.value ? humanMessages.value : messages.value)

const assistantChips = [
  { label: 'Mẫu bán chạy', question: 'Mẫu sản phẩm nào đang bán chạy nhất?', icon: 'bi-fire' },
  { label: 'Voucher hiện có', question: 'Có mã giảm giá hoặc voucher nào hôm nay?', icon: 'bi-ticket-perforated' },
  { label: 'Tư vấn chọn size', question: 'Tư vấn cho mình cách chọn size chuẩn', icon: 'bi-rulers' },
  { label: 'Tra cứu đơn hàng', question: 'Cho mình tra cứu trạng thái đơn hàng', icon: 'bi-truck' }
]

const stylistChips = [
  { label: 'Trang phục đi tiệc', question: 'Gợi ý cho mình set đồ đi tiệc sang trọng tôn dáng', icon: 'bi-balloon-heart' },
  { label: 'Trang phục công sở', question: 'Tư vấn outfit công sở thanh lịch lịch sự', icon: 'bi-briefcase' },
  { label: 'Dạo phố cuối tuần', question: 'Gợi ý set đồ dạo phố nhẹ nhàng cá tính', icon: 'bi-cup-hot' },
  { label: 'Trang phục du lịch', question: 'Gợi ý set đồ du lịch thoáng mát trẻ trung', icon: 'bi-sun' }
]

const sizeChips = [
  { label: '1m55 - 45 kg', question: 'Mình cao 1m55 nặng 45kg mặc size gì vừa vặn?', icon: 'bi-person' },
  { label: '1m62 - 52 kg', question: 'Mình cao 1m62 nặng 52kg mặc size gì?', icon: 'bi-person' },
  { label: '1m68 - 60 kg', question: 'Mình cao 1m68 nặng 60kg chọn size nào chuẩn?', icon: 'bi-person' }
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
  if (h < 155 && w < 48) size = 'S'
  else if (h >= 155 && h <= 165 && w >= 48 && w <= 56) size = 'M'
  else if (w > 56 && w <= 65) size = 'L'
  else if (w > 65) size = 'XL'
  return { size }
})

const supportStatusText = computed(() => {
  if (supportStatus.value === 'WAITING') return 'Đang chờ nhân viên phản hồi...'
  if (supportStatus.value === 'ACTIVE') return `Đang trò chuyện với ${supportEmployee.value || 'nhân viên Zestia'}`
  if (supportStatus.value === 'CLOSED') return 'Phiên hỗ trợ đã kết thúc'
  return 'Hỗ trợ trực tiếp'
})

function mapSupportMessages(items) {
  return (items || []).map(message => ({
    id: message.id,
    role: (message.senderType || message.senderRole) === 'CUSTOMER' ? 'user' : 'assistant',
    senderName: message.senderName,
    content: message.content
  }))
}

function applySupportSnapshot(data) {
  supportStatus.value = data?.status || ''
  supportEmployee.value = data?.employeeName || ''
  humanMessages.value = mapSupportMessages(data?.messages)
  if (supportStatus.value === 'CLOSED') stopSupportPolling()
}

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
    showToast('Vui lòng chọn một tệp hình ảnh hợp lệ.')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showToast('Ảnh không được vượt quá 5 MB.')
    return
  }

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: '[Đã gửi một hình ảnh để tìm sản phẩm theo màu sắc]'
  })
  scrollToBottom()
  loading.value = true

  try {
    const res = await api().visualSearch(file)
    playChimeSound()
    const found = Array.isArray(res?.results) ? res.results : []
    let reply = `**Đã phân tích màu nổi bật trong ảnh.**\n\nMình tìm thấy ${found.length} sản phẩm có màu gần nhất tại Zestia:`
    if (!found.length) {
      reply = 'Hiện chưa có sản phẩm đang bán với màu đủ gần ảnh bạn gửi.'
    }
    const cards = found.map(p => ({
      type: 'product',
      id: p.id,
      variantId: p.variantId,
      name: p.tenSanPham || p.name,
      price: p.giaCuoi || p.price,
      originalPrice: p.giaGoc || p.originalPrice,
      image: p.anhChinh || p.image,
      category: p.danhMuc || p.category || 'Thời trang'
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
    window.dispatchEvent(new Event('zestia-close-notifications'))
    scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
}

function closeChatWindow() {
  open.value = false
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  })
}

function onCardImgError(e) {
  e.target.src = '/images/products/catalog-v2/sp001_main.webp'
}

function goToProduct(id) {
  if (!id) return
  open.value = false
  router.push(`/product/${id}`)
}

function quickAddToCart(card) {
  if (!card.variantId) {
    open.value = false
    router.push(`/product/${card.id}`)
    showToast('Vui lòng chọn màu và kích thước trước khi thêm vào giỏ.')
    return
  }
  addItem({
    id: card.id,
    productId: card.id,
    variantId: card.variantId,
    name: card.name,
    price: card.price,
    originalPrice: card.originalPrice,
    promotionActive: card.promotionActive,
    campaign: card.campaign,
    image: card.image,
    color: card.color,
    size: card.size,
    variant: [card.color, card.size ? `Size ${card.size}` : ''].filter(Boolean).join(' · '),
    maxQty: Number(card.stock || 0)
  })
  showToast(`Đã thêm "${card.name}" vào giỏ hàng!`)
}

function addOutfitToCart(outfitCard) {
  if (!outfitCard || !outfitCard.items || !outfitCard.items.length) return
  if (outfitCard.items.some(item => !item.variantId)) {
    showToast('Một sản phẩm trong gợi ý chưa có biến thể còn hàng. Vui lòng mở sản phẩm để chọn lại.')
    return
  }
  outfitCard.items.forEach(item => {
    addItem({
      id: item.id,
      productId: item.id,
      variantId: item.variantId,
      name: item.name,
      price: item.price,
      originalPrice: item.originalPrice,
      promotionActive: item.promotionActive,
      campaign: item.campaign,
      image: item.image,
      color: item.color,
      size: item.size,
      variant: [item.color, item.size ? `Size ${item.size}` : ''].filter(Boolean).join(' · '),
      maxQty: Number(item.stock || 0)
    })
  })
  showToast(`Đã thêm trọn bộ phối gồm ${outfitCard.items.length} món vào giỏ hàng.`)
}

function applyVoucherCode(code) {
  if (!code) return
  navigator.clipboard.writeText(code)
  showToast(`Đã sao chép mã "${code}"! Nhập mã ở bước thanh toán nhé.`)
}

function copyText(text) {
  if (!text) return
  navigator.clipboard.writeText(text.replace(/[*_#`~]/g, ''))
  showToast('Đã sao chép câu trả lời')
}

function clearHistory() {
  messages.value = [
    {
      id: Date.now(),
      role: 'assistant',
      content: '**Lịch sử trò chuyện đã được làm mới.**\n\nMình có thể giúp gì cho bạn hôm nay?',
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
      content: res.reply || 'Zestia đã ghi nhận câu hỏi của bạn.',
      cards: res.cards || []
    })
  } catch (err) {
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: 'Trợ lý đang tạm gián đoạn. Bạn có thể chọn "Chat với nhân viên" bên dưới để được hỗ trợ trực tiếp.',
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
    sessionStorage.setItem(SUPPORT_SESSION_KEY, res.token)
    applySupportSnapshot(res)
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
    applySupportSnapshot(res)
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
      applySupportSnapshot(data)
      scrollToBottom()
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
  supportStatus.value = ''
  supportEmployee.value = ''
  humanMessages.value = []
  sessionStorage.removeItem(SUPPORT_SESSION_KEY)
  stopSupportPolling()
}

async function restoreSupportSession() {
  const token = sessionStorage.getItem(SUPPORT_SESSION_KEY)
  if (!token) return
  try {
    const data = await api().getCustomerSupportChat(token)
    supportToken.value = token
    humanMode.value = true
    applySupportSnapshot(data)
    if (data.status !== 'CLOSED') startSupportPolling()
  } catch {
    sessionStorage.removeItem(SUPPORT_SESSION_KEY)
  }
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

onMounted(() => {
  restoreSupportSession()
  window.addEventListener('zestia-close-customer-chat', closeChatWindow)
})

onBeforeUnmount(() => {
  window.removeEventListener('zestia-close-customer-chat', closeChatWindow)
  stopSupportPolling()
  if (recognition) recognition.stop()
})
</script>

<style scoped>
.z-customer-ai {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1040;
  font-family: var(--z-font-body);
}

.z-ai-toggle-btn {
  min-height: 46px;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 0 16px;
  border: 1px solid var(--z-dark);
  border-radius: var(--z-radius);
  background: var(--z-dark);
  color: var(--z-white);
  box-shadow: 0 12px 30px rgba(27, 27, 31, 0.2);
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-ai-toggle-btn i { font-size: 17px; }
.z-ai-toggle-btn:hover {
  border-color: var(--z-accent);
  background: var(--z-accent);
  color: var(--z-white);
  transform: translateY(-2px);
  box-shadow: 0 15px 32px rgba(27, 27, 31, 0.24);
}
.z-ai-toggle-btn:active { transform: translateY(0); }
.z-ai-badge-text { white-space: nowrap; }

.z-ai-window {
  width: min(420px, calc(100vw - 32px));
  height: min(650px, calc(100dvh - 96px));
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  box-shadow: 0 24px 64px rgba(27, 27, 31, 0.24);
}

.z-ai-header {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  background: var(--z-dark);
  color: var(--z-white);
}
.z-ai-header-info {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.z-ai-header-info > div:last-child { min-width: 0; }
.z-ai-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  display: grid;
  place-items: center;
  overflow: visible;
  border-radius: var(--z-radius);
  background: var(--z-white);
}
.z-ai-avatar img { width: 30px; height: 30px; object-fit: contain; }
.z-ai-status-dot {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 10px;
  height: 10px;
  border: 2px solid var(--z-dark);
  border-radius: 50%;
  background: #22C55E;
}
.z-ai-title {
  max-width: 205px;
  display: block;
  overflow: hidden;
  color: var(--z-white);
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-ai-subtitle {
  max-width: 230px;
  display: block;
  margin-top: 3px;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-ai-mode-badge {
  padding: 3px 6px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 10px;
  font-weight: 650;
  line-height: 1;
  text-transform: uppercase;
  white-space: nowrap;
}
.z-ai-header-actions {
  display: flex;
  align-items: center;
  gap: 5px;
}
.z-ai-icon-btn {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--z-white);
  cursor: pointer;
  transition: var(--z-ease);
}
.z-ai-icon-btn:hover {
  border-color: rgba(255, 255, 255, 0.42);
  background: rgba(255, 255, 255, 0.16);
  color: var(--z-white);
}
.z-ai-icon-btn.close-btn:hover {
  border-color: var(--z-accent);
  background: var(--z-accent);
}

.z-ai-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px;
  padding: 6px;
  border-bottom: 1px solid var(--z-gray-border);
  background: var(--z-bg-alt);
}
.z-ai-tab {
  min-width: 0;
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 7px 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  color: var(--z-gray);
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-ai-tab:hover {
  border-color: var(--z-gray-border);
  background: var(--z-white);
  color: var(--z-dark);
}
.z-ai-tab.active {
  border-color: var(--z-gray-border);
  background: var(--z-white);
  color: var(--z-accent-dark);
  box-shadow: 0 2px 6px rgba(27, 27, 31, 0.06);
}

.z-size-calculator-panel {
  padding: 12px 14px;
  border-bottom: 1px solid var(--z-gray-border);
  background: var(--z-bg);
}
.z-size-calc-header {
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--z-dark);
  font-size: 12px;
}
.z-size-calc-header i { color: var(--z-accent); }
.z-calc-label {
  display: block;
  margin-bottom: 3px;
  color: var(--z-gray);
  font-size: 11px;
}
.custom-range { accent-color: var(--z-accent); }
.z-size-result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.z-size-badge-val { color: var(--z-gray); font-size: 11px; line-height: 1.5; }
.z-size-tag {
  display: inline-grid;
  min-width: 26px;
  height: 26px;
  place-items: center;
  margin-left: 4px;
  border-radius: 4px;
  background: var(--z-dark);
  color: var(--z-white);
  font-size: 12px;
  font-weight: 700;
}
.z-calc-submit-btn {
  min-height: 34px;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 11px;
  white-space: nowrap;
}

.z-ai-body {
  min-height: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 14px;
  background: var(--z-bg);
  scrollbar-width: thin;
  scrollbar-color: var(--z-gray-light) transparent;
}
.z-ai-msg-group {
  max-width: 94%;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.z-ai-msg-group.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.z-ai-avatar-mini {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  display: grid;
  place-items: center;
  border: 1px solid var(--z-accent-light);
  border-radius: 50%;
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
  font-size: 12px;
}
.z-ai-msg-content { min-width: 0; flex: 1; }
.z-ai-sender-name {
  display: block;
  margin-bottom: 4px;
  color: var(--z-gray);
  font-size: 10px;
  font-weight: 700;
  line-height: 1.3;
}
.z-ai-msg-bubble {
  padding: 10px 12px;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  color: var(--z-dark-soft);
  font-size: 12px;
  line-height: 1.65;
  overflow-wrap: anywhere;
}
.z-ai-msg-group.user .z-ai-msg-bubble {
  border-color: var(--z-dark);
  background: var(--z-dark);
  color: var(--z-white);
}
.z-ai-msg-group.user .z-ai-sender-name { color: rgba(255, 255, 255, 0.82); }
.z-ai-text { color: inherit; }
.z-ai-text :deep(p) { margin: 0 0 8px; color: inherit; }
.z-ai-text :deep(p:last-child) { margin-bottom: 0; }
.z-ai-text :deep(strong),
.z-ai-text :deep(code),
.z-ai-text :deep(a) { color: inherit; }
.z-ai-text :deep(a) { text-decoration: underline; text-underline-offset: 2px; }
.z-ai-msg-actions {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}
.z-ai-action-sub-btn {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 1px solid transparent;
  border-radius: 5px;
  background: var(--z-bg-alt);
  color: var(--z-gray);
  cursor: pointer;
  transition: var(--z-ease);
}
.z-ai-action-sub-btn:hover {
  border-color: var(--z-gray-border);
  background: var(--z-white);
  color: var(--z-accent-dark);
}

.z-ai-cards-container { margin-top: 9px; }
.z-ai-cards-scroll {
  display: grid;
  gap: 10px;
}
.z-ai-card-item {
  width: 100%;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
}
.z-outfit-card { padding: 11px; }
.z-outfit-header {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-bottom: 9px;
}
.z-outfit-badge {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 4px;
  color: var(--z-accent-dark);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}
.z-outfit-title { color: var(--z-dark); font-size: 12px; }
.z-outfit-sub { color: var(--z-gray); font-size: 11px; }
.z-outfit-items-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
}
.z-outfit-mini-item {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-bg);
  cursor: pointer;
  transition: var(--z-ease);
}
.z-outfit-mini-item:hover {
  border-color: var(--z-accent-light);
  transform: translateY(-1px);
}
.z-outfit-mini-item img {
  width: 100%;
  aspect-ratio: 4 / 5;
  display: block;
  object-fit: cover;
}
.z-outfit-mini-info { display: grid; gap: 2px; padding: 6px; }
.z-outfit-mini-name {
  overflow: hidden;
  color: var(--z-dark);
  font-size: 10px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-outfit-mini-price { justify-content: flex-start; font-size: 10px; }
.z-outfit-footer {
  margin-top: 10px;
  padding-top: 9px;
  border-top: 1px solid var(--z-gray-border);
}
.z-outfit-combo-price { justify-content: flex-start; font-size: 13px; }
.z-outfit-add-btn {
  min-height: 36px;
  justify-content: center;
  padding: 8px 12px;
  font-size: 11px;
}

.z-ai-card-item.product {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
}
.z-card-img-wrapper {
  position: relative;
  min-height: 112px;
  overflow: hidden;
  background: var(--z-bg-alt);
  cursor: pointer;
}
.z-card-img-wrapper img { width: 100%; height: 100%; display: block; object-fit: cover; }
.z-card-cat-badge {
  position: absolute;
  left: 5px;
  bottom: 5px;
  max-width: calc(100% - 10px);
  overflow: hidden;
  padding: 3px 5px;
  border-radius: 4px;
  background: rgba(27, 27, 31, 0.82);
  color: var(--z-white);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-card-info { min-width: 0; display: flex; flex-direction: column; justify-content: center; padding: 10px; }
.z-card-name {
  display: -webkit-box;
  overflow: hidden;
  color: var(--z-dark);
  font-size: 11px;
  font-weight: 650;
  line-height: 1.4;
  cursor: pointer;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.z-card-price { justify-content: flex-start; font-size: 12px; }
.z-card-stock { color: #217A3D; font-size: 10px; font-weight: 600; }
.z-card-view-btn { min-height: 32px; justify-content: center; padding: 5px 9px; font-size: 10px; }
.z-card-cart-btn { width: 32px; height: 32px; flex-basis: 32px; }

.z-voucher-card {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  align-items: center;
  gap: 9px;
  padding: 10px;
}
.z-voucher-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
}
.z-voucher-details { min-width: 0; }
.z-voucher-code { display: block; color: var(--z-dark); font-size: 11px; }
.z-voucher-desc { margin-top: 2px; color: var(--z-gray); font-size: 10px; line-height: 1.4; }
.z-voucher-apply-btn {
  min-height: 32px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 8px;
  border: 1px solid var(--z-dark);
  border-radius: 6px;
  background: var(--z-dark);
  color: var(--z-white);
  font-size: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-voucher-apply-btn:hover { border-color: var(--z-accent); background: var(--z-accent); }

.z-ai-typing-bubble { min-width: 58px; display: flex; align-items: center; gap: 4px; }
.z-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--z-gray-light);
  animation: z-dot-bounce 1.15s infinite ease-in-out;
}
.z-dot:nth-child(2) { animation-delay: 0.14s; }
.z-dot:nth-child(3) { animation-delay: 0.28s; }
@keyframes z-dot-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.55; }
  30% { transform: translateY(-3px); opacity: 1; }
}

.z-ai-quick-section {
  padding: 9px 12px;
  border-top: 1px solid var(--z-gray-border);
  background: var(--z-white);
}
.z-ai-chips-scroll {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}
.z-ai-chip-btn {
  min-width: 0;
  min-height: 36px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 9px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-white);
  color: var(--z-dark-soft);
  font-size: 11px;
  font-weight: 600;
  line-height: 1.25;
  text-align: left;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-ai-chip-btn i { flex: 0 0 auto; color: var(--z-accent); font-size: 12px; }
.z-ai-chip-btn:hover:not(:disabled) {
  border-color: var(--z-accent-light);
  background: var(--z-accent-soft);
  color: var(--z-dark);
  transform: translateY(-1px);
}
.z-ai-chip-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.z-ai-handoff-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 9px 12px;
  border-top: 1px solid var(--z-gray-border);
  background: var(--z-warm-light);
  color: var(--z-dark-soft);
  font-size: 11px;
}
.z-handoff-btn {
  min-height: 32px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 9px;
  border: 1px solid var(--z-dark);
  border-radius: 6px;
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 10px;
  font-weight: 650;
  white-space: nowrap;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-handoff-btn:hover:not(:disabled) { background: var(--z-dark); color: var(--z-white); }
.z-handoff-btn:disabled { opacity: 0.55; cursor: not-allowed; }

.z-ai-form {
  display: grid;
  grid-template-columns: 36px 36px minmax(0, 1fr) 38px;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  border-top: 1px solid var(--z-gray-border);
  background: var(--z-white);
}
.z-ai-media-btn,
.z-ai-mic-btn,
.z-ai-send-btn {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-bg);
  color: var(--z-gray);
  cursor: pointer;
  transition: var(--z-ease);
}
.z-ai-media-btn:hover:not(:disabled),
.z-ai-mic-btn:hover:not(:disabled) {
  border-color: var(--z-accent-light);
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
}
.z-ai-mic-btn.listening {
  border-color: var(--z-accent);
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
}
.z-ai-input {
  width: 100%;
  min-width: 0;
  height: 38px;
  padding: 0 11px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 11px;
  outline: none;
  transition: var(--z-ease);
}
.z-ai-input::placeholder { color: var(--z-gray-light); }
.z-ai-input:focus {
  border-color: var(--z-accent);
  box-shadow: 0 0 0 3px var(--z-accent-soft);
}
.z-ai-send-btn {
  width: 38px;
  height: 38px;
  border-color: var(--z-dark);
  background: var(--z-dark);
  color: var(--z-white);
}
.z-ai-send-btn:hover:not(:disabled) { border-color: var(--z-accent); background: var(--z-accent); }
.z-ai-media-btn:disabled,
.z-ai-mic-btn:disabled,
.z-ai-send-btn:disabled { opacity: 0.42; cursor: not-allowed; }

.z-slide-up-enter-active,
.z-slide-up-leave-active { transition: transform 0.16s ease; }
.z-slide-up-enter-from,
.z-slide-up-leave-to { transform: translateY(8px); }

@media (max-width: 575px) {
  .z-customer-ai { right: 10px; bottom: 10px; }
  .z-ai-window {
    width: calc(100vw - 20px);
    height: calc(100dvh - 82px);
  }
  .z-ai-header { min-height: 64px; padding: 10px; }
  .z-ai-avatar { width: 36px; height: 36px; flex-basis: 36px; }
  .z-ai-avatar img { width: 27px; height: 27px; }
  .z-ai-subtitle { max-width: 155px; }
  .z-ai-title { max-width: 165px; }
  .z-ai-header-actions { gap: 3px; }
  .z-ai-icon-btn { width: 30px; height: 30px; flex-basis: 30px; }
  .z-size-result-bar { align-items: flex-start; flex-direction: column; }
  .z-calc-submit-btn { width: 100%; justify-content: center; }
  .z-ai-body { padding: 12px 10px; }
  .z-ai-quick-section { padding: 8px 10px; }
  .z-ai-form { grid-template-columns: 34px 34px minmax(0, 1fr) 36px; gap: 4px; padding: 8px 10px; }
  .z-ai-media-btn, .z-ai-mic-btn { width: 34px; height: 34px; }
  .z-ai-send-btn { width: 36px; height: 36px; }
  .z-ai-input { padding-inline: 9px; font-size: 11px; }
  .z-ai-handoff-bar { align-items: flex-start; flex-direction: column; }
  .z-handoff-btn { width: 100%; justify-content: center; }
}

@media (prefers-reduced-motion: reduce) {
  .z-ai-toggle-btn,
  .z-ai-tab,
  .z-ai-chip-btn,
  .z-ai-card-item,
  .z-slide-up-enter-active,
  .z-slide-up-leave-active { transition: none; }
  .z-dot { animation: none; }
}
</style>
