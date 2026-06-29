package com.zestia.datn.zestia.repository;
import com.zestia.datn.zestia.entity.LichSuTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LichSuTrackingRepository extends JpaRepository<LichSuTracking, Integer> {
    @Query("SELECT l FROM LichSuTracking l WHERE l.hoaDon.id = :hoaDonId ORDER BY l.ngayCapNhat DESC")
    List<LichSuTracking> findByHoaDonIdOrderByLatest(@Param("hoaDonId") Integer hoaDonId);
    
    List<LichSuTracking> findByHoaDonId(Integer hoaDonId);
}