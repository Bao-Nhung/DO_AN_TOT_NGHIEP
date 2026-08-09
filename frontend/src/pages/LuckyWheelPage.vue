<template>
  <div class="z-lucky-page">
    <header class="z-lucky-heading">
      <div class="container z-heading-inner">
        <div>
          <p class="lm-eyebrow">Zestia Gift Edition</p>
          <h1 class="z-display">Vòng quay <em>thời trang</em></h1>
        </div>
        <div class="z-heading-aside">
          <p>Một đặc quyền dành cho đơn hàng đủ điều kiện, với kết quả được ghi nhận minh bạch và quà hiện vật nhận tại showroom.</p>
          <div class="z-heading-stats">
            <div><strong>{{ segments.length ? String(segments.length).padStart(2, '0') : '--' }}</strong><span>Lựa chọn quà tặng</span></div>
            <div><strong>01</strong><span>Lượt duy nhất mỗi đơn</span></div>
          </div>
        </div>
      </div>
    </header>

    <div class="z-editorial-ribbon">
      <div class="container">
        <strong>THE GIFT EDIT</strong><i></i>
        <span>{{ campaign?.tenChienDich || 'Vòng quay quà tặng Zestia' }}</span><i></i>
        <span>{{ campaignPeriod }}</span><i></i>
        <span>Nhận quà tại showroom Hà Nội</span>
      </div>
    </div>

    <main class="container z-lucky-content">
      <div v-if="loadingCampaign" class="z-lucky-loading">
        <span class="spinner-border" aria-hidden="true"></span>
        <span>Đang chuẩn bị bộ quà tặng...</span>
      </div>

      <div v-else-if="!campaign?.active" class="z-lucky-empty">
        <i class="bi bi-calendar2-heart" aria-hidden="true"></i>
        <h2>Chưa có vòng quay đang diễn ra</h2>
        <p>Chương trình mới sẽ được hiển thị tại đây khi được mở.</p>
        <RouterLink to="/collections" class="lm-btn-primary"><span>Tiếp tục mua sắm</span></RouterLink>
      </div>

      <template v-else>
        <section class="z-lucky-grid">
          <div class="z-wheel-column">
            <div class="z-wheel-kicker">
              <span>{{ campaign.maChienDich }}</span>
              <span>{{ String(segments.length).padStart(2, '0') }} lựa chọn</span>
            </div>
            <div class="z-wheel-stage" :class="{ spinning }">
              <div class="z-wheel-pointer" aria-hidden="true"><span>CHỌN</span></div>
              <div class="z-wheel" :style="wheelStyle">
                <div
                  v-for="(segment, index) in segments"
                  :key="segment.id"
                  class="z-wheel-label-position"
                  :style="segmentPosition(index)"
                >
                  <div class="z-wheel-label" :style="[segmentLabelRotation(index), { '--segment-color': segment.mauHienThi }]" :title="segment.tenPhanThuong">
                    <span class="z-segment-visual">
                      <img v-if="segment.anhBieuTuong" :src="segment.anhBieuTuong" alt="" @error="hideBrokenIcon(segment)">
                      <i v-else class="bi" :class="segment.bieuTuong" aria-hidden="true"></i>
                    </span>
                    <strong>{{ padIndex(index) }}</strong>
                  </div>
                </div>
              </div>
              <div class="z-wheel-center" aria-hidden="true">
                <img src="/images/brand/zestia-mark.png" alt="">
                <span>GIFT EDIT</span>
              </div>
            </div>
            <p class="z-wheel-note"><i class="bi bi-shield-check"></i>Kết quả do máy chủ chọn và mỗi đơn chỉ được ghi nhận một lần.</p>
          </div>

          <div class="z-lucky-panel">
            <div class="z-campaign-meta">
              <span class="z-live-label"><i></i>Đang diễn ra</span>
              <span>{{ formatDate(campaign.ngayKetThuc) }}</span>
            </div>
            <p class="z-panel-index">THE GIFT EDIT / {{ campaign.maChienDich }}</p>
            <h2>{{ campaign.tenChienDich }}</h2>
            <p>{{ campaign.moTa }}</p>

            <div class="z-lucky-rule">
              <span>01</span>
              <div><strong>Điều kiện tham gia</strong><small>Đơn đã giao thành công từ {{ formatMoney(campaign.giaTriDonToiThieu) }}</small></div>
            </div>

            <form class="z-lucky-form" @submit.prevent="checkOrder">
              <div>
                <label for="lucky-order-code">Mã đơn hàng</label>
                <div class="z-input-with-icon">
                  <i class="bi bi-upc-scan" aria-hidden="true"></i>
                  <input id="lucky-order-code" v-model.trim="form.orderCode" class="lm-input" maxlength="80" placeholder="Ví dụ: HDAUG26001" autocomplete="off" @input="clearEligibility">
                </div>
              </div>
              <div>
                <label for="lucky-phone">Số điện thoại mua hàng</label>
                <div class="z-input-with-icon">
                  <i class="bi bi-telephone" aria-hidden="true"></i>
                  <input id="lucky-phone" v-model="form.phone" class="lm-input" inputmode="numeric" maxlength="15" placeholder="Nhập số điện thoại trên đơn" @input="normalizePhone">
                </div>
              </div>
              <button class="lm-btn-primary z-lucky-check" type="submit" :disabled="checking || spinning">
                <span v-if="checking" class="spinner-border spinner-border-sm" aria-hidden="true"></span>
                <i v-else class="bi bi-search" aria-hidden="true"></i>
                <span>{{ checking ? 'Đang kiểm tra...' : 'Kiểm tra lượt quay' }}</span>
              </button>
            </form>

            <div v-if="eligibility" class="z-eligibility" :class="eligibility.eligible ? 'success' : 'notice'" role="status">
              <i class="bi" :class="eligibility.eligible ? 'bi-check-circle-fill' : 'bi-info-circle-fill'" aria-hidden="true"></i>
              <div>
                <strong>{{ eligibility.eligible ? 'Đơn hàng hợp lệ' : eligibility.alreadyPlayed ? 'Lượt quay đã được dùng' : 'Chưa đủ điều kiện' }}</strong>
                <span>{{ eligibility.message }}</span>
              </div>
            </div>

            <button v-if="eligibility?.eligible" type="button" class="z-spin-button" :disabled="spinning" @click="spin">
              <i class="bi bi-stars" aria-hidden="true"></i>
              <span>{{ spinning ? 'Đang chọn món quà của bạn...' : 'Bắt đầu vòng quay' }}</span>
            </button>

            <div v-if="result" class="z-result" :class="{ winner: result.trungThuong }" aria-live="polite">
              <div class="z-result-icon">
                <img v-if="resultVisual?.anhBieuTuong" :src="resultVisual.anhBieuTuong" alt="" @error="hideBrokenIcon(resultVisual)">
                <i v-else class="bi" :class="resultVisual?.bieuTuong || (result.trungThuong ? 'bi-gift-fill' : 'bi-stars')"></i>
              </div>
              <div class="z-result-copy">
                <span>{{ result.trungThuong ? 'Chúc mừng bạn nhận được' : 'Kết quả lượt quay' }}</span>
                <strong>{{ result.tenKetQua }}</strong>
              </div>
              <div v-if="result.trungThuong" class="z-claim-code">
                <span>Mã nhận quà</span>
                <button type="button" title="Sao chép mã nhận quà" @click="copyClaimCode">
                  <strong>{{ result.maNhanThuong }}</strong><i class="bi bi-copy"></i>
                </button>
                <small>Mang mã đơn, số điện thoại và mã này tới showroom Zestia để nhận quà.</small>
              </div>
            </div>
          </div>
        </section>

        <section class="z-gift-edit" aria-labelledby="gift-edit-title">
          <div class="z-gift-heading">
            <div><p class="lm-eyebrow">The Gift Edit</p><h2 id="gift-edit-title" class="z-display">Bộ quà tháng này</h2></div>
            <p>Mỗi ô là một phần trong bộ sưu tập quà tặng. Số thứ tự tương ứng trực tiếp với ký hiệu trên vòng quay.</p>
          </div>
          <div class="z-gift-grid">
            <article v-for="(segment, index) in segments" :key="`gift-${segment.id}`" :style="{ '--gift-color': segment.mauHienThi }">
              <span class="z-gift-number">{{ padIndex(index) }}</span>
              <span class="z-gift-icon">
                <img v-if="segment.anhBieuTuong" :src="segment.anhBieuTuong" alt="" @error="hideBrokenIcon(segment)">
                <i v-else class="bi" :class="segment.bieuTuong"></i>
              </span>
              <div><strong>{{ segment.tenPhanThuong }}</strong><small>{{ segment.loaiPhanThuong === 'VAT_PHAM' ? 'Quà hiện vật tại showroom' : 'Một lời chúc cho lượt kế tiếp' }}</small></div>
            </article>
          </div>
        </section>

        <div class="z-steps-heading">
          <p class="lm-eyebrow">How It Works</p>
          <h2 class="z-display">Từ hóa đơn đến <em>món quà</em></h2>
        </div>
        <section class="z-lucky-steps">
          <article v-for="(step, index) in steps" :key="step.title">
            <span>0{{ index + 1 }}</span><i class="bi" :class="step.icon"></i>
            <h3>{{ step.title }}</h3><p>{{ step.text }}</p>
          </article>
        </section>
      </template>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppFooter from '@/components/layout/AppFooter.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()
const campaign = ref(null)
const loadingCampaign = ref(true)
const checking = ref(false)
const spinning = ref(false)
const eligibility = ref(null)
const result = ref(null)
const wheelRotation = ref(0)
const form = reactive({ orderCode: '', phone: '' })
let resultTimer

const steps = [
  { icon: 'bi-receipt', title: 'Nhập đúng đơn hàng', text: 'Dùng mã đơn và số điện thoại đã ghi trên đơn mua hàng.' },
  { icon: 'bi-patch-check', title: 'Xác minh điều kiện', text: 'Đơn cần giao thành công và đạt mức chi tiêu của chương trình.' },
  { icon: 'bi-stars', title: 'Quay một lần', text: 'Mỗi đơn chỉ tạo một lượt; tải lại trang cũng không phát sinh thêm lượt.' },
  { icon: 'bi-shop-window', title: 'Nhận quà tại cửa hàng', text: 'Quà hiện vật được admin xác nhận trao tại showroom, không đổi thành tiền mặt.' }
]

const segments = computed(() => campaign.value?.segments || [])
const slice = computed(() => segments.value.length ? 360 / segments.value.length : 360)
const resultVisual = computed(() => segments.value.find(segment => segment.id === result.value?.prizeId) || result.value)
const campaignPeriod = computed(() => {
  const start = campaign.value?.ngayBatDau ? new Date(campaign.value.ngayBatDau) : null
  if (!start || Number.isNaN(start.getTime())) return 'LIMITED EDITION 2026'
  return `THÁNG ${String(start.getMonth() + 1).padStart(2, '0')} / ${start.getFullYear()}`
})
const wheelGradient = computed(() => {
  if (!segments.value.length) return 'var(--z-bg-alt)'
  return `conic-gradient(${segments.value.map((segment, index) => {
    const start = index * slice.value
    const end = (index + 1) * slice.value
    return `${segment.mauHienThi} ${start}deg ${end}deg`
  }).join(', ')})`
})
const wheelStyle = computed(() => ({
  background: wheelGradient.value,
  transform: `rotate(${wheelRotation.value}deg)`
}))

onMounted(async () => {
  await loadCampaign()
  if (route.query.orderCode) {
    form.orderCode = String(route.query.orderCode).trim().toUpperCase()
  }
  if (route.query.phone) {
    form.phone = String(route.query.phone).trim()
  }
  if (form.orderCode && form.phone) {
    await checkOrder()
  }
})
onBeforeUnmount(() => window.clearTimeout(resultTimer))

async function loadCampaign() {
  loadingCampaign.value = true
  try { campaign.value = await api().getLuckyWheelCampaign() }
  catch (error) { toast.showToast(error.error || 'Không thể tải vòng quay', 'error') }
  finally { loadingCampaign.value = false }
}

async function checkOrder() {
  if (!form.orderCode.trim()) return toast.showToast('Vui lòng nhập mã đơn hàng', 'warning')
  if (form.phone.length < 9) return toast.showToast('Số điện thoại chưa hợp lệ', 'warning')
  checking.value = true
  result.value = null
  try {
    eligibility.value = await api().checkLuckyWheelEligibility(form.orderCode, form.phone)
    if (eligibility.value.alreadyPlayed) result.value = eligibility.value
  } catch (error) {
    eligibility.value = null
    toast.showToast(error.error || 'Không thể kiểm tra lượt quay', 'error')
  } finally { checking.value = false }
}

async function spin() {
  if (!eligibility.value?.eligible || spinning.value) return
  spinning.value = true
  result.value = null
  try {
    const spinResult = await api().spinLuckyWheel(form.orderCode, form.phone)
    const selectedIndex = Math.max(0, segments.value.findIndex(segment => segment.id === spinResult.prizeId))
    const current = ((wheelRotation.value % 360) + 360) % 360
    const target = (360 - (selectedIndex * slice.value + slice.value / 2)) % 360
    const delta = (target - current + 360) % 360
    wheelRotation.value += 6 * 360 + delta
    resultTimer = window.setTimeout(() => {
      result.value = spinResult
      eligibility.value = { eligible: false, alreadyPlayed: true, message: 'Kết quả đã được lưu và không thể quay lại.' }
      spinning.value = false
    }, 5200)
  } catch (error) {
    spinning.value = false
    toast.showToast(error.error || 'Không thể thực hiện lượt quay', 'error')
  }
}

function segmentPosition(index) {
  return { transform: `rotate(${index * slice.value + slice.value / 2}deg) translateY(calc(var(--wheel-size) * -0.35))` }
}

function segmentLabelRotation(index) {
  return { transform: `rotate(${-index * slice.value - slice.value / 2 - wheelRotation.value}deg)` }
}

function normalizePhone(event) {
  form.phone = event.target.value.replace(/\D/g, '').slice(0, 15)
  clearEligibility()
}

function clearEligibility() {
  eligibility.value = null
  result.value = null
}

function hideBrokenIcon(item) {
  if (item) item.anhBieuTuong = null
}

function padIndex(index) {
  return String(index + 1).padStart(2, '0')
}

async function copyClaimCode() {
  if (!result.value?.maNhanThuong) return
  try { await navigator.clipboard.writeText(result.value.maNhanThuong); toast.showToast('Đã sao chép mã nhận quà', 'success') }
  catch { toast.showToast('Không thể sao chép tự động', 'warning') }
}

function formatMoney(value) { return Number(value || 0).toLocaleString('vi-VN') + 'đ' }
function formatDate(value) { return value ? `Kết thúc ${new Date(value).toLocaleString('vi-VN')}` : '' }
</script>

<style scoped>
.z-lucky-page { --wheel-size: clamp(310px, 39vw, 520px); background: var(--z-bg); }
.z-lucky-heading { position:relative;padding:68px 0 44px;border-top:5px solid var(--z-accent);border-bottom:1px solid var(--z-gray-border);background:var(--z-white);overflow:hidden; }
.z-lucky-heading::after { content:'GIFT';position:absolute;right:3vw;bottom:-42px;color:#f3f1f0;font:700 150px/1 var(--z-font-display);pointer-events:none; }
.z-heading-inner { position:relative;z-index:1;display:grid;grid-template-columns:minmax(0,1fr) minmax(390px,.9fr);gap:70px;align-items:end; }
.z-heading-inner .lm-eyebrow { margin:0 0 12px;color:var(--z-accent);font-weight:800; }
.z-heading-inner h1 { margin:0;color:var(--z-dark);font-size:58px;font-weight:400;line-height:1.02; }
.z-heading-inner h1 em { color: var(--z-accent); font-style: italic; }
.z-heading-aside { display:grid;gap:22px; }
.z-heading-aside > p { max-width:560px;margin:0;color:var(--z-gray);font-size:13px;line-height:1.75; }
.z-heading-stats { display:grid;grid-template-columns:1fr 1fr;border-top:1px solid var(--z-dark); }
.z-heading-stats > div { display:grid;grid-template-columns:54px 1fr;gap:10px;align-items:center;padding-top:13px; }
.z-heading-stats > div + div { padding-left:18px;border-left:1px solid var(--z-gray-border); }
.z-heading-stats strong { color:var(--z-dark);font:700 31px/1 var(--z-font-display); }
.z-heading-stats > div:first-child strong { color:var(--z-accent); }
.z-heading-stats span { color:var(--z-dark);font-size:9px;font-weight:750;text-transform:uppercase; }
.z-editorial-ribbon { overflow:hidden;background:var(--z-dark);color:var(--z-white); }
.z-editorial-ribbon .container { min-height:42px;display:flex;align-items:center;justify-content:center;gap:22px;white-space:nowrap;font-size:9px;font-weight:650;text-transform:uppercase; }
.z-editorial-ribbon strong { color:#ff766d;letter-spacing:.6px; }
.z-editorial-ribbon i { width:5px;height:5px;flex:none;background:var(--z-accent);transform:rotate(45deg); }
.z-lucky-content { padding-block:48px 80px; }
.z-lucky-loading, .z-lucky-empty { min-height: 420px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; color: var(--z-gray); text-align: center; }
.z-lucky-empty > i { color: var(--z-accent-light); font-size: 54px; }
.z-lucky-empty h2 { margin: 4px 0 0; color: var(--z-dark); font-size: 22px; }
.z-lucky-empty p { margin: 0 0 12px; }
.z-lucky-grid { position:relative;display:grid;grid-template-columns:minmax(0,1.08fr) minmax(350px,.92fr);gap:clamp(38px,5vw,72px);align-items:center;padding:56px 48px 50px;border-top:5px solid var(--z-accent);border-radius:4px;background:#1c1b20;box-shadow:0 24px 70px rgba(27,27,31,.12);overflow:hidden; }
.z-lucky-grid::after { content:'Z';position:absolute;left:-18px;bottom:-108px;color:rgba(255,255,255,.035);font:700 340px/1 var(--z-font-display);pointer-events:none; }
.z-wheel-column { min-width: 0; display: grid; justify-items: center; }
.z-wheel-kicker { position:relative;z-index:1;width:min(100%,calc(var(--wheel-size) + 30px));display:flex;justify-content:space-between;margin-bottom:24px;padding-bottom:9px;border-bottom:1px solid rgba(255,255,255,.48);color:rgba(255,255,255,.78);font-size:9px;font-weight:800;text-transform:uppercase; }
.z-wheel-stage { position: relative; width: var(--wheel-size); height: var(--wheel-size); isolation: isolate; }
.z-wheel-stage::before { content: ''; position: absolute; inset: -18px; z-index: -2; border: 1px solid #c9c6c3; border-radius: 50%; background: var(--z-white); box-shadow: 0 24px 60px rgba(27,27,31,.12); }
.z-wheel-stage::after { content: ''; position: absolute; inset: -12px; z-index: -1; border-radius: 50%; background: repeating-conic-gradient(from -1deg, #28272a 0deg 1deg, transparent 1deg 6deg); -webkit-mask: radial-gradient(circle, transparent 0 92%, #000 92% 100%); mask: radial-gradient(circle, transparent 0 92%, #000 92% 100%); pointer-events: none; }
.z-wheel { position: absolute; inset: 0; overflow: hidden; border: 7px solid var(--z-white); border-radius: 50%; box-shadow: inset 0 0 0 1px rgba(255,255,255,.55); transition: transform 5s cubic-bezier(.12,.68,.08,1); }
.z-wheel::after { content: ''; position: absolute; inset: 0; border: 1px solid rgba(255,255,255,.44); border-radius: 50%; pointer-events: none; }
.z-wheel-pointer { position:absolute;z-index:8;top:-27px;left:50%;width:64px;height:32px;display:grid;place-items:center;transform:translateX(-50%);background:var(--z-accent);color:var(--z-white);font-size:8px;font-weight:850;letter-spacing:1px;box-shadow:0 5px 14px rgba(0,0,0,.28); }
.z-wheel-pointer::after { content:'';position:absolute;left:50%;bottom:-13px;transform:translateX(-50%);border:8px solid transparent;border-top:13px solid var(--z-accent);border-bottom:0; }
.z-wheel-label-position { position: absolute; z-index: 2; top: 50%; left: 50%; width: 1px; height: 1px; transform-origin: 0 0; }
.z-wheel-label { width:62px;margin-left:-31px;display:grid;justify-items:center;gap:5px;color:var(--z-white);text-align:center;transition:transform 5s cubic-bezier(.12,.68,.08,1); }
.z-wheel-label strong { min-width:29px;height:19px;display:grid;place-items:center;border:1px solid rgba(255,255,255,.72);background:rgba(24,23,27,.88);color:#fff;font-size:10px;font-weight:850;line-height:1;box-shadow:0 2px 5px rgba(0,0,0,.2); }
.z-segment-visual { width:38px;height:38px;display:grid;place-items:center;overflow:hidden;border:2px solid rgba(255,255,255,.78);border-radius:50%;background:rgba(255,255,255,.96);color:var(--segment-color);box-shadow:0 3px 9px rgba(0,0,0,.18); }
.z-segment-visual i { font-size:19px; }
.z-segment-visual img { width: 100%; height: 100%; padding: 3px; object-fit: contain; background: rgba(255,255,255,.92); }
.z-wheel-center { position: absolute; z-index: 7; top: 50%; left: 50%; width: 102px; height: 102px; display: grid; place-items: center; align-content: center; gap: 2px; transform: translate(-50%,-50%); border: 6px double var(--z-dark); border-radius: 50%; background: var(--z-white); box-shadow: 0 7px 22px rgba(27,27,31,.25); }
.z-wheel-center img { width: 60px; height: 60px; object-fit: contain; }
.z-wheel-center span { margin-top: -8px; color: var(--z-dark); font-size: 7px; font-weight: 800; letter-spacing: 1px; }
.z-wheel-stage.spinning .z-wheel-pointer { animation: pointer-tick .16s ease-in-out infinite alternate; }
.z-wheel-note { position:relative;z-index:1;display:inline-flex;align-items:center;gap:7px;margin:30px 0 0;color:rgba(255,255,255,.68);font-size:10px; }
.z-wheel-note i { color:#69cf88;font-size:14px; }
.z-lucky-panel { position:relative;z-index:1;padding:clamp(24px,3.5vw,40px);border:1px solid #dedbda;border-top:4px solid var(--z-accent);border-radius:4px;background:var(--z-white);box-shadow:0 18px 46px rgba(0,0,0,.22); }
.z-campaign-meta { display: flex; align-items: center; justify-content: space-between; gap: 12px; color: var(--z-gray); font-size: 10px; }
.z-live-label { display: inline-flex; align-items: center; gap: 7px; color: #217a3d; font-weight: 700; }
.z-live-label i { width: 6px; height: 6px; border-radius: 50%; background: #2e9d4c; box-shadow: 0 0 0 3px rgba(46,157,76,.12); }
.z-panel-index { margin: 24px 0 7px !important; color: var(--z-accent) !important; font-size: 9px !important; font-weight: 700; text-transform: uppercase; }
.z-lucky-panel > h2 { margin: 0 0 10px; color: var(--z-dark); font: 500 28px/1.25 var(--z-font-display); }
.z-lucky-panel > p:not(.z-panel-index) { margin: 0 0 20px; color: var(--z-gray); font-size: 12px; line-height: 1.7; }
.z-lucky-rule { display:grid;grid-template-columns:40px 1fr;gap:12px;align-items:center;margin-bottom:22px;padding:12px 0;border-block:1px solid var(--z-gray-border); }
.z-lucky-rule > span { width:36px;height:36px;display:grid;place-items:center;background:var(--z-dark);color:var(--z-white);font:700 15px var(--z-font-display); }
.z-lucky-rule div { display: grid; gap: 2px; }
.z-lucky-rule strong { color: var(--z-dark); font-size: 11px; }
.z-lucky-rule small { color: var(--z-gray); font-size: 11px; }
.z-lucky-form { display: grid; gap: 14px; }
.z-lucky-form label { display: block; margin-bottom: 6px; color: var(--z-dark); font-size: 11px; font-weight: 650; }
.z-input-with-icon { position: relative; }
.z-input-with-icon > i { position: absolute; z-index: 1; left: 14px; top: 50%; transform: translateY(-50%); color: var(--z-gray-light); }
.z-input-with-icon .lm-input { padding-left: 42px; }
.z-lucky-check { width: 100%; min-height: 48px; justify-content: center; margin-top: 2px; }
.z-eligibility { display: flex; gap: 10px; margin-top: 16px; padding: 12px 14px; border: 1px solid; border-radius: 4px; }
.z-eligibility.success { border-color: #bbdfc4; background: #f0faf3; color: #217a3d; }
.z-eligibility.notice { border-color: #f0d8ae; background: #fff9ed; color: #8a5b00; }
.z-eligibility div { display: grid; gap: 2px; }
.z-eligibility strong { font-size: 11px; }
.z-eligibility span { font-size: 10px; line-height: 1.5; }
.z-spin-button { width: 100%; min-height: 51px; display: flex; align-items: center; justify-content: center; gap: 10px; margin-top: 14px; border: 1px solid var(--z-accent); border-radius: 3px; background: var(--z-accent); color: var(--z-white); font-size: 12px; font-weight: 700; cursor: pointer; transition: var(--z-ease); }
.z-spin-button:hover:not(:disabled) { border-color: var(--z-dark); background: var(--z-dark); transform: translateY(-1px); }
.z-spin-button:disabled { opacity: .7; cursor: wait; }
.z-result { display: grid; grid-template-columns: 48px 1fr; gap: 12px; margin-top: 17px; padding: 16px 0 0; border-top: 1px solid var(--z-gray-border); }
.z-result-icon { width: 46px; height: 46px; display: grid; place-items: center; overflow: hidden; border: 1px solid var(--z-gray-border); background: var(--z-bg-alt); color: var(--z-accent); font-size: 20px; }
.z-result-icon img { width: 100%; height: 100%; padding: 4px; object-fit: contain; }
.z-result-copy { display: grid; align-content: center; gap: 3px; }
.z-result-copy span { color: var(--z-gray); font-size: 10px; }
.z-result-copy strong { color: var(--z-dark); font-size: 14px; }
.z-claim-code { grid-column: 1 / -1; }
.z-claim-code > span, .z-claim-code small { display: block; color: var(--z-gray); font-size: 9px; }
.z-claim-code button { width: 100%; display: flex; align-items: center; justify-content: space-between; margin: 5px 0; padding: 10px 12px; border: 1px dashed var(--z-accent); background: var(--z-white); color: var(--z-dark); cursor: pointer; }
.z-claim-code button strong { letter-spacing: 1px; }
.z-gift-edit { margin-top:88px; }
.z-gift-heading { display: grid; grid-template-columns: 1fr minmax(280px, .72fr); gap: 40px; align-items: end; margin-bottom: 22px; }
.z-gift-heading .lm-eyebrow { margin: 0 0 8px; }
.z-gift-heading h2 { margin: 0; color: var(--z-dark); font-size: 30px; font-weight: 450; }
.z-gift-heading > p { margin: 0; color: var(--z-gray); font-size: 11px; line-height: 1.7; }
.z-gift-grid { display:grid;grid-template-columns:repeat(4,1fr);border-top:1px solid var(--z-dark);border-bottom:1px solid var(--z-gray-border); }
.z-gift-grid article { position:relative;min-width:0;display:grid;grid-template-columns:50px 1fr;gap:13px;align-items:center;min-height:126px;padding:26px 17px 18px;border-top:5px solid var(--gift-color);border-right:1px solid var(--z-gray-border);background:var(--z-white);transition:transform .22s ease,box-shadow .22s ease,background-color .22s ease; }
.z-gift-grid article:hover { z-index:2;transform:translateY(-5px);background:color-mix(in srgb,var(--gift-color) 5%,white);box-shadow:0 15px 30px rgba(27,27,31,.12); }
.z-gift-grid article:nth-child(4n) { border-right: 0; }
.z-gift-grid article:nth-child(n+5) { border-top: 1px solid var(--z-gray-border); }
.z-gift-number { position:absolute;top:0;right:0;min-width:39px;height:29px;display:grid;place-items:center;background:var(--gift-color);color:#fff;font:800 12px/1 var(--z-font-display);box-shadow:0 4px 10px color-mix(in srgb,var(--gift-color) 25%,transparent); }
.z-gift-icon { width:50px;height:50px;display:grid;place-items:center;overflow:hidden;border:2px solid var(--gift-color);color:var(--gift-color);font-size:22px;transition:background-color .22s ease,color .22s ease; }
.z-gift-grid article:hover .z-gift-icon { background:var(--gift-color);color:#fff; }
.z-gift-icon img { width: 100%; height: 100%; padding: 4px; object-fit: contain; }
.z-gift-grid article > div { min-width: 0; display: grid; gap: 4px; }
.z-gift-grid strong { padding-right:18px;color:var(--z-dark);font-size:12px;line-height:1.35; }
.z-gift-grid small { color:var(--z-gray);font-size:9px;line-height:1.45; }
.z-steps-heading { display:flex;align-items:end;justify-content:space-between;gap:30px;margin-top:86px;padding-bottom:18px;border-bottom:1px solid var(--z-dark); }
.z-steps-heading .lm-eyebrow { margin:0;color:var(--z-accent);font-weight:800; }
.z-steps-heading h2 { margin:0;color:var(--z-dark);font-size:28px;font-weight:450; }
.z-steps-heading h2 em { color:var(--z-accent);font-style:italic; }
.z-lucky-steps { display:grid;grid-template-columns:repeat(4,1fr);border-bottom:1px solid var(--z-gray-border); }
.z-lucky-steps article { position:relative;min-width:0;padding:34px 24px 28px;border-right:1px solid var(--z-gray-border);background:var(--z-white);transition:background-color .22s ease,transform .22s ease; }
.z-lucky-steps article:hover { background:#f7f7f6;transform:translateY(-3px); }
.z-lucky-steps article:last-child { border-right: 0; }
.z-lucky-steps article > span { position:absolute;top:0;right:0;width:42px;height:31px;display:grid;place-items:center;background:var(--z-dark);color:#fff;font:800 12px/1 var(--z-font-display);transition:background-color .22s ease; }
.z-lucky-steps article:hover > span { background:var(--z-accent); }
.z-lucky-steps article > i { color:var(--z-accent);font-size:24px; }
.z-lucky-steps h3 { margin: 12px 0 7px; color: var(--z-dark); font-size: 12px; font-weight: 650; }
.z-lucky-steps p { margin: 0; color: var(--z-gray); font-size: 10px; line-height: 1.6; }
@keyframes pointer-tick { to { transform: translateX(-50%) rotate(3deg); } }
@media (max-width: 991px) {
  .z-heading-inner { grid-template-columns: 1fr; gap: 18px; }
  .z-heading-aside { max-width:680px; }
  .z-lucky-grid { grid-template-columns:1fr;padding-inline:34px; }
  .z-lucky-panel { max-width: 640px; width: 100%; margin-inline: auto; }
  .z-gift-grid { grid-template-columns: repeat(2, 1fr); }
  .z-gift-grid article:nth-child(4n) { border-right: 1px solid var(--z-gray-border); }
  .z-gift-grid article:nth-child(2n) { border-right: 0; }
  .z-gift-grid article:nth-child(n+3) { border-top: 1px solid var(--z-gray-border); }
  .z-lucky-steps { grid-template-columns: repeat(2, 1fr); }
  .z-lucky-steps article:nth-child(2) { border-right: 0; }
  .z-lucky-steps article:nth-child(-n+2) { border-bottom: 1px solid var(--z-gray-border); }
}
@media (max-width: 575px) {
  .z-lucky-page { --wheel-size:min(81vw,350px); }
  .z-lucky-heading { padding:42px 0 30px; }
  .z-lucky-heading::after { right:-28px;bottom:-24px;font-size:92px; }
  .z-heading-inner h1 { font-size:39px; }
  .z-heading-aside { gap:17px; }
  .z-heading-aside > p { font-size:11px; }
  .z-heading-stats > div { grid-template-columns:43px 1fr;gap:7px; }
  .z-heading-stats > div + div { padding-left:10px; }
  .z-heading-stats strong { font-size:25px; }
  .z-heading-stats span { font-size:8px; }
  .z-editorial-ribbon .container { justify-content:flex-start;gap:14px;min-height:38px; }
  .z-editorial-ribbon .container span:last-child, .z-editorial-ribbon .container i:last-of-type { display:none; }
  .z-lucky-content { padding-block:32px 56px; }
  .z-lucky-grid { padding:42px 14px 28px; }
  .z-wheel-kicker { margin-bottom: 21px; }
  .z-wheel-stage::before { inset: -12px; }
  .z-wheel-stage::after { inset: -8px; }
  .z-wheel-center { width: 76px; height: 76px; border-width: 4px; }
  .z-wheel-center img { width: 48px; height: 48px; }
  .z-wheel-center span { margin-top: -7px; font-size: 6px; }
  .z-segment-visual { width: 28px; height: 28px; }
  .z-segment-visual i { font-size: 15px; }
  .z-wheel-label { width: 48px; margin-left: -24px; }
  .z-wheel-label strong { min-width:25px;height:17px;font-size:9px; }
  .z-wheel-note { max-width:290px;text-align:center;line-height:1.5; }
  .z-lucky-panel { padding: 22px 18px; }
  .z-campaign-meta { align-items: flex-start; flex-direction: column; }
  .z-gift-edit { margin-top: 60px; }
  .z-gift-heading { grid-template-columns: 1fr; gap: 12px; }
  .z-gift-grid { grid-template-columns: 1fr; }
  .z-gift-grid article, .z-gift-grid article:nth-child(4n), .z-gift-grid article:nth-child(2n) { border-right: 0; }
  .z-gift-grid article:nth-child(n+2) { border-top: 1px solid var(--z-gray-border); }
  .z-steps-heading { align-items:flex-start;flex-direction:column;gap:7px;margin-top:58px; }
  .z-steps-heading h2 { font-size:25px; }
  .z-lucky-steps { grid-template-columns:1fr; }
  .z-lucky-steps article { border-right: 0; border-bottom: 1px solid var(--z-gray-border); }
  .z-lucky-steps article:last-child { border-bottom: 0; }
}
</style>
