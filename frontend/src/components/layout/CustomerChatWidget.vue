<template>
  <div class="z-ai-chat">
    <button v-if="!open" class="z-ai-toggle" title="ChatAI Zestia" @click="open = true">
      <i class="bi bi-chat-dots"></i>
    </button>

    <section v-else class="z-ai-panel" aria-label="ChatAI Zestia">
      <header class="z-ai-header">
        <div>
          <strong>ChatAI Zestia</strong>
          <span>Hỗ trợ sản phẩm, voucher, đơn hàng</span>
        </div>
        <button class="z-ai-close" title="Đóng" @click="open = false">
          <i class="bi bi-x-lg"></i>
        </button>
      </header>

      <div ref="bodyRef" class="z-ai-body">
        <div v-for="(msg, index) in messages" :key="index" class="z-ai-msg" :class="msg.role">
          <div>{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="z-ai-msg assistant">
          <div>Đang trả lời...</div>
        </div>
      </div>

      <form class="z-ai-form" @submit.prevent="send">
        <input
          v-model="draft"
          class="z-ai-input"
          placeholder="Hỏi về váy, size, voucher, đơn hàng..."
          :disabled="loading"
        >
        <button class="z-ai-send" title="Gửi" :disabled="loading || !draft.trim()">
          <i class="bi bi-send"></i>
        </button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { api } from '@/composables/useApi'

const open = ref(false)
const loading = ref(false)
const draft = ref('')
const bodyRef = ref(null)
const messages = ref([
  {
    role: 'assistant',
    content: 'Chào bạn, mình có thể tư vấn sản phẩm, voucher đang có hoặc hỗ trợ tra cứu đơn hàng khi bạn đã đăng nhập.'
  }
])

async function send() {
  const text = draft.value.trim()
  if (!text || loading.value) return
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
