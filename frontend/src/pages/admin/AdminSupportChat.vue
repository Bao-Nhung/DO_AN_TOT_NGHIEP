<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-3">
      <div>
        <h1 class="z-display mb-1 z-page-title">Hỗ trợ khách hàng</h1>
        <p class="z-page-subtitle">Tiếp nhận các trường hợp ChatAI chưa giải quyết được</p>
      </div>
      <button class="lm-btn-secondary" type="button" :disabled="loadingList" @click="refreshList">
        <i class="bi bi-arrow-clockwise me-1"></i>Làm mới
      </button>
    </div>

    <div class="z-support-shell">
      <aside class="z-support-list">
        <div class="z-support-list-head">
          <strong>Cuộc trò chuyện</strong>
          <span>{{ conversations.length }}</span>
        </div>
        <div v-if="loadingList && !conversations.length" class="z-support-empty">Đang tải yêu cầu...</div>
        <button
          v-for="conversation in pagedConversations"
          :key="conversation.id"
          class="z-conversation-item"
          :class="{ active: selected?.id === conversation.id }"
          type="button"
          @click="openConversation(conversation)"
        >
          <div class="d-flex justify-content-between align-items-center gap-2">
            <strong>{{ conversation.customerName }}</strong>
            <span class="z-chat-status" :class="conversation.status.toLowerCase()">
              {{ conversation.status === 'WAITING' ? 'Chờ nhận' : 'Đang hỗ trợ' }}
            </span>
          </div>
          <p>{{ conversation.lastMessage || conversation.title }}</p>
          <small>{{ formatDateTime(conversation.updatedAt) }}</small>
        </button>
        <div v-if="!loadingList && !conversations.length" class="z-support-empty">
          <i class="bi bi-chat-square-check"></i>
          Chưa có yêu cầu đang chờ
        </div>
        <div v-if="conversations.length" class="z-support-pagination">
          <PageSizeSelect v-model="pageSize" :options="[5, 10, 20, 50]" />
          <div v-if="totalPages > 1">
            <button type="button" :disabled="currentPage === 1" aria-label="Trang trước" @click="currentPage--"><i class="bi bi-chevron-left"></i></button>
            <span>{{ currentPage }} / {{ totalPages }}</span>
            <button type="button" :disabled="currentPage === totalPages" aria-label="Trang sau" @click="currentPage++"><i class="bi bi-chevron-right"></i></button>
          </div>
        </div>
      </aside>

      <section class="z-support-chat">
        <template v-if="selected">
          <header class="z-support-chat-head">
            <div>
              <strong>{{ selected.customerName }}</strong>
              <span>{{ selected.employeeName ? `Phụ trách: ${selected.employeeName}` : 'Chưa có người tiếp nhận' }}</span>
            </div>
            <button class="z-icon-btn" title="Kết thúc hỗ trợ" aria-label="Kết thúc hỗ trợ" type="button" @click="closeConversation">
              <i class="bi bi-check2-circle"></i>
            </button>
          </header>

          <div ref="messageBody" class="z-support-messages">
            <div
              v-for="message in selected.messages || []"
              :key="message.id"
              class="z-support-message"
              :class="messageClass(message.senderType)"
            >
              <div>
                <small>{{ message.senderName || senderLabel(message.senderType) }}</small>
                <p>{{ message.content }}</p>
                <time>{{ formatClock(message.createdAt) }}</time>
              </div>
            </div>
          </div>

          <form class="z-support-compose" @submit.prevent="sendMessage">
            <input
              v-model="draft"
              class="lm-input"
              maxlength="1000"
              placeholder="Nhập câu trả lời cho khách hàng..."
              :disabled="sending"
            >
            <button class="lm-btn-primary" :disabled="sending || draft.trim().length < 2" title="Gửi" aria-label="Gửi tin nhắn" type="submit">
              <span><i class="bi bi-send"></i></span>
            </button>
          </form>
        </template>
        <div v-else class="z-support-placeholder">
          <i class="bi bi-headset"></i>
          <strong>Chọn một cuộc trò chuyện</strong>
          <span>Yêu cầu chờ sẽ được nhận khi bạn mở cuộc trò chuyện.</span>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'
import { api } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useToast } from '@/composables/useToast'

const { confirmDialog } = useConfirm()
const { showToast } = useToast()
const conversations = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const selected = ref(null)
const loadingList = ref(false)
const sending = ref(false)
const draft = ref('')
const messageBody = ref(null)
let pollTimer = null
const totalPages = computed(() => Math.max(1, Math.ceil(conversations.value.length / pageSize.value)))
const pagedConversations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return conversations.value.slice(start, start + pageSize.value)
})

watch(pageSize, () => { currentPage.value = 1 })
watch(totalPages, total => {
  if (currentPage.value > total) currentPage.value = total
})

onMounted(async () => {
  await refreshList()
  pollTimer = window.setInterval(poll, 4000)
})

onBeforeUnmount(() => {
  if (pollTimer) window.clearInterval(pollTimer)
})

async function refreshList() {
  loadingList.value = true
  try {
    conversations.value = await api().getStaffSupportChats()
  } catch (error) {
    showToast(error.error || 'Không thể tải yêu cầu hỗ trợ')
  } finally {
    loadingList.value = false
  }
}

async function openConversation(conversation) {
  try {
    selected.value = conversation.status === 'WAITING'
      ? await api().claimStaffSupportChat(conversation.id)
      : await api().getStaffSupportChat(conversation.id)
    await refreshList()
    await scrollBottom()
  } catch (error) {
    showToast(error.error || 'Không thể mở cuộc trò chuyện')
    await refreshList()
  }
}

async function sendMessage() {
  const message = draft.value.trim()
  if (!selected.value || message.length < 2 || sending.value) return
  sending.value = true
  try {
    selected.value = await api().sendStaffSupportMessage(selected.value.id, message)
    draft.value = ''
    await refreshList()
    await scrollBottom()
  } catch (error) {
    showToast(error.error || 'Không thể gửi tin nhắn')
  } finally {
    sending.value = false
  }
}

async function closeConversation() {
  if (!selected.value) return
  const accepted = await confirmDialog({
    title: 'Kết thúc hỗ trợ',
    message: `Xác nhận đã hỗ trợ xong cho ${selected.value.customerName}?`,
    confirmText: 'Kết thúc'
  })
  if (!accepted) return
  try {
    await api().closeStaffSupportChat(selected.value.id)
    selected.value = null
    showToast('Đã kết thúc phiên hỗ trợ')
    await refreshList()
  } catch (error) {
    showToast(error.error || 'Không thể kết thúc phiên hỗ trợ')
  }
}

async function poll() {
  try {
    const list = await api().getStaffSupportChats()
    conversations.value = list
    if (selected.value) {
      const stillOpen = list.some(item => item.id === selected.value.id)
      if (!stillOpen) selected.value = null
      else {
        selected.value = await api().getStaffSupportChat(selected.value.id)
        await scrollBottom()
      }
    }
  } catch (_) {
    // A temporary polling failure must not interrupt the reply being typed.
  }
}

function messageClass(senderType) {
  if (senderType === 'CUSTOMER') return 'customer'
  if (senderType === 'SYSTEM') return 'system'
  return 'employee'
}

function senderLabel(senderType) {
  return senderType === 'CUSTOMER' ? 'Khách hàng' : senderType === 'SYSTEM' ? 'Zestia' : 'Nhân viên'
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''
}

function formatClock(value) {
  return value ? new Date(value).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : ''
}

async function scrollBottom() {
  await nextTick()
  if (messageBody.value) messageBody.value.scrollTop = messageBody.value.scrollHeight
}
</script>

<style scoped>
.z-page-title { color: var(--z-dark); font-size: 26px; font-weight: 500; }
.z-page-subtitle { margin: 0; color: var(--z-gray); font-size: 14px; }
.z-support-shell { min-height: 640px; display: grid; grid-template-columns: minmax(260px, 340px) 1fr; overflow: hidden; border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-support-list { min-width: 0; border-right: 1px solid var(--z-gray-border); overflow-y: auto; }
.z-support-list-head { min-height: 58px; padding: 16px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--z-gray-border); }
.z-support-list-head strong { color: var(--z-dark); font-size: 14px; }
.z-support-list-head span { min-width: 24px; padding: 3px 7px; border-radius: 12px; background: var(--z-bg-alt); color: var(--z-gray); font-size: 11px; text-align: center; }
.z-conversation-item { width: 100%; padding: 15px 16px; border: 0; border-bottom: 1px solid var(--z-gray-border); background: var(--z-white); color: var(--z-dark); text-align: left; }
.z-conversation-item:hover, .z-conversation-item.active { background: var(--z-bg-alt); }
.z-conversation-item.active { box-shadow: inset 3px 0 var(--z-accent); }
.z-conversation-item strong { overflow: hidden; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.z-conversation-item p { margin: 7px 0 4px; overflow: hidden; color: var(--z-gray); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.z-conversation-item small { color: var(--z-gray-light); font-size: 10px; }
.z-chat-status { flex-shrink: 0; padding: 3px 6px; border-radius: 4px; font-size: 9px; font-weight: 700; }
.z-chat-status.waiting { background: #fef3c7; color: #92400e; }
.z-chat-status.active { background: #dcfce7; color: #166534; }
.z-support-chat { min-width: 0; display: flex; flex-direction: column; }
.z-support-chat-head { min-height: 58px; padding: 10px 16px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--z-gray-border); }
.z-support-chat-head strong, .z-support-chat-head span { display: block; }
.z-support-chat-head strong { color: var(--z-dark); font-size: 14px; }
.z-support-chat-head span { margin-top: 2px; color: var(--z-gray); font-size: 11px; }
.z-support-messages { flex: 1; padding: 20px; overflow-y: auto; background: var(--z-bg); }
.z-support-message { display: flex; margin-bottom: 12px; }
.z-support-message > div { max-width: min(76%, 620px); padding: 9px 12px; border: 1px solid var(--z-gray-border); background: var(--z-white); }
.z-support-message.employee { justify-content: flex-end; }
.z-support-message.employee > div { border-color: var(--z-dark); background: var(--z-dark); color: var(--z-white); }
.z-support-message.system { justify-content: center; }
.z-support-message.system > div { padding: 6px 10px; border: 0; background: transparent; color: var(--z-gray); text-align: center; }
.z-support-message small, .z-support-message time { display: block; font-size: 9px; opacity: .7; }
.z-support-message p { margin: 4px 0; font-size: 13px; line-height: 1.5; white-space: pre-wrap; }
.z-support-message time { text-align: right; }
.z-support-compose { padding: 12px; display: grid; grid-template-columns: 1fr 44px; gap: 8px; border-top: 1px solid var(--z-gray-border); }
.z-support-compose .lm-btn-primary { width: 44px; min-width: 44px; padding: 0; justify-content: center; }
.z-support-placeholder, .z-support-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--z-gray); text-align: center; }
.z-support-placeholder { flex: 1; padding: 30px; }
.z-support-placeholder i, .z-support-empty i { color: var(--z-accent); font-size: 34px; }
.z-support-placeholder strong { color: var(--z-dark); }
.z-support-placeholder span, .z-support-empty { font-size: 12px; }
.z-support-empty { min-height: 160px; padding: 20px; }
.z-support-pagination { position:sticky;bottom:0;display:flex;align-items:center;justify-content:space-between;gap:8px;padding:10px 12px;border-top:1px solid var(--z-gray-border);background:var(--z-white); }
.z-support-pagination > div { display:flex;align-items:center;gap:6px;color:var(--z-gray);font-size:11px; }
.z-support-pagination button { width:28px;height:28px;border:1px solid var(--z-gray-border);background:var(--z-white);color:var(--z-dark); }
.z-support-pagination button:hover:not(:disabled) { border-color:var(--z-accent);color:var(--z-accent); }
.z-support-pagination button:disabled { opacity:.4; }
.z-icon-btn { width: 34px; height: 34px; border: 0; background: var(--z-bg-alt); color: var(--z-dark); }
.z-icon-btn:hover { background: var(--z-accent); color: var(--z-white); }
@media (max-width: 820px) {
  .z-support-shell { min-height: 720px; grid-template-columns: 1fr; grid-template-rows: minmax(180px, 240px) 1fr; }
  .z-support-list { border-right: 0; border-bottom: 1px solid var(--z-gray-border); }
  .z-support-message > div { max-width: 88%; }
}
</style>
