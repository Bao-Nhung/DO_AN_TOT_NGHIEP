<template>
  <div class="z-copilot-wrapper">
    <!-- Floating Trigger Button -->
    <button
      class="z-copilot-trigger"
      :class="{ active: isOpen }"
      type="button"
      @click="toggleCopilot"
      title="Bật/Tắt Zestia AI Copilot (Phím tắt: Alt + A hoặc Ctrl + K)"
    >
      <div class="z-copilot-trigger-icon">
        <i v-if="!isOpen" class="bi bi-stars"></i>
        <i v-else class="bi bi-x-lg"></i>
      </div>
      <span class="z-copilot-trigger-text d-none d-md-inline">AI Copilot</span>
      <span class="z-copilot-badge">Staff</span>
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
                <span class="z-pill-ai">v2.5 Pro</span>
              </h6>
              <small class="text-muted" style="font-size: 11px">Trợ lý quản trị & Bán hàng POS thông minh</small>
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
                <span class="z-msg-author">Zestia Copilot</span>
                <span class="z-msg-time">{{ msg.time }}</span>
              </div>
              <div class="z-msg-text" v-html="formatMessageText(msg.text)"></div>

              <!-- Render Interactive Rich Cards -->
              <div v-if="msg.cards?.length" class="z-msg-cards mt-2">
                <div v-for="(card, cIdx) in msg.cards" :key="cIdx" class="z-rich-card">
                  <!-- Product Card -->
                  <div v-if="card.type === 'product'" class="z-product-card-mini">
                    <img :src="card.image || '/images/products/shirt1.jpg'" :alt="card.name" class="z-mini-thumb" />
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
                      <span class="badge bg-success">Live</span>
                    </div>
                    <div class="row g-2 text-center">
                      <div class="col-6">
                        <div class="z-stat-box">
                          <small>Sản phẩm</small>
                          <div>{{ card.totalProducts }}</div>
                        </div>
                      </div>
                      <div class="col-6">
                        <div class="z-stat-box">
                          <small>Đơn hàng</small>
                          <div>{{ card.totalOrders }}</div>
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
            <input
              v-model="inputQuery"
              ref="inputRef"
              type="text"
              class="lm-input z-copilot-input"
              placeholder="Hỏi AI về tồn kho, phối đồ POS, doanh thu..."
              :disabled="loading"
            />
            <button type="submit" class="lm-btn-primary z-send-btn" :disabled="loading || !inputQuery.trim()">
              <i class="bi bi-send-fill"></i>
            </button>
          </form>
          <div class="d-flex align-items-center justify-content-between mt-2 px-1" style="font-size: 11px; color: var(--z-gray)">
            <span>Gợi ý: Thử gõ <i>"Phối đồ với quần jeans"</i> hoặc <i>"Báo cáo kho"</i></span>
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

const quickChips = [
  { label: 'Doanh thu hôm nay', icon: 'bi-graph-up', prompt: 'Báo cáo doanh thu và tổng số đơn hàng hôm nay' },
  { label: 'Tra cứu tồn kho', icon: 'bi-box-seam', prompt: 'Báo cáo tra cứu tồn kho sản phẩm hiện tại' },
  { label: 'Gợi ý phối đồ POS', icon: 'bi-magic', prompt: 'Gợi ý phối đồ tư vấn bán hàng POS' },
  { label: 'Voucher giảm giá', icon: 'bi-ticket-perforated', prompt: 'Tra cứu danh sách voucher đang áp dụng' },
  { label: 'Mẫu trả lời CSKH', icon: 'bi-chat-heart', prompt: 'Soạn tin mẫu trả lời xin lỗi và tri ân khách hàng' }
]

const messages = ref([
  {
    role: 'assistant',
    text: 'Xin chào! Tôi là **Zestia AI Copilot** hỗ trợ Quản trị & Bán hàng POS.\n\nBạn có thể yêu cầu tôi **tra cứu tồn kho theo size/màu**, **gợi ý phối đồ cross-sell cho khách tại quầy**, hoặc **xem nhanh báo cáo doanh thu**!',
    time: getCurrentTime(),
    cards: []
  }
])

function getCurrentTime() {
  const d = new Date()
  return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
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
})

function sendQuickPrompt(promptText) {
  inputQuery.value = promptText
  sendMessage()
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

    const res = await api().postAiChat(query, historyPayload).catch(() => null)

    if (res && res.reply) {
      messages.value.push({
        role: 'assistant',
        text: res.reply,
        time: getCurrentTime(),
        cards: res.cards || [],
        copyable: res.copyable || false
      })
    } else {
      // Fallback local smart response for offline testing
      const localResp = generateLocalStaffResponse(query)
      messages.value.push({
        role: 'assistant',
        text: localResp.text,
        time: getCurrentTime(),
        cards: localResp.cards || [],
        copyable: localResp.copyable || false
      })
    }
    playChime()
  } catch (e) {
    const localResp = generateLocalStaffResponse(query)
    messages.value.push({
      role: 'assistant',
      text: localResp.text,
      time: getCurrentTime(),
      cards: localResp.cards || []
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function generateLocalStaffResponse(query) {
  const q = query.toLowerCase()
  if (q.includes('doanh thu') || q.includes('báo cáo') || q.includes('thống kê') || q.includes('đơn hàng')) {
    return {
      text: '📊 **BÁO CÁO NHANH THỜI GIAN THỰC ZESTIA**\n\n• **Doanh thu hôm nay**: 128.500.000đ\n• **Tổng đơn hoàn tất**: 142 đơn hàng\n• **Sản phẩm đang kinh doanh**: 62 mã sản phẩm\n• **Trạng thái hệ thống**: Hoạt động bình thường.',
      cards: [
        { type: 'stats', title: 'Thống kê tổng quan hệ thống', totalProducts: 62, totalOrders: 142 }
      ]
    }
  }

  if (q.includes('kho') || q.includes('tồn kho') || q.includes('hết hàng') || q.includes('còn bao nhiêu')) {
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

  if (q.includes('phối đồ') || q.includes('tư vấn') || q.includes('outfit') || q.includes('cross-sell')) {
    const top = MOCK_PRODUCTS.find(p => p.category === 'Áo thời trang') || MOCK_PRODUCTS[0]
    const bottom = MOCK_PRODUCTS.find(p => p.category === 'Quần & Jeans') || MOCK_PRODUCTS[1]
    const acc = MOCK_PRODUCTS.find(p => p.category === 'Phụ kiện thời trang') || MOCK_PRODUCTS[3]

    return {
      text: '💡 **GỢI Ý PHỐI ĐỒ CHUYÊN NGHIỆP CHO NHÂN VIÊN POS (STYLIST COPILOT)**\n\nSet đồ kết hợp cực chuẩn dáng cho khách hàng:',
      cards: [top, bottom, acc].map(p => ({
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
  showToast(`Đã chọn sản phẩm [${card.code}] ${card.name}`)
  router.push('/admin/pos')
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
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br/>')
  return html
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
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 30px;
  background: linear-gradient(135deg, var(--z-dark, #1f2937), #374151);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.z-copilot-trigger:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.35);
  background: linear-gradient(135deg, var(--z-accent, #c08b7e), #8b5cf6);
}

.z-copilot-trigger-icon {
  font-size: 16px;
  display: flex;
  align-items: center;
  color: #f59e0b;
}

.z-copilot-trigger-text {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.z-copilot-badge {
  font-size: 10px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 7px;
  border-radius: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

.z-copilot-panel {
  position: fixed;
  right: 24px;
  bottom: 80px;
  width: min(420px, calc(100vw - 32px));
  height: 580px;
  max-height: calc(100vh - 110px);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16px);
  border: 1px solid var(--z-gray-border, #e5e7eb);
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
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
  background: var(--z-bg-alt, #f9fafb);
  border-bottom: 1px solid var(--z-gray-border, #e5e7eb);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.z-copilot-avatar {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--z-accent, #c08b7e), #7c3aed);
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
  border: 2px solid #fff;
}

.z-pill-ai {
  font-size: 10px;
  padding: 1px 6px;
  background: rgba(124, 58, 237, 0.12);
  color: #7c3aed;
  border-radius: 10px;
  font-weight: 700;
}

.z-icon-btn {
  border: none;
  background: transparent;
  color: var(--z-gray, #6b7280);
  font-size: 14px;
  padding: 4px 6px;
  border-radius: 6px;
  cursor: pointer;
}

.z-icon-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--z-dark, #111827);
}

.z-copilot-chips {
  padding: 10px 14px;
  display: flex;
  gap: 6px;
  overflow-x: auto;
  white-space: nowrap;
  background: #fff;
  border-bottom: 1px solid var(--z-gray-border, #f3f4f6);
}

.z-copilot-chips::-webkit-scrollbar {
  height: 3px;
}

.z-copilot-chips::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.z-chip-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 11px;
  border-radius: 16px;
  border: 1px solid var(--z-gray-border, #e5e7eb);
  background: #f9fafb;
  font-size: 12px;
  color: var(--z-dark, #374151);
  cursor: pointer;
  transition: all 0.2s ease;
}

.z-chip-btn:hover {
  background: var(--z-accent-soft, #f4ebe8);
  border-color: var(--z-accent, #c08b7e);
  color: var(--z-accent, #c08b7e);
}

.z-copilot-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #fafafa;
}

.z-chat-row {
  display: flex;
  gap: 10px;
  max-width: 92%;
}

.z-chat-row.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.z-msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--z-dark, #1f2937);
  color: #f59e0b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.z-msg-bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.z-chat-row.assistant .z-msg-bubble {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-top-left-radius: 4px;
  color: #1f2937;
}

.z-chat-row.user .z-msg-bubble {
  background: var(--z-dark, #1f2937);
  color: #fff;
  border-top-right-radius: 4px;
}

.z-msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.z-msg-author {
  font-size: 11px;
  font-weight: 700;
  color: var(--z-accent, #c08b7e);
}

.z-msg-time {
  font-size: 10px;
  color: #9ca3af;
}

.z-product-card-mini {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  margin-top: 6px;
}

.z-mini-thumb {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
}

.z-mini-info {
  flex: 1;
  min-width: 0;
}

.z-mini-title {
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.z-mini-sub {
  font-size: 11px;
  color: #6b7280;
}

.z-stock-tag {
  color: #059669;
  font-weight: 600;
}

.z-mini-price {
  font-size: 12px;
  font-weight: 700;
  color: var(--z-accent, #c08b7e);
}

.z-mini-btn {
  padding: 4px 8px !important;
  font-size: 11px !important;
  min-height: 28px !important;
  white-space: nowrap;
}

.z-stat-box {
  background: #f3f4f6;
  padding: 6px;
  border-radius: 6px;
}

.z-stat-box small {
  font-size: 10px;
  color: #6b7280;
  display: block;
}

.z-stat-box div {
  font-size: 14px;
  font-weight: 700;
  color: var(--z-dark, #111827);
}

.z-copy-btn {
  border: none;
  background: rgba(192, 139, 126, 0.1);
  color: var(--z-accent, #c08b7e);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  cursor: pointer;
}

.z-copy-btn:hover {
  background: var(--z-accent, #c08b7e);
  color: #fff;
}

.z-typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #9ca3af;
  border-radius: 50%;
  margin-right: 4px;
  animation: z-blink 1.4s infinite ease-in-out both;
}

.z-typing span:nth-child(1) { animation-delay: -0.32s; }
.z-typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes z-blink {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.z-copilot-footer {
  padding: 12px;
  background: #fff;
  border-top: 1px solid var(--z-gray-border, #e5e7eb);
}

.z-copilot-input {
  border-radius: 20px !important;
  padding: 8px 16px !important;
  font-size: 13px !important;
}

.z-send-btn {
  border-radius: 50% !important;
  width: 38px;
  height: 38px;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
