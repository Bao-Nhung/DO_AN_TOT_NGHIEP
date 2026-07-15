<template>
  <div class="z-ai-chat">
    <button v-if="!open" class="z-ai-toggle" title="Hỗ trợ Zestia" @click="open = true">
      <i class="bi bi-chat-dots"></i>
    </button>

    <section v-else class="z-ai-panel" aria-label="Hỗ trợ Zestia">
      <header class="z-ai-header">
        <div>
          <strong>{{ humanMode ? 'Nhân viên Zestia' : 'ChatAI Zestia' }}</strong>
          <span v-if="humanMode">{{ supportStatusText }}</span>
          <span v-else>Hỗ trợ sản phẩm, voucher, đơn hàng</span>
        </div>
        <button class="z-ai-close" title="Đóng" @click="open = false">
          <i class="bi bi-x-lg"></i>
        </button>
      </header>

      <div ref="bodyRef" class="z-ai-body">
        <div v-for="(msg, index) in displayMessages" :key="msg.id || index" class="z-ai-msg" :class="msg.role">
          <div>
            <small v-if="humanMode && msg.senderName">{{ msg.senderName }}</small>
            {{ msg.content }}
          </div>
        </div>
        <div v-if="loading" class="z-ai-msg assistant">
          <div>{{ humanMode ? 'Đang gửi...' : 'Đang trả lời...' }}</div>
        </div>
      </div>

      <div v-if="!humanMode && hasAskedAi && !loading" class="z-ai-handoff">
        <span>ChatAI chưa giải quyết được?</span>
        <button type="button" :disabled="handoffLoading" @click="requestEmployee">
          <i class="bi bi-headset"></i>{{ handoffLoading ? 'Đang kết nối...' : 'Chat với nhân viên' }}
        </button>
      </div>
      <div v-if="humanMode && supportStatus === 'CLOSED'" class="z-ai-handoff">
        <span>Phiên hỗ trợ đã kết thúc.</span>
        <button type="button" @click="backToAi"><i class="bi bi-stars"></i>Quay lại ChatAI</button>
      </div>

      <form v-if="!humanMode || supportStatus !== 'CLOSED'" class="z-ai-form" @submit.prevent="send">
        <input
          v-model="draft"
          class="z-ai-input"
          :placeholder="humanMode ? 'Nhắn cho nhân viên đang trực...' : 'Hỏi về váy, size, voucher, đơn hàng...'"
          :disabled="loading"
          maxlength="1000"
        >
        <button class="z-ai-send" title="Gửi" :disabled="loading || draft.trim().length < 2">
          <i class="bi bi-send"></i>
        </button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { api } from '@/composables/useApi'

const open = ref(false)
const loading = ref(false)
const handoffLoading = ref(false)
const draft = ref('')
const bodyRef = ref(null)
const humanMode = ref(false)
const supportToken = ref('')
const supportStatus = ref('')
const supportEmployee = ref('')
const humanMessages = ref([])
let supportPoll = null
const messages = ref([
  {
    role: 'assistant',
    content: 'Chào bạn, mình có thể tư vấn sản phẩm, voucher đang có hoặc hỗ trợ tra cứu đơn hàng khi bạn đã đăng nhập.'
  }
])

const displayMessages = computed(() => humanMode.value ? humanMessages.value : messages.value)
const hasAskedAi = computed(() => messages.value.some(message => message.role === 'user'))
const supportStatusText = computed(() => {
  if (supportStatus.value === 'CLOSED') return 'Phiên hỗ trợ đã kết thúc'
  if (supportEmployee.value) return `${supportEmployee.value} đang hỗ trợ bạn`
  return 'Đang chờ nhân viên trong ca tiếp nhận'
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

async function send() {
  const text = draft.value.trim()
  if (text.length < 2 || loading.value) return
  if (humanMode.value) return sendHumanMessage(text)
  messages.value.push({ role: 'user', content: text })
  draft.value = ''
  loading.value = true
  await scrollToBottom()
  try {
    const history = messages.value
      .filter(msg => msg.role === 'user' || msg.role === 'assistant')
      .slice(-10)
      .map(msg => ({ role: msg.role, content: msg.content }))
    const res = await api().sendAiChat({ message: text, history })
    messages.value.push({
      role: 'assistant',
      content: res?.reply || 'Mình chưa nhận được phản hồi phù hợp. Bạn thử hỏi lại ngắn hơn nhé.'
    })
  } catch (e) {
    messages.value.push({
      role: 'assistant',
      content: e.reply || e.error || 'ChatAI đang tạm thời không phản hồi được. Bạn vui lòng thử lại sau.'
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function requestEmployee() {
  if (handoffLoading.value) return
  const lastQuestion = [...messages.value].reverse().find(message => message.role === 'user')?.content
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
      content: error.error || 'Hiện chưa thể kết nối với nhân viên. Bạn vui lòng thử lại sau.'
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
    humanMessages.value.push({ role: 'assistant', content: error.error || 'Không thể gửi tin nhắn lúc này.' })
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
  humanMessages.value = (snapshot.messages || []).map(message => ({
    id: message.id,
    role: message.senderType === 'CUSTOMER' ? 'user' : message.senderType === 'SYSTEM' ? 'system' : 'assistant',
    senderName: message.senderName,
    content: message.content
  }))
}

function startSupportPoll() {
  stopSupportPoll()
  supportPoll = window.setInterval(async () => {
    if (!supportToken.value || supportStatus.value === 'CLOSED') return
    try {
      applySupportSnapshot(await api().getCustomerSupportChat(supportToken.value))
      await scrollToBottom()
    } catch (_) {
      // Keep the current conversation visible during a temporary connection issue.
    }
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

async function scrollToBottom() {
  await nextTick()
  if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
}
</script>

<style scoped>
.z-ai-chat {
  position: fixed;
  right: 22px;
  bottom: 24px;
  z-index: 1200;
  font-family: var(--z-font-body);
}

.z-ai-toggle {
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: var(--z-dark);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16px 34px rgba(0, 0, 0, 0.22);
  transition: all 0.2s ease;
}

.z-ai-toggle:hover {
  background: var(--z-accent);
  transform: translateY(-2px);
}

.z-ai-toggle i { font-size: 22px; }

.z-ai-panel {
  width: min(360px, calc(100vw - 28px));
  height: min(540px, calc(100vh - 110px));
  background: #fff;
  border: 1px solid var(--z-gray-border);
  border-radius: 12px;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.22);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.z-ai-header {
  padding: 16px 18px;
  background: var(--z-dark);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.z-ai-header strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
}

.z-ai-header span {
  display: block;
  margin-top: 2px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.72);
}

.z-ai-close {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.z-ai-close:hover { background: var(--z-accent); }

.z-ai-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: var(--z-bg);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.z-ai-msg {
  display: flex;
}

.z-ai-msg div {
  max-width: 86%;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.45;
  white-space: pre-line;
}

.z-ai-msg.assistant { justify-content: flex-start; }
.z-ai-msg.assistant div {
  background: #fff;
  border: 1px solid var(--z-gray-border);
  color: var(--z-dark);
}

.z-ai-msg.user { justify-content: flex-end; }
.z-ai-msg.user div {
  background: var(--z-dark);
  color: #fff;
}

.z-ai-msg.system { justify-content: center; }
.z-ai-msg.system div {
  max-width: 94%;
  padding: 6px 8px;
  border: 0;
  background: transparent;
  color: var(--z-gray);
  font-size: 11px;
  text-align: center;
}
.z-ai-msg small { display: block; margin-bottom: 3px; color: var(--z-gray); font-size: 9px; }

.z-ai-handoff {
  padding: 9px 12px;
  border-top: 1px solid var(--z-gray-border);
  background: var(--z-bg-alt);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.z-ai-handoff span { color: var(--z-gray); font-size: 11px; }
.z-ai-handoff button {
  border: 0;
  background: transparent;
  color: var(--z-dark);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}
.z-ai-handoff button:hover { color: var(--z-accent); }
.z-ai-handoff button i { margin-right: 5px; }

.z-ai-form {
  padding: 12px;
  border-top: 1px solid var(--z-gray-border);
  display: flex;
  gap: 8px;
  background: #fff;
}

.z-ai-input {
  flex: 1;
  height: 42px;
  border: 1px solid var(--z-gray-border);
  border-radius: 8px;
  padding: 0 12px;
  font-size: 13px;
  outline: none;
}

.z-ai-input:focus { border-color: var(--z-dark); }

.z-ai-send {
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 8px;
  background: var(--z-dark);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.z-ai-send:hover:not(:disabled) { background: var(--z-accent); }
.z-ai-send:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 575px) {
  .z-ai-chat {
    right: 14px;
    bottom: 14px;
  }

  .z-ai-panel {
    width: calc(100vw - 28px);
    height: min(560px, calc(100vh - 90px));
  }
}
</style>
