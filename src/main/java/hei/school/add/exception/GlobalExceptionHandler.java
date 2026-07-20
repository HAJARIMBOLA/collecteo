package hei.school.add.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuth(
      org.springframework.security.core.AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(buildBody(ex.getMessage(), HttpStatus.UNAUTHORIZED));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(buildBody(ex.getMessage(), HttpStatus.NOT_FOUND));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(buildBody(ex.getMessage(), HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> erreurs = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      erreurs.put(fe.getField(), fe.getDefaultMessage());
    }
    Map<String, Object> body = buildBody("Erreur de validation", HttpStatus.BAD_REQUEST);
    body.put("erreurs", erreurs);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  private Map<String, Object> buildBody(String message, HttpStatus status) {
    Map<String, Object> body = new HashMap<>();
    body.put("horodatage", LocalDateTime.now());
    body.put("statut", status.value());
    body.put("message", message);
    return body;
  }
}
