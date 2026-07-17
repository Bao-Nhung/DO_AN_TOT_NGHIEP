package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.ThongBaoDaDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ThongBaoDaDocRepository extends JpaRepository<ThongBaoDaDoc, Long> {

    boolean existsByKhachHangIdAndThongBaoId(Integer khachHangId, Integer thongBaoId);

    @Query("""
            SELECT d.thongBao.id
            FROM ThongBaoDaDoc d
            WHERE d.khachHang.id = :khachHangId
              AND d.thongBao.id IN :thongBaoIds
            """)
    List<Integer> findReadNotificationIds(@Param("khachHangId") Integer khachHangId,
                                          @Param("thongBaoIds") Collection<Integer> thongBaoIds);
}
