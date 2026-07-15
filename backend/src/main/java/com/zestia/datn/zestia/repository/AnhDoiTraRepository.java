package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.AnhDoiTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnhDoiTraRepository extends JpaRepository<AnhDoiTra, Integer> {
    List<AnhDoiTra> findByYeuCauIdOrderByIdAsc(Integer requestId);
    List<AnhDoiTra> findByYeuCauIdInOrderByIdAsc(List<Integer> requestIds);
}
