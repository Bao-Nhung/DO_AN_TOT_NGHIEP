package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.Anh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnhRepository extends JpaRepository<Anh, Integer> {
    List<Anh> findByVayIdOrderByIdAsc(Integer vayId);
    List<Anh> findByVayIdAndTrangThai(Integer vayId, Byte trangThai);
}
