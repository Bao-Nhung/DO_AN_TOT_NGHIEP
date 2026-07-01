package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByKhachHangId(Integer khachHangId);
    Optional<DiaChi> findByKhachHangIdAndMacDinh(Integer khachHangId, Byte macDinh);
}
