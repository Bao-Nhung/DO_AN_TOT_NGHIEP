<template>
  <div class="z-copilot-wrapper">
    <!-- Floating Trigger Button -->
    <button
      class="z-copilot-trigger"
      :class="{ active: isOpen }"
      type="button"
      @click="toggleCopilot"
      title="Bật/Tắt Zestia Staff AI (Phím tắt: Alt + A hoặc Ctrl + K)"
    >
      <div v-if="!isOpen" class="z-ai-pulse"></div>
      <div class="z-copilot-trigger-icon">
        <i v-if="!isOpen" class="bi bi-stars z-ai-icon-stars"></i>
        <i v-else class="bi bi-x-lg"></i>
      </div>
      <span class="z-copilot-trigger-text d-none d-md-inline">Zestia AI PRO</span>
    </button>

    <!-- Slide-over Copilot Panel -->
    <Transition name="z-copilot-slide">
      <div v-if="isOpen" class="z-copilot-panel">
        <!-- Header -->
        <header class="z-copilot-header">
          <div class="d-flex align-items-center gap-2">
            <div class="z-copilot-avatar">
              <i class="bi bi-robot"></i>
              <span class="z-copilot-status-dot"></span>
            </div>
            <div>
              <h6 class="mb-0 fw-bold d-flex align-items-center gap-2">
                Zestia Staff AI Copilot
                <span class="z-pill-ai">v3.0 PRO</span>
              </h6>
              <small class="text-muted" style="font-size: 11px">Trợ lý quản trị, tồn kho & bán hàng POS thông minh</small>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button
              class="z-icon-btn"
              type="button"
              :title="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'"
              @click="soundEnabled = !soundEnabled"
            >
              <i class="bi" :class="soundEnabled ? 'bi-volume-up-fill' : 'bi-volume-mute-fill'"></i>
            </button>
            <button class="z-icon-btn" type="button" title="Xóa lịch sử chat" @click="clearChat">
              <i class="bi bi-trash3"></i>
            </button>
            <button class="z-icon-btn" type="button" title="Đóng Copilot" @click="isOpen = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </header>

        <!-- Quick Prompt Chips -->
        <div class="z-copilot-chips">
          <button
            v-for="chip in quickChips"
            :key="chip.label"
            class="z-chip-btn"
            type="button"
            :disabled="loading"
            @click="sendQuickPrompt(chip.prompt)"
          >
            <i class="bi" :class="chip.icon"></i>
            <span>{{ chip.label }}</span>
          </button>
        </div>

        <!-- Chat Body -->
        <div ref="chatContainer" class="z-copilot-body">
          <div v-for="(msg, index) in messages" :key="index" class="z-chat-row" :class="msg.role">
            <div v-if="msg.role === 'assistant'" class="z-msg-avatar">
              <i class="bi bi-stars"></i>
            </div>
            <div class="z-msg-bubble">
              <div class="z-msg-header" v-if="msg.role === 'assistant'">
                <span class="z-msg-author">Zestia Copilot 3.0</span>
                <div class="d-flex align-items-center gap-2">
                  <span class="z-msg-time">{{ msg.time }}</span>
                  <button type="button" class="z-speech-btn" title="Đọc phát âm" @click="speakText(msg.text)">
                    <i class="bi bi-volume-up"></i>
                  </button>
                </div>
              </div>
              <div class="z-msg-text" v-html="formatMessageText(msg.text)"></div>

              <!-- Render Interactive Rich Cards -->
              <div v-if="msg.cards?.length" class="z-msg-cards mt-2">
                <div v-for="(card, cIdx) in msg.cards" :key="cIdx" class="z-rich-card">
                  <!-- Low Stock Warning Card -->
                  <div v-if="card.type === 'low_stock'" class="z-low-stock-card">
                    <div class="d-flex align-items-center justify-content-between mb-2">
                      <strong class="text-danger"><i class="bi bi-exclamation-triangle-fill me-1"></i>{{ card.title }}</strong>
                      <span class="badge bg-danger">{{ card.count }} sản phẩm</span>
                    </div>
                    <div class="z-low-stock-list">
                      <div v-for="(item, iIdx) in card.items" :key="iIdx" class="z-low-stock-item">
                        <img :src="item.image || '/images/products/shirt1.jpg'" :alt="item.name" class="z-mini-thumb" @error="onCardImgError" />
                        <div class="z-mini-info flex-grow-1">
                          <div class="fw-bold z-mini-title">{{ item.name }}</div>
                          <div class="z-mini-sub">{{ item.code }} · {{ item.color }} / {{ item.size }}</div>
                        </div>
                        <span class="badge bg-danger-subtle text-danger font-monospace fw-bold">Còn {{ item.stock }}</span>
                      </div>
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-danger w-100 mt-2" @click="goToAdminProducts">
                      <i class="bi bi-box-seam me-1"></i> Chuyển tới Quản lý Sản Phẩm
                    </button>
                  </div>

                  <!-- Outfit Set POS Card -->
                  <div v-else-if="card.type === 'outfit'" class="z-outfit-pos-card">
                    <div class="d-flex align-items-center justify-content-between mb-2">
                      <span class="badge bg-purple text-white"><i class="bi bi-stars me-1"></i> GỢI Ý PHỐI ĐỒ POS</span>
                      <small class="text-muted">{{ card.occasion || 'Cross-sell tại quầy' }}</small>
                    </div>
                    <strong class="d-block mb-1">{{ card.title }}</strong>
                    <div class="z-outfit-mini-grid">
                      <div v-for="(item, iIdx) in card.items" :key="iIdx" class="z-outfit-mini-box" @click="addSingleToPos(item)">
                        <img :src="item.image || '/images/products/shirt1.jpg'" :alt="item.name" @error="onCardImgError" />
                        <div class="z-mini-box-text">
                          <div class="z-mini-box-name">{{ item.name }}</div>
                          <div class="z-mini-box-price">{{ fmtPrice(item.price) }}</div>
                        </div>
                      </div>
                    </div>
                    <div class="d-flex align-items-center justify-content-between mt-2 pt-2 border-top">
                      <div>
                        <span class="fw-bold text-danger me-1">{{ fmtPrice(card.comboPrice || card.totalPrice) }}</span>
                        <small class="text-muted text-decoration-line-through" v-if="card.totalPrice > card.comboPrice">{{ fmtPrice(card.totalPrice) }}</small>
                      </div>
                      <button type="button" class="btn btn-sm btn-dark" @click="addOutfitToPos(card)">
                        <i class="bi bi-cart-plus-fill me-1"></i> Thêm vào POS
                      </button>
                    </div>
                  </div>

                  <!-- Product Card -->
                  <div v-else-if="card.type === 'product'" class="z-product-card-mini">
                    <img :src="card.image || '/images/products/shirt1.jpg'" :alt="card.name" class="z-mini-thumb" @error="onCardImgError" />
                    <div class="z-mini-info">
                      <div class="fw-bold z-mini-title">{{ card.name }}</div>
                      <div class="z-mini-sub">{{ card.code }} · <span class="z-stock-tag">Tồn: {{ card.stock ?? 'Sẵn hàng' }}</span></div>
                      <div class="z-mini-price">{{ fmtPrice(card.price) }}</div>
                    </div>
                    <button class="lm-btn-primary z-mini-btn" type="button" @click="handleCardAction(card)">
                      <i class="bi bi-cart-plus me-1"></i>Thêm POS
                    </button>
                  </div>

                  <!-- Stats Card -->
                  <div v-else-if="card.type === 'stats'" class="z-stats-card-mini">
                    <div class="d-flex align-items-center justify-content-between mb-2">
                      <strong style="color:var(--z-accent)"><i class="bi bi-graph-up-arrow me-1"></i>{{ card.title }}</strong>
                      <span class="badge bg-success">Dữ liệu thực</span>
                    </div>
                    <div class="row g-2 text-center">
                      <div class="col-4">
                        <div class="z-stat-box">
                          <small>Sản phẩm</small>
                          <div>{{ card.totalProducts }}</div>
                        </div>
                      </div>
                      <div class="col-4">
                        <div class="z-stat-box">
                          <small>Đơn hàng</small>
                          <div>{{ card.totalOrders }}</div>
                        </div>
                      </div>
                      <div v-if="card.totalStock" class="col-4">
                        <div class="z-stat-box">
                          <small>Tồn kho</small>
                          <div>{{ card.totalStock }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Quick Copy Option for CSKH Draft -->
              <div v-if="msg.copyable" class="mt-2 text-end">
                <button class="z-copy-btn" type="button" @click="copyText(msg.text)">
                  <i class="bi bi-clipboard me-1"></i>Sao chép mẫu trả lời CSKH
                </button>
              </div>
            </div>
          </div>

          <!-- Typing Indicator -->
          <div v-if="loading" class="z-chat-row assistant">
            <div class="z-msg-avatar"><i class="bi bi-stars"></i></div>
            <div class="z-msg-bubble z-typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- Input Bar -->
        <footer class="z-copilot-footer">
          <form @submit.prevent="sendMessage" class="d-flex align-items-center gap-2">
            <!-- Nút Nói bằng Giọng Nói Voice Input -->
            <button
              type="button"
              class="z-mic-btn"
              :class="{ listening: isListening }"
              :title="isListening ? 'Đang lắng nghe...' : 'Nói lệnh với Zestia AI'"
              :disabled="loading"
              @click="toggleVoiceInput"
            >
              <i class="bi" :class="isListening ? 'bi-mic-fill text-danger' : 'bi-mic'"></i>
            </button>

            <input
              v-model="inputQuery"
              ref="inputRef"
              type="text"
              class="lm-input z-copilot-input"
              placeholder="Hỏi AI về tồn kho, cảnh báo hết hàng, phối đồ POS..."
              :disabled="loading"
            />
            <button type="submit" class="lm-btn-primary z-send-btn" :disabled="loading || !inputQuery.trim()">
              <i class="bi bi-send-fill"></i>
            </button>
          </form>
          <div class="d-flex align-items-center justify-content-between mt-2 px-1" style="font-size: 11px; color: var(--z-gray)">
            <span>Gợi ý: Thử gõ <i>"Cảnh báo tồn kho"</i> hoặc <i>"Phối đồ POS"</i></span>
            <span class="d-none d-sm-inline">Alt + A</span>
          </div>
        </footer>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { fmtPrice, MOCK_PRODUCTS } from '@/composables/useProducts'

const router = useRouter()
const { showToast } = useToast()

const isOpen = ref(false)
const loading = ref(false)
const inputQuery = ref('')
const soundEnabled = ref(true)
const chatContainer = ref(null)
const inputRef = ref(null)

const isListening = ref(false)
let recognition = null

const quickChips = [
  { label: '⚠️ Cảnh báo tồn kho', icon: 'bi-exclamation-triangle', prompt: 'Báo cáo cảnh báo tồn kho các sản phẩm sắp hết hàng' },
  { label: '🥂 Phối đồ POS', icon: 'bi-magic', prompt: 'Gợi ý phối đồ bán hàng POS' },
  { label: '📊 Doanh thu hôm nay', icon: 'bi-graph-up', prompt: 'Báo cáo doanh thu và tổng số đơn hàng hôm nay' },
  { label: '🎟️ Tra cứu Voucher', icon: 'bi-ticket-perforated', prompt: 'Tra cứu danh sách voucher đang áp dụng' },
  { label: '✍️ Mẫu CSKH Xin lỗi', icon: 'bi-chat-heart', prompt: 'Soạn tin mẫu trả lời xin lỗi và tri ân khách hàng' }
]

const messages = ref([
  {
    role: 'assistant',
    text: 'Xin chào! Tôi là **Zestia Staff AI Copilot v3.0 PRO**.\n\nTôi có thể giúp bạn **cảnh báo tồn kho sắp hết**, **tư vấn phối đồ POS trọn bộ**, **báo cáo doanh thu thời gian thực**, hoặc **soạn mẫu trả lời CSKH**!',
    time: getCurrentTime(),
    cards: []
  }
])

function getCurrentTime() {
  const d = new Date()
  return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
}

function speakText(text) {
  if (!('speechSynthesis' in window)) {
    showToast('Trình duyệt không hỗ trợ phát âm thanh.')
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
    showToast('Đang lắng nghe câu lệnh Admin...')
  }

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript
    if (transcript) {
      inputQuery.value = transcript
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

function toggleCopilot() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => {
      inputRef.value?.focus()
      scrollToBottom()
    })
  }
}

function clearChat() {
  messages.value = [
    {
      role: 'assistant',
      text: 'Đã xóa lịch sử hội thoại. Tôi sẵn sàng hỗ trợ công việc mới của bạn!',
      time: getCurrentTime()
    }
  ]
}

function handleKeydown(e) {
  if ((e.altKey && e.key.toLowerCase() === 'a') || (e.ctrlKey && e.key.toLowerCase() === 'k')) {
    e.preventDefault()
    toggleCopilot()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (recognition) recognition.stop()
})

function sendQuickPrompt(promptText) {
  inputQuery.value = promptText
  sendMessage()
}

function onCardImgError(e) {
  e.target.src = '/images/products/shirt1.jpg'
}

function goToAdminProducts() {
  isOpen.value = false
  router.push('/admin/products')
}

function addSingleToPos(item) {
  window.dispatchEvent(new CustomEvent('pos-add-item', { detail: item }))
  showToast(`Đã thêm "${item.name}" vào đơn POS!`)
  router.push('/admin/pos')
}

function addOutfitToPos(outfitCard) {
  if (!outfitCard || !outfitCard.items) return
  outfitCard.items.forEach(item => {
    window.dispatchEvent(new CustomEvent('pos-add-item', { detail: item }))
  })
  showToast(`🎉 Đã thêm cả Set Outfit (${outfitCard.items.length} món) vào đơn POS!`)
  router.push('/admin/pos')
}

async function sendMessage() {
  const query = inputQuery.value.trim()
  if (!query || loading.value) return

  messages.value.push({
    role: 'user',
    text: query,
    time: getCurrentTime()
  })
  inputQuery.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const historyPayload = messages.value.slice(-6).map(m => ({
      role: m.role,
      content: m.text
    }))

    const res = await api().postAiChat(query, historyPayload, 'staff').catch(() => null)

    const replyText = res?.reply || null
    const replyCards = res?.cards || []
    const replyTime = getCurrentTime()

    if (replyText) {
      const newMsg = { role: 'assistant', text: '', time: replyTime, cards: [], copyable: res?.copyable || false }
      messages.value.push(newMsg)
      playChime()
      loading.value = false
      scrollToBottom()
      await streamText(newMsg, replyText)
      newMsg.cards = replyCards
      scrollToBottom()
    } else {
      const localResp = generateLocalStaffResponse(query)
      const newMsg = { role: 'assistant', text: '', time: replyTime, cards: [], copyable: localResp.copyable || false }
      messages.value.push(newMsg)
      playChime()
      loading.value = false
      scrollToBottom()
      await streamText(newMsg, localResp.text)
      newMsg.cards = localResp.cards || []
      scrollToBottom()
    }
  } catch (e) {
    const localResp = generateLocalStaffResponse(query)
    const newMsg = { role: 'assistant', text: '', time: getCurrentTime(), cards: [] }
    messages.value.push(newMsg)
    loading.value = false
    await streamText(newMsg, localResp.text)
    newMsg.cards = localResp.cards || []
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function generateLocalStaffResponse(query) {
  const q = query.toLowerCase()
  if (q.includes('cảnh báo') || q.includes('sắp hết') || (q.includes('tồn kho') && q.includes('hết'))) {
    return {
      text: '⚠️ **CẢNH BÁO TỒN KHO DƯỚI NGƯỠNG (Zestia Alert)**\n\nPhát hiện các sản phẩm có số lượng tồn kho $\\le 5$ chiếc cần bổ sung kho khẩn cấp:',
      cards: [
        {
          type: 'low_stock',
          title: 'CẢNH BÁO KHO SẮP HẾT (Số lượng <= 5)',
          count: 3,
          items: [
            { id: 1, code: 'ASM001', name: 'Áo Sơ Mi Lụa Cổ Điển', color: 'Trắng Ngà', size: 'S', stock: 2, image: '/images/products/shirt1.jpg' },
            { id: 2, code: 'QJN001', name: 'Quần Jeans Wide Leg', color: 'Xanh Vintage', size: 'M', stock: 3, image: '/images/products/pants1.jpg' },
            { id: 3, code: 'VDH001', name: 'Váy Dạ Hội Gấm Hoàng Gia', color: 'Đỏ Đô', size: 'S', stock: 1, image: '/images/products/dress1.jpg' }
          ]
        }
      ]
    }
  }

  if (q.includes('doanh thu') || q.includes('báo cáo') || q.includes('thống kê') || q.includes('đơn hàng')) {
    return {
      text: '📊 **BÁO CÁO NHANH THỜI GIAN THỰC ZESTIA**\n\n• **Doanh thu hôm nay**: 128.500.000đ\n• **Tổng đơn hoàn tất**: 142 đơn hàng\n• **Sản phẩm đang kinh doanh**: 62 mã sản phẩm\n• **Trạng thái hệ thống**: Hoạt động bình thường.',
      cards: [
        { type: 'stats', title: 'Thống kê tổng quan hệ thống', totalProducts: 62, totalOrders: 142, totalStock: 1240 }
      ]
    }
  }

  if (q.includes('kho') || q.includes('tồn kho') || q.includes('còn bao nhiêu')) {
    const sampleProducts = MOCK_PRODUCTS.slice(0, 3)
    return {
      text: '📦 **BÁO CÁO TRA CỨU TỒN KHO THỜI GIAN THỰC**\n\nCác sản phẩm đang có số lượng tồn kho tốt nhất tại cửa hàng:',
      cards: sampleProducts.map(p => ({
        type: 'product',
        id: p.id,
        code: p.code,
        name: p.name,
        price: p.price,
        stock: p.stock,
        image: p.image
      }))
    }
  }

  if (q.includes('phối đồ') || q.includes('tư vấn') || q.includes('outfit') || q.includes('cross-sell') || q.includes('pos')) {
    const top = MOCK_PRODUCTS.find(p => p.category === 'Áo thời trang') || MOCK_PRODUCTS[0]
    const bottom = MOCK_PRODUCTS.find(p => p.category === 'Quần & Jeans') || MOCK_PRODUCTS[1]
    const acc = MOCK_PRODUCTS.find(p => p.category === 'Phụ kiện thời trang') || MOCK_PRODUCTS[3]

    return {
      text: '💡 **GỢI Ý PHỐI ĐỒ CHUYÊN NGHIỆP CHO NHÂN VIÊN POS (STYLIST COPILOT v3.0)**\n\nSet đồ kết hợp cực chuẩn dáng cho khách hàng:',
      cards: [
        {
          type: 'outfit',
          title: 'Set Outfit Thanh Lịch Công Sở Zestia',
          occasion: 'Tư vấn Cross-sell POS tại quầy',
          totalPrice: top.price + bottom.price + acc.price,
          comboPrice: Math.round((top.price + bottom.price + acc.price) * 0.9),
          items: [top, bottom, acc].map(p => ({ id: p.id, name: p.name, price: p.price, image: p.image }))
        }
      ]
    }
  }

  if (q.includes('voucher') || q.includes('khuyến mãi') || q.includes('mã')) {
    return {
      text: '🎟️ **DANH SÁCH VOUCHER GIẢM GIÁ ĐANG ÁP DỤNG**\n\n• **Mã ZESTIA100K**: Giảm 100.000đ cho đơn từ 1.000.000đ\n• **Mã VIPFASHION**: Giảm 15% cho khách hàng thân thiết\n• **Mã FREESHIP**: Miễn phí vận chuyển toàn quốc'
    }
  }

  if (q.includes('soạn') || q.includes('trả lời') || q.includes('xin lỗi') || q.includes('cskh')) {
    return {
      text: '✍️ **MẪU SOẠN TIN TRẢ LỜI KHÁCH HÀNG CHUYÊN NGHIỆP:**\n\n"Kính chào Quý khách! Zestia chân thành xin lỗi về sự chậm trễ đơn hàng của Quý khách. Đội ngũ nhân viên đã ưu tiên xử lý và tặng kèm Quý khách voucher giảm 100K cho lần mua tiếp theo. Cảm ơn Quý khách luôn tin tưởng Zestia!"',
      copyable: true
    }
  }

  return {
    text: 'Zestia AI Copilot đã nhận câu hỏi của bạn. Bạn có thể sử dụng các thẻ gợi ý nhanh để tra cứu kho, báo cáo doanh thu hoặc gợi ý phối đồ bán hàng POS!'
  }
}

function handleCardAction(card) {
  addSingleToPos(card)
}

function copyText(text) {
  navigator.clipboard?.writeText(text)
  showToast('Đã sao chép nội dung câu trả lời!')
}

function playChime() {
  if (!soundEnabled.value) return
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'sine'
    osc.frequency.setValueAtTime(587.33, ctx.currentTime) // D5
    gain.gain.setValueAtTime(0.05, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.3)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.3)
  } catch (e) {
    /* ignore audio context restrictions */
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

function formatMessageText(text) {
  if (!text) return ''
  let html = text
    .replace(/```([\s\S]*?)```/g, '<pre class="z-copilot-code"><code>$1</code></pre>')
    .replace(/`([^`]+)`/g, '<code class="z-copilot-inline-code">$1</code>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/^[•\-\*]\s+(.+)$/gm, '<li>$1</li>')
    .replace(/^(\d+)\.\s+(.+)$/gm, '<li class="z-num-li"><span class="z-num">$1.</span> $2</li>')
    .replace(/\n/g, '<br/>')
  html = html.replace(/((?:<li[^>]*>.*?<\/li><br\/?>?)+)/g, (match) => {
    const items = match.replace(/<br\/?>/g, '')
    return `<ul class="z-copilot-list">${items}</ul>`
  })
  return html
}

async function streamText(msgObj, fullText, chunkSize = 6, delayMs = 16) {
  if (!fullText) return
  msgObj.text = ''
  let i = 0
  while (i < fullText.length) {
    msgObj.text += fullText.slice(i, i + chunkSize)
    i += chunkSize
    await new Promise(resolve => setTimeout(resolve, delayMs))
    scrollToBottom()
  }
  msgObj.text = fullText
}
</script>

<style scoped>
.z-copilot-wrapper {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1050;
}

.z-copilot-trigger {
  position: relative;
  height: 52px;
  padding: 0 20px 0 16px;
  border: none;
  border-radius: 30px;
  background: linear-gradient(135deg, #1A1A1A 0%, #333333 50%, #D4564E 100%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 12px 32px rgba(212, 86, 78, 0.35);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.z-copilot-trigger:hover {
  transform: translateY(-4px) scale(1.03);
  box-shadow: 0 16px 40px rgba(212, 86, 78, 0.45);
}

.z-copilot-trigger-icon {
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.z-copilot-trigger-text {
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

.z-copilot-panel {
  position: fixed;
  right: 24px;
  bottom: 86px;
  width: min(440px, calc(100vw - 32px));
  height: 600px;
  max-height: calc(100vh - 110px);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16px);
  border: 1px solid var(--z-gray-border, #e5e7eb);
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: z-fade-in 0.3s ease;
}

@keyframes z-fade-in {
  from { opacity: 0; transform: translateY(12px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.z-copilot-header {
  padding: 12px 16px;
  background: #1A1A1A;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.z-copilot-avatar {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #D4564E, #7c3aed);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.z-copilot-status-dot {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #10b981;
  border: 2px solid #1A1A1A;
}

.z-pill-ai {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(212, 86, 78, 0.2);
  color: #FF8A80;
  border-radius: 10px;
  font-weight: 700;
}

.z-icon-btn {
  border: none;
  background: rgba(255,255,255,0.1);
  color: #fff;
  font-size: 14px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.z-icon-btn:hover {
  background: rgba(255,255,255,0.2);
}

.z-copilot-chips {
  padding: 10px 14px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.z-chip-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #374151;
  font-size: 11px;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 20px;
  white-space: nowrap;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.z-chip-btn:hover {
  background: #1A1A1A;
  color: #fff;
  border-color: #1A1A1A;
}

.z-copilot-body {
  flex: 1;
  padding: 14px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.z-chat-row {
  display: flex;
  gap: 10px;
  max-width: 94%;
}

.z-chat-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.z-msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1A1A1A, #D4564E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
}

.z-msg-bubble {
  background: #f3f4f6;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.5;
  color: #1f2937;
}

.z-chat-row.user .z-msg-bubble {
  background: #1A1A1A;
  color: #fff;
}

.z-msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 11px;
  color: #6b7280;
}

.z-speech-btn {
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  padding: 0;
}
.z-speech-btn:hover { color: #111; }

/* LOW STOCK CARD */
.z-low-stock-card {
  background: #FFF5F5;
  border: 1px solid #FEE2E2;
  border-radius: 12px;
  padding: 12px;
}
.z-low-stock-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.z-low-stock-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 6px 8px;
  border-radius: 8px;
  border: 1px solid #FEE2E2;
}

/* OUTFIT POS CARD */
.z-outfit-pos-card {
  background: #FDF4FF;
  border: 1px solid #F5D0FE;
  border-radius: 12px;
  padding: 12px;
}
.z-outfit-mini-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}
.z-outfit-mini-box {
  background: #fff;
  border-radius: 6px;
  padding: 4px;
  text-align: center;
  cursor: pointer;
  border: 1px solid #F0ABFC;
}
.z-outfit-mini-box img {
  width: 100%;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}
.z-mini-box-name {
  font-size: 9px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.z-mini-box-price {
  font-size: 9px;
  color: #D4564E;
  font-weight: 700;
}

/* MINI PRODUCT CARD */
.z-product-card-mini {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #e5e7eb;
  padding: 8px;
  border-radius: 10px;
}
.z-mini-thumb {
  width: 42px;
  height: 52px;
  object-fit: cover;
  border-radius: 6px;
}
.z-mini-info { flex: 1; }
.z-mini-title { font-size: 12px; }
.z-mini-sub { font-size: 10px; color: #6b7280; }
.z-stock-tag { color: #10b981; font-weight: 600; }
.z-mini-price { font-size: 12px; font-weight: 700; color: #D4564E; }
.z-mini-btn { font-size: 11px; padding: 4px 10px; border-radius: 6px; border: none; background: #1A1A1A; color: #fff; cursor: pointer; }

/* STATS CARD */
.z-stats-card-mini {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  padding: 10px;
  border-radius: 10px;
}
.z-stat-box {
  background: #fff;
  padding: 6px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}
.z-stat-box small { font-size: 10px; color: #6b7280; }
.z-stat-box div { font-size: 13px; font-weight: 700; color: #111827; }

.z-copy-btn {
  border: none;
  background: #e5e7eb;
  color: #374151;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

/* TYPING */
.z-typing {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 12px 16px;
}
.z-typing span {
  width: 6px;
  height: 6px;
  background: #9ca3af;
  border-radius: 50%;
  animation: z-bounce 1.4s infinite ease-in-out;
}
.z-typing span:nth-child(1) { animation-delay: 0s; }
.z-typing span:nth-child(2) { animation-delay: 0.2s; }
.z-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes z-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

/* FOOTER INPUT */
.z-copilot-footer {
  padding: 12px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
}
.z-mic-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #f3f4f6;
  color: #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.z-mic-btn.listening {
  background: #FEE2E2;
  animation: z-mic-pulse 1.2s infinite;
}
@keyframes z-mic-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
.z-copilot-input {
  flex: 1;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: 13px;
  outline: none;
}
.z-copilot-input:focus { border-color: #1A1A1A; }
.z-send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #1A1A1A;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.z-send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
