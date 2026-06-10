package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.VayChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VayChiTietRepository extends JpaRepository<VayChiTiet, Integer> {

    List<VayChiTiet> findByVayId(Integer vayId);

    List<VayChiTiet> findByMauSacId(Integer mauSacId);

    List<VayChiTiet> findByKichThuocId(Integer kichThuocId);

    List<VayChiTiet> findByTrangThai(Byte trangThai);

    Optional<VayChiTiet> findByMaVayChiTiet(String maVayChiTiet);

    Optional<VayChiTiet> findByVayIdAndMauSacIdAndKichThuocId(
            Integer vayId,
            Integer mauSacId,
            Integer kichThuocId
    );
}