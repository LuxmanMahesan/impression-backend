package fr.magasin.impression.service;

import fr.magasin.impression.repository.DepotRepository;
import fr.magasin.impression.repository.FichierDepotRepository;
import fr.magasin.impression.model.Depot;
import fr.magasin.impression.model.FichierDepot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServiceDepot {

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

    public UUID demarrerDepot(String code) {
        serviceCodeDepot.verifierCodeOuEchouer(code);

        Depot depot = new Depot();
        depot.setStatut("BROUILLON");
        depotRepository.save(depot);
        return depot.getId();
    }

    public List<UUID> ajouterFichiers(UUID idDepot, List<MultipartFile> fichiers) {
        Depot depot = depotRepository.findById(idDepot)
                .orElseThrow(() -> new IllegalArgumentException("Depot introuvable"));

        if (!"BROUILLON".equals(depot.getStatut())) {
            throw new IllegalStateException("Depot deja valide");
        }

        return fichiers.stream().map(f -> {
            var resultat = serviceStockageFichiers.enregistrer(idDepot, f);

            FichierDepot fd = new FichierDepot();
            fd.setDepot(depot);
            fd.setNomOriginal(f.getOriginalFilename() == null ? "fichier" : f.getOriginalFilename());
            fd.setTypeMime(f.getContentType() == null ? "application/octet-stream" : f.getContentType());
            fd.setTaille(f.getSize());
            fd.setCheminStockage(resultat.chemin());
            fd.setEmpreinteSha256(resultat.sha256());

            fichierDepotRepository.save(fd);
            return fd.getId();
        }).toList();
    }

    public UUID validerDepot(UUID idDepot) {
        Depot depot = depotRepository.findById(idDepot)
                .orElseThrow(() -> new IllegalArgumentException("Depot introuvable"));

        if (!"BROUILLON".equals(depot.getStatut())) {
            throw new IllegalStateException("Depot deja valide");
        }

        depot.setStatut("VALIDE");
        depot.setValideLe(Instant.now());
        depotRepository.save(depot);

        return depot.getId();
    }
}
