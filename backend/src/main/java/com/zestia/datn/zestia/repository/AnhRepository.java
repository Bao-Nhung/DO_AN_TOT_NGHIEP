package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.Anh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnhRepository extends JpaRepository<Anh, Integer> {
    List<Anh> findBySanPhamIdOrderByIdAsc(Integer sanPhamId);
    List<Anh> findBySanPhamIdAndTrangThai(Integer sanPhamId, Byte trangThai);
    List<Anh> findBySanPhamIdInAndTrangThaiOrderByIdAsc(List<Integer> sanPhamIds, Byte trangThai);
}
