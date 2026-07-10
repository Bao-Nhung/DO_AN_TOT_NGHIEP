package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.HoaDonAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoaDonAuditLogRepository extends JpaRepository<HoaDonAuditLog, Integer> {
    List<HoaDonAuditLog> findByHoaDonIdOrderByNgayTaoDesc(Integer hoaDonId);
}
