package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.Anh;
import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.HoaDonChiTiet;
import com.zestia.datn.zestia.repository.AnhRepository;
import com.zestia.datn.zestia.repository.HoaDonChiTietRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonCtRepo;
    private final AnhRepository anhRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return hoaDonRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return hoaDonRepo.findById(id)
                .map(hd -> ResponseEntity.ok(toDetailMap(hd)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return hoaDonRepo.findById(id).map(hd -> {
            if (body.get("trangThai") != null) {
                hd.setTrangThai(((Number) body.get("trangThai")).byteValue());
            }
            if (body.get("ghiChu") != null) {
                hd.setGhiChu((String) body.get("ghiChu"));
            }
            if (body.get("daThanhToan") != null) {
                hd.setDaThanhToan(Boolean.parseBoolean(String.valueOf(body.get("daThanhToan"))));
            }
            hoaDonRepo.save(hd);
            return ResponseEntity.ok(toMap(hd));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return hoaDonRepo.findById(id).map(hd -> {
            if (hd.getTrangThai() == 0) {
                hd.setTrangThai((byte) 5); // 5 = Cancelled
                if (body.get("ghiChu") != null) {
                    hd.setGhiChu((String) body.get("ghiChu"));
                }
                hoaDonRepo.save(hd);
                return ResponseEntity.ok(toMap(hd));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ được huỷ khi đơn hàng đang chờ xử lý"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(HoaDon hd) {
        long soSanPham = hoaDonCtRepo.countByHoaDonId(hd.getId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hd.getId());
        map.put("maHoaDon", hd.getMaHoaDon());
        
        // GIỮ NGUYÊN KEY GỐC: khachHang
        map.put("khachHang", hd.getKhachHang() != null ? hd.getKhachHang().getHoVaTen() : "Khách lẻ");
        map.put("soDienThoai", hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : null);
        
        map.put("soSanPham", soSanPham);
        
        // Bọc chống null an toàn
        map.put("tongTien", hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO);
        map.put("phiVanChuyen", hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO);
        map.put("giamGiaKhuyenMai", hd.getGiamGiaKhuyenMai() != null ? hd.getGiamGiaKhuyenMai() : BigDecimal.ZERO);
        
        map.put("nhanVien", hd.getNhanVien() != null ? hd.getNhanVien().getHoVaTen() : null);
        map.put("khuyenMai", hd.getKhuyenMai() != null ? hd.getKhuyenMai().getTenKhuyenMai() : null);
        map.put("giamGia", hd.getGiamGia() != null ? hd.getGiamGia().getTenGiamGia() : null);
        map.put("hinhThucThanhToan", hd.getHinhThucThanhToan());
        map.put("phuongThucThanhToanOnline", hd.getPhuongThucThanhToanOnline());
        map.put("daThanhToan", Boolean.TRUE.equals(hd.getDaThanhToan()));
        map.put("trangThai", hd.getTrangThai()); 
        map.put("diaChiGiaoHang", hd.getDiaChiGiaoHang());
        map.put("hinhThucNhanHang", hd.getHinhThucNhanHang()); 
        map.put("ghiChu", hd.getGhiChu());
        map.put("ngayTao", hd.getNgayTao());
        return map;
    }

    private Map<String, Object> toDetailMap(HoaDon hd) {
        Map<String, Object> map = toMap(hd);
        map.put("emailKhachHang", hd.getKhachHang() != null ? hd.getKhachHang().getEmail() : null);
        
        List<HoaDonChiTiet> chiTiets = hoaDonCtRepo.findByHoaDonId(hd.getId());
        List<Map<String, Object>> items = new ArrayList<>();
        for (HoaDonChiTiet ct : chiTiets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ct.getId());
            if (ct.getVayChiTiet() != null) {
                // GIỮ NGUYÊN KEY GỐC: tenVay
                item.put("tenVay", ct.getVayChiTiet().getVay() != null 
                        ? ct.getVayChiTiet().getVay().getTenVay() : null);
                item.put("maSanPham", ct.getVayChiTiet().getVay() != null 
                        ? ct.getVayChiTiet().getVay().getMaVay() : null);
                item.put("mauSac", ct.getVayChiTiet().getMauSac() != null 
                        ? ct.getVayChiTiet().getMauSac().getTenMauSac() : null);
                item.put("maHex", ct.getVayChiTiet().getMauSac() != null 
                        ? ct.getVayChiTiet().getMauSac().getMaHex() : null);
                item.put("kichThuoc", ct.getVayChiTiet().getKichThuoc() != null 
                        ? ct.getVayChiTiet().getKichThuoc().getTenKichThuoc() : null);
                
                if (ct.getVayChiTiet().getVay() != null) {
                    List<Anh> anhs = anhRepo.findByVayIdAndTrangThai(
                            ct.getVayChiTiet().getVay().getId(), (byte) 1);
                    item.put("anhUrl", !anhs.isEmpty() ? anhs.get(0).getAnhUrl() : null);
                }
            }
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
            item.put("phanTramGiam", ct.getPhanTramGiam());
            items.add(item);
        }
        map.put("chiTiets", items);
        return map;
    }
}

