package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffDashboardService {
    private final HoaDonRepository orderRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Integer employeeId) {
        LocalDateTime startAt = LocalDate.now().atStartOfDay();
        LocalDateTime endAt = startAt.plusDays(1);
        HoaDonRepository.StaffTodaySummary summary =
                orderRepository.summarizeStaffToday(employeeId, startAt, endAt);

        long orderCount = summary != null && summary.getOrderCount() != null
                ? summary.getOrderCount()
                : 0;
        BigDecimal posRevenue = summary != null && summary.getPosRevenue() != null
                ? summary.getPosRevenue()
                : BigDecimal.ZERO;
        List<Map<String, Object>> recentOrders = orderRepository.findStaffRecentOrders(
                        employeeId,
                        PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "ngayTao", "id"))
                )
                .getContent()
                .stream()
                .map(this::toSummary)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("todayOrderCount", orderCount);
        response.put("todayPosRevenue", posRevenue);
        response.put("pendingOrderCount", orderRepository.countActionablePendingOrders());
        response.put("recentOrders", recentOrders);
        return response;
    }

    private Map<String, Object> toSummary(HoaDon order) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", order.getId());
        item.put("maHoaDon", order.getMaHoaDon());
        item.put("tenKhachHang", order.getKhachHang() != null
                ? order.getKhachHang().getHoVaTen()
                : order.getTenKhachHang());
        item.put("tongTien", order.getTongTien());
        item.put("trangThai", order.getTrangThai());
        item.put("ngayTao", order.getNgayTao());
        return item;
    }
}
