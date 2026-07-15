package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.HoaDon;
import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LichLamViecRepository scheduleRepository;
    private final HoaDonRepository orderRepository;

    @Transactional(readOnly = true)
    public List<ShiftReport> build(LocalDate startDate, LocalDate endDate, Integer employeeId) {
        validateRange(startDate, endDate);
        List<LichLamViec> shifts = employeeId == null
                ? scheduleRepository.findByNgayLamBetweenOrderByNgayLamAscGioBatDauAsc(startDate, endDate)
                : scheduleRepository.findByNhanVienIdOrderByNgayLamAscGioBatDauAsc(employeeId).stream()
                    .filter(shift -> !shift.getNgayLam().isBefore(startDate) && !shift.getNgayLam().isAfter(endDate))
                    .toList();

        List<HoaDon> orders = orderRepository.findPosOrdersForShiftReport(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), employeeId
        );
        Map<Integer, List<HoaDon>> ordersByEmployee = orders.stream()
                .filter(order -> order.getNhanVien() != null)
                .collect(Collectors.groupingBy(order -> order.getNhanVien().getId()));

        LocalDateTime now = LocalDateTime.now();
        return shifts.stream()
                .sorted(Comparator.comparing(LichLamViec::getNgayLam).reversed()
                        .thenComparing(LichLamViec::getGioBatDau, Comparator.reverseOrder()))
                .map(shift -> report(shift, ordersByEmployee.getOrDefault(employeeId(shift), List.of()), now))
                .toList();
    }

    public byte[] exportExcel(List<ShiftReport> reports) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lich su ca lam");
            sheet.createFreezePane(0, 1);
            String[] headers = {
                    "Ngày", "Mã nhân viên", "Nhân viên", "Ca làm", "Giờ dự kiến",
                    "Check-in", "Check-out", "Giờ thực tế", "Số đơn POS",
                    "Doanh thu", "Chuyển khoản", "Tiền mặt cần bàn giao", "Trạng thái"
            };
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle currencyStyle = currencyStyle(workbook);
            CellStyle decimalStyle = workbook.createCellStyle();
            decimalStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ShiftReport report : reports) {
                Row row = sheet.createRow(rowIndex++);
                set(row, 0, report.date().format(DATE_FORMAT));
                set(row, 1, report.employeeCode());
                set(row, 2, report.employeeName());
                set(row, 3, report.shiftName());
                set(row, 4, time(report.scheduledStart()) + " - " + time(report.scheduledEnd()));
                set(row, 5, dateTime(report.checkIn()));
                set(row, 6, dateTime(report.checkOut()));
                Cell hours = row.createCell(7);
                hours.setCellValue(report.workedMinutes() / 60d);
                hours.setCellStyle(decimalStyle);
                set(row, 8, report.orderCount());
                currency(row, 9, report.revenue(), currencyStyle);
                currency(row, 10, report.transferAmount(), currencyStyle);
                currency(row, 11, report.cashToHandover(), currencyStyle);
                set(row, 12, statusText(report.status()));
            }

            int[] widths = {13, 16, 24, 16, 18, 20, 20, 14, 14, 18, 18, 24, 20};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, headers.length - 1));
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("Không thể tạo file Excel ca làm", error);
        }
    }

    private ShiftReport report(LichLamViec shift, List<HoaDon> employeeOrders, LocalDateTime now) {
        LocalDateTime scheduledStart = LocalDateTime.of(shift.getNgayLam(), shift.getGioBatDau());
        LocalDateTime scheduledEnd = LocalDateTime.of(shift.getNgayLam(), shift.getGioKetThuc());
        List<HoaDon> shiftOrders = employeeOrders.stream()
                .filter(order -> order.getNgayTao() != null)
                .filter(order -> !order.getNgayTao().isBefore(scheduledStart) && order.getNgayTao().isBefore(scheduledEnd))
                .toList();
        List<HoaDon> successfulOrders = shiftOrders.stream().filter(this::isSuccessfulSale).toList();

        BigDecimal revenue = sum(successfulOrders);
        BigDecimal cash = successfulOrders.stream()
                .filter(order -> isCash(order.getHinhThucThanhToan()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transfer = revenue.subtract(cash).max(BigDecimal.ZERO);

        List<ShiftActivity> activities = new ArrayList<>();
        if (shift.getGioCheckIn() != null) {
            activities.add(new ShiftActivity(shift.getGioCheckIn(), "CHECK_IN", "Nhân viên check-in bắt đầu ca"));
        }
        shiftOrders.forEach(order -> activities.add(new ShiftActivity(
                order.getNgayTao(), "POS_SALE",
                order.getMaHoaDon() + " · " + safe(order.getHinhThucThanhToan()) + " · " + amount(order).toPlainString() + "đ"
        )));
        if (shift.getGioCheckOut() != null) {
            activities.add(new ShiftActivity(shift.getGioCheckOut(), "CHECK_OUT", "Nhân viên check-out kết thúc ca"));
        }
        activities.sort(Comparator.comparing(ShiftActivity::time));

        LocalDateTime workedStart = shift.getGioCheckIn() != null ? shift.getGioCheckIn() : null;
        LocalDateTime workedEnd = shift.getGioCheckOut() != null
                ? shift.getGioCheckOut()
                : (workedStart != null && now.isBefore(scheduledEnd) ? now : null);
        long workedMinutes = workedStart != null && workedEnd != null && !workedEnd.isBefore(workedStart)
                ? Duration.between(workedStart, workedEnd).toMinutes()
                : 0;

        return new ShiftReport(
                shift.getId(), shift.getNgayLam(), employeeId(shift),
                shift.getNhanVien() != null ? safe(shift.getNhanVien().getMaNhanVien()) : "",
                shift.getNhanVien() != null ? safe(shift.getNhanVien().getHoVaTen()) : "Chưa rõ",
                safe(shift.getCaLam()), shift.getGioBatDau(), shift.getGioKetThuc(),
                shift.getGioCheckIn(), shift.getGioCheckOut(), shift.getTrangThai(), workedMinutes,
                successfulOrders.size(), revenue, cash, transfer, List.copyOf(activities)
        );
    }

    private boolean isSuccessfulSale(HoaDon order) {
        byte status = order.getTrangThai() != null ? order.getTrangThai() : 0;
        return Boolean.TRUE.equals(order.getDaThanhToan()) && !Set.of((byte) 5, (byte) 6, (byte) 7, (byte) 9).contains(status);
    }

    private boolean isCash(String method) {
        if (method == null) return false;
        String normalized = Normalizer.normalize(method, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace('Đ', 'D');
        return normalized.contains("TIEN MAT") || normalized.equals("CASH");
    }

    private BigDecimal sum(List<HoaDon> orders) {
        return orders.stream().map(this::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal amount(HoaDon order) {
        return order.getTongTien() != null ? order.getTongTien() : BigDecimal.ZERO;
    }

    private Integer employeeId(LichLamViec shift) {
        return shift.getNhanVien() != null ? shift.getNhanVien().getId() : null;
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Khoảng ngày ca làm không hợp lệ");
        }
        if (startDate.plusYears(1).isBefore(endDate)) {
            throw new IllegalArgumentException("Mỗi lần chỉ được xuất tối đa 1 năm dữ liệu");
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle currencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0\"đ\""));
        return style;
    }

    private void set(Row row, int column, String value) {
        row.createCell(column).setCellValue(value != null ? value : "");
    }

    private void set(Row row, int column, long value) {
        row.createCell(column).setCellValue(value);
    }

    private void currency(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value.doubleValue() : 0d);
        cell.setCellStyle(style);
    }

    private String statusText(Byte status) {
        if (status == null || status == ShiftAccessService.STATUS_PENDING) return "Chờ xác nhận";
        if (status == ShiftAccessService.STATUS_CONFIRMED) return "Đã xác nhận";
        if (status == ShiftAccessService.STATUS_UNAVAILABLE) return "Đã báo bận";
        if (status == ShiftAccessService.STATUS_UNAVAILABLE_PENDING) return "Chờ duyệt báo bận";
        return "Không xác định";
    }

    private String time(LocalTime value) {
        return value != null ? value.format(TIME_FORMAT) : "";
    }

    private String dateTime(LocalDateTime value) {
        return value != null ? value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    public record ShiftReport(Integer id, LocalDate date, Integer employeeId, String employeeCode,
                              String employeeName, String shiftName, LocalTime scheduledStart,
                              LocalTime scheduledEnd, LocalDateTime checkIn, LocalDateTime checkOut,
                              Byte status, long workedMinutes, long orderCount, BigDecimal revenue,
                              BigDecimal cashToHandover, BigDecimal transferAmount,
                              List<ShiftActivity> activities) {
    }

    public record ShiftActivity(LocalDateTime time, String type, String description) {
    }
}
