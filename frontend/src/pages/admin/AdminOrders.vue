<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý đơn hàng</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredOrders.length }} đơn hàng</p>
      </div>
    </div>

    <!-- Status tabs -->
    <div class="d-flex gap-2 mb-3 flex-wrap">
      <button v-for="tab in statusTabs" :key="tab.value"
              class="z-tab" :class="{ active: activeStatus === tab.value }"
              @click="activeStatus = tab.value">
        {{ tab.label }}
        <span class="z-tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- Search -->
    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-2" style="max-width:400px">
        <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
        <input v-model="search" class="lm-input" placeholder="Tìm theo mã đơn, tên khách..." style="border:none;padding:8px 0;box-shadow:none">
      </div>
    </div>

    <!-- Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Sản phẩm</th>
            <th>Tổng tiền</th>
            <th>Thanh toán</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th style="width:140px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in filteredOrders" :key="o.id" class="z-clickable-row" @click="openDetail(o)">
            <td style="font-weight:600">{{ o.id }}</td>
            <td>
              <div style="font-weight:500">{{ o.customer }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ o.phone }}</div>
            </td>
            <td>{{ o.items }} sản phẩm</td>
            <td style="font-weight:600">{{ o.total }}</td>
            <td>{{ o.payment }}</td>
            <td><span class="z-status" :class="o.statusClass">{{ o.status }}</span></td>
            <td style="color:var(--z-gray)">{{ o.date }}</td>
            <td @click.stop>
              <div class="d-flex gap-1">
                <button v-if="canAdvance(o)" class="z-action-btn success" :title="nextStatusLabel(o)" @click="confirmAdvance(o)">
                  <i class="bi bi-check-lg"></i>
                </button>
                <button v-if="canCancel(o)" class="z-action-btn danger" title="Huỷ đơn" @click="confirmCancel(o)">
                  <i class="bi bi-x-lg"></i>
                </button>
                <button class="z-action-btn" title="Chi tiết" @click="openDetail(o)">
                  <i class="bi bi-eye"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredOrders.length === 0" class="text-center py-5">
        <i class="bi bi-inbox" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có đơn hàng nào</p>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="showDetail" class="z-modal-overlay" @click.self="showDetail = false">
      <div class="z-modal" style="max-width:750px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết đơn hàng</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ detailData?.maHoaDon }}</div>
          </div>
          <button class="z-icon-btn" @click="showDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingDetail" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
        </div>

        <div v-else-if="detailData">
          <!-- Status timeline -->
          <div class="d-flex align-items-center gap-2 mb-4 pb-3" style="border-bottom:1px solid var(--z-gray-border);overflow-x:auto">
            <div v-for="(step, i) in statusSteps" :key="i"
                 class="z-step" :class="{ active: detailData.trangThai >= i, current: detailData.trangThai === i }">
              <div class="z-step-dot"></div>
              <div class="z-step-label">{{ step }}</div>
              <div v-if="i < statusSteps.length - 1" class="z-step-line" :class="{ filled: detailData.trangThai > i }"></div>
            </div>
          </div>

          <!-- Info -->
          <div class="row g-3 mb-4">
            <div class="col-6">
              <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Khách hàng</div>
              <div style="font-size:14px;font-weight:500">{{ detailData.khachHang || 'N/A' }}</div>
            </div>
            <div class="col-6">
              <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Số điện thoại</div>
              <div style="font-size:14px;font-weight:500">{{ detailData.soDienThoai || 'N/A' }}</div>
            </div>
            <div class="col-6">
              <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Thanh toán</div>
              <div style="font-size:14px;font-weight:500">{{ detailData.hinhThucThanhToan || 'N/A' }}</div>
            </div>
            <div class="col-6">
              <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Ngày tạo</div>
              <div style="font-size:14px;font-weight:500">{{ detailData.ngayTao ? new Date(detailData.ngayTao).toLocaleString('vi-VN') : '' }}</div>
            </div>
            <div v-if="detailData.diaChiGiaoHang" class="col-12">
              <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Địa chỉ giao hàng</div>
              <div style="font-size:14px;font-weight:500">{{ detailData.diaChiGiaoHang }}</div>
            </div>
          </div>

          <!-- Items -->
          <h4 style="font-size:14px;font-weight:600;margin-bottom:12px">Sản phẩm ({{ detailData.chiTiets?.length || 0 }})</h4>
          <div class="d-flex flex-column gap-0 mb-4" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);overflow:hidden">
            <div v-for="item in detailData.chiTiets" :key="item.id"
                 class="d-flex align-items-center gap-3" style="padding:12px 14px;border-bottom:1px solid var(--z-gray-border)">
              <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover">
                <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:16px;color:var(--z-gray-light)">
                  <i class="bi bi-image"></i>
                </div>
              </div>
              <div class="flex-grow-1">
                <div style="font-size:13px;font-weight:500;color:var(--z-dark)">{{ item.tenVay || 'Sản phẩm' }}</div>
                <div style="font-size:12px;color:var(--z-gray)">
                  <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                    <span v-if="item.maHex" :style="{ width:'8px', height:'8px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                    {{ item.mauSac }}
                  </span>
                  <span v-if="item.mauSac && item.kichThuoc"> · </span>
                  <span v-if="item.kichThuoc">Size {{ item.kichThuoc }}</span>
                </div>
              </div>
              <div class="text-end">
                <div style="font-size:13px;font-weight:600">{{ fmtPrice(item.donGia) }}</div>
                <div style="font-size:11px;color:var(--z-gray)">x{{ item.soLuong }}</div>
              </div>
            </div>
          </div>

          <div v-if="detailData.ghiChu" class="mb-3 p-3" style="background:#fef3cd;border-radius:var(--z-radius);font-size:13px">
            <strong>Ghi chú:</strong> {{ detailData.ghiChu }}
          </div>

          <div class="d-flex justify-content-between align-items-center pt-3" style="border-top:2px solid var(--z-dark)">
            <div style="font-size:16px;font-weight:600">Tổng cộng</div>
            <div style="font-size:22px;font-weight:700;color:var(--z-accent)">{{ fmtPrice(detailData.tongTien) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Action Modal -->
    <div v-if="showConfirm" class="z-modal-overlay" @click.self="showConfirm = false">
      <div class="z-modal" style="max-width:440px">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h3 style="font-size:16px;font-weight:600;margin:0">{{ confirmTitle }}</h3>
          <button class="z-icon-btn" @click="showConfirm = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <p style="font-size:14px;color:var(--z-gray);margin-bottom:16px">{{ confirmMessage }}</p>
        <div v-if="confirmType === 'cancel'" class="mb-3">
          <label class="z-label">Lý do huỷ <span style="color:var(--z-accent)">*</span></label>
          <textarea v-model="cancelNote" class="lm-input" rows="3" placeholder="Nhập lý do huỷ đơn hàng..."></textarea>
        </div>
        <div v-else class="mb-3">
          <label class="z-label">Ghi chú (tuỳ chọn)</label>
          <input v-model="actionNote" class="lm-input" placeholder="Ghi chú thêm...">
        </div>
        <div class="d-flex justify-content-end gap-2">
          <button class="lm-btn-secondary" @click="showConfirm = false">Huỷ bỏ</button>
          <button class="lm-btn-primary" @click="executeAction"
                  :disabled="confirmType === 'cancel' && !cancelNote.trim()"
                  :style="confirmType === 'cancel' ? 'background:var(--z-accent);border-color:var(--z-accent)' : ''">
            <span>{{ confirmType === 'cancel' ? 'Xác nhận huỷ' : 'Xác nhận' }}</span>
          </button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

const statusMap = { 0: { text: 'Chờ xử lý', cls: 'pending' }, 1: { text: 'Đã xác nhận', cls: 'warning' }, 2: { text: 'Đang giao', cls: 'info' }, 3: { text: 'Hoàn thành', cls: 'success' }, 4: { text: 'Đã huỷ', cls: 'danger' } }
const statusSteps = ['Chờ xử lý', 'Xác nhận', 'Đang giao', 'Hoàn thành']

const search = ref('')
const activeStatus = ref('all')
const allOrders = ref([])
const rawOrders = ref([])

const showDetail = ref(false)
const detailData = ref(null)
const loadingDetail = ref(false)

const showConfirm = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmType = ref('')
const confirmOrder = ref(null)
const confirmNewStatus = ref(0)
const cancelNote = ref('')
const actionNote = ref('')

onMounted(async () => {
  await loadOrders()
})

async function loadOrders() {
  try {
    const data = await api().getHoaDon()
    rawOrders.value = data
    const sorted = [...data].sort((a, b) => {
      const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
      const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
      return db - da
    })
    allOrders.value = sorted.map(o => {
      const st = statusMap[o.trangThai] || statusMap[0]
      return {
        id: o.maHoaDon, dbId: o.id, customer: o.khachHang || 'N/A', phone: o.soDienThoai || '',
        items: o.soSanPham || 0, total: fmtPrice(o.tongTien), payment: o.hinhThucThanhToan || 'N/A',
        status: st.text, statusClass: st.cls, statusValue: String(o.trangThai ?? 0),
        date: o.ngayTao ? new Date(o.ngayTao).toLocaleDateString('vi-VN') : '',
        raw: o
      }
    })
  } catch (e) { console.error('Không thể tải đơn hàng:', e) }
}

const statusTabs = computed(() => [
  { label: 'Tất cả',        value: 'all',  count: allOrders.value.length },
  { label: 'Chờ xử lý',     value: '0',    count: allOrders.value.filter(o => o.statusValue === '0').length },
  { label: 'Đã xác nhận',   value: '1',    count: allOrders.value.filter(o => o.statusValue === '1').length },
  { label: 'Đang giao',     value: '2',    count: allOrders.value.filter(o => o.statusValue === '2').length },
  { label: 'Hoàn thành',    value: '3',    count: allOrders.value.filter(o => o.statusValue === '3').length },
  { label: 'Đã huỷ',        value: '4',    count: allOrders.value.filter(o => o.statusValue === '4').length },
])

const filteredOrders = computed(() => {
  return allOrders.value.filter(o => {
    const matchSearch = !search.value || o.id.toLowerCase().includes(search.value.toLowerCase()) || o.customer.toLowerCase().includes(search.value.toLowerCase())
    const matchStatus = activeStatus.value === 'all' || o.statusValue === activeStatus.value
    return matchSearch && matchStatus
  })
})

function canAdvance(o) {
  const v = Number(o.statusValue)
  return v >= 0 && v < 3
}

function canCancel(o) {
  const v = Number(o.statusValue)
  return v === 0
}

function nextStatusLabel(o) {
  const v = Number(o.statusValue)
  if (v === 0) return 'Xác nhận đơn'
  if (v === 1) return 'Chuyển giao hàng'
  if (v === 2) return 'Hoàn thành'
  return ''
}

function confirmAdvance(o) {
  const v = Number(o.statusValue)
  const nextVal = v + 1
  const labels = { 1: 'xác nhận', 2: 'chuyển sang đang giao', 3: 'đánh dấu hoàn thành' }
  confirmTitle.value = nextStatusLabel(o)
  confirmMessage.value = `Bạn có chắc muốn ${labels[nextVal]} đơn hàng ${o.id}?`
  confirmType.value = 'advance'
  confirmOrder.value = o
  confirmNewStatus.value = nextVal
  actionNote.value = ''
  showConfirm.value = true
}

function confirmCancel(o) {
  confirmTitle.value = 'Huỷ đơn hàng'
  confirmMessage.value = `Bạn có chắc muốn huỷ đơn hàng ${o.id}? Hành động này không thể hoàn tác.`
  confirmType.value = 'cancel'
  confirmOrder.value = o
  confirmNewStatus.value = 4
  cancelNote.value = ''
  showConfirm.value = true
}

async function executeAction() {
  const note = confirmType.value === 'cancel' ? cancelNote.value.trim() : actionNote.value.trim()
  if (confirmType.value === 'cancel' && !note) {
    showToast('Vui lòng nhập lý do huỷ đơn')
    return
  }
  try {
    await api().updateOrderStatus(confirmOrder.value.dbId, confirmNewStatus.value, note || null)
    showToast('Cập nhật trạng thái thành công!')
    showConfirm.value = false
    await loadOrders()
  } catch (e) {
    showToast('Lỗi: ' + (e.message || 'Không thể cập nhật'))
  }
}

async function openDetail(o) {
  showDetail.value = true
  loadingDetail.value = true
  try {
    detailData.value = await api().getHoaDonById(o.dbId)
  } catch (e) {
    detailData.value = o.raw
  } finally {
    loadingDetail.value = false
  }
}
</script>

<style scoped>
.z-tab {
  padding: 8px 16px; border: 1px solid var(--z-gray-border);
  background: var(--z-white); border-radius: 20px;
  font-size: 13px; font-weight: 500; color: var(--z-gray);
  cursor: pointer; transition: all 0.2s;
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--z-font-body);
}
.z-tab:hover { border-color: var(--z-dark); color: var(--z-dark); }
.z-tab.active { background: var(--z-dark); color: var(--z-white); border-color: var(--z-dark); }
.z-tab-count { font-size: 11px; font-weight: 700; opacity: 0.7; }
.z-clickable-row { cursor: pointer; }
.z-clickable-row:hover td { background: var(--z-accent-soft) !important; }
.z-action-btn {
  width: 30px; height: 30px; border: 1px solid var(--z-gray-border); background: var(--z-white);
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 12px;
}
.z-action-btn:hover { background: var(--z-bg-alt); }
.z-action-btn.success { color: #16a34a; border-color: #bbf7d0; }
.z-action-btn.success:hover { background: #dcfce7; }
.z-action-btn.danger { color: #dc2626; border-color: #fecaca; }
.z-action-btn.danger:hover { background: #fee2e2; }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-icon-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
.z-step {
  display: flex; align-items: center; gap: 6px; flex-shrink: 0;
}
.z-step-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--z-gray-border); transition: all 0.2s;
}
.z-step.active .z-step-dot { background: var(--z-accent); }
.z-step.current .z-step-dot { box-shadow: 0 0 0 3px var(--z-accent-soft); }
.z-step-label { font-size: 12px; font-weight: 500; color: var(--z-gray-light); white-space: nowrap; }
.z-step.active .z-step-label { color: var(--z-dark); }
.z-step-line { width: 32px; height: 2px; background: var(--z-gray-border); margin: 0 4px; }
.z-step-line.filled { background: var(--z-accent); }
</style>
