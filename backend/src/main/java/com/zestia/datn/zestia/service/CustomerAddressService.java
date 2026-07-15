package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.DiaChi;
import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.DiaChiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {
    private static final int MAX_ADDRESSES = 10;
    private final DiaChiRepository addressRepo;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(KhachHang customer) {
        return addressRepo.findByKhachHangIdOrderByMacDinhDescIdDesc(customer.getId()).stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> create(KhachHang customer, Map<String, ?> body) {
        if (addressRepo.countByKhachHangId(customer.getId()) >= MAX_ADDRESSES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mỗi tài khoản được lưu tối đa 10 địa chỉ");
        }
        boolean first = addressRepo.countByKhachHangId(customer.getId()) == 0;
        boolean makeDefault = first || truthy(body.get("macDinh"));
        DiaChi address = DiaChi.builder().khachHang(customer).build();
        apply(address, body);
        if (makeDefault) addressRepo.clearDefault(customer.getId());
        address.setMacDinh((byte) (makeDefault ? 1 : 0));
        return toMap(addressRepo.save(address));
    }

    @Transactional
    public Map<String, Object> update(KhachHang customer, Integer id, Map<String, ?> body) {
        DiaChi address = owned(customer, id);
        apply(address, body);
        boolean makeDefault = truthy(body.get("macDinh"));
        if (makeDefault) {
            addressRepo.clearDefault(customer.getId());
            address.setMacDinh((byte) 1);
        }
        return toMap(addressRepo.save(address));
    }

    @Transactional
    public Map<String, Object> setDefault(KhachHang customer, Integer id) {
        DiaChi address = owned(customer, id);
        addressRepo.clearDefault(customer.getId());
        address.setMacDinh((byte) 1);
        return toMap(addressRepo.save(address));
    }

    @Transactional
    public void delete(KhachHang customer, Integer id) {
        DiaChi address = owned(customer, id);
        boolean wasDefault = Byte.valueOf((byte) 1).equals(address.getMacDinh());
        addressRepo.delete(address);
        if (wasDefault) {
            addressRepo.findByKhachHangIdOrderByMacDinhDescIdDesc(customer.getId()).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setMacDinh((byte) 1);
                        addressRepo.save(next);
                    });
        }
    }

    @Transactional
    public void saveCheckoutAddress(KhachHang customer, Map<String, Object> body) {
        if (customer == null) return;
        String city = clean(body.get("tinhThanhPho"));
        String district = clean(body.get("quanHuyen"));
        String ward = clean(body.get("xaPhuong"));
        String street = clean(body.get("duong"));
        if (city == null || district == null || ward == null || street == null) return;

        List<DiaChi> existing = addressRepo.findByKhachHangIdOrderByMacDinhDescIdDesc(customer.getId());
        boolean duplicate = existing.stream().anyMatch(address -> same(address.getTinhThanhPho(), city)
                && same(address.getQuanHuyen(), district)
                && same(address.getXaPhuong(), ward)
                && same(address.getDuong(), street));
        if (duplicate || existing.size() >= MAX_ADDRESSES) return;

        boolean makeDefault = existing.isEmpty();
        addressRepo.save(DiaChi.builder()
                .khachHang(customer)
                .tinhThanhPho(city)
                .quanHuyen(district)
                .xaPhuong(ward)
                .duong(street)
                .macDinh((byte) (makeDefault ? 1 : 0))
                .build());
    }

    private DiaChi owned(KhachHang customer, Integer id) {
        return addressRepo.findByIdAndKhachHangId(id, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"));
    }

    private void apply(DiaChi address, Map<String, ?> body) {
        String city = required(body.get("tinhThanhPho"), "Tỉnh/Thành phố", 100);
        String district = required(body.get("quanHuyen"), "Quận/Huyện", 100);
        String ward = required(body.get("xaPhuong"), "Phường/Xã", 100);
        String street = required(body.get("duong"), "Địa chỉ cụ thể", 255);
        address.setTinhThanhPho(city);
        address.setQuanHuyen(district);
        address.setXaPhuong(ward);
        address.setDuong(street);
    }

    private String required(Object value, String label, int maxLength) {
        String result = clean(value);
        if (result == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập " + label);
        if (result.length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " không được vượt quá " + maxLength + " ký tự");
        }
        return result;
    }

    private String clean(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private boolean truthy(Object value) {
        return Boolean.TRUE.equals(value) || "1".equals(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private boolean same(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }

    public Map<String, Object> toMap(DiaChi address) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", address.getId());
        map.put("tinhThanhPho", address.getTinhThanhPho());
        map.put("quanHuyen", address.getQuanHuyen());
        map.put("xaPhuong", address.getXaPhuong());
        map.put("duong", address.getDuong());
        map.put("macDinh", Byte.valueOf((byte) 1).equals(address.getMacDinh()));
        return map;
    }
}
