package com.zestia.datn.zestia.repository;
import com.zestia.datn.zestia.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    @Query("SELECT t FROM ThongBao t WHERE t.khachHang.id = :khachHangId ORDER BY t.ngayTao DESC")
    List<ThongBao> findByCustomerIdOrderByLatest(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT t FROM ThongBao t WHERE t.khachHang.id = :khachHangId AND t.daDoc = 0 ORDER BY t.ngayTao DESC")
    List<ThongBao> findUnreadNotificationsByCustomerId(@Param("khachHangId") Integer khachHangId);
}