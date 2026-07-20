package com.example.demo.repository;

import com.example.demo.entity.Produit;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
  Optional<Produit> findByNomIgnoreCase(String nom);

  // Valeur totale du stock = somme(quantite * prixMoyenAchat) pour tous les produits
  @Query("SELECT COALESCE(SUM(p.quantiteStock * p.prixMoyenAchat), 0) FROM Produit p")
  BigDecimal calculerValeurTotaleStock();

  @Query("SELECT p FROM Produit p WHERE p.quantiteStock <= :seuil")
  List<Produit> trouverProduitsEnRupture(@Param("seuil") BigDecimal seuil);
}
