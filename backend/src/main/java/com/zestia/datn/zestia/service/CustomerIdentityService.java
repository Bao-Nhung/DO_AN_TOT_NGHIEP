package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerIdentityService {
    private final KhachHangRepository customerRepo;
    private final HoaDonRepository orderRepo;

    @Transactional
    public KhachHang resolveForOrder(KhachHang authenticatedCustomer, String fullName,
                                     String phone, String email) {
        return resolve(authenticatedCustomer, fullName, phone, email, true);
    }

    @Transactional
    public KhachHang resolveForRegistration(String fullName, String phone, String email) {
        return resolve(null, fullName, phone, email, false);
    }

    private KhachHang resolve(KhachHang authenticatedCustomer, String fullName,
                              String phone, String email, boolean linkHistoricalOrders) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedEmail = normalizeEmail(email);

        KhachHang customer;
        if (authenticatedCustomer != null) {
            customer = lockExisting(authenticatedCustomer);
            rejectContactOwnedByAnotherCustomer(customer, normalizedPhone, normalizedEmail);
        } else {
            Optional<KhachHang> byPhone = normalizedPhone == null
                    ? Optional.empty() : customerRepo.findBySoDienThoai(normalizedPhone);
            Optional<KhachHang> byEmail = normalizedEmail == null
                    ? Optional.empty() : customerRepo.findByEmailIgnoreCase(normalizedEmail);
            if (!linkHistoricalOrders && (byPhone.isPresent() || byEmail.isPresent())) {
                throw new IllegalStateException("Email hoặc số điện thoại vừa được sử dụng bởi một tài khoản khác");
            }
            if (byPhone.isPresent() && byEmail.isPresent()
                    && !Objects.equals(byPhone.get().getId(), byEmail.get().getId())) {
                throw new IllegalStateException(
                        "Số điện thoại và email đang thuộc hai hồ sơ khách hàng khác nhau. Vui lòng chọn đúng khách hàng hoặc nhờ quản trị viên kiểm tra."
                );
            }
            if (byPhone.isPresent() && normalizedEmail != null
                    && !isBlank(byPhone.get().getEmail())
                    && !normalizedEmail.equalsIgnoreCase(byPhone.get().getEmail())) {
                throw new IllegalStateException("Số điện thoại đang gắn với một email khác");
            }
            if (byEmail.isPresent() && normalizedPhone != null
                    && !isBlank(byEmail.get().getSoDienThoai())
                    && !normalizedPhone.equals(normalizePhone(byEmail.get().getSoDienThoai()))) {
                throw new IllegalStateException("Email đang gắn với một số điện thoại khác");
            }
            customer = byPhone.or(() -> byEmail).orElseGet(() -> KhachHang.builder()
                    .maKhachHang(newCustomerCode())
                    .hoVaTen(fullName)
                    .soDienThoai(normalizedPhone)
                    .email(normalizedEmail)
                    .ngayTao(LocalDateTime.now())
                    .build());
            if (customer.getId() != null) customer = lockExisting(customer);
        }

        if (isBlank(customer.getHoVaTen()) || isPlaceholderName(customer.getHoVaTen())) {
            customer.setHoVaTen(fullName);
        }
        if (isBlank(customer.getSoDienThoai()) && normalizedPhone != null) {
            customer.setSoDienThoai(normalizedPhone);
        }
        if (isBlank(customer.getEmail()) && normalizedEmail != null) {
            customer.setEmail(normalizedEmail);
        }
        if (customer.getNgayTao() == null) customer.setNgayTao(LocalDateTime.now());
        if (isBlank(customer.getMaKhachHang())) customer.setMaKhachHang(newCustomerCode());

        customer = customerRepo.save(customer);
        if (linkHistoricalOrders) linkUnassignedOrders(customer);
        return customer;
    }

    @Transactional
    public void linkUnassignedOrders(KhachHang customer) {
        if (customer == null) return;
        String phone = normalizePhone(customer.getSoDienThoai());
        String email = normalizeEmail(customer.getEmail());
        if (phone != null || email != null) orderRepo.linkUnassignedOrdersByIdentity(customer, phone, email);
    }

    private void rejectContactOwnedByAnotherCustomer(KhachHang customer, String phone, String email) {
        if (phone != null) {
            customerRepo.findBySoDienThoai(phone)
                    .filter(other -> !Objects.equals(other.getId(), customer.getId()))
                    .ifPresent(other -> {
                        throw new IllegalStateException("Số điện thoại đã thuộc một khách hàng khác");
                    });
        }
        if (email != null) {
            customerRepo.findByEmailIgnoreCase(email)
                    .filter(other -> !Objects.equals(other.getId(), customer.getId()))
                    .ifPresent(other -> {
                        throw new IllegalStateException("Email đã thuộc một khách hàng khác");
                    });
        }
    }

    private KhachHang lockExisting(KhachHang customer) {
        return customerRepo.findByIdForUpdate(customer.getId())
                .orElseThrow(() -> new IllegalStateException("Hồ sơ khách hàng không còn tồn tại"));
    }

    public String normalizePhone(String value) {
        if (value == null) return null;
        String digits = value.replaceAll("\\D", "");
        if (digits.startsWith("84") && digits.length() == 11) digits = "0" + digits.substring(2);
        return digits.isBlank() ? null : digits;
    }

    public String normalizeEmail(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String newCustomerCode() {
        String code;
        do {
            code = "KH" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 10).toUpperCase(Locale.ROOT);
        } while (customerRepo.existsByMaKhachHang(code));
        return code;
    }

    private boolean isPlaceholderName(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("khách lẻ") || normalized.equals("khach le");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
