package fr.magasin.impression.service;

import fr.magasin.impression.repository.DepotRepository;
import fr.magasin.impression.repository.FichierDepotRepository;
import fr.magasin.impression.model.Depot;
import fr.magasin.impression.model.FichierDepot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceDepot {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_CODE = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ServiceCodeDepot serviceCodeDepot;
    private final ServiceStockageFichiers serviceStockageFichiers;
    private final DepotRepository depotRepository;
    private final FichierDepotRepository fichierDepotRepository;

    public ServiceDepot(
            ServiceCodeDepot serviceCodeDepot,
            ServiceStockageFichiers serviceStockageFichiers,
            DepotRepository depotRepository,
            FichierDepotRepository fichierDepotRepository
    ) {
        this.serviceCodeDepot = serviceCodeDepot;
        this.serviceStockageFichiers = serviceStockageFichiers;
        this.depotRepository = depotRepository;
        this.fichierDepotRepository = fichierDepotRepository;
    }

    @Transactional
    public String demarrerDepot(String code) {
        serviceCodeDepot.verifierCodeOuEchouer(code);

        Depot depot = new Depot();
        depot.setStatut("BROUILLON");
        depot.setCodePublic(genererCodePublicUnique());
        depotRepository.save(depot);

        return depot.getCodePublic();
    }

    @Transactional(readOnly = true)
    public List<Depot> listerTousLesDepots() {
        return depotRepository.findAllByOrderByCreeLeDesc();
    }

    @Transactional
    public List<UUID> ajouterFichiers(String codePublic, List<MultipartFile> fichiers) {
        Depot depot = trouverParCodePublic(codePublic);

        if (!"BROUILLON".equals(depot.getStatut())) {
            throw new IllegalStateException("Depot deja valide");
        }

        return fichiers.stream().map(f -> {
            var resultat = serviceStockageFichiers.lireEtHasher(f);

            FichierDepot fd = new FichierDepot();
            fd.setDepot(depot);
            fd.setNomOriginal(f.getOriginalFilename() == null ? "fichier" : f.getOriginalFilename());
            fd.setTypeMime(f.getContentType() == null ? "application/octet-stream" : f.getContentType());
            fd.setTaille(f.getSize());
            fd.setContenu(resultat.contenu());
            fd.setEmpreinteSha256(resultat.sha256());

            fichierDepotRepository.save(fd);
            return fd.getId();
        }).toList();
    }

    @Transactional
    public String validerDepot(String codePublic) {
        Depot depot = trouverParCodePublic(codePublic);

        if ("VALIDE".equals(depot.getStatut())) {
            return depot.getCodePublic();
        }

        if (!"BROUILLON".equals(depot.getStatut())) {
            throw new IllegalStateException("Depot dans un statut inattendu : " + depot.getStatut());
        }

        depot.setStatut("VALIDE");
        depot.setValideLe(Instant.now());
        depotRepository.save(depot);

        return depot.getCodePublic();
    }

    @Transactional
    public void supprimerDepot(String codePublic) {
        Depot depot = trouverParCodePublic(codePublic);
        depotRepository.delete(depot);
    }

    private Depot trouverParCodePublic(String codePublic) {
        return depotRepository.findByCodePublic(codePublic.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Depot introuvable"));
    }

    private String genererCodePublicUnique() {
        for (int tentative = 0; tentative < 100; tentative++) {
            String code = genererCode();
            if (!depotRepository.existsByCodePublic(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de generer un code unique apres 100 tentatives");
    }

    private String genererCode() {
        StringBuilder sb = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}