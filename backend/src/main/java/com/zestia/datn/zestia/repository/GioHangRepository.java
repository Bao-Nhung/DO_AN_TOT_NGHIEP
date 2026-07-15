package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GioHang;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    Optional<GioHang> findByKhachHangId(Integer khachHangId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GioHang g WHERE g.khachHang.id = :customerId")
    Optional<GioHang> findByKhachHangIdForUpdate(@Param("customerId") Integer customerId);

    boolean existsByKhachHangId(Integer khachHangId);
}
