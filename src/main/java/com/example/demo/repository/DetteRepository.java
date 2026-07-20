package com.example.demo.repository;

import com.example.demo.entity.Dette;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DetteRepository extends JpaRepository<Dette, Long> {
  @Query("SELECT d FROM Dette d WHERE d.montantRestant > 0")
  List<Dette> trouverDettesNonSoldees();

  @Query("SELECT COALESCE(SUM(d.montantRestant), 0) FROM Dette d")
  BigDecimal calculerTotalDettesFournisseurs();
}
