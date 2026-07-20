package com.example.demo.repository;

import com.example.demo.entity.RemboursementPret;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemboursementPretRepository extends JpaRepository<RemboursementPret, Long> {
  List<RemboursementPret> findByPretIdOrderByDateDesc(Long pretId);
}
