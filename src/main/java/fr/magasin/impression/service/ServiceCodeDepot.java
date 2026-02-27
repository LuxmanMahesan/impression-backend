package fr.magasin.impression.service;

import fr.magasin.impression.repository.CodeDepotRepository;
import fr.magasin.impression.model.CodeDepot;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ServiceCodeDepot {

    private static final short ID_LIGNE = 1;
    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");

    private final CodeDepotRepository codeDepotRepository;
    private final SecureRandom alea = new SecureRandom();

    public ServiceCodeDepot(CodeDepotRepository codeDepotRepository) {
        this.codeDepotRepository = codeDepotRepository;
    }

    @PostConstruct
    public void initialiser() {
        assurerCodeDuJour();
    }

    /**
     * Vérifie le code saisi. Ne fait AUCUNE écriture.
     * REQUIRES_NEW pour ne pas interférer avec la transaction appelante.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void verifierCodeOuEchouer(String codeSaisi) {
        String propre = codeSaisi == null ? "" : codeSaisi.trim();
        if (propre.isEmpty()) {
            throw new IllegalArgumentException("Code obligatoire");
        }
        String attendu = lireCodeCourant();
        if (!attendu.equals(propre)) {
            throw new IllegalArgumentException("Code invalide");
        }
    }

    /**
     * Retourne le code courant. Utilisé par l'admin.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public String obtenirCodeCourant() {
        return lireCodeCourant();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    @Transactional
    public void tournerCodeChaqueNuit() {
        genererEtEnregistrerNouveauCode();
    }

    // ── méthodes privées ──

    private String lireCodeCourant() {
        return codeDepotRepository.findById(ID_LIGNE)
                .map(CodeDepot::getValeur)
                .orElse("0000");
    }

    @Transactional
    public void assurerCodeDuJour() {
        var opt = codeDepotRepository.findById(ID_LIGNE);
        if (opt.isEmpty()) {
            // Première exécution : créer la ligne
            CodeDepot c = new CodeDepot();
            c.setId(ID_LIGNE);
            c.setValeur(genererCode4Chiffres());
            c.setDateGeneration(LocalDate.now(ZONE));
            codeDepotRepository.save(c);
        } else {
            CodeDepot ligne = opt.get();
            LocalDate aujourdHui = LocalDate.now(ZONE);
            if (!aujourdHui.equals(ligne.getDateGeneration())) {
                ligne.setValeur(genererCode4Chiffres());
                ligne.setDateGeneration(aujourdHui);
                codeDepotRepository.save(ligne);
            }
        }
    }

    private void genererEtEnregistrerNouveauCode() {
        var opt = codeDepotRepository.findById(ID_LIGNE);
        if (opt.isPresent()) {
            CodeDepot ligne = opt.get();
            ligne.setValeur(genererCode4Chiffres());
            ligne.setDateGeneration(LocalDate.now(ZONE));
            codeDepotRepository.save(ligne);
        }
    }

    private String genererCode4Chiffres() {
        return String.format("%04d", alea.nextInt(10_000));
    }
}