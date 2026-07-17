<template>
  <div>
    <!-- Page Hero -->
    <div class="lm-page-hero" data-title="MY ORDERS">
      <div class="container">
        <p class="lm-eyebrow mb-3">Tài Khoản</p>
        <h1 class="mb-3">Đơn hàng <em>của tôi</em></h1>
        <p>Theo dõi lịch sử đơn hàng, tiến trình vận chuyển và thực hiện thanh toán trực tuyến</p>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container py-5">
      <!-- Member Statistics Banner -->
      <div class="row g-3 mb-5">
        <div v-for="stat in stats" :key="stat.label" class="col-6 col-md-3">
          <div class="z-stat-box p-4 text-center shadow-sm">
            <div class="z-stat-icon mb-2">
              <i :class="stat.icon" style="font-size: 24px; color: var(--z-accent)"></i>
            </div>
            <div class="z-display" style="font-size: 26px; font-weight: 600; color: var(--z-dark)">{{ stat.num }}</div>
            <div style="font-size: 12px; font-weight: 500; color: var(--z-gray); margin-top: 4px">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <!-- Sidebar Navigation -->
        <div class="col-lg-3">
          <div class="z-orders-sidebar p-4 shadow-sm">
            <h5 class="mb-4 font-weight-bold" style="font-size: 16px; color: var(--z-dark)">Trạng thái đơn</h5>
            
            <div class="d-flex flex-column gap-2">
              <button 
                v-for="tab in orderTabs" 
                :key="tab.value"
                class="lm-tab-btn text-start d-flex justify-content-between align-items-center"
                :class="{ active: currentTab === tab.value }"
                @click="currentTab = tab.value"
              >
                <span>{{ tab.label }}</span>
                <span class="badge rounded-pill" :class="currentTab === tab.value ? 'bg-dark' : 'bg-light text-dark'">
                  {{ getCountByTab(tab.value) }}
                </span>
              </button>
            </div>
          </div>
        </div>

        <!-- Orders List -->
        <div class="col-lg-9">
          <!-- Search & Meta -->
          <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div class="position-relative flex-grow-1" style="max-width: 480px;">
              <input 
                v-model="searchQuery" 
                type="text" 
                class="lm-input ps-5" 
                placeholder="Tìm đơn hàng theo mã đơn (HD...)"
              />
              <i class="bi bi-search position-absolute" style="left: 18px; top: 50%; transform: translateY(-50%); color: var(--z-gray-light)"></i>
            </div>
            <div style="font-size: 13px; color: var(--z-gray)">
              Có <strong>{{ currentTab === 'return' ? filteredReturnRequests.length : filteredOrders.length }}</strong> {{ currentTab === 'return' ? 'yêu cầu' : 'đơn hàng' }}
            </div>
          </div>

          <div v-if="currentTab === 'return'" class="d-flex flex-column gap-3">
            <div v-if="loading" class="text-center py-5"><div class="spinner-border text-secondary mb-3"></div><p class="text-muted">Đang tải yêu cầu đổi trả...</p></div>
            <div v-else-if="!filteredReturnRequests.length" class="z-empty-state text-center py-5">
              <i class="bi bi-arrow-left-right" style="font-size:48px;color:var(--z-gray-light)"></i>
              <h3 class="lm-display mt-3 mb-2" style="font-size:20px">Chưa có yêu cầu đổi trả</h3>
              <p class="text-muted" style="font-size:13px">Yêu cầu được tạo theo từng sản phẩm trong đơn đã giao thành công.</p>
            </div>
            <div v-for="item in filteredReturnRequests" :key="item.id" class="z-order-card p-4 shadow-sm">
              <div class="d-flex justify-content-between align-items-start gap-3 pb-3 mb-3 border-bottom">
                <div><strong>Yêu cầu {{ item.type === 'DOI' ? 'đổi' : 'trả' }} #{{ item.id }}</strong><div class="z-return-muted">{{ item.orderCode }} · {{ formatDateTime(item.createdAt) }}</div></div>
                <span class="z-return-status" :class="returnStatusInfo(item.status).cls">{{ returnStatusInfo(item.status).label }}</span>
              </div>
              <div class="d-flex align-items-center gap-3">
                <img v-if="item.productImage" :src="item.productImage" class="z-return-product-image" alt="">
                <div class="flex-grow-1"><strong style="font-size:13px">{{ item.productName }}</strong><div class="z-return-muted">{{ item.productCode }} · {{ item.color }} · Size {{ item.size }} · SL {{ item.quantity }}</div><div v-if="item.replacement" class="z-return-muted mt-1">Đổi sang: {{ item.replacement.color }} · Size {{ item.replacement.size }}</div></div>
              </div>
              <div class="z-return-progress-note">{{ returnStatusInfo(item.status).hint }}<span v-if="item.rejectionReason"><br><strong>Lý do:</strong> {{ item.rejectionReason }}</span></div>
            </div>
          </div>

          <!-- Loading State -->
          <div v-else-if="loading" class="text-center py-5">
            <div class="spinner-border text-secondary mb-3"></div>
            <p class="text-muted">Đang tải lịch sử đơn hàng...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="filteredOrders.length === 0" class="z-empty-state text-center py-5">
            <div class="mb-4">
              <i class="bi bi-box2" style="font-size: 56px; color: var(--z-gray-light)"></i>
            </div>
            <h3 class="lm-display mb-2" style="font-weight: 500; color: var(--z-dark)">Không có đơn hàng nào</h3>
            <p class="text-muted mx-auto mb-4" style="max-width: 420px; font-size: 14px;">
              Hiện tại không có đơn hàng nào nằm trong danh mục này hoặc khớp với mã tìm kiếm của bạn.
            </p>
            <RouterLink to="/collections" class="lm-btn-primary">
              <span>Mua Sắm Ngay</span>
            </RouterLink>
          </div>

          <!-- List -->
          <div v-else class="d-flex flex-column gap-4">
            <div 
              v-for="(order, i) in paginatedOrders" 
              :key="order.id"
              class="z-order-card p-4 shadow-sm"
              :style="{ animationDelay: i * 0.05 + 's' }"
            >
              <!-- Card Top -->
              <div class="d-flex justify-content-between align-items-start pb-3 mb-3 border-bottom flex-wrap gap-2">
                <div>
                  <div class="d-flex align-items-center gap-2">
                    <span style="font-weight: 700; font-size: 15px; color: var(--z-dark)">{{ order.maHoaDon }}</span>
                    <span class="badge-payment">{{ order.hinhThucThanhToan }}</span>
                  </div>
                  <div style="font-size: 12px; color: var(--z-gray); margin-top: 4px;">
                    Đặt ngày: {{ formatDateTime(order.ngayTao) }}
                  </div>
                </div>

                <div class="d-flex flex-column align-items-end gap-1">
                  <span :class="['badge-status', statusMap[order.trangThai]?.key || 'pending']">
                    {{ statusMap[order.trangThai]?.label || 'Chờ xử lý' }}
                  </span>
                  
                  <span v-if="order.hinhThucThanhToan !== 'COD'" 
                        :class="['badge-payment-status', order.daThanhToan ? 'paid' : 'unpaid']">
                    {{ order.daThanhToan ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                  </span>
                </div>
              </div>

              <!-- Card Middle -->
              <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                <div>
                  <div style="font-size: 13px; color: var(--z-gray)">Tổng số tiền thanh toán</div>
                  <div class="z-display mt-1" style="font-size: 22px; font-weight: 700; color: var(--z-accent)">
                    {{ formatMoney(order.tongTien) }}
                  </div>
                  <div style="font-size: 12px; color: var(--z-gray-light); margin-top: 2px;">
                    {{ order.soSanPham || 1 }} sản phẩm
                  </div>
                </div>

                <!-- Action buttons -->
                <div class="d-flex gap-2 flex-wrap">
                  <!-- Repay Online -->
                  <button 
                    v-if="!order.daThanhToan && order.trangThai !== 5 && order.trangThai !== 7 && (order.hinhThucThanhToan === 'MOMO' || order.hinhThucThanhToan === 'ZALOPAY')"
                    class="lm-btn-outline-accent py-2 px-3 d-flex align-items-center gap-2"
                    style="font-size: 12px; height: auto;"
                    @click="repayOrder(order)"
                    :disabled="payingId === order.id"
                  >
                    <i class="bi bi-credit-card-2-back"></i>
                    <span>{{ payingId === order.id ? 'Đang tạo...' : 'Thanh toán lại' }}</span>
                  </button>

                  <!-- Cancel Request -->
                  <button 
                    v-if="order.trangThai === 0"
                    class="lm-btn-outline-danger py-2 px-3 d-flex align-items-center gap-2"
                    style="font-size: 12px; height: auto;"
                    @click="openCancel(order)"
                  >
                    <i class="bi bi-x-circle"></i>
                    <span>Hủy đơn</span>
                  </button>

                  <button
                    v-if="order.trangThai === 4 && order.hinhThucNhanHang !== 0"
                    class="lm-btn-outline-accent py-2 px-3 d-flex align-items-center gap-2"
                    style="font-size: 12px; height: auto;"
                    @click="openReturn(order)"
                  >
                    <i class="bi bi-arrow-counterclockwise"></i>
                    <span>Đổi / trả hàng</span>
                  </button>

                  <!-- View details -->
                  <button 
                    class="lm-btn-primary py-2 px-3 d-flex align-items-center gap-2"
                    style="font-size: 12px; height: auto;"
                    @click="openDetail(order)"
                  >
                    <i class="bi bi-eye"></i>
                    <span>Chi tiết</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="currentTab !== 'return' && totalPages > 1" class="d-flex justify-content-center gap-2 mt-5">
            <button type="button" class="lm-pagination-btn" aria-label="Trang đơn hàng trước" :disabled="currentPage === 1" @click="currentPage--">
              <i class="bi bi-chevron-left"></i>
            </button>
            <button 
              v-for="page in totalPages" 
              :key="page"
              class="lm-pagination-btn"
              :class="{ active: currentPage === page }"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
            <button type="button" class="lm-pagination-btn" aria-label="Trang đơn hàng sau" :disabled="currentPage === totalPages" @click="currentPage++">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="showDetail" class="z-modal-overlay animate-fade" @click.self="showDetail = false" style="z-index: 1050; backdrop-filter: blur(4px);">
      <div class="z-modal shadow-lg" style="max-width:840px; border-radius: var(--z-radius-lg)">
        <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
          <div>
            <h3 style="font-size:18px;font-weight:700;margin:0;color:var(--z-dark)">Chi tiết đơn hàng</h3>
            <div style="font-size:13px;color:var(--z-gray);margin-top:2px;">Mã hóa đơn: <strong>{{ detailOrder?.maHoaDon }}</strong></div>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng chi tiết đơn hàng" @click="showDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingDetail" class="text-center py-5">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
          <p style="font-size:13px;color:var(--z-gray);margin-top:8px">Đang tải thông tin chi tiết đơn hàng...</p>
        </div>

        <div v-else-if="detailOrder">
          <!-- Tracking Card component -->
          <div class="mb-4">
            <OrderTrackingCard :order="detailOrder" />
          </div>

          <!-- Progress timeline -->
          <div class="d-flex align-items-center gap-2 mb-4 pb-3" style="border-bottom:1px solid var(--z-gray-border);overflow-x:auto">
            <template v-if="detailOrder.trangThai === 5 || detailOrder.trangThai === 7">
               <div class="z-step active">
                  <div class="z-step-dot" style="background: var(--z-danger);"></div>
                  <div class="z-step-label" style="color: var(--z-danger); font-weight: 600;">Đã huỷ</div>
               </div>
            </template>
            <template v-else>
                <div v-for="(step, i) in statusSteps" :key="i"
                     class="z-step" :class="{ active: detailOrder.trangThai >= i && detailOrder.trangThai !== 6, current: detailOrder.trangThai === i, failed: i === 4 && detailOrder.trangThai === 6 }">
                  <div class="z-step-dot" :style="i === 4 && detailOrder.trangThai === 6 ? 'background: var(--z-danger)' : ''"></div>
                  <div class="z-step-label" :style="i === 4 && detailOrder.trangThai === 6 ? 'color: var(--z-danger); font-weight: 600;' : ''">
                      {{ i === 4 && detailOrder.trangThai === 6 ? 'Giao thất bại' : step }}
                  </div>
                  <div v-if="i < statusSteps.length - 1" class="z-step-line" :class="{ filled: detailOrder.trangThai > i && detailOrder.trangThai !== 6 }"></div>
                </div>
            </template>
          </div>

          <!-- Customer info / Payment info split -->
          <div class="row mb-4 mt-2">
            <div class="col-md-6 p-3 rounded" style="background: var(--z-bg); border: 1px solid var(--z-gray-border)">
              <h5 style="font-size:14px;font-weight:700;color:var(--z-dark);margin-bottom:12px;">
                <i class="bi bi-geo-alt me-1"></i> Thông tin nhận hàng
              </h5>
              <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Người nhận:</strong> {{ detailOrder.khachHang || 'Khách hàng' }}</div>
              <div v-if="detailOrder.soDienThoai" style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Số điện thoại:</strong> {{ detailOrder.soDienThoai }}</div>
              <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Hình thức:</strong> {{ detailOrder.hinhThucNhanHang === 0 ? 'Nhận tại cửa hàng' : 'Giao hàng tận nơi' }}</div>
              <div v-if="detailOrder.diaChiGiaoHang && detailOrder.hinhThucNhanHang !== 0" style="font-size:13px;color:var(--z-dark);"><strong>Địa chỉ:</strong> {{ detailOrder.diaChiGiaoHang }}</div>
            </div>
            <div class="col-md-6 p-3 rounded mt-3 mt-md-0" style="background: var(--z-bg); border: 1px solid var(--z-gray-border)">
               <h5 style="font-size:14px;font-weight:700;color:var(--z-dark);margin-bottom:12px;">
                 <i class="bi bi-credit-card me-1"></i> Thông tin thanh toán
               </h5>
               <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Ngày đặt:</strong> {{ formatDateTime(detailOrder.ngayTao) }}</div>
               <div style="font-size:13px;color:var(--z-dark); margin-bottom: 6px;"><strong>Phương thức:</strong> {{ detailOrder.hinhThucThanhToan }}</div>
               <div style="font-size:13px;color:var(--z-dark);">
                 <strong>Trạng thái: </strong> 
                 <span v-if="detailOrder.phuongThucThanhToanOnline === 'FAILED' || (detailOrder.trangThai === 5 && (detailOrder.hinhThucThanhToan === 'MOMO' || detailOrder.hinhThucThanhToan === 'ZALOPAY') && !detailOrder.daThanhToan)"
                       class="text-danger" style="font-weight: 600;">
                    Thanh toán thất bại
                 </span>
                 <span v-else :class="detailOrder.daThanhToan ? 'text-success' : 'text-warning'" style="font-weight: 600;">
                    {{ detailOrder.daThanhToan ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                 </span>
               </div>
            </div>
          </div>

          <!-- Products list -->
          <h5 style="font-size:14px;font-weight:700;color:var(--z-dark);margin-bottom:12px;">Sản phẩm đã mua ({{ detailOrder.chiTiets?.length || 0 }})</h5>
          <div class="d-flex flex-column gap-0 mb-4" style="border:1px solid var(--z-gray-border);border-radius:var(--z-radius);overflow:hidden; max-height: 220px; overflow-y: auto;">
            <div v-for="item in detailOrder.chiTiets" :key="item.id"
                 class="d-flex align-items-center justify-content-between gap-3 bg-white" style="padding:12px 16px;border-bottom:1px solid var(--z-gray-border)">
              <div class="d-flex align-items-center gap-3">
                <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                  <img v-if="item.anhUrl" :src="item.anhUrl" style="width:100%;height:100%;object-fit:cover">
                  <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:18px;color:var(--z-gray-light)">
                    <i class="bi bi-image"></i>
                  </div>
                </div>
                <div>
                  <div style="font-size:13px;font-weight:600;color:var(--z-dark)">{{ item.tenVay || 'Sản phẩm' }}</div>
                  <div style="font-size:11px;color:var(--z-gray);margin-top:2px">
                    <span v-if="item.maSanPham">Mã SP: {{ item.maSanPham }} · </span>
                    <span v-if="item.mauSac" class="d-inline-flex align-items-center gap-1">
                      <span v-if="item.maHex" :style="{ width:'8px', height:'8px', borderRadius:'50%', background: item.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                      {{ item.mauSac }}
                    </span>
                    <span v-if="item.mauSac && item.kichThuoc"> · </span>
                    <span v-if="item.kichThuoc">Size: {{ item.kichThuoc }}</span>
                    <span> · SL: {{ item.soLuong }}</span>
                  </div>
                </div>
              </div>
              <div class="d-flex flex-column align-items-end gap-2">
                <div style="font-size:13px;font-weight:700;color:var(--z-dark)">{{ formatMoney(item.donGia) }}</div>
                <RouterLink
                  v-if="detailOrder.trangThai === 4 && item.productId"
                  :to="`/product/${item.productId}#reviews`"
                  class="z-order-review-link"
                  @click="showDetail = false"
                >
                  <i class="bi bi-star"></i> Đánh giá sản phẩm
                </RouterLink>
              </div>
            </div>
          </div>

          <!-- Ghi chu -->
          <div v-if="detailOrder.ghiChu" class="mb-3 p-3 rounded" style="background:var(--z-bg-alt);font-size:13px;color:var(--z-gray)">
            <strong>Ghi chú đơn hàng:</strong> {{ detailOrder.ghiChu }}
          </div>

          <!-- Total fees card -->
          <div class="p-3 bg-light rounded" style="font-size:13px; color:var(--z-gray)">
             <div class="d-flex justify-content-between mb-2">
                <span>Tổng tiền hàng:</span>
                <span style="font-weight: 500; color: var(--z-dark);">{{ formatMoney(detailOrder.tongTien + (detailOrder.giamGiaVoucher || 0) - (detailOrder.phiVanChuyen || 0)) }}</span>
             </div>
             
             <div class="d-flex justify-content-between mb-2">
                <span>Phí vận chuyển:</span>
                <span style="font-weight: 500; color: var(--z-dark);">
                  {{ detailOrder.phiVanChuyen > 0 ? '+ ' + formatMoney(detailOrder.phiVanChuyen) : '0đ (Miễn phí)' }}
                </span>
             </div>
             
             <div class="d-flex justify-content-between mb-2">
                <span>Voucher giảm giá:</span>
                <span class="text-danger font-weight-bold">
                  {{ detailOrder.giamGiaVoucher > 0 ? '- ' + formatMoney(detailOrder.giamGiaVoucher) : '0đ' }}
                </span>
             </div>
             
             <div class="d-flex justify-content-between align-items-center pt-2 mt-2" style="border-top:1px dashed var(--z-gray-border)">
                <div style="font-size:14px;font-weight:600; color: var(--z-dark);">Tổng cộng</div>
                <div class="z-display" style="font-size:22px;font-weight:700;color:var(--z-accent)">{{ formatMoney(detailOrder.tongTien) }}</div>
             </div>
          </div>

        </div>

        <div class="d-flex justify-content-end mt-4">
          <button class="lm-btn-primary" style="padding: 10px 28px; font-size:12px; height:auto;" @click="showDetail = false">
            <span>Đóng</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Cancel Confirmation Modal -->
    <div v-if="showCancelModal" class="z-modal-overlay" @click.self="showCancelModal = false" style="z-index: 1060; background: rgba(0,0,0,0.6);">
      <div class="z-modal" style="max-width:500px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">Lý do huỷ đơn hàng</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng hủy đơn hàng" @click="showCancelModal = false"><i class="bi bi-x-lg"></i></button>
        </div>
        <p style="font-size:14px;color:var(--z-gray);margin-bottom:20px">Vui lòng cho Zestia biết lý do bạn muốn huỷ đơn hàng này nhé:</p>
        
        <div class="d-flex flex-column gap-3 mb-4">
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Thay đổi ý định mua">
            <span style="font-size:14px;color:var(--z-dark)">Thay đổi ý định mua</span>
          </label>
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Tìm thấy giá rẻ hơn ở nơi khác">
            <span style="font-size:14px;color:var(--z-dark)">Tìm thấy giá rẻ hơn ở nơi khác</span>
          </label>
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Quên áp dụng mã giảm giá">
            <span style="font-size:14px;color:var(--z-dark)">Quên áp dụng mã giảm giá</span>
          </label>
          <label class="d-flex align-items-center gap-2" style="cursor:pointer">
            <input type="radio" v-model="cancelReason" value="Khác">
            <span style="font-size:14px;color:var(--z-dark)">Khác...</span>
          </label>
          
          <textarea v-if="cancelReason === 'Khác'" v-model="otherCancelReason" 
                    class="lm-input mt-2" rows="3" placeholder="Vui lòng nhập lý do cụ thể..."></textarea>
        </div>

        <div class="d-flex gap-3">
          <button class="lm-btn-secondary flex-fill" style="height:44px;" @click="showCancelModal = false">Đóng</button>
          <button class="z-danger-action-btn flex-fill" style="height:44px;" @click="submitCancelOrder">
            <span>Xác nhận huỷ</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="showReturnModal" class="z-modal-overlay" @click.self="showReturnModal = false" style="z-index: 1060; background: rgba(0,0,0,0.6);">
      <div class="z-modal z-customer-return-modal">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div><h3 style="font-size:18px;font-weight:600;margin:0">Yêu cầu đổi hoặc trả hàng</h3><div class="z-return-muted">{{ orderToReturn?.maHoaDon }} · Áp dụng cho đơn giao online</div></div>
          <button type="button" class="z-icon-btn" aria-label="Đóng yêu cầu đổi trả" @click="showReturnModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="z-return-type-switch mb-3">
          <button :class="{ active: returnType === 'DOI' }" @click="returnType = 'DOI'; loadReturnReplacementOptions()"><i class="bi bi-arrow-left-right"></i> Đổi hàng</button>
          <button :class="{ active: returnType === 'TRA' }" @click="returnType = 'TRA'; replacementVariantId = null"><i class="bi bi-cash-coin"></i> Trả hàng</button>
        </div>

        <div class="row g-3">
          <div class="col-md-8">
            <label class="z-return-label">Sản phẩm cần {{ returnType === 'DOI' ? 'đổi' : 'trả' }} *</label>
            <select v-model="selectedReturnDetailId" class="lm-input" @change="onReturnLineChanged">
              <option :value="null">Chọn sản phẩm trong đơn</option>
              <option v-for="line in eligibleReturnLines" :key="line.id" :value="line.id">{{ line.maSanPham }} · {{ line.tenVay }} · {{ line.mauSac }} · Size {{ line.kichThuoc }}</option>
            </select>
          </div>
          <div class="col-md-4"><label class="z-return-label">Số lượng *</label><input v-model.number="returnQuantity" class="lm-input" type="number" min="1" :max="selectedReturnLine?.soLuong || 1"></div>
          <div v-if="returnType === 'DOI'" class="col-12">
            <label class="z-return-label">Màu / kích cỡ muốn đổi *</label>
            <select v-model="replacementVariantId" class="lm-input"><option :value="null">Chọn biến thể còn hàng</option><option v-for="variant in returnReplacementOptions" :key="variant.id" :value="variant.id">{{ variant.mauSac }} · Size {{ variant.kichThuoc }} · còn {{ variant.soLuong }}</option></select>
          </div>
          <div class="col-12"><label class="z-return-label">Lý do *</label><textarea v-model="returnReason" class="lm-input" rows="3" maxlength="1000" placeholder="Ví dụ: sản phẩm không vừa, giao sai màu..."></textarea></div>
          <div class="col-12"><label class="z-return-label">Chi tiết tình trạng hàng *</label><textarea v-model="returnCondition" class="lm-input" rows="4" maxlength="2000" placeholder="Mô tả tem mác, bao bì, dấu hiệu đã sử dụng và lỗi nhìn thấy..."></textarea></div>
          <div v-if="returnType === 'TRA'" class="col-12">
            <label class="z-return-label">Thông tin nhận tiền hoàn *</label>
            <textarea v-model="refundReceivingInfo" class="lm-input" rows="3" maxlength="500" placeholder="Tên chủ tài khoản, ngân hàng/ví và số tài khoản/số điện thoại..."></textarea>
            <div class="z-return-help">Không nhập mật khẩu, mã PIN hoặc OTP thanh toán.</div>
          </div>
          <div class="col-12">
            <label class="z-return-label">Ảnh tình trạng hàng (1–5 ảnh) *</label>
            <input class="lm-input z-file-input" type="file" accept="image/jpeg,image/png" multiple @change="onReturnImages">
            <div class="z-return-help">Mỗi ảnh tối đa 5MB. Hãy chụp rõ sản phẩm, tem mác và vị trí lỗi.</div>
            <div v-if="returnImagePreviews.length" class="z-return-preview-list"><div v-for="(url, index) in returnImagePreviews" :key="url"><img :src="url" alt="Ảnh tình trạng"><button type="button" title="Bỏ ảnh" aria-label="Bỏ ảnh tình trạng" @click="removeReturnImage(index)"><i class="bi bi-x"></i></button></div></div>
          </div>
        </div>
        <div class="d-flex gap-3 mt-4 pt-3 border-top">
          <button class="lm-btn-secondary flex-fill" style="height:44px;" @click="showReturnModal = false">Đóng</button>
          <button class="lm-btn-primary flex-fill" style="height:44px;" :disabled="submittingReturn" @click="submitReturnOrder">{{ submittingReturn ? 'Đang gửi...' : 'Xác nhận gửi' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import OrderTrackingCard from '@/components/OrderTrackingCard.vue'
import { api } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const toast = useToast()
const { confirmDialog } = useConfirm()

const loading = ref(false)
const orders = ref([])
const currentTab = ref('all')
const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 6

const showDetail = ref(false)
const detailOrder = ref(null)
const loadingDetail = ref(false)

const showCancelModal = ref(false)
const cancelReason = ref('')
const otherCancelReason = ref('')
const orderToCancel = ref(null)
const showReturnModal = ref(false)
const returnRequests = ref([])
const returnType = ref('DOI')
const selectedReturnDetailId = ref(null)
const returnQuantity = ref(1)
const returnReason = ref('')
const returnCondition = ref('')
const refundReceivingInfo = ref('')
const replacementVariantId = ref(null)
const returnReplacementOptions = ref([])
const returnImages = ref([])
const returnImagePreviews = ref([])
const submittingReturn = ref(false)
const orderToReturn = ref(null)
const payingId = ref(null)

const orderTabs = [
  { label: 'Tất cả', value: 'all' },
  { label: 'Chờ thanh toán', value: 'unpaid' },
  { label: 'Chờ xử lý', value: 'pending' },
  { label: 'Đang xử lý / giao', value: 'processing' },
  { label: 'Đã hoàn thành', value: 'completed' },
  { label: 'Đã huỷ', value: 'cancelled' }
  , { label: 'Đổi/trả', value: 'return' }
]

const statusMap = {
  0: { key: 'pending', label: 'Chờ xử lý' },
  1: { key: 'warning', label: 'Đã xác nhận' },
  2: { key: 'info', label: 'Đang chuẩn bị' },
  3: { key: 'primary', label: 'Đang giao' },
  4: { key: 'success', label: 'Hoàn thành' },
  5: { key: 'danger', label: 'Đã huỷ' },
  6: { key: 'danger', label: 'Giao thất bại' },
}

const statusSteps = ['Chờ xử lý', 'Xác nhận', 'Chuẩn bị', 'Đang giao', 'Hoàn thành']

statusMap[7] = { key: 'danger', label: 'Thanh toán thất bại' }
statusMap[8] = { key: 'warning', label: 'Yêu cầu đổi/trả' }
statusMap[9] = { key: 'danger', label: 'Đã hoàn tiền' }

onMounted(loadOrders)
onBeforeUnmount(clearReturnImagePreviews)

async function loadOrders() {
  loading.value = true
  try {
    const [ordersRes, returnsRes] = await Promise.all([
      api().getMyOrders(),
      api().getMyReturnRequests().catch(() => [])
    ])
    orders.value = ordersRes.data || ordersRes || []
    returnRequests.value = returnsRes || []
  } catch (e) {
    toast.showToast('Không thể tải danh sách đơn hàng', 'error')
  } finally {
    loading.value = false
  }
}

watch([currentTab, searchQuery], () => {
  currentPage.value = 1
})

// Statistics calculations
const stats = computed(() => {
  const total = orders.value.length
  const spent = orders.value
    .filter(o => o.trangThai === 4 || o.trangThai === 3)
    .reduce((s, o) => s + Number(o.tongTien || 0), 0)
  return [
    { num: String(total), label: 'Tổng đơn hàng', icon: 'bi bi-box2' },
    { num: formatMoney(spent), label: 'Đã chi tiêu', icon: 'bi bi-wallet2' },
    { num: String(Math.floor(spent / 10000)), label: 'Điểm tích luỹ', icon: 'bi bi-gem' },
    { num: total >= 10 ? 'Vàng' : total >= 5 ? 'Bạc' : 'Mới', label: 'Hạng thành viên', icon: 'bi bi-award' },
  ]
})

function getCountByTab(tabValue) {
  if (tabValue === 'all') return orders.value.length
  if (tabValue === 'unpaid') {
    return orders.value.filter(o => o.hinhThucThanhToan !== 'COD' && !o.daThanhToan && o.trangThai !== 5 && o.trangThai !== 7).length
  }
  if (tabValue === 'pending') return orders.value.filter(o => o.trangThai === 0).length
  if (tabValue === 'processing') return orders.value.filter(o => o.trangThai >= 1 && o.trangThai <= 3).length
  if (tabValue === 'completed') return orders.value.filter(o => o.trangThai === 4).length
  if (tabValue === 'return') return returnRequests.value.length
  if (tabValue === 'cancelled') return orders.value.filter(o => o.trangThai === 5 || o.trangThai === 6 || o.trangThai === 7).length
  return 0
}

// Filtered Orders
const filteredOrders = computed(() => {
  return orders.value.filter(o => {
    // Filter by Tab
    let matchesTab = true
    if (currentTab.value === 'unpaid') {
      matchesTab = o.hinhThucThanhToan !== 'COD' && !o.daThanhToan && o.trangThai !== 5 && o.trangThai !== 7
    } else if (currentTab.value === 'pending') {
      matchesTab = o.trangThai === 0
    } else if (currentTab.value === 'processing') {
      matchesTab = o.trangThai >= 1 && o.trangThai <= 3
    } else if (currentTab.value === 'completed') {
      matchesTab = o.trangThai === 4
    } else if (currentTab.value === 'return') {
      matchesTab = false
    } else if (currentTab.value === 'cancelled') {
      matchesTab = o.trangThai === 5 || o.trangThai === 6 || o.trangThai === 7
    }

    // Filter by Search
    const matchesSearch = !searchQuery.value.trim() || 
      o.maHoaDon.toLowerCase().includes(searchQuery.value.toLowerCase())

    return matchesTab && matchesSearch
  }).sort((a, b) => {
    const da = a.ngayTao ? new Date(a.ngayTao).getTime() : 0
    const db = b.ngayTao ? new Date(b.ngayTao).getTime() : 0
    return db - da
  })
})

// Pagination
const totalPages = computed(() => Math.ceil(filteredOrders.value.length / itemsPerPage))
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredOrders.value.slice(start, start + itemsPerPage)
})

const filteredReturnRequests = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  return returnRequests.value.filter(item => !query || String(item.orderCode || '').toLowerCase().includes(query))
})

const eligibleReturnLines = computed(() => (orderToReturn.value?.chiTiets || []).filter(line =>
  !returnRequests.value.some(request => Number(request.orderDetailId) === Number(line.id))
))

const selectedReturnLine = computed(() => eligibleReturnLines.value.find(line => Number(line.id) === Number(selectedReturnDetailId.value)))

// Actions
async function openDetail(order) {
  showDetail.value = true
  loadingDetail.value = true
  try {
    const [orderRes, trackingRes] = await Promise.all([
      api().getHoaDonById(order.id).catch(() => null),
      api().getOrderTracking(order.id).catch(() => null)
    ])
    const orderData = orderRes?.data || orderRes || order
    const trackingData = trackingRes?.data || trackingRes
    
    detailOrder.value = { 
      ...orderData, 
      trackingHistory: trackingData?.trackingHistory || [] 
    }
  } catch (e) {
    detailOrder.value = order
  } finally {
    loadingDetail.value = false
  }
}

function openCancel(order) {
  orderToCancel.value = order
  cancelReason.value = ''
  otherCancelReason.value = ''
  showCancelModal.value = true
}

async function openReturn(order) {
  clearReturnImagePreviews()
  returnType.value = 'DOI'
  selectedReturnDetailId.value = null
  returnQuantity.value = 1
  returnReason.value = ''
  returnCondition.value = ''
  refundReceivingInfo.value = ''
  replacementVariantId.value = null
  returnReplacementOptions.value = []
  try {
    orderToReturn.value = await api().getHoaDonById(order.id)
    if (!eligibleReturnLines.value.length) {
      toast.showToast('Các sản phẩm trong đơn này đã có hồ sơ đổi hoặc trả', 'warning')
      return
    }
    showReturnModal.value = true
  } catch (error) {
    toast.showToast(error.error || 'Không thể tải chi tiết đơn hàng', 'error')
  }
}

async function onReturnLineChanged() {
  returnQuantity.value = 1
  await loadReturnReplacementOptions()
}

async function loadReturnReplacementOptions() {
  replacementVariantId.value = null
  returnReplacementOptions.value = []
  if (returnType.value !== 'DOI' || !selectedReturnLine.value?.productId) return
  try {
    const product = await api().getVayById(selectedReturnLine.value.productId)
    returnReplacementOptions.value = (product.bienThe || []).filter(variant =>
      Number(variant.id) !== Number(selectedReturnLine.value.variantId)
      && Number(variant.trangThai) === 1 && Number(variant.soLuong || 0) > 0
    )
  } catch (error) {
    toast.showToast(error.error || 'Không thể tải màu và kích cỡ đổi mới', 'error')
  }
}

function onReturnImages(event) {
  const files = Array.from(event.target.files || []).slice(0, 5)
  if (files.some(file => file.size > 5 * 1024 * 1024)) {
    toast.showToast('Mỗi ảnh chỉ được tối đa 5MB', 'warning')
    event.target.value = ''
    return
  }
  clearReturnImagePreviews()
  returnImages.value = files
  returnImagePreviews.value = files.map(file => URL.createObjectURL(file))
}

function removeReturnImage(index) {
  URL.revokeObjectURL(returnImagePreviews.value[index])
  returnImagePreviews.value.splice(index, 1)
  returnImages.value.splice(index, 1)
}

function clearReturnImagePreviews() {
  returnImagePreviews.value.forEach(url => URL.revokeObjectURL(url))
  returnImagePreviews.value = []
  returnImages.value = []
}

async function submitCancelOrder() {
  if (!cancelReason.value) {
    toast.showToast('Vui lòng chọn lý do hủy!', 'warning')
    return
  }
  const finalReason = cancelReason.value === 'Khác' ? otherCancelReason.value : cancelReason.value
  if (cancelReason.value === 'Khác' && !finalReason.trim()) {
    toast.showToast('Vui lòng nhập lý do cụ thể!', 'warning')
    return
  }

  try {
    const res = await api().cancelMyOrder(orderToCancel.value.id, finalReason)
    if (res && res.error) {
      toast.showToast(res.error, 'error')
    } else {
      toast.showToast('Huỷ đơn hàng thành công!', 'success')
      showCancelModal.value = false
      await loadOrders()
    }
  } catch (e) {
    toast.showToast(e.error || 'Lỗi khi hủy đơn hàng', 'error')
  }
}

async function submitReturnOrder() {
  if (!selectedReturnLine.value) return toast.showToast('Vui lòng chọn sản phẩm', 'warning')
  if (returnQuantity.value < 1 || returnQuantity.value > Number(selectedReturnLine.value.soLuong || 0)) return toast.showToast('Số lượng không hợp lệ', 'warning')
  if (returnType.value === 'DOI' && !replacementVariantId.value) return toast.showToast('Vui lòng chọn màu và kích cỡ muốn đổi', 'warning')
  if (returnReason.value.trim().length < 5) return toast.showToast('Lý do cần ít nhất 5 ký tự', 'warning')
  if (returnCondition.value.trim().length < 10) return toast.showToast('Vui lòng mô tả tình trạng hàng ít nhất 10 ký tự', 'warning')
  if (returnType.value === 'TRA' && refundReceivingInfo.value.trim().length < 10) return toast.showToast('Vui lòng nhập đầy đủ thông tin nhận tiền hoàn', 'warning')
  if (!returnImages.value.length) return toast.showToast('Vui lòng tải ít nhất một ảnh tình trạng hàng', 'warning')
  const accepted = await confirmDialog({ title: `Gửi yêu cầu ${returnType.value === 'DOI' ? 'đổi' : 'trả'} hàng`, message: 'Zestia sẽ dùng thông tin và ảnh này để kiểm tra điều kiện đổi trả.', confirmText: 'Xác nhận gửi' })
  if (!accepted) return
  submittingReturn.value = true
  try {
    await api().createOnlineReturnRequest({
      orderId: orderToReturn.value.id,
      orderDetailId: selectedReturnLine.value.id,
      type: returnType.value,
      quantity: returnQuantity.value,
      reason: returnReason.value.trim(),
      condition: returnCondition.value.trim(),
      refundInfo: returnType.value === 'TRA' ? refundReceivingInfo.value.trim() : null,
      replacementVariantId: returnType.value === 'DOI' ? replacementVariantId.value : null,
      images: returnImages.value
    })
    toast.showToast(`Đã gửi yêu cầu ${returnType.value === 'DOI' ? 'đổi' : 'trả'} hàng`, 'success')
    showReturnModal.value = false
    clearReturnImagePreviews()
    await loadOrders()
  } catch (e) {
    toast.showToast(e.error || 'Không thể gửi yêu cầu đổi/trả', 'error')
  } finally {
    submittingReturn.value = false
  }
}

function returnStatusInfo(status) {
  return {
    CHO_DUYET: { label: 'Chờ nhân viên duyệt', cls: 'pending', hint: 'Zestia đang kiểm tra nội dung và ảnh tình trạng hàng.' },
    CHO_NHAN_HANG: { label: 'Đã duyệt · Chờ gửi hàng', cls: 'waiting', hint: 'Vui lòng gửi sản phẩm về cửa hàng theo hướng dẫn của nhân viên.' },
    CHO_HOAN_TAT: { label: 'Đã nhận hàng', cls: 'processing', hint: 'Sản phẩm đã được nhận và đang chờ hoàn tất xử lý.' },
    TU_CHOI: { label: 'Không đủ điều kiện', cls: 'rejected', hint: 'Yêu cầu đã bị từ chối.' },
    TRA_LAI_KHACH: { label: 'Trả lại hàng', cls: 'rejected', hint: 'Hàng gửi về không đạt điều kiện và sẽ được gửi lại.' },
    DA_DOI: { label: 'Đã đổi hàng', cls: 'done', hint: 'Yêu cầu đổi hàng đã hoàn tất.' },
    DA_HOAN_TIEN: { label: 'Đã hoàn tiền', cls: 'done', hint: 'Yêu cầu trả hàng và hoàn tiền đã hoàn tất.' }
  }[status] || { label: status, cls: 'pending', hint: '' }
}

// Repay Online
async function repayOrder(order) {
  payingId.value = order.id
  try {
    let res = null
    if (order.hinhThucThanhToan === 'MOMO') {
      res = await api().createMomoPayment(order.id, order.maHoaDon, order.soDienThoai)
    } else if (order.hinhThucThanhToan === 'ZALOPAY') {
      res = await api().createZaloPayment(order.id, order.maHoaDon, order.soDienThoai)
    }
    if (res && res.payUrl) {
      window.location.href = res.payUrl
    } else {
      toast.showToast(res?.error || 'Không thể kết nối cổng thanh toán, vui lòng thử lại sau!', 'error')
    }
  } catch (e) {
    toast.showToast(e.error || 'Lỗi khi kết nối thanh toán trực tuyến', 'error')
  } finally {
    payingId.value = null
  }
}

// Formatters
function formatMoney(n) {
  return Number(n || 0).toLocaleString('vi-VN') + 'đ'
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  return d.toLocaleString('vi-VN')
}
</script>

<style scoped>
/* Box layout */
.z-stat-box {
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
}

.z-orders-sidebar {
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
  position: sticky;
  top: 90px;
}

.lm-tab-btn {
  border: none;
  background: none;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--z-gray);
  border-radius: var(--z-radius);
  transition: all 0.3s ease;
  width: 100%;
}
.lm-tab-btn:hover {
  background: var(--z-bg-alt);
  color: var(--z-dark);
}
.lm-tab-btn.active {
  background: var(--z-dark);
  color: var(--z-white) !important;
}

/* Card Order design */
.z-order-card {
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
  transition: all 0.3s ease;
  animation: z-item-in 0.5s ease both;
}
.z-order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0,0,0,0.04);
}

/* Badges */
.badge-payment {
  background: var(--z-bg-alt);
  color: var(--z-dark-soft);
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  letter-spacing: 0;
  text-transform: uppercase;
}

.badge-status {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0;
  display: inline-block;
}
.badge-status.pending { background: #fef3c7; color: #d97706; }
.badge-status.warning { background: #fee2e2; color: #b91c1c; }
.badge-status.info { background: #e0f2fe; color: #0284c7; }
.badge-status.primary { background: #e0e7ff; color: #4338ca; }
.badge-status.success { background: #dcfce7; color: #16a34a; }
.badge-status.danger { background: #fee2e2; color: #b91c1c; }

.badge-payment-status {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
  display: inline-block;
}
.badge-payment-status.paid { background: #dcfce7; color: #16a34a; }
.badge-payment-status.unpaid { background: #f3f2ee; color: #71717a; }

/* Buttons styling */
.lm-btn-outline-accent {
  background: transparent;
  border: 1px solid var(--z-accent);
  color: var(--z-accent);
  border-radius: var(--z-radius);
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}
.lm-btn-outline-accent:hover:not(:disabled) {
  background: var(--z-accent);
  color: var(--z-white);
}

.lm-btn-outline-danger {
  background: transparent;
  border: 1px solid var(--z-accent);
  color: var(--z-accent);
  border-radius: var(--z-radius);
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}
.lm-btn-outline-danger:hover {
  background: var(--z-accent);
  color: var(--z-white);
}

.z-return-label {
  display: block;
  margin-bottom: 6px;
  color: var(--z-dark);
  font-size: 13px;
  font-weight: 600;
}

.z-return-help {
  margin-top: 6px;
  color: var(--z-gray);
  font-size: 11px;
}

.z-order-review-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--z-accent);
  font-size: 11px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
}
.z-order-review-link:hover { color: var(--z-dark); }

/* Pagination */
.lm-pagination-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--z-gray-border);
  background: var(--z-white);
  color: var(--z-dark);
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  cursor: pointer;
}
.lm-pagination-btn:hover:not(:disabled) {
  border-color: var(--z-dark);
  background: var(--z-bg-alt);
}
.lm-pagination-btn.active {
  background: var(--z-dark);
  border-color: var(--z-dark);
  color: var(--z-white);
}
.lm-pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Tracking Step elements */
.z-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
  min-width: 80px;
}
.z-step-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--z-gray-light);
  border: 2px solid var(--z-white);
  z-index: 2;
  box-shadow: 0 0 0 2px var(--z-gray-border);
  transition: all 0.4s ease;
}
.z-step.active .z-step-dot {
  background: var(--z-dark);
  box-shadow: 0 0 0 2px var(--z-dark);
}
.z-step.current .z-step-dot {
  background: var(--z-accent);
  box-shadow: 0 0 0 2px var(--z-accent);
  transform: scale(1.2);
}
.z-step-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--z-gray);
  margin-top: 8px;
  text-align: center;
}
.z-step.active .z-step-label {
  color: var(--z-dark);
}
.z-step.current .z-step-label {
  color: var(--z-accent);
  font-weight: 600;
}
.z-step-line {
  position: absolute;
  top: 6px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: var(--z-gray-border);
  z-index: 1;
}
.z-step-line.filled {
  background: var(--z-dark);
}

@keyframes z-item-in {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Modals overlay positioning */
.z-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  backdrop-filter: blur(4px);
}
.z-modal {
  background: var(--z-white);
  border-radius: var(--z-radius-lg);
  border: 1px solid var(--z-gray-border);
  width: 100%;
  max-width: 840px;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding: 24px;
}
.z-customer-return-modal { max-width: 760px; }
.z-return-muted { color: var(--z-gray); font-size: 12px; margin-top: 3px; }
.z-return-type-switch {
  display: grid; grid-template-columns: 1fr 1fr; gap: 4px;
  padding: 4px; border: 1px solid var(--z-gray-border); background: var(--z-bg-alt);
}
.z-return-type-switch button {
  height: 40px; border: 0; background: transparent; color: var(--z-gray); font-size: 13px; font-weight: 600;
}
.z-return-type-switch button.active { background: var(--z-dark); color: var(--z-white); }
.z-file-input { height: auto; padding: 9px 12px; }
.z-return-preview-list { display: flex; gap: 8px; margin-top: 10px; overflow-x: auto; }
.z-return-preview-list > div { position: relative; flex: none; }
.z-return-preview-list img { width: 78px; height: 92px; object-fit: cover; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius); }
.z-return-preview-list button {
  position: absolute; top: 4px; right: 4px; width: 24px; height: 24px; border: 0;
  background: rgba(24,24,27,.86); color: white; display: grid; place-items: center; border-radius: 50%;
}
.z-return-product-image { width: 54px; height: 66px; object-fit: cover; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius); }
.z-return-status { padding: 5px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; white-space: nowrap; }
.z-return-status.pending { background: #fff4d6; color: #8a5b00; }
.z-return-status.waiting { background: #eaf5ff; color: #1769aa; }
.z-return-status.processing { background: #f2edff; color: #6440a4; }
.z-return-status.rejected { background: #ffeded; color: #b42318; }
.z-return-status.done { background: #eaf8ee; color: #217a3d; }
.z-return-progress-note { margin-top: 14px; padding: 10px 12px; background: var(--z-bg-alt); font-size: 12px; color: var(--z-gray); }
@media (max-width: 575px) {
  .z-modal { margin: 8px; padding: 18px; }
  .z-return-type-switch button { font-size: 12px; }
}
</style>
