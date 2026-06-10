package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {

    List<GioHangChiTiet> findByGioHangId(Integer gioHangId);

    Optional<GioHangChiTiet> findByGioHangIdAndVayChiTietId(
            Integer gioHangId,
            Integer vayChiTietId
    );

    long countByGioHangId(Integer gioHangId);

    void deleteByGioHangId(Integer gioHangId);
}