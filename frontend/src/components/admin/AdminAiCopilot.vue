<template>
  <div class="z-copilot-wrapper">
    <!-- Floating Trigger Button -->
    <button
      v-if="!isOpen"
      class="z-copilot-trigger"
      type="button"
      @click="toggleCopilot"
      title="Mở trợ lý nghiệp vụ"
      aria-label="Mở trợ lý nghiệp vụ Zestia"
    >
      <div class="z-copilot-trigger-icon">
        <i class="bi bi-chat-square-text"></i>
      </div>
      <span class="z-copilot-trigger-text d-none d-md-inline">Trợ lý nghiệp vụ</span>
    </button>

    <!-- Slide-over Copilot Panel -->
    <Transition name="z-copilot-slide">
      <section v-if="isOpen" class="z-copilot-panel" aria-label="Trợ lý nghiệp vụ Zestia">
        <!-- Header -->
        <header class="z-copilot-header">
          <div class="d-flex align-items-center gap-2">
            <div class="z-copilot-avatar">
              <img src="/images/brand/zestia-mark.png" alt="" aria-hidden="true">
              <span class="z-copilot-status-dot"></span>
            </div>
            <div>
              <h6 class="z-copilot-title">
                Trợ lý nghiệp vụ Zestia
                <span class="z-pill-ai">Nội bộ</span>
              </h6>
              <small class="z-copilot-subtitle">Tra cứu dữ liệu kho, đơn hàng và bán tại quầy</small>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button
              class="z-icon-btn"
              type="button"
              :title="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'"
              :aria-label="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'"
              @click="soundEnabled = !soundEnabled"
            >
              <i class="bi" :class="soundEnabled ? 'bi-volume-up-fill' : 'bi-volume-mute-fill'"></i>
            </button>
            <button class="z-icon-btn" type="button" title="Xóa lịch sử chat" aria-label="Xóa lịch sử chat" @click="clearChat">
              <i class="bi bi-trash3"></i>
            </button>
            <button class="z-icon-btn" type="button" title="Đóng trợ lý" aria-label="Đóng trợ lý nghiệp vụ" @click="isOpen = false">
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
              <i class="bi bi-chat-square-text"></i>
            </div>
            <div class="z-msg-bubble">
              <div class="z-msg-header" v-if="msg.role === 'assistant'">
                <span class="z-msg-author">Trợ lý Zestia</span>
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
                        <img :src="item.image || '/images/products/catalog-v2/sp001_main.webp'" :alt="item.name" class="z-mini-thumb" @error="onCardImgError" />
                        <div class="z-mini-info flex-grow-1">
                          <div class="fw-bold z-mini-title">{{ item.name }}</div>
                          <div class="z-mini-sub">{{ item.code }} · {{ item.color }} / {{ item.size }}</div>
                        </div>
                        <span class="badge bg-danger-subtle text-danger font-monospace fw-bold">Còn {{ item.stock }}</span>
                      </div>
                    </div>
                    <button type="button" class="z-card-action-btn z-card-action-btn--danger w-100 mt-2" @click="goToAdminProducts">
                      <i class="bi bi-box-seam me-1"></i> Chuyển tới Quản lý Sản Phẩm
                    </button>
                  </div>

                  <!-- Outfit Set POS Card -->
                  <div v-else-if="card.type === 'outfit'" class="z-outfit-pos-card">
                    <div class="d-flex align-items-center justify-content-between mb-2">
                      <span class="z-card-kicker"><i class="bi bi-bag-heart me-1"></i> Gợi ý phối đồ tại quầy</span>
                      <small class="text-muted">{{ card.occasion || 'Cross-sell tại quầy' }}</small>
                    </div>
                    <strong class="d-block mb-1">{{ card.title }}</strong>
                    <div class="z-outfit-mini-grid">
                      <div v-for="(item, iIdx) in card.items" :key="iIdx" class="z-outfit-mini-box" @click="addSingleToPos(item)">
                        <img :src="item.image || '/images/products/catalog-v2/sp001_main.webp'" :alt="item.name" @error="onCardImgError" />
                        <div class="z-mini-box-text">
                          <div class="z-mini-box-name">{{ item.name }}</div>
                          <div class="z-mini-box-price">{{ fmtPrice(item.price) }}</div>
                        </div>
                      </div>
                    </div>
                    <div class="d-flex align-items-center justify-content-between mt-2 pt-2 border-top">
                      <div>
                        <span class="fw-bold text-danger me-1">{{ fmtPrice(card.totalPrice) }}</span>
                        <small class="text-muted">Tổng giá hiện tại</small>
                      </div>
                      <button type="button" class="z-card-action-btn" @click="addOutfitToPos(card)">
                        <i class="bi bi-cart-plus-fill me-1"></i> Thêm vào POS
                      </button>
                    </div>
                  </div>

                  <!-- Product Card -->
                  <div v-else-if="card.type === 'product'" class="z-product-card-mini">
                    <img :src="card.image || '/images/products/catalog-v2/sp001_main.webp'" :alt="card.name" class="z-mini-thumb" @error="onCardImgError" />
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
                      <strong class="z-rich-card-title"><i class="bi bi-graph-up-arrow me-1"></i>{{ card.title }}</strong>
                      <span class="z-card-data-label">Đã đối chiếu</span>
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
            <div class="z-msg-avatar"><i class="bi bi-chat-square-text"></i></div>
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
              :title="isListening ? 'Đang lắng nghe...' : 'Nhập câu hỏi bằng giọng nói'"
              :aria-label="isListening ? 'Dừng ghi âm' : 'Nhập câu hỏi bằng giọng nói'"
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
              placeholder="Nhập câu hỏi về kho, đơn hàng hoặc POS..."
              :disabled="loading"
            />
            <button type="submit" class="lm-btn-primary z-send-btn" :disabled="loading || !inputQuery.trim()">
              <i class="bi bi-send-fill"></i>
            </button>
          </form>
        </footer>
      </section>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { fmtPrice } from '@/composables/useProducts'

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
  { label: 'Cảnh báo tồn kho', icon: 'bi-exclamation-triangle', prompt: 'Báo cáo cảnh báo tồn kho các sản phẩm sắp hết hàng' },
  { label: 'Phối đồ tại quầy', icon: 'bi-bag-heart', prompt: 'Gợi ý phối đồ bán hàng POS' },
  { label: 'Doanh thu hôm nay', icon: 'bi-graph-up', prompt: 'Báo cáo doanh thu và tổng số đơn hàng hôm nay' },
  { label: 'Voucher đang dùng', icon: 'bi-ticket-perforated', prompt: 'Tra cứu danh sách voucher đang áp dụng' },
  { label: 'Mẫu trả lời CSKH', icon: 'bi-chat-heart', prompt: 'Soạn tin mẫu trả lời xin lỗi và tri ân khách hàng' }
]

const messages = ref([
  {
    role: 'assistant',
    text: 'Xin chào! Tôi là **trợ lý nhân viên Zestia**.\n\nTôi có thể tra cứu **tồn kho**, gợi ý **phối đồ tại POS**, xem **số liệu tổng quan**, hoặc hỗ trợ soạn câu trả lời khách hàng. Các số liệu được lấy từ hệ thống hiện tại.',
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
  e.target.src = '/images/products/catalog-v2/sp001_main.webp'
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
  showToast(`Đã thêm bộ phối gồm ${outfitCard.items.length} món vào đơn POS.`)
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

    const res = await api().postAiChat(query, historyPayload, 'staff')

    const replyText = res?.reply || null
    const replyCards = res?.cards || []
    const replyTime = getCurrentTime()

    if (!replyText) throw new Error('Phản hồi từ máy chủ không hợp lệ')

    const newMsg = { role: 'assistant', text: '', time: replyTime, cards: [], copyable: res?.copyable || false }
    messages.value.push(newMsg)
    playChime()
    loading.value = false
    scrollToBottom()
    await streamText(newMsg, replyText)
    newMsg.cards = replyCards
    scrollToBottom()
  } catch (e) {
    const newMsg = { role: 'assistant', text: '', time: getCurrentTime(), cards: [] }
    messages.value.push(newMsg)
    loading.value = false
    await streamText(newMsg, 'Không thể tải dữ liệu trợ lý lúc này. Vui lòng kiểm tra kết nối backend rồi thử lại; hệ thống không hiển thị số liệu thay thế để tránh sai báo cáo.')
  } finally {
    loading.value = false
    scrollToBottom()
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
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
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
  right: 20px;
  bottom: 20px;
  z-index: 1035;
  font-family: var(--z-font-body);
}

.z-copilot-trigger {
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
.z-copilot-trigger:hover {
  border-color: var(--z-accent);
  background: var(--z-accent);
  color: var(--z-white);
  transform: translateY(-2px);
  box-shadow: 0 15px 32px rgba(27, 27, 31, 0.24);
}
.z-copilot-trigger:active { transform: translateY(0); }
.z-copilot-trigger-icon {
  display: grid;
  place-items: center;
  font-size: 17px;
}
.z-copilot-trigger-text { white-space: nowrap; }

.z-copilot-panel {
  position: fixed;
  top: 76px;
  right: 20px;
  bottom: 20px;
  width: min(430px, calc(100vw - 32px));
  min-height: 420px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  box-shadow: 0 24px 64px rgba(27, 27, 31, 0.24);
}

.z-copilot-header {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  background: var(--z-dark);
  color: var(--z-white);
}
.z-copilot-header > div:first-child { min-width: 0; }
.z-copilot-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  display: grid;
  place-items: center;
  border-radius: var(--z-radius);
  background: var(--z-white);
}
.z-copilot-avatar img { width: 30px; height: 30px; object-fit: contain; }
.z-copilot-status-dot {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 10px;
  height: 10px;
  border: 2px solid var(--z-dark);
  border-radius: 50%;
  background: #22C55E;
}
.z-copilot-title {
  max-width: 240px;
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  overflow: hidden;
  color: var(--z-white);
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-copilot-subtitle {
  max-width: 250px;
  display: block;
  margin-top: 3px;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-pill-ai {
  padding: 3px 6px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 10px;
  font-weight: 650;
  line-height: 1;
  text-transform: uppercase;
}
.z-copilot-header .z-icon-btn {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--z-white);
  box-shadow: none;
}
.z-copilot-header .z-icon-btn:hover {
  border-color: rgba(255, 255, 255, 0.42);
  background: rgba(255, 255, 255, 0.16);
  color: var(--z-white);
  transform: none;
}
.z-copilot-header .z-icon-btn:last-child:hover {
  border-color: var(--z-accent);
  background: var(--z-accent);
}

.z-copilot-chips {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  padding: 9px 12px;
  border-bottom: 1px solid var(--z-gray-border);
  background: var(--z-bg-alt);
}
.z-chip-btn {
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
.z-chip-btn:last-child:nth-child(odd) { grid-column: 1 / -1; }
.z-chip-btn i { flex: 0 0 auto; color: var(--z-accent); font-size: 12px; }
.z-chip-btn:hover:not(:disabled) {
  border-color: var(--z-accent-light);
  background: var(--z-accent-soft);
  color: var(--z-dark);
  transform: translateY(-1px);
}
.z-chip-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.z-copilot-body {
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
.z-chat-row {
  max-width: 95%;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.z-chat-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.z-msg-avatar {
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
.z-msg-bubble {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
  color: var(--z-dark-soft);
  font-size: 12px;
  line-height: 1.65;
  overflow-wrap: anywhere;
}
.z-chat-row.user .z-msg-bubble {
  border-color: var(--z-dark);
  background: var(--z-dark);
  color: var(--z-white);
}
.z-msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 5px;
  color: var(--z-gray);
  font-size: 10px;
}
.z-msg-author { color: var(--z-dark); font-weight: 650; }
.z-msg-time { white-space: nowrap; }
.z-msg-text { color: inherit; }
.z-msg-text :deep(p) { margin: 0 0 8px; color: inherit; }
.z-msg-text :deep(p:last-child) { margin-bottom: 0; }
.z-msg-text :deep(strong),
.z-msg-text :deep(code),
.z-msg-text :deep(a) { color: inherit; }
.z-msg-text :deep(ul),
.z-msg-text :deep(ol) { margin: 7px 0; padding-left: 18px; }
.z-speech-btn {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--z-gray);
  cursor: pointer;
  transition: var(--z-ease);
}
.z-speech-btn:hover { background: var(--z-bg-alt); color: var(--z-accent-dark); }

.z-msg-cards { display: grid; gap: 8px; }
.z-rich-card {
  overflow: hidden;
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-white);
}
.z-low-stock-card,
.z-outfit-pos-card,
.z-stats-card-mini { padding: 10px; background: var(--z-white); }
.z-low-stock-list { display: grid; gap: 6px; }
.z-low-stock-item {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-bg);
}
.z-mini-thumb {
  width: 42px;
  height: 50px;
  flex: 0 0 42px;
  border: 1px solid var(--z-gray-border);
  border-radius: 5px;
  object-fit: cover;
}
.z-mini-info { min-width: 0; flex: 1; }
.z-mini-title {
  overflow: hidden;
  color: var(--z-dark);
  font-size: 11px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-mini-sub { margin-top: 2px; color: var(--z-gray); font-size: 10px; }
.z-stock-tag { color: #217A3D; font-weight: 650; }
.z-mini-price { margin-top: 3px; color: var(--z-accent-dark); font-size: 11px; font-weight: 700; }
.z-card-action-btn {
  min-height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 5px 9px;
  border: 1px solid var(--z-dark);
  border-radius: 6px;
  background: var(--z-dark);
  color: var(--z-white);
  font-size: 10px;
  font-weight: 650;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-card-action-btn:hover { border-color: var(--z-accent); background: var(--z-accent); }
.z-card-action-btn--danger {
  border-color: var(--z-danger);
  background: var(--z-white);
  color: var(--z-danger);
}
.z-card-action-btn--danger:hover { border-color: var(--z-danger); background: var(--z-danger); color: var(--z-white); }
.z-card-kicker {
  display: inline-flex;
  align-items: center;
  color: var(--z-accent-dark);
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}
.z-outfit-mini-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
}
.z-outfit-mini-box {
  min-width: 0;
  overflow: hidden;
  padding: 0;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-bg);
  text-align: left;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-outfit-mini-box:hover { border-color: var(--z-accent-light); transform: translateY(-1px); }
.z-outfit-mini-box img { width: 100%; height: 68px; display: block; object-fit: cover; }
.z-mini-box-text { padding: 5px; }
.z-mini-box-name {
  overflow: hidden;
  color: var(--z-dark);
  font-size: 10px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.z-mini-box-price { margin-top: 2px; color: var(--z-accent-dark); font-size: 10px; font-weight: 700; }

.z-product-card-mini {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
}
.z-mini-btn {
  min-height: 30px;
  flex: 0 0 auto;
  padding: 5px 8px;
  border-radius: 6px;
  font-size: 10px;
}
.z-rich-card-title { color: var(--z-accent-dark); font-size: 11px; }
.z-card-data-label {
  padding: 3px 5px;
  border-radius: 4px;
  background: #EAF8EE;
  color: #217A3D;
  font-size: 10px;
  font-weight: 650;
}
.z-stat-box {
  min-height: 52px;
  display: grid;
  place-content: center;
  padding: 5px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-bg);
}
.z-stat-box small { color: var(--z-gray); font-size: 10px; }
.z-stat-box div { color: var(--z-dark); font-size: 12px; font-weight: 700; }
.z-copy-btn {
  min-height: 30px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 8px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-white);
  color: var(--z-dark-soft);
  font-size: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--z-ease);
}
.z-copy-btn:hover { border-color: var(--z-accent-light); background: var(--z-accent-soft); color: var(--z-accent-dark); }

.z-typing {
  min-width: 58px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.z-typing span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--z-gray-light);
  animation: z-admin-dot 1.15s infinite ease-in-out;
}
.z-typing span:nth-child(2) { animation-delay: 0.14s; }
.z-typing span:nth-child(3) { animation-delay: 0.28s; }
@keyframes z-admin-dot {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.55; }
  30% { transform: translateY(-3px); opacity: 1; }
}

.z-copilot-footer {
  padding: 10px 12px;
  border-top: 1px solid var(--z-gray-border);
  background: var(--z-white);
}
.z-copilot-footer form {
  display: grid !important;
  grid-template-columns: 36px minmax(0, 1fr) 38px;
  gap: 6px !important;
}
.z-mic-btn,
.z-send-btn {
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
.z-mic-btn:hover:not(:disabled) {
  border-color: var(--z-accent-light);
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
}
.z-mic-btn.listening {
  border-color: var(--z-accent);
  background: var(--z-accent-soft);
  color: var(--z-accent-dark);
}
.z-copilot-input {
  min-width: 0;
  height: 38px;
  padding: 0 11px;
  border: 1px solid var(--z-gray-border);
  border-radius: 6px;
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 11px;
}
.z-copilot-input::placeholder { color: var(--z-gray-light); }
.z-copilot-input:focus {
  border-color: var(--z-accent);
  box-shadow: 0 0 0 3px var(--z-accent-soft);
}
.z-send-btn {
  width: 38px;
  height: 38px;
  border-color: var(--z-dark);
  background: var(--z-dark);
  color: var(--z-white);
}
.z-send-btn:hover:not(:disabled) { border-color: var(--z-accent); background: var(--z-accent); }
.z-mic-btn:disabled,
.z-send-btn:disabled { opacity: 0.42; cursor: not-allowed; }

.z-copilot-slide-enter-active,
.z-copilot-slide-leave-active { transition: transform 0.16s ease; }
.z-copilot-slide-enter-from,
.z-copilot-slide-leave-to { transform: translateY(8px); }

@media (max-width: 767px) {
  .z-copilot-wrapper { right: 10px; bottom: 10px; }
  .z-copilot-panel {
    top: 66px;
    right: 10px;
    bottom: 10px;
    width: calc(100vw - 20px);
  }
  .z-copilot-header { min-height: 64px; padding: 10px; }
  .z-copilot-avatar { width: 36px; height: 36px; flex-basis: 36px; }
  .z-copilot-avatar img { width: 27px; height: 27px; }
  .z-copilot-title { max-width: 180px; }
  .z-copilot-subtitle { max-width: 195px; }
  .z-copilot-header .z-icon-btn { width: 30px; height: 30px; flex-basis: 30px; }
  .z-copilot-body { padding: 12px 10px; }
  .z-copilot-chips { padding: 8px 10px; }
  .z-copilot-footer { padding: 8px 10px; }
}

@media (prefers-reduced-motion: reduce) {
  .z-copilot-trigger,
  .z-chip-btn,
  .z-outfit-mini-box,
  .z-copilot-slide-enter-active,
  .z-copilot-slide-leave-active { transition: none; }
  .z-typing span { animation: none; }
}
</style>
