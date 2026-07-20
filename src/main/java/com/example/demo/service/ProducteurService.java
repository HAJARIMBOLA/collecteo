package com.example.demo.service;

import com.example.demo.entity.Producteur;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProducteurRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProducteurService {
  private final ProducteurRepository producteurRepository;

  public List<Producteur> listerTous() {
    return producteurRepository.findAll();
  }

  public Producteur trouverParId(Long id) {
    return producteurRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Producteur introuvable avec l'id " + id));
  }

  public List<Producteur> rechercher(String motCle) {
    return producteurRepository.rechercherParNom(motCle);
  }

  public Producteur creer(Producteur producteur) {
    return producteurRepository.save(producteur);
  }

  public Producteur modifier(Long id, Producteur donnees) {
    Producteur existant = trouverParId(id);
    existant.setNom(donnees.getNom());
    existant.setTelephone(donnees.getTelephone());
    existant.setAdresse(donnees.getAdresse());
    return producteurRepository.save(existant);
  }

  public void supprimer(Long id) {
    Producteur existant = trouverParId(id);
    producteurRepository.delete(existant);
  }
}
