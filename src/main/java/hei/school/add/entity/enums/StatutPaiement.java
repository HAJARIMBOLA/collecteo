package hei.school.add.entity.enums;

/** Statut de paiement d'un achat ou d'une vente. */
public enum StatutPaiement {
  PAYEE, // montantRestant == 0
  PARTIELLEMENT_PAYEE, // 0 < montantPaye < montantTotal
  IMPAYEE // montantPaye == 0
}
