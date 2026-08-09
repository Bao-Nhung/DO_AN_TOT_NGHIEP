<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý đơn hàng</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ totalItems }} đơn hàng</p>
      </div>
    </div>

    <div class="d-flex gap-2 mb-3 flex-wrap">
      <button v-for="tab in statusTabs" :key="tab.value"
              class="z-tab" :class="{ active: activeStatus === tab.value }"
              @click="activeStatus = tab.value">
        {{ tab.label }}
        <span class="z-tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- Nguồn đơn hàng (Online/Offline) -->
    <div class="d-flex gap-2 mb-3">
      <button class="lm-btn-secondary" 
              :style="{
                padding:'6px 16px', fontSize:'13px', borderRadius:'20px', height:'auto',
                background: orderTypeTab === 'all' ? 'var(--z-dark)' : '',
                color: orderTypeTab === 'all' ? '#fff' : '',
                borderColor: orderTypeTab === 'all' ? 'var(--z-dark)' : ''
              }"
              @click="orderTypeTab = 'all'">
        Tất cả nguồn
      </button>
      <button class="lm-btn-secondary" 
              :style="{
                padding:'6px 16px', fontSize:'13px', borderRadius:'20px', height:'auto',
                background: orderTypeTab === 'online' ? 'var(--z-dark)' : '',
                color: orderTypeTab === 'online' ? '#fff' : '',
                borderColor: orderTypeTab === 'online' ? 'var(--z-dark)' : ''
              }"
              @click="orderTypeTab = 'online'">
        <i class="bi bi-globe me-1"></i> Online (Đặt trực tuyến)
      </button>
      <button class="lm-btn-secondary" 
              :style="{
                padding:'6px 16px', fontSize:'13px', borderRadius:'20px', height:'auto',
                background: orderTypeTab === 'offline' ? 'var(--z-dark)' : '',
                color: orderTypeTab === 'offline' ? '#fff' : '',
                borderColor: orderTypeTab === 'offline' ? 'var(--z-dark)' : ''
              }"
              @click="orderTypeTab = 'offline'">
        <i class="bi bi-shop me-1"></i> Offline (Tại quầy)
      </button>
    </div>

    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-2" style="max-width:400px">
        <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
        <input v-model="search" class="lm-input" placeholder="Tìm theo mã đơn, tên khách..." style="border:none;padding:8px 0;box-shadow:none">
      </div>
    </div>

    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <div class="table-responsive">
      <table class="z-table" style="min-width:1050px">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Sản phẩm</th>
            <th>Tổng tiền</th>
            <th>Thanh toán</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th style="width:140px; text-align: right; padding-right: 20px;">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in paginatedOrders" :key="o.id" class="z-clickable-row" @click="openDetail(o)">
            <td style="font-weight:600">
              <div class="d-flex align-items-center gap-1">
                <span>{{ o.id }}</span>
                <button type="button" class="btn btn-sm btn-light border py-0 px-1" style="font-size:10px" title="Sao chép mã đơn" @click.stop="copyOrderCode(o.id)">
                  <i class="bi bi-clipboard"></i>
                </button>
                <span v-if="o.raw?.yeuCauVat" class="badge bg-danger ms-1" style="font-size: 10px;" title="Khách yêu cầu Hóa Đơn VAT">
                  <i class="bi bi-file-earmark-text"></i> VAT
                </span>
              </div>
            </td>
            <td>
              <div style="font-weight:500">{{ o.customer }}</div>
              <div style="font-size:12px;color:var(--z-gray)">{{ o.phone }}</div>
            </td>
            <td data-no-i18n>{{ orderItemCountLabel(o.items) }}</td>
            <td style="font-weight:600">{{ o.total }}</td>
            <td>
              <div>{{ o.payment }}</div>
              <span v-if="o.raw?.phuongThucThanhToanOnline === 'FAILED' || (o.statusValue === '5' && (o.payment === 'MOMO' || o.payment === 'ZALOPAY') && !o.paid)"
                    class="z-pay-badge unpaid" style="background:#fee2e2; color:#b91c1c; border-color:#fee2e2">
                <i class="bi bi-x-circle-fill"></i>
                Thanh toán thất bại
              </span>
              <span v-else class="z-pay-badge" :class="o.paid ? 'paid' : 'unpaid'">
                <i class="bi" :class="o.paid ? 'bi-check-circle-fill' : 'bi-clock'"></i>
                {{ o.paid ? 'Đã thanh toán' : 'Chưa thanh toán' }}
              </span>
            </td>
            <td><span class="z-status" :class="o.statusClass">{{ o.status }}</span></td>
            <td style="color:var(--z-gray)">{{ o.date }}</td>
            <td @click.stop style="text-align: right; padding-right: 20px;">
              <button v-if="o.raw?.yeuCauVat || o.raw?.soHoaDonVat" type="button" class="btn btn-sm btn-outline-danger me-1 py-1 px-2" style="font-size:11px;" title="Xem / Xuất Hóa Đơn Điện Tử VAT" @click="openEInvoiceModal(o.id)">
                <i class="bi bi-receipt me-1"></i> VAT
              </button>
              <button type="button" class="z-action-btn d-inline-block" title="Xem & Xử lý" :aria-label="`Xem và xử lý đơn ${o.maHoaDon || o.id}`" @click="openDetail(o)">
                <i class="bi bi-pencil-square"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      </div>

      <div v-if="totalItems === 0" class="text-center py-5">
        <i class="bi bi-inbox" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có đơn hàng nào</p>
      </div>

      <!-- Pagination Controls -->
      <div v-if="totalItems > 0" class="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
        <span data-no-i18n style="font-size: 13px; color: var(--z-gray)">{{ orderRangeLabel }}</span>
        <PageSizeSelect v-model="itemsPerPage" />
        <div v-if="totalPages > 1" class="d-flex gap-2">
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === 1" @click="currentPage--">
            Trước
          </button>
          <button v-for="page in pageNumbers" :key="page"
                  class="lm-btn-secondary" 
                  :style="{
                    padding:'6px 12px', fontSize:'12px', height:'auto', borderRadius:'6px',
                    background: currentPage === page ? 'var(--z-dark)' : '',
                    color: currentPage === page ? '#fff' : '',
                    borderColor: currentPage === page ? 'var(--z-dark)' : ''
                  }"
                  @click="currentPage = page">
            {{ page }}
          </button>
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === totalPages" @click="currentPage++">
            Sau
          </button>
        </div>
      </div>
    </div>

    <div v-if="showDetail" class="z-modal-overlay" @click.self="showDetail = false">
      <div class="z-modal" style="max-width:900px"> 
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Xử lý đơn hàng: {{ detailData?.maHoaDon }}</h3>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng chi tiết đơn hàng" @click="showDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingDetail" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
        </div>

        <div v-else-if="detailData">
          <div class="d-flex align-items-center gap-2 mb-4 pb-3" style="border-bottom:1px solid var(--z-gray-border);overflow-x:auto">
            <template v-if="detailData.hinhThucNhanHang === 0">
               <span class="z-offline-order-status" :class="offlineOrderStatus(detailData).cls">
                 <i class="bi" :class="offlineOrderStatus(detailData).icon"></i>
                 {{ offlineOrderStatus(detailData).label }}
               </span>
            </template>
            <template v-else-if="detailData.trangThai === 7">
               <div class="z-step active">
                  <div class="z-step-dot" style="background: var(--z-danger);"></div>
                  <div class="z-step-label" style="color: var(--z-danger); font-weight: 600;">Thanh toán thất bại</div>
               </div>
            </template>
            <template v-else-if="detailData.trangThai === 5">
               <div class="z-step active">
                  <div class="z-step-dot" style="background: var(--z-danger);"></div>
                  <div class="z-step-label" style="color: var(--z-danger); font-weight: 600;">Đã huỷ</div>
               </div>
            </template>
            <template v-else-if="detailData.trangThai === 8 || detailData.trangThai === 9">
               <div class="z-step active">
                  <div class="z-step-dot" :style="detailData.trangThai === 9 ? 'background: var(--z-danger);' : 'background: var(--z-accent);'"></div>
                  <div class="z-step-label" :style="detailData.trangThai === 9 ? 'color: var(--z-danger); font-weight: 600;' : 'color: var(--z-accent); font-weight: 600;'">
                    {{ detailData.trangThai === 9 ? 'Đã hoàn tiền/hoàn tất' : 'Yêu cầu đổi/trả' }}
                  </div>
               </div>
            </template>
            <template v-else>
                <div v-for="(step, i) in statusSteps" :key="i"
                     class="z-step" :class="{ active: detailData.trangThai >= i && detailData.trangThai !== 6, current: detailData.trangThai === i, failed: i === 4 && detailData.trangThai === 6 }">
                  <div class="z-step-dot" :style="i === 4 && detailData.trangThai === 6 ? 'background: var(--z-danger)' : ''"></div>
                  <div class="z-step-label" :style="i === 4 && detailData.trangThai === 6 ? 'color: var(--z-danger); font-weight: 600;' : ''">
                      {{ i === 4 && detailData.trangThai === 6 ? 'Giao thất bại' : step }}
                  </div>
                  <div v-if="i < statusSteps.length - 1" class="z-step-line" :class="{ filled: detailData.trangThai > i && detailData.trangThai !== 6 }"></div>
                </div>
            </template>
          </div>

          <div class="row">
              <div class="col-md-7 border-end pe-4">
                  <h4 style="font-size:14px;font-weight:600;margin-bottom:12px; color: var(--z-dark);">Thông tin khách hàng & Giao nhận</h4>
                  <div class="row g-3 mb-4 p-3 bg-light rounded">
                    <div class="col-12">
                      <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Khách hàng</div>
                      <div style="font-size:14px;font-weight:500" class="d-flex align-items-center gap-2">
                        <span>{{ detailData.tenKhachHang || detailData.khachHang || 'Khách lẻ' }}</span>
                        <span v-if="detailData.soDienThoai"> - {{ detailData.soDienThoai }}</span>
                      </div>
                      <div style="font-size:13px; color:var(--z-gray);" v-if="detailData.emailKhachHang">{{ detailData.emailKhachHang }}</div>
                    </div>
                    
                    <div class="col-12 mt-2">
                        <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Phương thức vận chuyển</div>
                        <div style="font-size:14px;font-weight:500; display:flex; align-items: center; gap: 5px;">
                            <i class="bi" :class="detailData.hinhThucNhanHang === 0 ? 'bi-shop' : 'bi-truck'"></i>
                            {{ detailData.hinhThucNhanHang === 0 ? 'Mua trực tiếp' : 'Giao hàng tận nơi' }}
                        </div>
                    </div>

                    <div v-if="detailData.diaChiGiaoHang && detailData.hinhThucNhanHang !== 0" class="col-12 mt-2">
                      <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Địa chỉ giao hàng</div>
                      <div style="font-size:14px;font-weight:500"><i class="bi bi-geo-alt me-1"></i>{{ detailData.diaChiGiaoHang }}</div>
                    </div>
                  </div>

                  <!-- VAT Enterprise Information Box -->
                  <div v-if="detailData.yeuCauVat || detailData.soHoaDonVat" class="mb-4 p-3 border border-danger rounded bg-light">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <strong class="text-danger"><i class="bi bi-file-earmark-text me-1"></i>Hóa Đơn Điện Tử VAT Doanh Nghiệp</strong>
                      <button type="button" class="btn btn-sm btn-danger py-1 px-3 fw-bold" @click="openEInvoiceModal(detailData.id)">
                        <i class="bi bi-receipt me-1"></i> {{ detailData.trangThaiVat === 'DA_PHAT_HANH' ? 'Xem Hóa Đơn' : 'Phát Hành VAT' }}
                      </button>
                    </div>
                    <div style="font-size: 13px;">
                      <div><strong>Tên công ty:</strong> {{ detailData.tenCongTyVat || detailData.tenKhachHang }}</div>
                      <div><strong>Mã số thuế:</strong> {{ detailData.maSoThueVat || 'Không cung cấp' }}</div>
                      <div><strong>Email nhận HD:</strong> {{ detailData.emailVat || detailData.emailKhachHang }}</div>
                      <div><strong>Địa chỉ:</strong> {{ detailData.diaChiVat || detailData.diaChiGiaoHang }}</div>
                      <div class="mt-2">
                        <strong>Trạng thái VAT: </strong>
                        <span :class="detailData.trangThaiVat === 'DA_PHAT_HANH' ? 'badge bg-success' : 'badge bg-warning text-dark'">
                          {{ detailData.trangThaiVat === 'DA_PHAT_HANH' ? 'Đã Phát Hành' : 'Chờ Phát Hành' }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <h4 style="font-size:14px;font-weight:600;margin-bottom:12px; color: var(--z-dark);">Thông tin bổ sung</h4>
                  <div class="row g-3 mb-4">
                    <div class="col-6">
                      <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Thanh toán</div>
                      <div style="font-size:14px;font-weight:500">
                        {{ detailData.hinhThucThanhToan || 'N/A' }}
                        <div class="mt-1">
                            <span v-if="detailData.phuongThucThanhToanOnline === 'FAILED' || (detailData.trangThai === 5 && (detailData.hinhThucThanhToan === 'MOMO' || detailData.hinhThucThanhToan === 'ZALOPAY') && !detailData.daThanhToan)"
                                  class="z-pay-badge unpaid" style="background:#fee2e2; color:#b91c1c; border-color:#fee2e2">
                              <i class="bi bi-x-circle-fill"></i> Thanh toán thất bại
                            </span>
                            <span v-else class="z-pay-badge" :class="detailData.daThanhToan ? 'paid' : 'unpaid'">
                            <i class="bi" :class="detailData.daThanhToan ? 'bi-check-circle-fill' : 'bi-clock'"></i>
                            {{ detailData.daThanhToan ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                            </span>
                        </div>
                      </div>
                    </div>
                    <div class="col-6">
                      <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Ngày tạo</div>
                      <div style="font-size:14px;font-weight:500">{{ detailData.ngayTao ? new Date(detailData.ngayTao).toLocaleString('vi-VN') : '' }}</div>
                    </div>
                    <div class="col-12">
                      <div style="font-size:12px;color:var(--z-gray);margin-bottom:2px">Người tạo đơn</div>
                      <div style="font-size:14px;font-weight:500"><i class="bi bi-person-badge me-1"></i>{{ detailData.nguoiTaoDon || detailData.nhanVien || detailData.tenKhachHang || detailData.khachHang || 'Khách hàng tự đặt' }}</div>
                    </div>
                  </div>

                  <div v-if="detailData.ghiChu" class="mb-3 p-3" style="background:#fef3cd;border-radius:var(--z-radius);font-size:13px">
                    <strong>Ghi chú:</strong> {{ detailData.ghiChu }}
                  </div>
                  <div v-if="detailData.thongTinHoanTien" class="mb-3 p-3 z-refund-info">
                    <strong>Thông tin nhận tiền hoàn:</strong>
                    <div class="mt-1">{{ detailData.thongTinHoanTien }}</div>
                  </div>
                  <div v-if="detailData.auditLogs?.length" class="mb-3">
                    <h4 style="font-size:14px;font-weight:600;margin-bottom:12px;color:var(--z-dark)">Lịch sử thao tác</h4>
                    <div class="d-flex flex-column gap-2">
                      <div v-for="log in detailData.auditLogs.slice(0, 5)" :key="log.id" class="z-audit-row">
                        <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ log.hanhDong }}</div>
                        <div style="font-size:12px;color:var(--z-gray)">
                          {{ log.nguoiThucHien || 'System' }} · {{ log.vaiTro || 'System' }} · {{ log.ngayTao ? new Date(log.ngayTao).toLocaleString('vi-VN') : '' }}
                        </div>
                        <div v-if="log.ghiChu" style="font-size:12px;color:var(--z-gray)">{{ log.ghiChu }}</div>
                      </div>
                    </div>
                  </div>
              </div>

              <div class="col-md-5 ps-4">
                  <h4 style="font-size:14px;font-weight:600;margin-bottom:12px; color: var(--z-dark);">Sản phẩm ({{ detailData.chiTiets?.length || 0 }})</h4>
                  <div class="d-flex flex-column gap-0 mb-4" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);overflow:hidden; max-height: 300px; overflow-y: auto;">
                    <div v-for="item in detailData.chiTiets" :key="item.id"
                         class="d-flex align-items-center gap-3" style="padding:12px 14px;border-bottom:1px solid var(--z-gray-border)">
                      <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                        <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover">
                        <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:16px;color:var(--z-gray-light)">
                          <i class="bi bi-image"></i>
                        </div>
                      </div>
                      <div class="flex-grow-1">
                        <div style="font-size:13px;font-weight:500;color:var(--z-dark)">{{ item.tenSanPham || item.tenVay || 'Sản phẩm' }}</div>
                        <div style="font-size:12px;color:var(--z-gray)">
                          <span v-if="item.maSanPham">Mã SP: {{ item.maSanPham }}<br></span>
                          <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                            <span v-if="item.maHex" :style="{ width:'8px', height:'8px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                            {{ item.mauSac }}
                          </span>
                          <span v-if="item.mauSac && item.kichThuoc"> · </span>
                          <span v-if="item.kichThuoc"> Size: {{ item.kichThuoc }}</span>
                          <span v-if="item.phanTramGiam > 0" style="color:var(--z-danger); font-weight: 500;"> | Giảm: {{ item.phanTramGiam }}%</span>
                        </div>
                      </div>
                      <div class="text-end">
                        <div style="font-size:13px;font-weight:600">{{ fmtPrice(item.donGia) }}</div>
                        <div style="font-size:11px;color:var(--z-gray)">x{{ item.soLuong }}</div>
                      </div>
                    </div>
                  </div>

                  <div class="mb-3 p-3 bg-light rounded" style="font-size:13px; color:var(--z-gray)">
                    <div class="d-flex justify-content-between mb-2">
                      <span>Tổng tiền hàng:</span>
                      <span style="font-weight: 500; color: var(--z-dark);">{{ fmtPrice(detailData.tongTien + (detailData.giamGiaVoucher || 0) - (detailData.phiVanChuyen || 0)) }}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2" v-if="detailData.phiVanChuyen > 0">
                      <span>Phí vận chuyển:</span>
                      <span style="font-weight: 500; color: var(--z-dark);">+ {{ fmtPrice(detailData.phiVanChuyen) }}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2" v-if="detailData.giamGiaVoucher > 0">
                      <span>Voucher giảm giá:</span>
                      <span class="text-danger font-weight-bold">- {{ fmtPrice(detailData.giamGiaVoucher) }}</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-2" v-if="detailData.giamGia">
                      <div style="font-size:13px;color:var(--z-gray)">Voucher</div>
                      <div style="font-size:13px;font-weight:500;color:var(--z-success)">{{ detailData.giamGia }}</div>
                    </div>
                    
                    <div class="d-flex justify-content-between align-items-center pt-2 mt-2" style="border-top:1px dashed var(--z-gray-border)">
                        <div style="font-size:14px;font-weight:600; color: var(--z-dark);">Khách cần trả</div>
                        <div style="font-size:20px;font-weight:700;color:var(--z-accent)">{{ fmtPrice(detailData.tongTien) }}</div>
                    </div>
                  </div>
              </div>
          </div>

          <div class="d-flex justify-content-end gap-2 mt-2 pt-3" style="border-top:1px solid var(--z-gray-border)">
             <button v-if="detailData.trangThai === 3 && detailActionOrder" class="z-btn-action z-btn-danger"
                     @click="confirmFail(detailActionOrder)">
              <i class="bi bi-x-circle me-1"></i> Giao thất bại
            </button>
              
            <button v-if="detailActionOrder && canCancel(detailActionOrder)" class="z-btn-action z-btn-danger"
                    @click="confirmCancel(detailActionOrder)">
              <i class="bi bi-trash me-1"></i> Huỷ đơn
            </button>
            
            <button v-if="detailData.hinhThucNhanHang !== 0 && detailActionOrder && canAdvance(detailActionOrder)" class="z-btn-action z-btn-primary"
                    @click="confirmAdvance(detailActionOrder)">
              <i class="bi bi-check-circle me-1"></i> {{ nextStatusLabel(detailActionOrder) }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showConfirm" class="z-modal-overlay" @click.self="showConfirm = false" style="z-index: 1060; background: rgba(0,0,0,0.6);">
      <div class="z-modal" style="max-width:440px">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h3 style="font-size:16px;font-weight:600;margin:0">{{ confirmTitle }}</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng xác nhận" @click="showConfirm = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <p style="font-size:14px;color:var(--z-gray);margin-bottom:16px">{{ confirmMessage }}</p>
        
        <div v-if="confirmType === 'cancel' || confirmType === 'fail'" class="mb-3">
          <label class="z-label">Lý do <span style="color:var(--z-accent)">*</span></label>
          <textarea v-model="cancelNote" class="lm-input" rows="3" :placeholder="confirmType === 'cancel' ? 'Nhập lý do huỷ đơn hàng...' : 'Nhập lý do giao hàng thất bại...'"></textarea>
        </div>
        <div v-else class="mb-3">
          <label class="z-label">Ghi chú (tuỳ chọn)</label>
          <input v-model="actionNote" class="lm-input" placeholder="Ghi chú thêm...">
        </div>

        <div v-if="needPaidConfirm" class="z-cod-paid mb-3">
          <label class="d-flex align-items-start gap-2" style="cursor:pointer">
            <input type="checkbox" v-model="confirmPaid" style="margin-top:3px">
            <span style="font-size:13px;color:var(--z-dark)">
              <strong>Khách đã thanh toán tiền (COD)</strong><br>
              <span style="font-size:12px;color:var(--z-gray)">Đơn COD phải xác nhận đã thu tiền trước khi hoàn thành.</span>
            </span>
          </label>
        </div>

        <div class="d-flex gap-3 w-100 mt-4">
          <button class="z-btn-action z-btn-secondary flex-fill" style="height: 44px;" @click="showConfirm = false">
            Huỷ bỏ
          </button>
          <button class="z-btn-action flex-fill" style="height: 44px;" @click="executeAction"
                   :class="confirmType === 'cancel' || confirmType === 'fail' ? 'z-btn-danger' : 'z-btn-primary'"
                   :disabled="actionSubmitting || ((confirmType === 'cancel' || confirmType === 'fail') && !cancelNote.trim()) || (needPaidConfirm && !confirmPaid)">
            {{ actionSubmitting ? 'Đang xử lý...' : (confirmType === 'cancel' ? 'Xác nhận huỷ' : (confirmType === 'fail' ? 'Xác nhận thất bại' : 'Xác nhận')) }}
          </button>
        </div>
      </div>
    </div>

    <!-- E-Invoice Modal -->
    <EInvoiceModal :show="showEInvoiceModal" :invoice="eInvoiceData" :orderId="selectedEInvoiceOrderId" @close="showEInvoiceModal = false" @updated="onEInvoiceUpdated" />
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import EInvoiceModal from '@/components/EInvoiceModal.vue'
import { api } from '@/composables/useApi'
import { fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/composables/useI18n'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'

const showEInvoiceModal = ref(false)
const selectedEInvoiceOrderId = ref(null)
const eInvoiceData = ref(null)

async function openEInvoiceModal(orderId) {
  selectedEInvoiceOrderId.value = orderId
  try {
    eInvoiceData.value = await api().getEInvoice(orderId)
    showEInvoiceModal.value = true
  } catch (error) {
    showToast(error.error || 'Không thể lấy dữ liệu Hóa Đơn Điện Tử')
  }
}

function onEInvoiceUpdated(updated) {
  eInvoiceData.value = updated
  showToast('Đã phát hành Hóa Đơn Điện Tử VAT thành công!')
  fetchOrders()
}

const { showToast } = useToast()
const { isEn } = useI18n()

function copyOrderCode(codeVal) {
  if (!codeVal) return
  navigator.clipboard.writeText(codeVal)
  showToast(`Đã sao chép mã đơn hàng "${codeVal}"!`)
}

const statusMap = { 
    0: { text: 'Chờ xử lý', cls: 'pending' }, 
    1: { text: 'Đã xác nhận', cls: 'warning' }, 
    2: { text: 'Đang chuẩn bị', cls: 'info' }, 
    3: { text: 'Đang giao', cls: 'primary' }, 
    4: { text: 'Hoàn thành', cls: 'success' }, 
    5: { text: 'Đã huỷ', cls: 'danger' },
    6: { text: 'Giao thất bại', cls: 'danger' } 
}
const statusSteps = ['Chờ xử lý', 'Xác nhận', 'Chuẩn bị', 'Đang giao', 'Hoàn thành']

statusMap[7] = { text: 'Thanh toán thất bại', cls: 'danger' }
statusMap[8] = { text: 'Yêu cầu đổi/trả', cls: 'warning' }
statusMap[9] = { text: 'Đã hoàn tiền', cls: 'danger' }

const search = ref('')
const activeStatus = ref('all')
const allOrders = ref([])
const rawOrders = ref([])

const showDetail = ref(false)
const detailData = ref(null)
const loadingDetail = ref(false)

const orderTypeTab = ref('all') // 'all', 'online', 'offline'
const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalItems = ref(0)
const totalPages = ref(0)
const allStatusesTotal = ref(0)
const statusCounts = ref({})
const orderRangeLabel = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value + 1
  const end = Math.min(currentPage.value * itemsPerPage.value, totalItems.value)
  return isEn.value
    ? `Showing ${start}-${end} of ${totalItems.value} orders`
    : `Hiển thị từ ${start} đến ${end} trong tổng số ${totalItems.value} đơn hàng`
})
function orderItemCountLabel(count) {
  const value = Number(count || 0)
  if (!isEn.value) return `${value} sản phẩm`
  return `${value} ${value === 1 ? 'product' : 'products'}`
}

const showConfirm = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmType = ref('')
const confirmOrder = ref(null)
const confirmNewStatus = ref(0)
const cancelNote = ref('')
const actionNote = ref('')
const confirmPaid = ref(false)
const actionSubmitting = ref(false)

let pollingInterval = null

const needPaidConfirm = computed(() =>
  confirmType.value === 'advance' &&
  confirmNewStatus.value === 4 && // 4 là Hoàn thành mới
  confirmOrder.value && confirmOrder.value.isCod && !confirmOrder.value.paid
)

const detailActionOrder = computed(() => {
  const detail = detailData.value
  if (!detail) return null
  return allOrders.value.find(order => order.dbId === detail.id) || {
    id: detail.maHoaDon || detail.id,
    dbId: detail.id,
    statusValue: String(detail.trangThai ?? 0),
    isCod: isCodPayment(detail.hinhThucThanhToan),
    paid: detail.daThanhToan === true
  }
})

onMounted(async () => {
  await loadOrders()
  pollingInterval = setInterval(async () => {
      if (!showDetail.value && !showConfirm.value) {
          await loadOrdersSilent()
      }
  }, 10000)
})

onUnmounted(() => {
    if (pollingInterval) clearInterval(pollingInterval)
})

async function loadOrdersSilent() {
    try {
        const data = await fetchOrderPage()
        processOrderData(data)
    } catch(e) { }
}

async function loadOrders() {
  try {
    const data = await fetchOrderPage()
    processOrderData(data)
  } catch (e) { console.error('Không thể tải đơn hàng:', e) }
}

function fetchOrderPage() {
  const orderType = orderTypeTab.value === 'offline' ? 0 : orderTypeTab.value === 'online' ? 1 : null
  return api().getHoaDonPage({
    page: currentPage.value - 1,
    size: itemsPerPage.value,
    q: search.value.trim() || null,
    status: activeStatus.value === 'all' ? null : activeStatus.value,
    orderType
  })
}

function processOrderData(data) {
    const rows = data.content || []
    rawOrders.value = rows
    totalItems.value = Number(data.totalElements || 0)
    totalPages.value = Number(data.totalPages || 0)
    allStatusesTotal.value = Number(data.allStatusesTotal || 0)
    statusCounts.value = data.statusCounts || {}
    const sorted = [...rows].sort((a, b) => {
      const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
      const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
      return db - da
    })
    allOrders.value = sorted.map(o => {
      const st = statusMap[o.trangThai] || statusMap[0]
      const displayStatus = Number(o.hinhThucNhanHang) === 0 ? offlineOrderTableStatus(o) : st
      const method = o.hinhThucThanhToan || 'N/A'
      return {
        id: o.maHoaDon, dbId: o.id, customer: o.khachHang || 'Khách lẻ', phone: o.soDienThoai || '',
        items: o.soSanPham || 0, total: fmtPrice(o.tongTien), payment: method,
        paid: o.daThanhToan === true,
        isCod: isCodPayment(method),
        status: displayStatus.text, statusClass: displayStatus.cls, statusValue: String(o.trangThai ?? 0),
        date: o.ngayTao ? new Date(o.ngayTao).toLocaleDateString('vi-VN') : '',
        raw: o
      }
    })
}

const statusTabs = computed(() => [
  { label: 'Tất cả',        value: 'all', count: allStatusesTotal.value },
  { label: 'Chờ xử lý',     value: '0', count: Number(statusCounts.value['0'] || 0) },
  { label: 'Đã xác nhận',   value: '1', count: Number(statusCounts.value['1'] || 0) },
  { label: 'Đang chuẩn bị', value: '2', count: Number(statusCounts.value['2'] || 0) },
  { label: 'Đang giao',     value: '3', count: Number(statusCounts.value['3'] || 0) },
  { label: 'Hoàn thành',    value: '4', count: Number(statusCounts.value['4'] || 0) },
  { label: 'Đã huỷ',        value: '5', count: Number(statusCounts.value['5'] || 0) },
  { label: 'Giao thất bại', value: '6', count: Number(statusCounts.value['6'] || 0) },
  { label: 'Thanh toán thất bại', value: '7', count: Number(statusCounts.value['7'] || 0) },
  { label: 'Yêu cầu đổi/trả', value: '8', count: Number(statusCounts.value['8'] || 0) },
  { label: 'Đã hoàn tiền', value: '9', count: Number(statusCounts.value['9'] || 0) },
])

const filteredOrders = computed(() => allOrders.value)
const paginatedOrders = computed(() => allOrders.value)
const pageNumbers = computed(() => {
  const start = Math.max(1, Math.min(currentPage.value - 2, totalPages.value - 4))
  const end = Math.min(totalPages.value, start + 4)
  return Array.from({ length: Math.max(0, end - start + 1) }, (_, index) => start + index)
})

let orderSearchTimer
watch(search, () => {
  clearTimeout(orderSearchTimer)
  orderSearchTimer = setTimeout(resetOrderPage, 300)
})
watch([activeStatus, orderTypeTab], resetOrderPage)
watch(currentPage, loadOrders)
watch(itemsPerPage, resetOrderPage)

function resetOrderPage() {
  if (currentPage.value === 1) loadOrders()
  else currentPage.value = 1
}

function canAdvance(o) {
  const v = Number(o?.statusValue)
  return v >= 0 && v < 4 // Có 4 bước chuyển trạng thái (0->1->2->3->4)
}

function canCancel(o) {
  const status = Number(o?.statusValue)
  if (o?.paid) return false
  if (status === 0) return true
  return (status === 1 || status === 2) && o?.isCod === true
}

function isCodPayment(method) {
  return String(method || '').trim().toUpperCase() === 'COD'
}

function nextStatusLabel(o) {
  const v = Number(o.statusValue)
  if (v === 0) return 'Xác nhận đơn'
  if (v === 1) return 'Chuyển chuẩn bị'
  if (v === 2) return 'Chuyển giao hàng'
  if (v === 3) return 'Hoàn thành đơn'
  return ''
}

function confirmAdvance(o) {
  const v = Number(o.statusValue)
  const nextVal = v + 1
  const labels = { 1: 'xác nhận', 2: 'chuyển sang chuẩn bị', 3: 'chuyển sang đang giao', 4: 'đánh dấu hoàn thành' }
  confirmTitle.value = nextStatusLabel(o)
  confirmMessage.value = `Bạn có chắc muốn ${labels[nextVal]} đơn hàng ${o.id}?`
  confirmType.value = 'advance'
  confirmOrder.value = o
  confirmNewStatus.value = nextVal
  actionNote.value = ''
  confirmPaid.value = false
  showConfirm.value = true
}

function confirmFail(o) {
  confirmTitle.value = 'Giao hàng thất bại'
  confirmMessage.value = `Ghi nhận giao thất bại cho đơn hàng ${o.id}?`
  confirmType.value = 'fail'
  confirmOrder.value = o
  confirmNewStatus.value = 6 // Giao thất bại là 6
  cancelNote.value = ''
  showConfirm.value = true
}

function confirmCancel(o) {
  confirmTitle.value = 'Huỷ đơn hàng'
  confirmMessage.value = `Bạn có chắc muốn huỷ đơn hàng ${o.id}? Hành động này không thể hoàn tác.`
  confirmType.value = 'cancel'
  confirmOrder.value = o
  confirmNewStatus.value = 5 // Đã hủy là 5
  cancelNote.value = ''
  showConfirm.value = true
}

function offlineOrderStatus(order) {
  if (order.returnRequestStatus === 'DA_HOAN_TIEN') return { label: 'Đã hoàn tiền', cls: 'refunded', icon: 'bi-arrow-counterclockwise' }
  if (order.returnRequestStatus === 'DA_DOI') return { label: 'Đã đổi hàng', cls: 'paid', icon: 'bi-arrow-left-right' }
  if (['CHO_DUYET', 'CHO_NHAN_HANG', 'CHO_HOAN_TAT'].includes(order.returnRequestStatus)) return { label: 'Đang xử lý đổi/trả', cls: 'pending', icon: 'bi-arrow-repeat' }
  if (order.returnRequestStatus === 'TU_CHOI' || order.returnRequestStatus === 'TRA_LAI_KHACH') return { label: 'Đổi/trả không được duyệt', cls: 'cancelled', icon: 'bi-x-circle' }
  if (Number(order.trangThai) === 5) return { label: 'Đã huỷ', cls: 'cancelled', icon: 'bi-x-circle' }
  if (order.daThanhToan) return { label: 'Đã thanh toán tại quầy', cls: 'paid', icon: 'bi-check-circle' }
  return { label: 'Chưa thanh toán tại quầy', cls: 'pending', icon: 'bi-clock' }
}

function offlineOrderTableStatus(order) {
  const info = offlineOrderStatus(order)
  const cls = info.cls === 'paid' ? 'success' : info.cls === 'pending' ? 'warning' : 'danger'
  return { text: info.label, cls }
}

async function executeAction() {
  if (actionSubmitting.value) return
  const note = (confirmType.value === 'cancel' || confirmType.value === 'fail') ? cancelNote.value.trim() : actionNote.value.trim()
  if ((confirmType.value === 'cancel' || confirmType.value === 'fail') && !note) {
    showToast(`Vui lòng nhập lý do ${confirmType.value === 'cancel' ? 'huỷ đơn' : 'thất bại'}`)
    return
  }
  
  const targetOrderId = confirmOrder.value?.dbId
  if (!targetOrderId) {
    showToast('Không xác định được đơn hàng cần cập nhật')
    showConfirm.value = false
    return
  }

  const refreshOpenDetail = showDetail.value && detailData.value?.id === targetOrderId
  actionSubmitting.value = true
  try {
    await api().updateOrderStatus(targetOrderId, confirmNewStatus.value, note || null)
    showConfirm.value = false
    showToast('Cập nhật trạng thái thành công!')
    await loadOrders()

    if (refreshOpenDetail) {
      try {
        detailData.value = await api().getHoaDonById(targetOrderId)
      } catch (refreshError) {
        console.warn('Đã cập nhật trạng thái nhưng không thể tải lại chi tiết đơn hàng:', refreshError)
        showDetail.value = false
      }
    }
    confirmOrder.value = null
  } catch (e) {
    showToast('Lỗi: ' + (e?.error || e?.message || 'Không thể cập nhật'))
  } finally {
    actionSubmitting.value = false
  }
}

async function openDetail(o) {
  if (!o?.dbId) {
    showToast('Không tìm thấy đơn hàng để hiển thị chi tiết')
    return
  }
  showDetail.value = true
  loadingDetail.value = true
  try {
    detailData.value = await api().getHoaDonById(o.dbId)
  } catch (e) {
    if (o.raw) {
      detailData.value = o.raw
    } else {
      detailData.value = null
      showDetail.value = false
      showToast('Không thể tải chi tiết đơn hàng')
    }
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
  width: 32px; height: 32px; border: 1px solid var(--z-gray-border); background: var(--z-white);
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; font-size: 14px;
}
.z-action-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
  backdrop-filter: blur(2px);
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
.z-pay-badge {
  display: inline-flex; align-items: center; gap: 3px;
  font-size: 11px; font-weight: 600; padding: 2px 7px; border-radius: 20px;
  margin-top: 3px;
}
.z-pay-badge.paid { background: #dcfce7; color: #16a34a; }
.z-pay-badge.unpaid { background: #fef3cd; color: #b45309; }
.z-offline-order-status { display: inline-flex; align-items: center; gap: 7px; padding: 7px 12px; font-size: 12px; font-weight: 600; }
.z-offline-order-status.paid { color: #166534; background: #dcfce7; }
.z-offline-order-status.pending { color: #92400e; background: #fef3c7; }
.z-offline-order-status.refunded, .z-offline-order-status.cancelled { color: #991b1b; background: #fee2e2; }
.z-cod-paid {
  background: #fff8e1; border: 1px solid #fde68a; border-radius: var(--z-radius); padding: 12px 14px;
}
.z-audit-row {
  border: 1px solid var(--z-gray-border);
  border-radius: var(--z-radius);
  background: var(--z-bg-alt);
  padding: 10px 12px;
}

/* ============================================================== */
/* BỘ NÚT CHUẨN XÁC DÀNH RIÊNG CHO MODAL (KHÔNG CHE CHỮ) */
/* ============================================================== */
.z-btn-action {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.z-btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.z-refund-info {
  border: 1px solid #bfdbfe;
  border-radius: var(--z-radius);
  background: #eff6ff;
  color: #1e3a8a;
  font-size: 13px;
  overflow-wrap: anywhere;
}

/* Nút Primary (Giống nút Tra cứu) - Màu cam */
.z-btn-primary {
  background-color: var(--z-accent, #e85d04);
  color: #ffffff !important;
  border-color: var(--z-accent, #e85d04);
}
.z-btn-primary:hover:not(:disabled) {
  background-color: #d04c02; 
  color: #ffffff !important;
}

/* Nút Secondary (Xám nhạt) */
.z-btn-secondary {
  background-color: #f3f4f6;
  color: #374151 !important;
  border-color: #e5e7eb;
}
.z-btn-secondary:hover:not(:disabled) {
  background-color: #e5e7eb;
  color: #374151 !important;
}

/* Nút Danger (Đỏ) */
.z-btn-danger {
  background-color: #fef2f2;
  color: #dc2626 !important;
  border-color: #fecaca;
}
.z-btn-danger:hover:not(:disabled) {
  background-color: #dc2626;
  color: #ffffff !important;
  border-color: #dc2626;
}
</style>
