package fr.magasin.impression.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurApi> gererMauvaiseRequete(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErreurApi("REQUETE_INVALIDE", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErreurApi> gererConflit(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErreurApi("CONFLIT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> gererErreurGenerale(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErreurApi("ERREUR_SERVEUR", "Une erreur est survenue"));
    }

    public record ErreurApi(String code, String message, Instant date) {
        public ErreurApi(String code, String message) {
            this(code, message, Instant.now());
        }
    }
}
