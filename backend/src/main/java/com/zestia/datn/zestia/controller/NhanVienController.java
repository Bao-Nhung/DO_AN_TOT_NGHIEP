package com.zestia.datn.zestia.controller;

import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.VaiTro;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.VaiTroRepository;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/nhan-vien")
@RequiredArgsConstructor
public class NhanVienController {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,149}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{4,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:0\\d{9,10}|\\+[1-9]\\d{7,14})$");

    private final NhanVienRepository nhanVienRepo;
    private final VaiTroRepository vaiTroRepo;
    private final PasswordEncoder passwordEncoder;
    private final LichLamViecRepository lichLamViecRepo;
    private final HoaDonRepository hoaDonRepo;

    @GetMapping("/paged")
    public Map<String, Object> getPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) String role,
                                       @RequestParam(required = false) Byte status) {
        if (status != null && status != 0 && status != 1) {
            throw new IllegalArgumentException("Trạng thái nhân viên không hợp lệ");
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String keyword = cleanFilter(q);
        String roleFilter = cleanFilter(role);
        var result = nhanVienRepo.findAdminPage(
                keyword,
                roleFilter,
                status,
                PageRequest.of(safePage, safeSize)
        );

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("active", nhanVienRepo.countByTinhTrangLamViec((byte) 1));
        summary.put("inactive", nhanVienRepo.countByTinhTrangLamViec((byte) 0));
        summary.put("admins", nhanVienRepo.countAdmins());
        summary.put("createdThisMonth", nhanVienRepo.countByNgayTaoGreaterThanEqualAndNgayTaoLessThan(
                monthStart, nextMonthStart));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream().map(this::toMap).toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("summary", summary);
        return response;
    }

    @GetMapping("/{id}/hieu-suat")
    public ResponseEntity<?> getPerformance(@PathVariable Integer id) {
        return nhanVienRepo.findById(id).map(nv -> {
            java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
            List<com.zestia.datn.zestia.entity.HoaDon> orders = hoaDonRepo.findByNhanVienId(id);
            long orderCount = orders.size();
            long completedOrders = 0;
            for (com.zestia.datn.zestia.entity.HoaDon hd : orders) {
                if (hd.getTrangThai() != null && hd.getTrangThai() == 4) {
                    completedOrders++;
                    if (hd.getTongTien() != null) {
                        totalSales = totalSales.add(hd.getTongTien());
                    }
                }
            }
            
            double scheduledHours = 0;
            long workedMinutes = 0;
            long completedShifts = 0;
            List<com.zestia.datn.zestia.entity.LichLamViec> schedules = lichLamViecRepo.findByNhanVienIdOrderByNgayLamAscGioBatDauAsc(id);
            for (com.zestia.datn.zestia.entity.LichLamViec sch : schedules) {
                if (sch.getGioBatDau() != null && sch.getGioKetThuc() != null) {
                    double diff = java.time.Duration.between(sch.getGioBatDau(), sch.getGioKetThuc()).toMinutes() / 60.0;
                    scheduledHours += Math.max(0.0, diff);
                }
                if (sch.getGioCheckIn() != null && sch.getGioCheckOut() != null
                        && !sch.getGioCheckOut().isBefore(sch.getGioCheckIn())) {
                    workedMinutes += java.time.Duration.between(sch.getGioCheckIn(), sch.getGioCheckOut()).toMinutes();
                    completedShifts++;
                }
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalSales", totalSales);
            stats.put("totalOrders", orderCount);
            stats.put("completedOrders", completedOrders);
            stats.put("scheduledHours", scheduledHours);
            stats.put("workedHours", workedMinutes / 60.0);
            stats.put("completedShifts", completedShifts);
            stats.put("totalHours", scheduledHours);
            stats.put("shiftCount", schedules.size());
            return ResponseEntity.ok(stats);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vai-tro")
    public List<Map<String, Object>> getVaiTro() {
        return vaiTroRepo.findAll().stream().filter(this::isSupportedRole).map(vt -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", vt.getId());
            map.put("tenVaiTro", vt.getTenVaiTro());
            return map;
        }).toList();
    }

    @PostMapping
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        if (body == null) return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu nhân viên không hợp lệ"));
        String hoVaTen = trim(toStringVal(body.get("hoVaTen")));
        String tenNguoiDung = trim(toStringVal(body.get("tenNguoiDung")));
        String email = normalizeEmail(toStringVal(body.get("email")));
        String matKhau = toStringVal(body.get("matKhau"));

        String validationError = validateEmployeeRequest(body, null, true);
        if (validationError != null) return ResponseEntity.badRequest().body(Map.of("message", validationError));
        if (nhanVienRepo.existsByTenNguoiDungIgnoreCase(tenNguoiDung)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên đăng nhập đã tồn tại"));
        }
        if (nhanVienRepo.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng"));
        }

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(nextMaNhanVien());
        applyFields(nv, body, true);
        nv.setMatKhau(passwordEncoder.encode(matKhau));
        nv.setNgayTao(LocalDateTime.now());
        if (nv.getTinhTrangLamViec() == null) nv.setTinhTrangLamViec((byte) 1);

        return ResponseEntity.ok(toMap(nhanVienRepo.save(nv)));
    }

    @PutMapping("/{id}")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        if (body == null) return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu nhân viên không hợp lệ"));
        return nhanVienRepo.findByIdForUpdate(id).map(existing -> {
            String validationError = validateEmployeeRequest(body, existing, false);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(Map.of("message", validationError));
            }
            String tenNguoiDung = trim(toStringVal(body.get("tenNguoiDung")));
            String email = normalizeEmail(toStringVal(body.get("email")));
            String matKhau = toStringVal(body.get("matKhau"));

            if (!isBlank(tenNguoiDung) && nhanVienRepo.findByTenNguoiDungIgnoreCase(tenNguoiDung)
                    .filter(nv -> !nv.getId().equals(id)).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tên đăng nhập đã tồn tại"));
            }
            if (!isBlank(email) && nhanVienRepo.findByEmailIgnoreCase(email)
                    .filter(nv -> !nv.getId().equals(id)).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng"));
            }
            if (wouldRemoveLastActiveAdmin(existing, body)) {
                return ResponseEntity.status(409).body(Map.of(
                        "message", "Phải giữ lại ít nhất một quản trị viên đang hoạt động"
                ));
            }
            if (!isBlank(matKhau)) {
                existing.setMatKhau(passwordEncoder.encode(matKhau));
            }

            applyFields(existing, body, false);
            return ResponseEntity.ok(toMap(nhanVienRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/trang-thai")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        if (body == null) return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu trạng thái không hợp lệ"));
        Integer status = parseInteger(toStringVal(body.get("tinhTrangLamViec")));
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái nhân viên chỉ được là đang làm hoặc tạm khóa"));
        }
        return nhanVienRepo.findByIdForUpdate(id).map(existing -> {
            if (status == 0 && isActiveAdmin(existing) && nhanVienRepo.countActiveAdmins() <= 1) {
                return ResponseEntity.status(409).body(Map.of(
                        "message", "Không thể tạm khóa quản trị viên đang hoạt động cuối cùng"
                ));
            }
            existing.setTinhTrangLamViec(status.byteValue());
            return ResponseEntity.ok(toMap(nhanVienRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void applyFields(NhanVien nv, Map<String, Object> body, boolean create) {
        if (create || body.containsKey("hoVaTen")) nv.setHoVaTen(trim(toStringVal(body.get("hoVaTen"))));
        if (create || body.containsKey("tenNguoiDung")) nv.setTenNguoiDung(trim(toStringVal(body.get("tenNguoiDung"))));
        if (create || body.containsKey("email")) nv.setEmail(normalizeEmail(toStringVal(body.get("email"))));
        if (body.containsKey("soDienThoai")) nv.setSoDienThoai(normalizePhone(toStringVal(body.get("soDienThoai"))));
        if (body.containsKey("diaChi")) nv.setDiaChi(trim(toStringVal(body.get("diaChi"))));
        if (body.containsKey("ngaySinh")) nv.setNgaySinh(isBlank(toStringVal(body.get("ngaySinh"))) ? null : LocalDate.parse(toStringVal(body.get("ngaySinh"))));
        if (body.containsKey("gioiTinh")) nv.setGioiTinh(parseByte(toStringVal(body.get("gioiTinh")), null));
        if (create && body.containsKey("tinhTrangLamViec")) {
            nv.setTinhTrangLamViec(parseByte(toStringVal(body.get("tinhTrangLamViec")), (byte) 1));
        }

        Integer vaiTroId = parseInteger(toStringVal(body.get("vaiTroId")));
        if (vaiTroId != null) {
            vaiTroRepo.findById(vaiTroId).ifPresent(nv::setVaiTro);
        } else if (create) {
            VaiTro defaultRole = vaiTroRepo.findByTenVaiTro("Nhân viên").orElse(null);
            nv.setVaiTro(defaultRole);
        }
    }

    private String toStringVal(Object value) {
        return value == null ? null : value.toString();
    }

    private Map<String, Object> toMap(NhanVien nv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", nv.getId());
        map.put("maNhanVien", nv.getMaNhanVien());
        map.put("hoVaTen", nv.getHoVaTen());
        map.put("gioiTinh", nv.getGioiTinh());
        map.put("ngaySinh", nv.getNgaySinh());
        map.put("soDienThoai", nv.getSoDienThoai());
        map.put("diaChi", nv.getDiaChi());
        map.put("email", nv.getEmail());
        map.put("tenNguoiDung", nv.getTenNguoiDung());
        map.put("tinhTrangLamViec", nv.getTinhTrangLamViec());
        map.put("ngayTao", nv.getNgayTao());
        if (nv.getVaiTro() != null) {
            map.put("vaiTroId", nv.getVaiTro().getId());
            map.put("tenVaiTro", nv.getVaiTro().getTenVaiTro());
        }
        return map;
    }

    private String nextMaNhanVien() {
        long next = nhanVienRepo.count() + 1;
        String code;
        do {
            code = "NV" + String.format("%03d", next++);
        } while (nhanVienRepo.existsByMaNhanVien(code));
        return code;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanFilter(String value) {
        String cleaned = trim(value);
        return isBlank(cleaned) ? null : cleaned;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Integer parseInteger(String value) {
        if (isBlank(value)) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Byte parseByte(String value, Byte fallback) {
        if (isBlank(value)) return fallback;
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String validateEmployeeRequest(Map<String, Object> body, NhanVien existing, boolean create) {
        String name = value(body, "hoVaTen", existing != null ? existing.getHoVaTen() : null);
        String username = value(body, "tenNguoiDung", existing != null ? existing.getTenNguoiDung() : null);
        String email = normalizeEmail(value(body, "email", existing != null ? existing.getEmail() : null));
        String password = toStringVal(body.get("matKhau"));
        String phone = normalizePhone(value(body, "soDienThoai", existing != null ? existing.getSoDienThoai() : null));
        String address = value(body, "diaChi", existing != null ? existing.getDiaChi() : null);

        if (isBlank(name) || !NAME_PATTERN.matcher(name).matches()) {
            return "Họ tên phải từ 2 đến 150 ký tự và không chứa chữ số hoặc ký tự đặc biệt không hợp lệ";
        }
        if (isBlank(username) || !USERNAME_PATTERN.matcher(username).matches()) {
            return "Tên đăng nhập phải từ 4 đến 50 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang";
        }
        if (isBlank(email) || email.length() > 150 || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Email không đúng định dạng hoặc vượt quá 150 ký tự";
        }
        if (create || !isBlank(password)) {
            if (password == null || password.length() < 8 || password.length() > 72
                    || !password.matches(".*\\p{L}.*") || !password.matches(".*\\d.*")) {
                return "Mật khẩu phải từ 8 đến 72 ký tự, có ít nhất một chữ cái và một chữ số";
            }
        }
        if (!isBlank(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
            return "Số điện thoại phải gồm 10-11 số trong nước hoặc mã quốc gia dạng +84901234567";
        }
        if (!isBlank(address) && address.length() > 255) {
            return "Địa chỉ không được vượt quá 255 ký tự";
        }

        String birthValue = value(body, "ngaySinh", existing != null && existing.getNgaySinh() != null
                ? existing.getNgaySinh().toString() : null);
        if (!isBlank(birthValue)) {
            try {
                LocalDate birthDate = LocalDate.parse(birthValue);
                if (birthDate.isAfter(LocalDate.now()) || birthDate.isBefore(LocalDate.of(1900, 1, 1))) {
                    return "Ngày sinh không hợp lệ";
                }
                if (Period.between(birthDate, LocalDate.now()).getYears() < 18) {
                    return "Nhân viên phải đủ 18 tuổi";
                }
            } catch (Exception ignored) {
                return "Ngày sinh không đúng định dạng";
            }
        }

        String rawGender = value(body, "gioiTinh", null);
        Byte gender = parseByte(rawGender, null);
        if (!isBlank(rawGender) && (gender == null || (gender != 0 && gender != 1))) {
            return "Giới tính không hợp lệ";
        }
        Byte workStatus = parseByte(value(body, "tinhTrangLamViec",
                existing != null ? existing.getTinhTrangLamViec() : (byte) 1), null);
        if (workStatus == null || (workStatus != 0 && workStatus != 1)) {
            return "Trạng thái làm việc không hợp lệ";
        }
        if (body.containsKey("vaiTroId")) {
            Integer roleId = parseInteger(toStringVal(body.get("vaiTroId")));
            VaiTro role = roleId == null ? null : vaiTroRepo.findById(roleId).orElse(null);
            if (!isSupportedRole(role)) return "Vai trò nhân viên không hợp lệ";
        }
        return null;
    }

    private boolean wouldRemoveLastActiveAdmin(NhanVien employee, Map<String, Object> body) {
        if (!isActiveAdmin(employee) || !body.containsKey("vaiTroId")) return false;
        Integer roleId = parseInteger(toStringVal(body.get("vaiTroId")));
        VaiTro targetRole = roleId == null ? null : vaiTroRepo.findById(roleId).orElse(null);
        return !isAdminRole(targetRole) && nhanVienRepo.countActiveAdmins() <= 1;
    }

    private boolean isActiveAdmin(NhanVien employee) {
        return employee != null
                && Byte.valueOf((byte) 1).equals(employee.getTinhTrangLamViec())
                && isAdminRole(employee.getVaiTro());
    }

    private boolean isSupportedRole(VaiTro role) {
        if (role == null || role.getTenVaiTro() == null) return false;
        String name = role.getTenVaiTro().trim();
        return "Admin".equalsIgnoreCase(name) || "Nhân viên".equalsIgnoreCase(name);
    }

    private boolean isAdminRole(VaiTro role) {
        return role != null && role.getTenVaiTro() != null
                && "Admin".equalsIgnoreCase(role.getTenVaiTro().trim());
    }

    private String value(Map<String, Object> body, String key, Object fallback) {
        Object raw = body.containsKey(key) ? body.get(key) : fallback;
        return trim(toStringVal(raw));
    }

    private String normalizeEmail(String value) {
        String email = trim(value);
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String value) {
        String phone = trim(value);
        return phone == null ? null : phone.replaceAll("[\\s.()-]", "");
    }
}
