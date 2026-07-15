package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByKhachHangId(Integer khachHangId);
    List<DiaChi> findByKhachHangIdOrderByMacDinhDescIdDesc(Integer khachHangId);
    Optional<DiaChi> findByKhachHangIdAndMacDinh(Integer khachHangId, Byte macDinh);
    Optional<DiaChi> findByIdAndKhachHangId(Integer id, Integer khachHangId);
    long countByKhachHangId(Integer khachHangId);

    @Modifying
    @Query("UPDATE DiaChi d SET d.macDinh = 0 WHERE d.khachHang.id = :customerId")
    int clearDefault(@Param("customerId") Integer customerId);
}
