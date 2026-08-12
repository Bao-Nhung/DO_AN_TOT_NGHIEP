package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {
    boolean existsByTenKichThuocIgnoreCase(String name);
    boolean existsByTenKichThuocIgnoreCaseAndIdNot(String name, Integer id);
}
