package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Integer> {

    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);

    boolean existsByTenVaiTro(String tenVaiTro);
}