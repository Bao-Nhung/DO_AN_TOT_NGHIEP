<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý sản phẩm</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ totalItems }} sản phẩm</p>
      </div>
      <div class="d-flex gap-2">
        <button class="lm-btn-secondary" @click="showAttrModal = true">
          <i class="bi bi-sliders"></i>
          <span>Thuộc tính</span>
        </button>
        <button class="lm-btn-primary" @click="openAdd">
          <i class="bi bi-plus-lg" style="position:relative;z-index:1"></i>
          <span>Thêm sản phẩm</span>
        </button>
      </div>
    </div>

    <!-- Main Navigation Tabs -->
    <div class="d-flex gap-2 mb-3">
      <button class="btn btn-sm" :class="activeMainTab === 'products' ? 'btn-dark font-weight-bold' : 'btn-outline-secondary'" @click="activeMainTab = 'products'">
        <i class="bi bi-box-seam me-1"></i> Danh sách sản phẩm ({{ totalItems }})
      </button>
      <button class="btn btn-sm" :class="activeMainTab === 'logs' ? 'btn-dark font-weight-bold' : 'btn-outline-secondary'" @click="activeMainTab = 'logs'; fetchStockLogs(0)">
        <i class="bi bi-clock-history me-1"></i> Nhật ký biến động tồn kho
      </button>
    </div>

    <div v-if="activeMainTab === 'products'">
      <!-- Filters -->
      <div class="z-admin-card mb-3" style="padding:14px 20px">
        <div class="d-flex flex-column gap-3">
          <!-- Row 1: Search & Status -->
          <div class="d-flex align-items-center gap-3 flex-wrap">
            <div class="d-flex align-items-center gap-2 flex-grow-1" style="max-width:320px; border-bottom: 1px solid var(--z-gray-border); padding-bottom: 4px;">
              <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
              <input v-model="search" class="lm-input" placeholder="Tìm kiếm sản phẩm..." style="border:none;padding:8px 0;box-shadow:none">
            </div>
            <div class="ms-auto">
              <select v-model="filterStatus" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
                <option value="">Tất cả trạng thái</option>
                <option value="1">Đang bán</option>
                <option value="0">Ngừng bán</option>
              </select>
            </div>
          </div>

          <!-- Row 2: Categories (phân loại sản phẩm theo mục giống trang khách hàng) -->
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <span style="font-size:13px;font-weight:600;color:var(--z-gray);flex-shrink:0">Mục:</span>
            <div class="d-flex gap-2 flex-wrap">
              <button v-for="f in filters" :key="f"
                      class="lm-filter-tag" :class="{ active: activeFilter === f }"
                      style="padding: 4px 12px; font-size: 12px;"
                      @click="activeFilter = f">{{ f }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="z-admin-card" style="padding:0;overflow:hidden">
        <div class="table-responsive">
        <table class="z-table" style="min-width:900px">
          <thead>
            <tr>
              <th style="width:50px"><input type="checkbox"></th>
              <th>Sản phẩm</th>
              <th>Loại</th>
              <th>Giá</th>
              <th>Tồn kho</th>
              <th>Trạng thái</th>
              <th style="width:120px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in paginatedProducts" :key="p.id" class="z-clickable-row" @click="openProductDetail(p)">
              <td @click.stop><input type="checkbox"></td>
              <td>
                <div class="d-flex align-items-center gap-3">
                  <div style="width:48px;height:56px;border-radius:var(--z-radius);overflow:hidden;flex-shrink:0;background:var(--z-bg-alt)">
                    <img v-if="p.image" :src="p.image" style="width:100%;height:100%;object-fit:cover">
                    <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center"
                         :style="{ background: p.bg, fontFamily:'var(--z-font-display)', fontSize:'14px', color:'rgba(255,255,255,0.3)' }">
                      {{ p.letter }}
                    </div>
                  </div>
                  <div>
                    <div style="font-weight:500">{{ p.name }}</div>
                    <div style="font-size:12px;color:var(--z-gray)">{{ p.code }}</div>
                  </div>
                </div>
              </td>
              <td>{{ p.category }}</td>
              <td style="font-weight:500">{{ p.priceDisplay }}</td>
              <td>
                <span :style="{ color: p.stock < 10 ? 'var(--z-accent)' : 'var(--z-dark)', fontWeight: p.stock < 10 ? 600 : 400 }">
                  {{ p.stock }}
                </span>
              </td>
              <td><span class="z-status" :class="p.active ? 'success' : 'pending'">{{ p.active ? 'Đang bán' : 'Ngừng' }}</span></td>
              <td @click.stop>
                <div class="d-flex gap-1">
                  <button type="button" class="z-icon-btn" title="Sửa" aria-label="Sửa sản phẩm" @click="openEdit(p)"><i class="bi bi-pencil"></i></button>
                  <button type="button" class="z-icon-btn" title="Chi tiết" aria-label="Xem chi tiết sản phẩm" @click="openProductDetail(p)"><i class="bi bi-eye"></i></button>
                  <a :href="'/product/' + p.id" target="_blank" class="z-icon-btn text-decoration-none" title="Xem trên Website" aria-label="Xem sản phẩm trên Website"><i class="bi bi-box-arrow-up-right"></i></a>
                  <button type="button" class="z-icon-btn" :title="p.active ? 'Khóa' : 'Mở khóa'" :aria-label="p.active ? 'Khóa sản phẩm' : 'Mở khóa sản phẩm'"
                          :style="{ color: p.active ? 'var(--z-accent)' : '#16a34a' }"
                          @click="toggleLock(p)">
                    <i class="bi" :class="p.active ? 'bi-lock' : 'bi-unlock'"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        </div>
        <div v-if="totalItems === 0" class="text-center py-5">
          <i class="bi bi-inbox" style="font-size:36px;color:var(--z-gray-border)"></i>
          <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có sản phẩm nào</p>
        </div>

        <!-- Pagination Controls -->
        <div v-if="totalItems > 0" class="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
          <span data-no-i18n style="font-size: 13px; color: var(--z-gray)">{{ productRangeLabel }}</span>
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
    </div>

    <!-- Stock Movement Logs Tab -->
    <div v-else-if="activeMainTab === 'logs'" class="z-admin-card" style="padding:20px">
      <div class="d-flex align-items-center gap-3 mb-3">
        <div class="d-flex align-items-center gap-2 flex-grow-1" style="max-width:360px; border-bottom: 1px solid var(--z-gray-border); padding-bottom: 4px;">
          <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
          <input v-model="stockLogSearch" class="lm-input" placeholder="Tìm nhật ký tồn kho..." style="border:none;padding:8px 0;box-shadow:none" @keyup.enter="fetchStockLogs(0)">
        </div>
        <button class="lm-btn-secondary" @click="fetchStockLogs(0)">
          <i class="bi bi-search me-1"></i> Lọc
        </button>
        <span class="ms-auto text-muted" style="font-size:13px">Tổng số: {{ stockLogTotalElements }} bản ghi</span>
      </div>

      <div class="table-responsive">
        <table class="z-table" style="min-width:900px">
          <thead>
            <tr>
              <th>Thời gian</th>
              <th>Sản phẩm</th>
              <th>Biến thể</th>
              <th class="text-center">Tồn trước</th>
              <th class="text-center">Biến động</th>
              <th class="text-center">Tồn sau</th>
              <th>Loại biến động</th>
              <th>Mã tham chiếu</th>
              <th>Người thực hiện</th>
              <th>Ghi chú</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loadingStockLogs">
              <td colspan="10" class="text-center py-4">Đang tải nhật ký...</td>
            </tr>
            <tr v-else-if="stockLogs.length === 0">
              <td colspan="10" class="text-center py-4 text-muted">Chưa có nhật ký biến động tồn kho nào</td>
            </tr>
            <tr v-for="log in stockLogs" :key="log.id">
              <td style="font-size:12px;color:var(--z-gray)">{{ formatDate(log.ngayTao) }}</td>
              <td>
                <div style="font-weight:500">{{ log.tenVay || 'N/A' }}</div>
                <div style="font-size:11px;color:var(--z-gray)">{{ log.maVay }}</div>
              </td>
              <td>
                <span class="badge bg-light text-dark border">
                  {{ log.mauSac || '' }} / {{ log.kichThuoc || '' }}
                </span>
              </td>
              <td class="text-center">{{ log.soLuongTruoc }}</td>
              <td class="text-center" :style="{ color: log.soLuongThayDoi > 0 ? '#16a34a' : '#dc2626', fontWeight: 600 }">
                {{ log.soLuongThayDoi > 0 ? '+' + log.soLuongThayDoi : log.soLuongThayDoi }}
              </td>
              <td class="text-center" style="font-weight:600">{{ log.soLuongSau }}</td>
              <td>
                <span class="badge" :class="getMovementTypeBadgeClass(log.loaiBienDong)">
                  {{ log.loaiBienDong }}
                </span>
              </td>
              <td style="font-size:12px;font-family:monospace">{{ log.maThamChieu || '-' }}</td>
              <td style="font-size:13px">{{ log.nguoiThucHien || 'System' }}</td>
              <td style="font-size:12px;color:var(--z-gray);max-width:200px" class="text-truncate">{{ log.ghiChu || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="stockLogTotalPages > 1" class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
        <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="stockLogPage === 0" @click="fetchStockLogs(stockLogPage - 1)">Trang trước</button>
        <span style="font-size:13px;color:var(--z-gray)">Trang {{ stockLogPage + 1 }} / {{ stockLogTotalPages }}</span>
        <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="stockLogPage >= stockLogTotalPages - 1" @click="fetchStockLogs(stockLogPage + 1)">Trang sau</button>
      </div>
    </div>

    <!-- Product Detail Modal -->
    <div v-if="showProductDetail" class="z-modal-overlay" @click.self="showProductDetail = false">
      <div class="z-modal" style="max-width:800px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết sản phẩm</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ productDetail?.maVay }}</div>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng chi tiết sản phẩm" @click="showProductDetail = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div v-if="loadingProductDetail" class="text-center py-4">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
        </div>

        <div v-else-if="productDetail">
          <div class="row g-4 mb-4">
            <div class="col-md-4">
              <div style="aspect-ratio:3/4;border-radius:var(--z-radius-lg);overflow:hidden;background:var(--z-bg-alt)">
                <img v-if="productDetail.anhUrl" :src="productDetail.anhUrl" style="width:100%;height:100%;object-fit:cover">
                <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center" style="font-size:48px;color:var(--z-gray-light)">
                  <i class="bi bi-image"></i>
                </div>
              </div>
            </div>
            <div class="col-md-8">
              <h2 style="font-size:22px;font-weight:600;margin-bottom:8px">{{ productDetail.tenVay }}</h2>
              <div class="d-flex gap-2 align-items-center mb-3">
                <span class="z-status" :class="productDetail.trangThai === 1 ? 'success' : 'pending'">
                  {{ productDetail.trangThai === 1 ? 'Đang bán' : 'Ngừng bán' }}
                </span>
                <span v-if="productDetail.loaiVay" style="font-size:12px;color:var(--z-gray);background:var(--z-bg-alt);padding:4px 10px;border-radius:20px">{{ productDetail.loaiVay }}</span>
              </div>
              <div class="row g-2 mb-3">
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Chất liệu:</span> <strong style="font-size:13px">{{ productDetail.chatLieu || 'N/A' }}</strong></div>
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Tồn kho:</span> <strong style="font-size:13px">{{ productDetail.tonKho }}</strong></div>
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Giá bán:</span> <strong style="font-size:13px;color:var(--z-accent)">{{ fmtPrice(productDetail.giaBanCoSo ?? productDetail.giaBan) }}</strong></div>
              </div>
              <div v-if="productDetail.moTa" style="font-size:13px;color:var(--z-gray);line-height:1.6">{{ productDetail.moTa }}</div>
            </div>
          </div>

          <!-- Variants -->
          <h4 style="font-size:14px;font-weight:600;margin-bottom:12px">Biến thể ({{ productDetail.bienThe?.length || 0 }})</h4>
          <div class="z-admin-card" style="padding:0;overflow:hidden">
            <table class="z-table" style="font-size:12px">
              <thead>
                <tr>
                  <th>Mã</th>
                  <th>Màu sắc</th>
                  <th>Kích thước</th>
                  <th>Giá bán</th>
                  <th>Số lượng</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="bt in productDetail.bienThe" :key="bt.id">
                  <td style="font-weight:500">{{ bt.maVayChiTiet }}</td>
                  <td>
                    <span class="d-inline-flex align-items-center gap-1">
                      <span v-if="bt.maHex" :style="{ width:'12px', height:'12px', borderRadius:'50%', background: bt.maHex, display:'inline-block', border:'1px solid var(--z-gray-border)' }"></span>
                      {{ bt.mauSac || 'N/A' }}
                    </span>
                  </td>
                  <td>{{ bt.kichThuoc || 'N/A' }}</td>
                  <td style="font-weight:500">{{ fmtPrice(bt.giaBanCoSo ?? bt.giaBan) }}</td>
                  <td :style="{ color: bt.soLuong < 5 ? 'var(--z-accent)' : '', fontWeight: bt.soLuong < 5 ? 600 : 400 }">{{ bt.soLuong }}</td>
                  <td><span class="z-status" :class="bt.trangThai === 1 ? 'success' : 'pending'" style="font-size:10px;padding:2px 8px">{{ bt.trangThai === 1 ? 'Bán' : 'Ngừng' }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Attributes Modal -->
    <div v-if="showAttrModal" class="z-modal-overlay" @click.self="showAttrModal = false">
      <div class="z-modal" style="max-width:900px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Thuộc tính sản phẩm</h3>
            <div style="font-size:13px;color:var(--z-gray)">Quản lý màu sắc, kích thước, chất liệu, danh mục và nhà cung cấp</div>
          </div>
          <button type="button" class="z-icon-btn" aria-label="Đóng quản lý thuộc tính" @click="showAttrModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <div class="row g-3">
          <!-- Colors -->
          <div class="col-lg-6">
            <div class="z-attr-section">
              <h4 class="z-attr-title"><i class="bi bi-palette me-2" style="color:var(--z-accent)"></i>Màu sắc</h4>
              <div class="d-flex gap-2 mb-3">
                <input v-model="newColor.tenMauSac" class="lm-input" placeholder="Tên màu" style="flex:1;font-size:13px;padding:8px 12px">
                <input v-model="newColor.maHex" type="color" style="width:36px;height:36px;border:none;padding:0;cursor:pointer;border-radius:var(--z-radius)">
                <button class="lm-btn-primary" style="padding:6px 14px;font-size:12px" @click="addColor"><span>Thêm</span></button>
              </div>
              <div class="d-flex flex-column gap-1" style="max-height:200px;overflow-y:auto">
                <div v-for="c in attrColors" :key="c.id" class="d-flex align-items-center gap-2 py-1 px-2"
                     style="border-bottom:1px solid var(--z-gray-border)">
                  <span :style="{ width:'16px', height:'16px', borderRadius:'50%', background: c.hex, border:'1px solid var(--z-gray-border)', flexShrink:0 }"></span>
                  <span style="font-size:13px;flex:1">{{ c.name }}</span>
                  <code style="font-size:11px;color:var(--z-gray)">{{ c.hex }}</code>
                  <button type="button" class="z-icon-btn-sm" :aria-label="`Xóa màu ${c.name}`" @click="deleteColor(c)"><i class="bi bi-x"></i></button>
                </div>
              </div>
            </div>
          </div>

          <!-- Sizes -->
          <div class="col-lg-6">
            <div class="z-attr-section">
              <h4 class="z-attr-title"><i class="bi bi-rulers me-2" style="color:var(--z-accent)"></i>Kích thước</h4>
              <div class="d-flex gap-2 mb-3">
                <input v-model="newSize" class="lm-input" placeholder="VD: S, M, L, XL" style="flex:1;font-size:13px;padding:8px 12px" @keyup.enter="addSize">
                <button class="lm-btn-primary" style="padding:6px 14px;font-size:12px" @click="addSize"><span>Thêm</span></button>
              </div>
              <div class="d-flex flex-wrap gap-2">
                <div v-for="s in attrSizes" :key="s.id" class="z-chip">
                  {{ s.name }}
                  <button type="button" class="z-chip-x" :aria-label="`Xóa kích thước ${s.name}`" @click="deleteSize(s)"><i class="bi bi-x"></i></button>
                </div>
              </div>
            </div>
          </div>

          <!-- Materials -->
          <div class="col-lg-6">
            <div class="z-attr-section">
              <h4 class="z-attr-title"><i class="bi bi-scissors me-2" style="color:var(--z-accent)"></i>Chất liệu</h4>
              <div class="d-flex gap-2 mb-3">
                <input v-model="newMaterial" class="lm-input" placeholder="Tên chất liệu" style="flex:1;font-size:13px;padding:8px 12px" @keyup.enter="addMaterial">
                <button class="lm-btn-primary" style="padding:6px 14px;font-size:12px" @click="addMaterial"><span>Thêm</span></button>
              </div>
              <div class="d-flex flex-wrap gap-2">
                <div v-for="m in attrMaterials" :key="m.id" class="z-chip">
                  {{ m.name }}
                  <button type="button" class="z-chip-x" :aria-label="`Xóa chất liệu ${m.name}`" @click="deleteMaterial(m)"><i class="bi bi-x"></i></button>
                </div>
              </div>
            </div>
          </div>

          <!-- Categories -->
          <div class="col-lg-6">
            <div class="z-attr-section">
              <h4 class="z-attr-title"><i class="bi bi-tags me-2" style="color:var(--z-accent)"></i>Danh mục</h4>
              <div class="d-flex gap-2 mb-3">
                <input v-model="newCategory" class="lm-input" placeholder="Tên danh mục" style="flex:1;font-size:13px;padding:8px 12px" @keyup.enter="addCategory">
                <button class="lm-btn-primary" style="padding:6px 14px;font-size:12px" @click="addCategory"><span>Thêm</span></button>
              </div>
              <div class="d-flex flex-wrap gap-2">
                <div v-for="cat in attrCategories" :key="cat.id" class="z-chip">
                  {{ cat.name }}
                  <button type="button" class="z-chip-x" :aria-label="`Xóa danh mục ${cat.name}`" @click="deleteCategory(cat)"><i class="bi bi-x"></i></button>
                </div>
              </div>
            </div>
          </div>

          <!-- Suppliers -->
          <div class="col-12">
            <div class="z-attr-section">
              <h4 class="z-attr-title"><i class="bi bi-building me-2" style="color:var(--z-accent)"></i>Nhà cung cấp</h4>
              <div class="d-flex gap-2 mb-3 flex-wrap">
                <input v-model="newSupplier.tenNhaCungCap" class="lm-input" placeholder="Tên nhà cung cấp" style="flex:2;min-width:120px;font-size:13px;padding:8px 12px">
                <input v-model="newSupplier.diaChi" class="lm-input" placeholder="Địa chỉ" style="flex:2;min-width:120px;font-size:13px;padding:8px 12px">
                <input v-model="newSupplier.soDienThoai" class="lm-input" placeholder="SĐT" style="flex:1;min-width:100px;font-size:13px;padding:8px 12px">
                <input v-model="newSupplier.email" class="lm-input" placeholder="Email" style="flex:1;min-width:100px;font-size:13px;padding:8px 12px">
                <button class="lm-btn-primary" style="padding:6px 14px;font-size:12px" @click="addSupplier"><span>Thêm</span></button>
              </div>
              <table v-if="attrSuppliers.length" class="z-table" style="font-size:12px">
                <thead>
                  <tr><th>Tên</th><th>Địa chỉ</th><th>SĐT</th><th>Email</th><th style="width:40px"></th></tr>
                </thead>
                <tbody>
                  <tr v-for="s in attrSuppliers" :key="s.id">
                    <td style="font-weight:500">{{ s.name }}</td>
                    <td style="color:var(--z-gray)">{{ s.address }}</td>
                    <td>{{ s.phone }}</td>
                    <td style="color:var(--z-gray)">{{ s.email }}</td>
                    <td><button type="button" class="z-icon-btn-sm" :aria-label="`Xóa nhà cung cấp ${s.name}`" @click="deleteSupplier(s)"><i class="bi bi-x"></i></button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="z-modal-overlay" @click.self="showModal = false">
      <div class="z-modal" style="max-width:700px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3 style="font-size:18px;font-weight:600;margin:0">{{ editingId ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h3>
          <button type="button" class="z-icon-btn" aria-label="Đóng biểu mẫu sản phẩm" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
        </div>

        <!-- Product info -->
        <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">THÔNG TIN SẢN PHẨM</h4>
        <div class="d-flex flex-column gap-3 mb-4">
          <div>
            <label class="z-label">Tên sản phẩm *</label>
            <input v-model="form.tenVay" class="lm-input" placeholder="Nhập tên sản phẩm">
          </div>
          <div class="row g-3">
            <div class="col-6">
              <label class="z-label">Mã sản phẩm</label>
              <input :value="editingId ? form.maVay : 'Hệ thống tự sinh mã (SPXXXX)'" class="lm-input" disabled>
            </div>
            <div class="col-6">
              <label class="z-label">Trạng thái</label>
              <select v-model="form.trangThai" class="lm-input">
                <option :value="1">Đang bán</option>
                <option :value="0">Ngừng bán</option>
              </select>
            </div>
          </div>
          <div class="row g-3">
            <div class="col-4">
              <label class="z-label">Loại sản phẩm</label>
              <select v-model="form.idLoaiVay" class="lm-input">
                <option :value="null">-- Chọn --</option>
                <option v-for="lv in loaiVayList" :key="lv.id" :value="lv.id">{{ lv.tenLoaiVay }}</option>
              </select>
            </div>
            <div class="col-4">
              <label class="z-label">Chất liệu</label>
              <select v-model="form.idChatLieu" class="lm-input">
                <option :value="null">-- Chọn --</option>
                <option v-for="cl in chatLieuList" :key="cl.id" :value="cl.id">{{ cl.tenChatLieu }}</option>
              </select>
            </div>
            <div class="col-4">
              <label class="z-label">Nhà cung cấp</label>
              <select v-model="form.idNhaCungCap" class="lm-input">
                <option :value="null">-- Chọn --</option>
                <option v-for="ncc in nhaCungCapList" :key="ncc.id" :value="ncc.id">{{ ncc.tenNhaCungCap }}</option>
              </select>
            </div>
          </div>
          <div>
            <label class="z-label">Mô tả</label>
            <textarea v-model="form.moTa" class="lm-input" rows="2" placeholder="Mô tả sản phẩm..."></textarea>
          </div>
          <div class="row g-3">
            <div class="col-4">
              <label class="z-label">Chiều cao người mẫu (cm)</label>
              <input v-model.number="form.chieuCaoNguoiMau" type="number" min="120" max="210" class="lm-input" />
            </div>
            <div class="col-4">
              <label class="z-label">Cân nặng người mẫu (kg)</label>
              <input v-model.number="form.canNangNguoiMau" type="number" min="30" max="150" class="lm-input" />
            </div>
            <div class="col-4">
              <label class="z-label">Size người mẫu mặc</label>
              <input v-model.trim="form.sizeNguoiMau" class="lm-input" placeholder="S / M / L" />
            </div>
            <div class="col-12">
              <label class="z-label">Mô tả phom</label>
              <textarea v-model="form.moTaPhom" class="lm-input" rows="2" placeholder="Phom ôm, phom rộng; cách chọn khi ở giữa hai size..."></textarea>
            </div>
          </div>
        </div>

        <!-- Variants -->
        <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">BIẾN THỂ (Size, Màu, Giá, Số lượng)</h4>
        <div class="d-flex flex-column gap-2 mb-3">
          <div v-for="(v, i) in form.variants" :key="i"
               class="d-flex align-items-center gap-2 p-2" style="background:var(--z-bg-alt);border-radius:var(--z-radius)">
            <select v-model="v.idMauSac" class="lm-input" style="padding:6px 10px;font-size:12px;flex:1">
              <option :value="null">Màu sắc</option>
              <option v-for="ms in mauSacList" :key="ms.id" :value="ms.id">{{ ms.tenMauSac }}</option>
            </select>
            <select v-model="v.idKichThuoc" class="lm-input" style="padding:6px 10px;font-size:12px;flex:1">
              <option :value="null">Kích thước</option>
              <option v-for="kt in kichThuocList" :key="kt.id" :value="kt.id">{{ kt.tenKichThuoc }}</option>
            </select>
            <input :value="formatPriceInput(v.giaBan)" type="text" inputmode="numeric" class="lm-input" placeholder="Giá bán" style="padding:6px 10px;font-size:12px;flex:1" @input="updateVariantPrice(v, 'giaBan', $event)">
            <input v-model.number="v.soLuong" type="number" class="lm-input" placeholder="SL" style="padding:6px 10px;font-size:12px;width:70px">
            <button type="button" class="z-icon-btn" aria-label="Xóa biến thể" style="color:var(--z-accent);flex-shrink:0" @click="form.variants.splice(i, 1)">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
        <button class="lm-btn-secondary mb-4" @click="addVariant" style="font-size:13px">
          <i class="bi bi-plus-circle me-1"></i> Thêm biến thể
        </button>

        <template v-if="variantColors.length">
          <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">ẢNH THEO MÀU SẮC</h4>
          <div class="z-color-image-grid mb-4">
            <label v-for="color in variantColors" :key="color.id" class="z-color-image-item">
              <div class="z-color-image-preview">
                <img v-if="colorImagePreviews[color.id] || color.existingImage" :src="colorImagePreviews[color.id] || color.existingImage" :alt="color.name">
                <i v-else class="bi bi-image"></i>
              </div>
              <div>
                <strong>{{ color.name }}</strong>
                <span>{{ colorImageFiles[color.id] ? 'Đã chọn ảnh mới' : color.existingImage ? 'Đang có ảnh riêng' : 'Chọn ảnh cho màu này' }}</span>
              </div>
              <i class="bi bi-upload z-color-upload-icon"></i>
              <input type="file" accept="image/jpeg,image/png,image/webp,image/avif" hidden @change="onPickColorImage(color.id, $event)">
            </label>
          </div>
          <div class="z-color-image-note">Một ảnh màu sẽ được dùng cho mọi kích cỡ cùng màu. Khi khách đổi màu, ảnh chính và ảnh trong giỏ hàng cũng đổi theo.</div>
        </template>

        <!-- Images -->
        <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">HÌNH ẢNH SẢN PHẨM</h4>
        <div class="d-flex flex-wrap gap-2 mb-2">
          <div v-for="img in existingImages" :key="'e' + img.id" class="z-img-thumb">
            <img :src="img.url" alt="">
            <button type="button" class="z-img-del" title="Xoá ảnh" aria-label="Xóa ảnh sản phẩm" @click="removeExistingImage(img)"><i class="bi bi-x"></i></button>
          </div>
          <div v-for="(p, i) in newImagePreviews" :key="'n' + i" class="z-img-thumb">
            <img :src="p" alt="">
            <span class="z-img-new">Mới</span>
            <button type="button" class="z-img-del" title="Bỏ ảnh" aria-label="Bỏ ảnh mới" @click="removeNewImage(i)"><i class="bi bi-x"></i></button>
          </div>
          <label class="z-img-add">
            <i class="bi bi-plus-lg" style="font-size:18px"></i>
            <span style="font-size:11px">Thêm ảnh</span>
            <input type="file" accept="image/*" multiple style="display:none" @change="onPickImages">
          </label>
        </div>
        <div style="font-size:12px;color:var(--z-gray);margin-bottom:16px">
          Ảnh đầu tiên là ảnh đại diện. Hỗ trợ JPG/PNG/WebP, tối đa 10MB mỗi ảnh.
        </div>

        <div class="d-flex justify-content-end gap-2">
          <button class="lm-btn-secondary" @click="showModal = false">Huỷ</button>
          <button class="lm-btn-primary" @click="doSave" :disabled="saving">
            <span>{{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật' : 'Thêm mới') }}</span>
          </button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { api } from '@/composables/useApi'
import { mapProduct, fmtPrice, MOCK_PRODUCTS } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { useI18n } from '@/composables/useI18n'
import PageSizeSelect from '@/components/ui/PageSizeSelect.vue'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const { isEn } = useI18n()

// Tabs navigation
const activeMainTab = ref('products')
const stockLogs = ref([])
const stockLogPage = ref(0)
const stockLogTotalPages = ref(1)
const stockLogTotalElements = ref(0)
const stockLogSearch = ref('')
const loadingStockLogs = ref(false)

async function fetchStockLogs(page = 0) {
  loadingStockLogs.value = true
  stockLogPage.value = page
  try {
    const res = await api().getStockMovements({
      page,
      size: 15,
      q: stockLogSearch.value.trim() || undefined
    })
    if (res && res.content) {
      stockLogs.value = res.content
      stockLogTotalPages.value = res.totalPages || 1
      stockLogTotalElements.value = res.totalElements || 0
    }
  } catch (err) {
    console.error('Lỗi lấy nhật ký biến động tồn kho:', err)
  } finally {
    loadingStockLogs.value = false
  }
}

function getMovementTypeBadgeClass(type) {
  if (!type) return 'bg-secondary'
  const t = type.toUpperCase()
  if (t.includes('POS') || t.includes('CHECKOUT') || t.includes('BAN')) return 'bg-primary'
  if (t.includes('HUY') || t.includes('EXPIRE') || t.includes('HOAN')) return 'bg-warning text-dark'
  if (t.includes('DOI') || t.includes('TRA')) return 'bg-info text-dark'
  return 'bg-secondary'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('vi-VN')
}

const search = ref('')
const activeFilter = ref('Tất cả')
const filterStatus = ref('')
const showModal = ref(false)
const saving = ref(false)
const editingId = ref(null)
const allProducts = ref([])
const categories = ref([])
const filters = computed(() => ['Tất cả', ...categories.value])
const loaiVayList = ref([])
const chatLieuList = ref([])
const mauSacList = ref([])
const kichThuocList = ref([])
const nhaCungCapList = ref([])

const showProductDetail = ref(false)
const productDetail = ref(null)
const loadingProductDetail = ref(false)

const showAttrModal = ref(false)
const attrColors = ref([])
const attrSizes = ref([])
const attrMaterials = ref([])
const attrCategories = ref([])
const attrSuppliers = ref([])
const newColor = ref({ tenMauSac: '', maHex: '#c08b7e' })
const newSize = ref('')
const newMaterial = ref('')
const newCategory = ref('')
const newSupplier = ref({ tenNhaCungCap: '', diaChi: '', soDienThoai: '', email: '' })

const defaultForm = {
  tenVay: '', maVay: '', moTa: '', trangThai: 1,
  idLoaiVay: null, idChatLieu: null, idNhaCungCap: null,
  chieuCaoNguoiMau: null, canNangNguoiMau: null, sizeNguoiMau: '', moTaPhom: '',
  variants: []
}
const form = ref({ ...defaultForm, variants: [] })

// Ảnh sản phẩm
const existingImages = ref([])   // ảnh đã có (khi sửa): { id, url }
const newImageFiles = ref([])    // File[] mới chọn
const newImagePreviews = ref([]) // data URL xem trước
const deletedImageIds = ref([])  // id ảnh cũ bị xoá
const colorImageFiles = ref({})
const colorImagePreviews = ref({})
const variantColors = computed(() => {
  const colors = new Map()
  for (const variant of form.value.variants || []) {
    const colorId = Number(variant.idMauSac)
    if (!colorId || colors.has(colorId)) continue
    const color = mauSacList.value.find(item => Number(item.id) === colorId)
    colors.set(colorId, {
      id: colorId,
      name: color?.tenMauSac || `Màu #${colorId}`,
      existingImage: variant.anhUrl || null
    })
  }
  return [...colors.values()]
})

function resetImages() {
  existingImages.value = []
  newImageFiles.value = []
  newImagePreviews.value = []
  deletedImageIds.value = []
  colorImageFiles.value = {}
  colorImagePreviews.value = {}
}

function onPickColorImage(colorId, event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp', 'image/avif'].includes(file.type)) {
    showToast('Ảnh màu chỉ hỗ trợ JPG, PNG, WebP hoặc AVIF')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    showToast('Ảnh màu không được vượt quá 10MB')
    return
  }
  colorImageFiles.value = { ...colorImageFiles.value, [colorId]: file }
  const reader = new FileReader()
  reader.onload = e => {
    colorImagePreviews.value = { ...colorImagePreviews.value, [colorId]: e.target.result }
  }
  reader.readAsDataURL(file)
}

function onPickImages(e) {
  const files = Array.from(e.target.files || [])
  for (const f of files) {
    if (!f.type.startsWith('image/')) continue
    newImageFiles.value.push(f)
    const reader = new FileReader()
    reader.onload = ev => newImagePreviews.value.push(ev.target.result)
    reader.readAsDataURL(f)
  }
  e.target.value = '' // cho phép chọn lại cùng file
}

function removeNewImage(i) {
  newImageFiles.value.splice(i, 1)
  newImagePreviews.value.splice(i, 1)
}

function removeExistingImage(img) {
  deletedImageIds.value.push(img.id)
  existingImages.value = existingImages.value.filter(x => x.id !== img.id)
}

onMounted(async () => {
  await loadAttrs()
  await loadProducts()
})

async function loadAttrs() {
  try {
    const attrs = await api().getThuocTinh()
    loaiVayList.value = attrs.loaiVay || []
    chatLieuList.value = attrs.chatLieu || []
    mauSacList.value = attrs.mauSac || []
    kichThuocList.value = attrs.kichThuoc || []
    nhaCungCapList.value = attrs.nhaCungCap || []
    categories.value = (attrs.loaiVay || []).map(category => category.tenLoaiVay).filter(Boolean)
    attrColors.value = (attrs.mauSac || []).map(c => ({ id: c.id, name: c.tenMauSac, hex: c.maHex || '#ccc' }))
    attrSizes.value = (attrs.kichThuoc || []).map(s => ({ id: s.id, name: s.tenKichThuoc }))
    attrMaterials.value = (attrs.chatLieu || []).map(m => ({ id: m.id, name: m.tenChatLieu }))
    attrCategories.value = (attrs.loaiVay || []).map(l => ({ id: l.id, name: l.tenLoaiVay }))
    attrSuppliers.value = (attrs.nhaCungCap || []).map(s => ({
      id: s.id, name: s.tenNhaCungCap || '', address: s.diaChi || '', phone: s.soDienThoai || '', email: s.email || ''
    }))
  } catch (e) { console.error(e) }
}

async function loadProducts() {
  try {
    const category = activeFilter.value !== 'Tất cả' ? activeFilter.value : null
    const data = await api().getVayPage({
      page: currentPage.value - 1,
      size: itemsPerPage.value,
      q: search.value.trim() || null,
      status: filterStatus.value || null,
      category
    }).catch(() => null)
    if (data && Array.isArray(data.content) && data.content.length > 0) {
      allProducts.value = (data.content || []).map((p, i) => {
        const m = mapProduct(p, i)
        return { ...m, priceDisplay: fmtPrice(m.price), rawId: p.id, raw: p }
      })
      totalItems.value = Number(data.totalElements || 0)
      totalPages.value = Number(data.totalPages || 0)
    } else {
      let filtered = [...MOCK_PRODUCTS]
      if (search.value.trim()) {
        const q = search.value.trim().toLowerCase()
        filtered = filtered.filter(p => p.name.toLowerCase().includes(q) || p.code.toLowerCase().includes(q))
      }
      if (category) {
        filtered = filtered.filter(p => p.category.toLowerCase().includes(category.toLowerCase()))
      }
      allProducts.value = filtered.map((m, i) => ({
        ...m,
        priceDisplay: fmtPrice(m.price),
        rawId: m.id,
        raw: {
          id: m.id,
          maVay: m.code,
          tenVay: m.name,
          loaiVay: m.category,
          giaBan: m.price,
          tonKho: m.stock,
          trangThai: m.active ? 1 : 0,
          anhUrl: m.image
        }
      }))
      totalItems.value = filtered.length
      totalPages.value = 1
    }
  } catch (e) {
    let filtered = [...MOCK_PRODUCTS]
    allProducts.value = filtered.map((m, i) => ({
      ...m,
      priceDisplay: fmtPrice(m.price),
      rawId: m.id,
      raw: {
        id: m.id,
        maVay: m.code,
        tenVay: m.name,
        loaiVay: m.category,
        giaBan: m.price,
        tonKho: m.stock,
        trangThai: m.active ? 1 : 0,
        anhUrl: m.image
      }
    }))
    totalItems.value = filtered.length
    totalPages.value = 1
  }
}

const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalItems = ref(0)
const totalPages = ref(0)
const productRangeLabel = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value + 1
  const end = Math.min(currentPage.value * itemsPerPage.value, totalItems.value)
  return isEn.value
    ? `Showing ${start}-${end} of ${totalItems.value} products`
    : `Hiển thị từ ${start} đến ${end} trong tổng số ${totalItems.value} sản phẩm`
})
const filteredProducts = computed(() => allProducts.value)
const paginatedProducts = computed(() => allProducts.value)
const pageNumbers = computed(() => {
  const start = Math.max(1, Math.min(currentPage.value - 2, totalPages.value - 4))
  const end = Math.min(totalPages.value, start + 4)
  return Array.from({ length: Math.max(0, end - start + 1) }, (_, index) => start + index)
})

let productSearchTimer
watch(search, () => {
  clearTimeout(productSearchTimer)
  productSearchTimer = setTimeout(() => resetProductPage(), 300)
})
watch([activeFilter, filterStatus], resetProductPage)
watch(currentPage, loadProducts)
watch(itemsPerPage, resetProductPage)

function resetProductPage() {
  if (currentPage.value === 1) loadProducts()
  else currentPage.value = 1
}

function addVariant() {
  form.value.variants.push({ idMauSac: null, idKichThuoc: null, giaBan: null, soLuong: 0, anhUrl: null })
}

function formatPriceInput(value) {
  if (value === null || value === undefined || value === '') return ''
  const amount = Number(String(value).replace(/\D/g, ''))
  return Number.isFinite(amount) ? amount.toLocaleString('vi-VN') : ''
}

function updateVariantPrice(variant, field, event) {
  const digits = String(event.target.value || '').replace(/\D/g, '').slice(0, 15)
  variant[field] = digits ? Number(digits) : null
  event.target.value = formatPriceInput(variant[field])
}

function openAdd() {
  editingId.value = null
  form.value = { ...defaultForm, variants: [{ idMauSac: null, idKichThuoc: null, giaBan: null, soLuong: 0, anhUrl: null }] }
  resetImages()
  showModal.value = true
}

async function openEdit(p) {
  editingId.value = p.rawId || p.id
  resetImages()
  form.value = {
    tenVay: p.name || '',
    maVay: p.code || '',
    moTa: p.raw?.moTa || '',
    trangThai: p.active ? 1 : 0,
    idLoaiVay: p.raw?.idLoaiVay || null,
    idChatLieu: p.raw?.idChatLieu || null,
    idNhaCungCap: p.raw?.idNhaCungCap || null,
    chieuCaoNguoiMau: p.raw?.chieuCaoNguoiMau || null,
    canNangNguoiMau: p.raw?.canNangNguoiMau || null,
    sizeNguoiMau: p.raw?.sizeNguoiMau || '',
    moTaPhom: p.raw?.moTaPhom || '',
    variants: [],
  }
  try {
    const detail = await api().getVayById(p.rawId || p.id)
    form.value.chieuCaoNguoiMau = detail.chieuCaoNguoiMau || null
    form.value.canNangNguoiMau = detail.canNangNguoiMau || null
    form.value.sizeNguoiMau = detail.sizeNguoiMau || ''
    form.value.moTaPhom = detail.moTaPhom || ''
    existingImages.value = detail.anhList || []
    if (detail.bienThe && detail.bienThe.length > 0) {
      form.value.variants = detail.bienThe.map(bt => ({
        idMauSac: bt.idMauSac || mauSacList.value.find(m => m.tenMauSac === bt.mauSac)?.id || null,
        idKichThuoc: bt.idKichThuoc || kichThuocList.value.find(k => k.tenKichThuoc === bt.kichThuoc)?.id || null,
        giaBan: Number(bt.giaBanCoSo ?? bt.giaBan) || null,
        soLuong: bt.soLuong || 0,
        anhUrl: bt.anhUrl || null,
      }))
    } else {
      form.value.variants = [{ idMauSac: null, idKichThuoc: null, giaBan: p.price || 0, soLuong: p.stock || 0, anhUrl: null }]
    }
  } catch (e) {
    form.value.variants = [{ idMauSac: null, idKichThuoc: null, giaBan: p.price || 0, soLuong: p.stock || 0, anhUrl: null }]
  }
  showModal.value = true
}

async function openProductDetail(p) {
  showProductDetail.value = true
  loadingProductDetail.value = true
  try {
    productDetail.value = await api().getVayById(p.rawId || p.id)
  } catch (e) {
    productDetail.value = p.raw
  } finally {
    loadingProductDetail.value = false
  }
}

async function doSave() {
  if (!form.value.tenVay) { showToast('Vui lòng nhập tên sản phẩm'); return }
  
  // Validate variants list
  if (!form.value.variants || form.value.variants.length === 0) {
    showToast('Vui lòng thêm ít nhất một biến thể cho sản phẩm!', 'error')
    return
  }

  for (let idx = 0; idx < form.value.variants.length; idx++) {
    const v = form.value.variants[idx]
    if (!v.idMauSac) {
      showToast(`Biến thể số ${idx + 1} chưa chọn màu sắc!`, 'error')
      return
    }
    if (!v.idKichThuoc) {
      showToast(`Biến thể số ${idx + 1} chưa chọn kích thước!`, 'error')
      return
    }
    if (v.giaBan === null || v.giaBan === undefined || v.giaBan < 0) {
      showToast(`Biến thể số ${idx + 1} chưa nhập giá bán hợp lệ!`, 'error')
      return
    }
  }

  saving.value = true
  try {
    const firstVariant = form.value.variants[0] || {}
    const payload = {
      tenVay: form.value.tenVay,
      moTa: form.value.moTa,
      trangThai: form.value.trangThai,
      idLoaiVay: form.value.idLoaiVay,
      idChatLieu: form.value.idChatLieu,
      idNhaCungCap: form.value.idNhaCungCap,
      chieuCaoNguoiMau: form.value.chieuCaoNguoiMau,
      canNangNguoiMau: form.value.canNangNguoiMau,
      sizeNguoiMau: form.value.sizeNguoiMau,
      moTaPhom: form.value.moTaPhom,
      giaBan: firstVariant.giaBan,
      soLuong: firstVariant.soLuong || 0,
      variants: form.value.variants.map(v => ({
        idMauSac: v.idMauSac,
        idKichThuoc: v.idKichThuoc,
        giaBan: v.giaBan,
        soLuong: v.soLuong || 0,
        anhUrl: v.anhUrl || existingColorImage(v.idMauSac)
      }))
    }
    let result
    if (editingId.value) {
      result = await api().updateVay(editingId.value, payload)
      showToast('Cập nhật sản phẩm thành công!')
    } else {
      result = await api().createVay(payload)
      showToast('Thêm sản phẩm thành công!')
    }

    // Xử lý ảnh: xoá ảnh đã bỏ + upload ảnh mới
    const pid = (result && result.id) || editingId.value
    for (const aid of deletedImageIds.value) {
      try { await api().deleteVayAnh(aid) } catch (e) { /* bỏ qua */ }
    }
    if (pid) {
      for (const f of newImageFiles.value) {
        try { await api().uploadVayAnh(pid, f) } catch (e) { showToast('Lỗi upload ảnh: ' + (e.error || e.message || '')) }
      }
      for (const [colorId, file] of Object.entries(colorImageFiles.value)) {
        try {
          await api().uploadVayColorImage(pid, colorId, file)
        } catch (e) {
          showToast(`Lỗi upload ảnh màu: ${e.error || e.message || ''}`)
        }
      }
    }

    showModal.value = false
    await loadProducts()
  } catch (e) { showToast('Lỗi: ' + (e.message || 'Không thể lưu')) }
  finally { saving.value = false }
}

function existingColorImage(colorId) {
  if (!colorId) return null
  return form.value.variants.find(variant => Number(variant.idMauSac) === Number(colorId) && variant.anhUrl)?.anhUrl || null
}

async function addColor() {
  if (!newColor.value.tenMauSac) { showToast('Vui lòng nhập tên màu'); return }
  try { await api().addMauSac(newColor.value); showToast('Thêm màu thành công!'); newColor.value = { tenMauSac: '', maHex: '#c08b7e' }; await loadAttrs() } catch (e) { showToast('Lỗi khi thêm') }
}
async function deleteColor(c) {
  if (!await confirmDialog({ title: 'Xóa màu sắc', message: `Xóa màu "${c.name}"?`, confirmText: 'Xóa', variant: 'danger' })) return
  try { await api().deleteMauSac(c.id); showToast('Đã xóa!'); await loadAttrs() } catch (e) { showToast('Lỗi khi xóa') }
}
async function addSize() {
  if (!newSize.value) { showToast('Vui lòng nhập kích thước'); return }
  try { await api().addKichThuoc({ tenKichThuoc: newSize.value }); showToast('Thêm kích thước thành công!'); newSize.value = ''; await loadAttrs() } catch (e) { showToast('Lỗi khi thêm') }
}
async function deleteSize(s) {
  if (!await confirmDialog({ title: 'Xóa kích thước', message: `Xóa kích thước "${s.name}"?`, confirmText: 'Xóa', variant: 'danger' })) return
  try { await api().deleteKichThuoc(s.id); showToast('Đã xóa!'); await loadAttrs() } catch (e) { showToast('Lỗi khi xóa') }
}
async function addMaterial() {
  if (!newMaterial.value) { showToast('Vui lòng nhập chất liệu'); return }
  try { await api().addChatLieu({ tenChatLieu: newMaterial.value }); showToast('Thêm chất liệu thành công!'); newMaterial.value = ''; await loadAttrs() } catch (e) { showToast('Lỗi khi thêm') }
}
async function deleteMaterial(m) {
  if (!await confirmDialog({ title: 'Xóa chất liệu', message: `Xóa chất liệu "${m.name}"?`, confirmText: 'Xóa', variant: 'danger' })) return
  try { await api().deleteChatLieu(m.id); showToast('Đã xóa!'); await loadAttrs() } catch (e) { showToast('Lỗi khi xóa') }
}
async function addCategory() {
  if (!newCategory.value) { showToast('Vui lòng nhập tên danh mục'); return }
  try { await api().addLoaiVay({ tenLoaiVay: newCategory.value }); showToast('Thêm danh mục thành công!'); newCategory.value = ''; await loadAttrs() } catch (e) { showToast('Lỗi khi thêm') }
}
async function deleteCategory(cat) {
  if (!await confirmDialog({ title: 'Xóa danh mục', message: `Xóa danh mục "${cat.name}"?`, confirmText: 'Xóa', variant: 'danger' })) return
  try { await api().deleteLoaiVay(cat.id); showToast('Đã xóa!'); await loadAttrs() } catch (e) { showToast('Lỗi khi xóa') }
}
async function addSupplier() {
  if (!newSupplier.value.tenNhaCungCap) { showToast('Vui lòng nhập tên nhà cung cấp'); return }
  try { await api().addNhaCungCap(newSupplier.value); showToast('Thêm nhà cung cấp thành công!'); newSupplier.value = { tenNhaCungCap: '', diaChi: '', soDienThoai: '', email: '' }; await loadAttrs() } catch (e) { showToast('Lỗi khi thêm') }
}
async function deleteSupplier(s) {
  if (!await confirmDialog({ title: 'Xóa nhà cung cấp', message: `Xóa nhà cung cấp "${s.name}"?`, confirmText: 'Xóa', variant: 'danger' })) return
  try { await api().deleteNhaCungCap(s.id); showToast('Đã xóa!'); await loadAttrs() } catch (e) { showToast('Lỗi khi xóa') }
}

async function toggleLock(p) {
  const newStatus = p.active ? 0 : 1
  const actionText = p.active ? 'khóa' : 'mở khóa'
  
  if (!await confirmDialog({
    title: `${p.active ? 'Khóa' : 'Mở khóa'} sản phẩm`,
    message: `Bạn có chắc muốn ${actionText} sản phẩm "${p.name}"?`,
    confirmText: p.active ? 'Khóa' : 'Mở khóa',
    variant: p.active ? 'danger' : 'success'
  })) return

  try {
    await api().updateVay(p.rawId || p.id, { trangThai: newStatus })
    showToast(`Đã ${actionText} sản phẩm thành công!`)
    await loadProducts()
  } catch (e) {
    showToast(`Lỗi khi ${actionText}: ` + (e.message || ''))
  }
}
</script>

<style scoped>
.z-clickable-row { cursor: pointer; }
.z-clickable-row:hover td { background: var(--z-accent-soft) !important; }
.z-icon-btn {
  width: 32px; height: 32px;
  border: none; background: transparent;
  border-radius: var(--z-radius);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray);
  transition: all 0.2s; font-size: 14px;
}
.z-icon-btn:hover { background: var(--z-bg-alt); color: var(--z-dark); }
.z-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.z-modal {
  background: var(--z-white); border-radius: var(--z-radius-lg);
  padding: 28px; width: 100%; max-height: 90vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.z-label { display: block; font-size: 13px; font-weight: 500; color: var(--z-dark); margin-bottom: 6px; }
.z-img-thumb {
  position: relative; width: 84px; height: 100px; border-radius: var(--z-radius);
  overflow: hidden; border: 1px solid var(--z-gray-border); background: var(--z-bg-alt); flex-shrink: 0;
}
.z-img-thumb img { width: 100%; height: 100%; object-fit: cover; }
.z-img-del {
  position: absolute; top: 3px; right: 3px; width: 20px; height: 20px; border: none;
  background: rgba(0,0,0,0.55); color: #fff; border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center; font-size: 12px; padding: 0;
}
.z-img-del:hover { background: var(--z-accent); color: #fff; }
.z-img-new {
  position: absolute; bottom: 0; left: 0; right: 0; background: var(--z-accent); color: #fff;
  font-size: 10px; text-align: center; padding: 1px 0;
}
.z-img-add {
  width: 84px; height: 100px; border: 1.5px dashed var(--z-gray-border); border-radius: var(--z-radius);
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
  cursor: pointer; color: var(--z-gray); transition: all 0.2s; flex-shrink: 0;
}
.z-img-add:hover { border-color: var(--z-accent); color: var(--z-accent); }
.z-color-image-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.z-color-image-item { min-height: 72px; padding: 9px; border: 1px solid var(--z-gray-border); display: grid; grid-template-columns: 46px 1fr 20px; align-items: center; gap: 10px; cursor: pointer; background: var(--z-white); }
.z-color-image-item:hover { border-color: var(--z-accent); background: var(--z-accent-soft); }
.z-color-image-preview { width: 46px; height: 54px; display: grid; place-items: center; overflow: hidden; background: var(--z-bg-alt); color: var(--z-gray-light); }
.z-color-image-preview img { width: 100%; height: 100%; object-fit: cover; }
.z-color-image-item strong, .z-color-image-item span { display: block; }
.z-color-image-item strong { color: var(--z-dark); font-size: 12px; }
.z-color-image-item span { margin-top: 4px; color: var(--z-gray); font-size: 10px; }
.z-color-upload-icon { color: var(--z-gray); font-size: 14px; }
.z-color-image-note { margin: -8px 0 18px; color: var(--z-gray); font-size: 11px; line-height: 1.5; }
.z-attr-section {
  padding: 16px; background: var(--z-bg-alt); border-radius: var(--z-radius-lg);
}
.z-attr-title {
  font-size: 14px; font-weight: 600; margin-bottom: 12px;
  display: flex; align-items: center;
}
.z-chip {
  padding: 6px 10px; border: 1px solid var(--z-gray-border); border-radius: var(--z-radius);
  font-size: 13px; font-weight: 500; display: inline-flex; align-items: center; gap: 4px;
}
.z-chip-x {
  width: 16px; height: 16px; border: none; background: transparent; cursor: pointer;
  color: var(--z-gray); font-size: 13px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; padding: 0;
}
.z-chip-x:hover { background: #fee2e2; color: #dc2626; }
.z-icon-btn-sm {
  width: 24px; height: 24px; border: none; background: transparent;
  border-radius: var(--z-radius); display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--z-gray); font-size: 14px;
}
.z-icon-btn-sm:hover { background: #fee2e2; color: #dc2626; }
@media (max-width: 640px) { .z-color-image-grid { grid-template-columns: 1fr; } }
</style>
