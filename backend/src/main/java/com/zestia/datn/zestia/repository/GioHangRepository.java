package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    Optional<GioHang> findByKhachHangId(Integer khachHangId);

    boolean existsByKhachHangId(Integer khachHangId);
}