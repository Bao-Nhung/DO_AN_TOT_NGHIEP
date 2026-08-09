<template>
  <div v-if="show" class="z-modal-overlay" @click.self="$emit('close')" style="z-index: 3000; backdrop-filter: blur(4px);">
    <div class="z-modal p-0 overflow-hidden" style="max-width: 850px; width: 95%;">
      <!-- Header Bar -->
      <div class="bg-dark text-white p-3 d-flex justify-content-between align-items-center no-print">
        <div class="d-flex align-items-center gap-2 fw-bold" style="font-size: 16px;">
          <i class="bi bi-file-earmark-text text-danger fs-5"></i>
          <span>HÓA ĐƠN ĐIỆN TỬ (VAT e-INVOICE)</span>
          <span v-if="invoice?.trangThaiVat === 'DA_PHAT_HANH'" class="badge bg-success ms-2">Đã Phát Hành</span>
          <span v-else class="badge bg-warning text-dark ms-2">Chờ Phát Hành</span>
        </div>
        <div class="d-flex align-items-center gap-2">
          <button v-if="invoice?.trangThaiVat !== 'DA_PHAT_HANH'" type="button" class="btn btn-sm btn-danger px-3 fw-bold" :disabled="issuing" @click="handleIssueInvoice">
            <i class="bi bi-send-fill me-1"></i> {{ issuing ? 'Đang phát hành...' : 'Phát Hành Hóa Đơn' }}
          </button>
          <button type="button" class="btn btn-sm btn-light px-3 fw-bold" @click="printInvoice">
            <i class="bi bi-printer me-1"></i> In / Xuất PDF
          </button>
          <button type="button" class="btn btn-sm btn-outline-light" @click="$emit('close')">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
      </div>

      <!-- Invoice Document Body (Printable Area) -->
      <div id="printable-e-invoice" class="p-4 p-md-5 bg-white text-dark" style="font-family: 'Inter', sans-serif;">
        <!-- Header Document -->
        <div class="row align-items-center border-bottom pb-4 mb-4">
          <div class="col-7">
            <div class="h3 fw-bold tracking-wide text-uppercase mb-1" style="color: #1a1a1a;">CÔNG TY TNHH THỜI TRANG ZESTIA</div>
            <div style="font-size: 13px; color: #555;">
              <div><strong>Mã số thuế (MST):</strong> 0109876543</div>
              <div><strong>Địa chỉ:</strong> Tầng 5, Tòa nhà Zestia Tower, 123 Nguyễn Trãi, Thanh Xuân, Hà Nội</div>
              <div><strong>Điện thoại:</strong> 1900 6789 | <strong>Email:</strong> einvoice@zestia.vn</div>
            </div>
          </div>
          <div class="col-5 text-end">
            <div class="badge bg-danger text-uppercase p-2 mb-2" style="letter-spacing: 1px;">Mẫu số: 01GTKT0/001</div>
            <div style="font-size: 13px; color: #333;">
              <div><strong>Ký hiệu (Serial):</strong> {{ invoice?.kyHieu || '1K26TZE' }}</div>
              <div><strong>Số hóa đơn:</strong> <span class="fw-bold text-danger">{{ invoice?.soHoaDonVat || 'CHƯA CẤP SỐ' }}</span></div>
              <div><strong>Ngày phát hành:</strong> {{ invoice?.ngayPhatHanh || 'Chưa phát hành' }}</div>
            </div>
          </div>
        </div>

        <!-- Title -->
        <div class="text-center my-4">
          <h2 class="fw-bold text-uppercase mb-1" style="letter-spacing: 2px; color: #c92a2a;">HÓA ĐƠN GIÁ TRỊ GIA TĂNG</h2>
          <div class="fst-italic text-muted" style="font-size: 13px;">(Hóa đơn điện tử chuyển đổi từ hệ thống quản lý thuế Zestia)</div>
          <div class="mt-1" style="font-size: 12px; color: #666;">Mã tra cứu: <strong class="text-dark">{{ invoice?.maTraCuu }}</strong></div>
        </div>

        <!-- Buyer Info Box -->
        <div class="p-3 mb-4 rounded-3 border bg-light" style="font-size: 13px;">
          <div class="row g-2">
            <div class="col-12"><strong>Tên đơn vị mua hàng:</strong> {{ invoice?.buyer?.tenCongTy || 'Khách hàng cá nhân' }}</div>
            <div class="col-md-6"><strong>Mã số thuế:</strong> {{ invoice?.buyer?.maSoThue || 'Không cung cấp' }}</div>
            <div class="col-md-6"><strong>Người mua hàng:</strong> {{ invoice?.buyer?.tenNguoiMua }}</div>
            <div class="col-12"><strong>Địa chỉ công ty:</strong> {{ invoice?.buyer?.diaChi || 'Theo đơn hàng' }}</div>
            <div class="col-md-6"><strong>Email nhận hóa đơn:</strong> {{ invoice?.buyer?.email }}</div>
            <div class="col-md-6"><strong>Hình thức thanh toán:</strong> Chuyển khoản / Tiền mặt</div>
          </div>
        </div>

        <!-- Line Items Table -->
        <table class="table table-bordered align-middle mb-4" style="font-size: 13px;">
          <thead class="table-light text-center">
            <tr>
              <th style="width: 50px;">STT</th>
              <th>Tên hàng hóa, dịch vụ</th>
              <th style="width: 80px;">ĐVT</th>
              <th style="width: 80px;">Số lượng</th>
              <th style="width: 130px;">Đơn giá (đ)</th>
              <th style="width: 140px;">Thành tiền (đ)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in invoice?.items" :key="item.stt">
              <td class="text-center">{{ item.stt }}</td>
              <td>
                <div class="fw-bold">{{ item.tenSanPham }}</div>
              </td>
              <td class="text-center">{{ item.dvt }}</td>
              <td class="text-center fw-bold">{{ item.soLuong }}</td>
              <td class="text-end">{{ fmtPrice(item.donGia) }}</td>
              <td class="text-end fw-bold">{{ fmtPrice(item.thanhTien) }}</td>
            </tr>
          </tbody>
        </table>

        <!-- Total Calculation Box -->
        <div class="row mb-4">
          <div class="col-md-6">
            <div class="p-3 border rounded-3 text-center bg-light">
              <img :src="invoice?.qrCodeUrl" alt="QR Code Tra Cứu Hóa Đơn" style="width: 130px; height: 130px;" />
              <div class="mt-2 text-muted" style="font-size: 11px;">Quét mã QR để kiểm tra hóa đơn trên Tổng Cục Thuế</div>
            </div>
          </div>
          <div class="col-md-6">
            <table class="table table-borderless table-sm" style="font-size: 13px;">
              <tbody>
                <tr>
                  <td>Cộng tiền hàng:</td>
                  <td class="text-end fw-bold">{{ fmtPrice(invoice?.totalBeforeTax) }}</td>
                </tr>
                <tr v-if="invoice?.discountVoucher > 0">
                  <td>Chiết khấu Voucher:</td>
                  <td class="text-end text-danger">-{{ fmtPrice(invoice?.discountVoucher) }}</td>
                </tr>
                <tr>
                  <td>Thuế suất GTGT (VAT):</td>
                  <td class="text-end fw-bold text-danger">{{ invoice?.vatRatePercent }}%</td>
                </tr>
                <tr>
                  <td>Tổn tiền thuế GTGT:</td>
                  <td class="text-end fw-bold text-danger">{{ fmtPrice(invoice?.vatAmount) }}</td>
                </tr>
                <tr>
                  <td>Phí vận chuyển:</td>
                  <td class="text-end">{{ fmtPrice(invoice?.shippingFee) }}</td>
                </tr>
                <tr class="border-top">
                  <td class="fw-bold fs-6">Tổng cộng thanh toán:</td>
                  <td class="text-end fw-bold text-danger fs-5">{{ fmtPrice(invoice?.totalPayment) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="mb-4 p-2 bg-light border-start border-4 border-danger" style="font-size: 13px;">
          <strong>Số tiền viết bằng chữ:</strong> <span class="fst-italic text-dark">{{ invoice?.amountInWords }}</span>
        </div>

        <!-- Signature Section -->
        <div class="row text-center mt-5">
          <div class="col-6">
            <div class="fw-bold text-uppercase" style="font-size: 13px;">NGƯỜI MUA HÀNG</div>
            <div class="text-muted fst-italic" style="font-size: 11px;">(Ký, ghi rõ họ tên)</div>
            <div class="my-4 text-muted" style="font-size: 12px;">Đã xác nhận qua OTP / Hệ thống</div>
          </div>
          <div class="col-6">
            <div class="fw-bold text-uppercase" style="font-size: 13px;">NGƯỜI BÁN HÀNG</div>
            <div class="text-muted fst-italic" style="font-size: 11px;">(Chữ ký số doanh nghiệp)</div>
            
            <div v-if="invoice?.trangThaiVat === 'DA_PHAT_HANH'" class="d-inline-block border border-danger p-2 my-2 rounded-3 bg-light text-start" style="font-size: 11px; color: #c92a2a; max-width: 260px;">
              <div class="fw-bold text-center border-bottom pb-1 mb-1"><i class="bi bi-patch-check-fill me-1"></i>ĐÃ KÝ ĐIỆN TỬ</div>
              <div>Bởi: CÔNG TY TNHH THỜI TRANG ZESTIA</div>
              <div>Mã CA: {{ invoice?.digitalSignature?.substring(0, 24) }}</div>
              <div>Ngày ký: {{ invoice?.ngayPhatHanh }}</div>
            </div>
            <div v-else class="my-4 text-muted fst-italic" style="font-size: 12px;">(Chưa phát hành chữ ký số)</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '@/composables/useApi'

const props = defineProps({
  show: { type: Boolean, default: false },
  invoice: { type: Object, default: null },
  orderId: { type: [Number, String], default: null }
})

const emit = defineEmits(['close', 'updated'])
const issuing = ref(false)

function fmtPrice(val) {
  if (val == null) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

async function handleIssueInvoice() {
  if (!props.orderId) return
  issuing.value = true
  try {
    const updated = await api().issueEInvoice(props.orderId)
    emit('updated', updated)
  } catch (error) {
    alert(error.error || 'Không thể phát hành hóa đơn')
  } finally {
    issuing.value = false
  }
}

function printInvoice() {
  window.print()
}
</script>

<style scoped>
@media print {
  body * {
    visibility: hidden;
  }
  .no-print {
    display: none !important;
  }
  #printable-e-invoice, #printable-e-invoice * {
    visibility: visible;
  }
  #printable-e-invoice {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
  }
}
</style>
