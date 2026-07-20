package com.example.demo.entity.enums;

/** Catégorie d'une transaction de caisse, utile pour les statistiques et le tableau de bord. */
public enum CategorieTransactionCaisse {
  // Entrées
  VENTE,
  REMBOURSEMENT_CLIENT,
  APPORT_PROPRIETAIRE,
  AUTRE_ENTREE,

  // Sorties
  ACHAT,
  SALAIRE,
  CARBURANT,
  TRANSPORT,
  DIVERS
}
