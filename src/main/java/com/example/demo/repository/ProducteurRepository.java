package com.example.demo.repository;

import com.example.demo.entity.Producteur;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProducteurRepository extends JpaRepository<Producteur, Long> {
  // SELECT * FROM producteurs WHERE LOWER(nom) LIKE LOWER('%mot%')
  @Query("SELECT p FROM Producteur p WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :motCle, '%'))")
  List<Producteur> rechercherParNom(@Param("motCle") String motCle);
}
