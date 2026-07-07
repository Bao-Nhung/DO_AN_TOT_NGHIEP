<template>
  <AdminLayout>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="z-display mb-1" style="font-size:26px;font-weight:500;color:var(--z-dark)">Quản lý sản phẩm</h1>
        <p style="font-size:14px;color:var(--z-gray);margin:0">{{ filteredProducts.length }} sản phẩm</p>
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

    <!-- Filters -->
    <div class="z-admin-card mb-3" style="padding:14px 20px">
      <div class="d-flex align-items-center gap-3 flex-wrap">
        <div class="d-flex align-items-center gap-2 flex-grow-1" style="max-width:320px">
          <i class="bi bi-search" style="color:var(--z-gray-light)"></i>
          <input v-model="search" class="lm-input" placeholder="Tìm kiếm sản phẩm..." style="border:none;padding:8px 0;box-shadow:none">
        </div>
        <select v-model="filterCategory" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tất cả loại</option>
          <option v-for="cat in categories" :key="cat">{{ cat }}</option>
        </select>
        <select v-model="filterStatus" class="lm-input" style="width:auto;padding:8px 16px;font-size:13px">
          <option value="">Tất cả trạng thái</option>
          <option value="1">Đang bán</option>
          <option value="0">Ngừng bán</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <div class="z-admin-card" style="padding:0;overflow:hidden">
      <table class="z-table">
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
                <button class="z-icon-btn" title="Sửa" @click="openEdit(p)"><i class="bi bi-pencil"></i></button>
                <button class="z-icon-btn" title="Chi tiết" @click="openProductDetail(p)"><i class="bi bi-eye"></i></button>
                <button class="z-icon-btn" title="Xóa" style="color:var(--z-accent)" @click="doDelete(p)"><i class="bi bi-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredProducts.length === 0" class="text-center py-5">
        <i class="bi bi-inbox" style="font-size:36px;color:var(--z-gray-border)"></i>
        <p style="color:var(--z-gray);font-size:14px;margin-top:8px">Không có sản phẩm nào</p>
      </div>

      <!-- Pagination Controls -->
      <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center mt-3 px-3 pb-3" style="border-top: 1px solid var(--z-gray-border); padding-top: 16px;">
        <span style="font-size: 13px; color: var(--z-gray)">
          Hiển thị từ {{ (currentPage - 1) * itemsPerPage + 1 }} đến {{ Math.min(currentPage * itemsPerPage, filteredProducts.length) }} trong tổng số {{ filteredProducts.length }} sản phẩm
        </span>
        <div class="d-flex gap-2">
          <button class="lm-btn-secondary" style="padding:6px 12px; font-size:12px; height:auto; border-radius:6px" :disabled="currentPage === 1" @click="currentPage--">
            Trước
          </button>
          <button v-for="page in totalPages" :key="page" 
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

    <!-- Product Detail Modal -->
    <div v-if="showProductDetail" class="z-modal-overlay" @click.self="showProductDetail = false">
      <div class="z-modal" style="max-width:800px">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 style="font-size:18px;font-weight:600;margin:0">Chi tiết sản phẩm</h3>
            <div style="font-size:13px;color:var(--z-gray)">{{ productDetail?.maVay }}</div>
          </div>
          <button class="z-icon-btn" @click="showProductDetail = false"><i class="bi bi-x-lg"></i></button>
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
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Giá bán:</span> <strong style="font-size:13px;color:var(--z-accent)">{{ fmtPrice(productDetail.giaBan) }}</strong></div>
                <div class="col-6"><span style="font-size:12px;color:var(--z-gray)">Giá gốc:</span> <strong style="font-size:13px">{{ fmtPrice(productDetail.giaBanGoc) }}</strong></div>
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
                  <th>Giá gốc</th>
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
                  <td style="font-weight:500">{{ fmtPrice(bt.giaBan) }}</td>
                  <td style="color:var(--z-gray)">{{ fmtPrice(bt.giaBanGoc) }}</td>
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
          <button class="z-icon-btn" @click="showAttrModal = false"><i class="bi bi-x-lg"></i></button>
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
                  <button class="z-icon-btn-sm" @click="deleteColor(c)"><i class="bi bi-x"></i></button>
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
                  <button class="z-chip-x" @click="deleteSize(s)"><i class="bi bi-x"></i></button>
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
                  <button class="z-chip-x" @click="deleteMaterial(m)"><i class="bi bi-x"></i></button>
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
                  <button class="z-chip-x" @click="deleteCategory(cat)"><i class="bi bi-x"></i></button>
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
                    <td><button class="z-icon-btn-sm" @click="deleteSupplier(s)"><i class="bi bi-x"></i></button></td>
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
          <button class="z-icon-btn" @click="showModal = false"><i class="bi bi-x-lg"></i></button>
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
              <label class="z-label">Loại váy</label>
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
            <input v-model.number="v.giaBan" type="number" class="lm-input" placeholder="Giá bán" style="padding:6px 10px;font-size:12px;flex:1">
            <input v-model.number="v.giaBanGoc" type="number" class="lm-input" placeholder="Giá gốc" style="padding:6px 10px;font-size:12px;flex:1">
            <input v-model.number="v.soLuong" type="number" class="lm-input" placeholder="SL" style="padding:6px 10px;font-size:12px;width:70px">
            <button class="z-icon-btn" style="color:var(--z-accent);flex-shrink:0" @click="form.variants.splice(i, 1)">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
        <button class="lm-btn-secondary mb-4" @click="addVariant" style="font-size:13px">
          <i class="bi bi-plus-circle me-1"></i> Thêm biến thể
        </button>

        <!-- Images -->
        <h4 style="font-size:13px;font-weight:600;color:var(--z-accent);margin-bottom:12px">HÌNH ẢNH SẢN PHẨM</h4>
        <div class="d-flex flex-wrap gap-2 mb-2">
          <div v-for="img in existingImages" :key="'e' + img.id" class="z-img-thumb">
            <img :src="img.url" alt="">
            <button class="z-img-del" title="Xoá ảnh" @click="removeExistingImage(img)"><i class="bi bi-x"></i></button>
          </div>
          <div v-for="(p, i) in newImagePreviews" :key="'n' + i" class="z-img-thumb">
            <img :src="p" alt="">
            <span class="z-img-new">Mới</span>
            <button class="z-img-del" title="Bỏ ảnh" @click="removeNewImage(i)"><i class="bi bi-x"></i></button>
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
import { mapProduct, fmtPrice } from '@/composables/useProducts'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const { showToast } = useToast()
const { confirmDialog } = useConfirm()
const search = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const showModal = ref(false)
const saving = ref(false)
const editingId = ref(null)
const allProducts = ref([])
const rawProducts = ref([])
const categories = ref([])
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

const defaultForm = { tenVay: '', maVay: '', moTa: '', trangThai: 1, idLoaiVay: null, idChatLieu: null, idNhaCungCap: null, variants: [] }
const form = ref({ ...defaultForm, variants: [] })

// Ảnh sản phẩm
const existingImages = ref([])   // ảnh đã có (khi sửa): { id, url }
const newImageFiles = ref([])    // File[] mới chọn
const newImagePreviews = ref([]) // data URL xem trước
const deletedImageIds = ref([])  // id ảnh cũ bị xoá

function resetImages() {
  existingImages.value = []
  newImageFiles.value = []
  newImagePreviews.value = []
  deletedImageIds.value = []
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
  await loadProducts()
  await loadAttrs()
})

async function loadAttrs() {
  try {
    const attrs = await api().getThuocTinh()
    loaiVayList.value = attrs.loaiVay || []
    chatLieuList.value = attrs.chatLieu || []
    mauSacList.value = attrs.mauSac || []
    kichThuocList.value = attrs.kichThuoc || []
    nhaCungCapList.value = attrs.nhaCungCap || []
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
    const data = await api().getVay()
    const sortedData = [...data].sort((a, b) => {
      const da = a.ngayTao ? new Date(a.ngayTao).getTime() : Number(a.id || 0)
      const db = b.ngayTao ? new Date(b.ngayTao).getTime() : Number(b.id || 0)
      return db - da
    })
    rawProducts.value = sortedData
    allProducts.value = sortedData.map((p, i) => {
      const m = mapProduct(p, i)
      return { ...m, priceDisplay: fmtPrice(m.salePrice || m.price), rawId: p.id, raw: p }
    })
    categories.value = [...new Set(allProducts.value.map(p => p.category).filter(Boolean))]
  } catch (e) { console.error('Không thể tải sản phẩm:', e) }
}

const filteredProducts = computed(() => {
  return allProducts.value.filter(p => {
    const matchSearch = !search.value || p.name.toLowerCase().includes(search.value.toLowerCase()) || (p.code || '').toLowerCase().includes(search.value.toLowerCase())
    const matchCat = !filterCategory.value || p.category === filterCategory.value
    const matchStatus = !filterStatus.value || (filterStatus.value === '1' ? p.active : !p.active)
    return matchSearch && matchCat && matchStatus
  })
})

const currentPage = ref(1)
const itemsPerPage = 10

const totalPages = computed(() => Math.ceil(filteredProducts.value.length / itemsPerPage))

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredProducts.value.slice(start, start + itemsPerPage)
})

watch([search, filterCategory, filterStatus], () => {
  currentPage.value = 1
})

function addVariant() {
  form.value.variants.push({ idMauSac: null, idKichThuoc: null, giaBan: null, giaBanGoc: null, soLuong: 0 })
}

function openAdd() {
  editingId.value = null
  form.value = { ...defaultForm, variants: [{ idMauSac: null, idKichThuoc: null, giaBan: null, giaBanGoc: null, soLuong: 0 }] }
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
    variants: [],
  }
  try {
    const detail = await api().getVayById(p.rawId || p.id)
    existingImages.value = detail.anhList || []
    if (detail.bienThe && detail.bienThe.length > 0) {
      form.value.variants = detail.bienThe.map(bt => ({
        idMauSac: bt.idMauSac || mauSacList.value.find(m => m.tenMauSac === bt.mauSac)?.id || null,
        idKichThuoc: bt.idKichThuoc || kichThuocList.value.find(k => k.tenKichThuoc === bt.kichThuoc)?.id || null,
        giaBan: bt.giaBan ? Number(bt.giaBan) : null,
        giaBanGoc: bt.giaBanGoc ? Number(bt.giaBanGoc) : null,
        soLuong: bt.soLuong || 0,
      }))
    } else {
      form.value.variants = [{ idMauSac: null, idKichThuoc: null, giaBan: p.salePrice || p.price || 0, giaBanGoc: p.price || 0, soLuong: p.stock || 0 }]
    }
  } catch (e) {
    form.value.variants = [{ idMauSac: null, idKichThuoc: null, giaBan: p.salePrice || p.price || 0, giaBanGoc: p.price || 0, soLuong: p.stock || 0 }]
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
    if (v.giaBanGoc === null || v.giaBanGoc === undefined || v.giaBanGoc < 0) {
      showToast(`Biến thể số ${idx + 1} chưa nhập giá gốc hợp lệ!`, 'error')
      return
    }
    if (Number(v.giaBanGoc) >= Number(v.giaBan)) {
      showToast(`Biến thể số ${idx + 1}: Giá gốc phải nhỏ hơn Giá bán!`, 'error')
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
      giaBan: firstVariant.giaBan,
      giaBanGoc: firstVariant.giaBanGoc,
      soLuong: firstVariant.soLuong || 0,
      variants: form.value.variants.map(v => ({
        idMauSac: v.idMauSac,
        idKichThuoc: v.idKichThuoc,
        giaBan: v.giaBan,
        giaBanGoc: v.giaBanGoc,
        soLuong: v.soLuong || 0
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
    }

    showModal.value = false
    await loadProducts()
  } catch (e) { showToast('Lỗi: ' + (e.message || 'Không thể lưu')) }
  finally { saving.value = false }
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

async function doDelete(p) {
  if (!await confirmDialog({ title: 'Xóa sản phẩm', message: `Bạn có chắc muốn xóa "${p.name}"?`, confirmText: 'Xóa', variant: 'danger' })) return
  try {
    await api().deleteVay(p.rawId || p.id)
    showToast('Đã xóa sản phẩm!')
    await loadProducts()
  } catch (e) { showToast('Lỗi khi xóa: ' + (e.message || '')) }
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
.z-img-del:hover { background: var(--z-accent); }
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
</style>
