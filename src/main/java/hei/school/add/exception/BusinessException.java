package hei.school.add.exception;

/**
 * Exception pour les règles métier violées (ex: stock insuffisant, paiement supérieur au restant
 * dû).
 */
public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }
}
