package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.Vay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VayRepository extends JpaRepository<Vay, Integer> {

    Optional<Vay> findByMaVay(String maVay);

    List<Vay> findByTenVayContainingIgnoreCase(String tenVay);

    List<Vay> findByTrangThai(Byte trangThai);

    List<Vay> findByLoaiVayId(Integer loaiVayId);

    boolean existsByMaVay(String maVay);
}