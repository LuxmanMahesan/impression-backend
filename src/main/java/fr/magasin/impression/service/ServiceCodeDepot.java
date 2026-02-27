package fr.magasin.impression.service;

import fr.magasin.impression.repository.CodeDepotRepository;
import fr.magasin.impression.model.CodeDepot;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional
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

    public String obtenirCodeCourant() {
        return obtenirLigne().getValeur();
    }

    public void verifierCodeOuEchouer(String codeSaisi) {
        String propre = codeSaisi == null ? "" : codeSaisi.trim();
        if (propre.isEmpty()) {
            throw new IllegalArgumentException("Code obligatoire");
        }
        String attendu = obtenirCodeCourant();
        if (!attendu.equals(propre)) {
            throw new IllegalArgumentException("Code invalide");
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    public void tournerCodeChaqueNuit() {
        genererEtEnregistrerNouveauCode();
    }

    private void assurerCodeDuJour() {
        CodeDepot ligne = obtenirLigne();
        LocalDate aujourdHui = LocalDate.now(ZONE);
        if (!aujourdHui.equals(ligne.getDateGeneration())) {
            genererEtEnregistrerNouveauCode();
        }
    }

    private void genererEtEnregistrerNouveauCode() {
        CodeDepot ligne = obtenirLigne();
        ligne.setValeur(genererCode4Chiffres());
        ligne.setDateGeneration(LocalDate.now(ZONE));
        codeDepotRepository.save(ligne);
    }

    /** Code à 4 chiffres : plus simple à communiquer au client */
    private String genererCode4Chiffres() {
        return String.format("%04d", alea.nextInt(10_000));
    }

    private CodeDepot obtenirLigne() {
        return codeDepotRepository.findById(ID_LIGNE)
                .orElseGet(() -> {
                    CodeDepot c = new CodeDepot();
                    c.setId(ID_LIGNE);
                    c.setValeur("0000");
                    c.setDateGeneration(LocalDate.now(ZONE));
                    return codeDepotRepository.save(c);
                });
    }
}