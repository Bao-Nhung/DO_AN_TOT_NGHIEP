package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LichLamViecRepository extends JpaRepository<LichLamViec, Integer> {

    List<LichLamViec> findByTrangThaiXoaFalseOrderByNgayLamViecAscGioBatDauAsc();

    List<LichLamViec> findByNgayLamViecBetweenAndTrangThaiXoaFalseOrderByNgayLamViecAscGioBatDauAsc(LocalDate from, LocalDate to);
}
