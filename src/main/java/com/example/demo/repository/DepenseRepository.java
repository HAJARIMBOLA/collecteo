package com.example.demo.repository;

import com.example.demo.entity.Depense;
import com.example.demo.entity.enums.CategorieDepense;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepenseRepository extends JpaRepository<Depense, Long> {
  List<Depense> findByCategorieOrderByDateDesc(CategorieDepense categorie);

  List<Depense> findAllByOrderByDateDesc();

  @Query("SELECT COALESCE(SUM(d.montant), 0) FROM Depense d")
  BigDecimal calculerTotalDepenses();

  @Query("SELECT COALESCE(SUM(d.montant), 0) FROM Depense d WHERE d.date BETWEEN :debut AND :fin")
  BigDecimal calculerTotalDepensesEntre(
      @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

  @Query("SELECT d.categorie, COALESCE(SUM(d.montant), 0) FROM Depense d GROUP BY d.categorie")
  List<Object[]> calculerTotalParCategorie();
}
