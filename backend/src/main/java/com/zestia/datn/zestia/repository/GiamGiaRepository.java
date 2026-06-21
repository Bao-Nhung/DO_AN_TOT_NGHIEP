package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.GiamGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {
    Optional<GiamGia> findByMaGiamGiaIgnoreCase(String maGiamGia);
}
