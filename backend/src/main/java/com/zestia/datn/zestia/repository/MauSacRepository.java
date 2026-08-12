package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    boolean existsByTenMauSacIgnoreCase(String name);
    boolean existsByTenMauSacIgnoreCaseAndIdNot(String name, Integer id);
    boolean existsByMaHexIgnoreCase(String hex);
    boolean existsByMaHexIgnoreCaseAndIdNot(String hex, Integer id);
}
