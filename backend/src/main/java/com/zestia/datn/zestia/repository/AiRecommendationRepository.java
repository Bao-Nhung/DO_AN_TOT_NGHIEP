package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Integer> {
    List<AiRecommendation> findBySanPhamChinhId(Integer sanPhamChinhId);
}
