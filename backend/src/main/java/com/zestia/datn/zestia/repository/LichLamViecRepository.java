package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LichLamViecRepository extends JpaRepository<LichLamViec, Integer> {

    List<LichLamViec> findByNgayLamBetweenOrderByNgayLamAscGioBatDauAsc(LocalDate startDate, LocalDate endDate);

    List<LichLamViec> findByNhanVienIdOrderByNgayLamAscGioBatDauAsc(Integer nhanVienId);

    List<LichLamViec> findByNhanVienIdAndNgayLamOrderByGioBatDauAsc(Integer nhanVienId, LocalDate ngayLam);

    @Query("""
            SELECT l FROM LichLamViec l
            JOIN FETCH l.nhanVien n
            LEFT JOIN FETCH n.vaiTro
            WHERE l.ngayLam = :date
              AND l.trangThai = 1
              AND l.gioCheckIn IS NOT NULL
              AND l.gioCheckOut IS NULL
            ORDER BY l.gioCheckIn ASC
            """)
    List<LichLamViec> findCheckedInShifts(@Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LichLamViec l WHERE l.id = :id")
    Optional<LichLamViec> findByIdForUpdate(@Param("id") Integer id);

}
