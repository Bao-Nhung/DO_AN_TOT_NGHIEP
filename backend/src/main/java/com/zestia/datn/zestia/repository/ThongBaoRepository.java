package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    List<ThongBao> findByTrangThaiOrderByNgayTaoDesc(Byte trangThai);
    List<ThongBao> findAllByOrderByNgayTaoDesc();
}