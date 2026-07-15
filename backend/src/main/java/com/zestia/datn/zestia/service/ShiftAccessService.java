package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftAccessService {
    public static final byte STATUS_PENDING = 0;
    public static final byte STATUS_CONFIRMED = 1;
    public static final byte STATUS_UNAVAILABLE = 2;
    public static final byte STATUS_UNAVAILABLE_PENDING = 3;

    private final LichLamViecRepository scheduleRepository;

    public WorkStatus getWorkStatus(Integer employeeId, LocalDateTime now) {
        List<LichLamViec> shifts = employeeId == null
                ? List.of()
                : scheduleRepository.findByNhanVienIdAndNgayLamOrderByGioBatDauAsc(employeeId, now.toLocalDate());
        Optional<LichLamViec> operatingShift = shifts.stream()
                .filter(shift -> isOperating(shift, now))
                .findFirst();
        Optional<LichLamViec> relevantShift = operatingShift.or(() -> shifts.stream()
                .filter(shift -> shift.getGioCheckOut() == null)
                .filter(shift -> shift.getGioKetThuc() != null && !now.toLocalTime().isAfter(shift.getGioKetThuc()))
                .findFirst());

        String reason;
        if (operatingShift.isPresent()) {
            reason = "Đang trong ca làm việc";
        } else if (shifts.isEmpty()) {
            reason = "Hôm nay bạn không có ca làm việc";
        } else if (shifts.stream().anyMatch(shift -> shift.getTrangThai() != null
                && shift.getTrangThai() == STATUS_UNAVAILABLE_PENDING)) {
            reason = "Yêu cầu báo bận đang chờ admin duyệt";
        } else if (shifts.stream().anyMatch(shift -> shift.getTrangThai() != null
                && shift.getTrangThai() == STATUS_UNAVAILABLE)) {
            reason = "Ca làm đã được ghi nhận báo bận";
        } else if (shifts.stream().anyMatch(shift -> shift.getTrangThai() == null
                || shift.getTrangThai() == STATUS_PENDING)) {
            reason = "Bạn cần xác nhận ca làm trước";
        } else if (shifts.stream().anyMatch(shift -> shift.getGioCheckIn() == null
                && isWithinShiftTime(shift, now))) {
            reason = "Bạn cần check-in để bắt đầu làm việc";
        } else if (shifts.stream().allMatch(shift -> shift.getGioCheckOut() != null
                || (shift.getGioKetThuc() != null && now.toLocalTime().isAfter(shift.getGioKetThuc())))) {
            reason = "Ca làm hôm nay đã kết thúc";
        } else {
            reason = "Chưa đến giờ bắt đầu ca làm";
        }
        return new WorkStatus(operatingShift.isPresent(), reason, operatingShift.orElse(null), relevantShift.orElse(null), shifts);
    }

    public boolean canOperate(Integer employeeId, LocalDateTime now) {
        return getWorkStatus(employeeId, now).canOperate();
    }

    public boolean canCheckIn(LichLamViec shift, LocalDateTime now) {
        if (shift == null || shift.getNgayLam() == null || !shift.getNgayLam().equals(now.toLocalDate())) return false;
        if (shift.getTrangThai() == null || shift.getTrangThai() != STATUS_CONFIRMED) return false;
        if (shift.getGioCheckIn() != null || shift.getGioCheckOut() != null) return false;
        LocalDateTime earliest = LocalDateTime.of(shift.getNgayLam(), shift.getGioBatDau()).minusMinutes(30);
        LocalDateTime latest = LocalDateTime.of(shift.getNgayLam(), shift.getGioKetThuc());
        return !now.isBefore(earliest) && !now.isAfter(latest);
    }

    public boolean canCheckOut(LichLamViec shift, LocalDateTime now) {
        return shift != null
                && shift.getNgayLam() != null
                && shift.getNgayLam().equals(now.toLocalDate())
                && shift.getGioCheckIn() != null
                && shift.getGioCheckOut() == null;
    }

    public boolean isPast(LichLamViec shift, LocalDateTime now) {
        if (shift == null || shift.getNgayLam() == null || shift.getGioKetThuc() == null) return true;
        return now.isAfter(LocalDateTime.of(shift.getNgayLam(), shift.getGioKetThuc()));
    }

    private boolean isOperating(LichLamViec shift, LocalDateTime now) {
        return shift.getTrangThai() != null
                && shift.getTrangThai() == STATUS_CONFIRMED
                && shift.getGioCheckIn() != null
                && shift.getGioCheckOut() == null
                && isWithinShiftTime(shift, now);
    }

    private boolean isWithinShiftTime(LichLamViec shift, LocalDateTime now) {
        if (shift.getNgayLam() == null || shift.getGioBatDau() == null || shift.getGioKetThuc() == null) return false;
        LocalDateTime start = LocalDateTime.of(shift.getNgayLam(), shift.getGioBatDau());
        LocalDateTime end = LocalDateTime.of(shift.getNgayLam(), shift.getGioKetThuc());
        return !now.isBefore(start) && !now.isAfter(end);
    }

    public record WorkStatus(boolean canOperate, String reason, LichLamViec activeShift,
                             LichLamViec relevantShift, List<LichLamViec> todayShifts) {
    }
}
