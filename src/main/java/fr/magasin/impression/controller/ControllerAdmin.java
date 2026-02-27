package fr.magasin.impression.controller;

import fr.magasin.impression.repository.DepotRepository;
import fr.magasin.impression.repository.FichierDepotRepository;
import fr.magasin.impression.model.FichierDepot;
import fr.magasin.impression.service.ServiceCodeDepot;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class ControllerAdmin {

    private final ServiceCodeDepot serviceCodeDepot;
    private final DepotRepository depotRepository;
    private final FichierDepotRepository fichierDepotRepository;

    public ControllerAdmin(ServiceCodeDepot serviceCodeDepot,
                           DepotRepository depotRepository,
                           FichierDepotRepository fichierDepotRepository) {
        this.serviceCodeDepot = serviceCodeDepot;
        this.depotRepository = depotRepository;
        this.fichierDepotRepository = fichierDepotRepository;
    }

    @GetMapping("/code-courant")
    public ReponseCodeCourant codeCourant() {
        return new ReponseCodeCourant(serviceCodeDepot.obtenirCodeCourant());
    }

    @GetMapping("/depots/{idDepot}")
    public ReponseDepotAdmin depot(@PathVariable UUID idDepot) {
        var depot = depotRepository.findById(idDepot)
                .orElseThrow(() -> new IllegalArgumentException("Depot introuvable"));
        List<FichierDepot> fichiers = fichierDepotRepository.findByDepot_Id(idDepot);

        List<ReponseFichier> liste = fichiers.stream()
                .map(f -> new ReponseFichier(f.getId(), f.getNomOriginal(), f.getTypeMime(), f.getTaille()))
                .toList();

        return new ReponseDepotAdmin(depot.getId(), depot.getStatut(), liste);
    }

    @GetMapping("/depots/{idDepot}/fichiers/{idFichier}/telechargement")
    public ResponseEntity<byte[]> telecharger(@PathVariable UUID idDepot, @PathVariable UUID idFichier) {
        FichierDepot fichier = fichierDepotRepository.findById(idFichier)
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable"));

        if (!fichier.getDepot().getId().equals(idDepot)) {
            throw new IllegalArgumentException("Fichier ne correspond pas au depot");
        }

        byte[] contenu = fichier.getContenu();
        if (contenu == null || contenu.length == 0) {
            throw new IllegalStateException("Contenu du fichier absent en base");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichier.getNomOriginal() + "\"")
                .contentType(MediaType.parseMediaType(fichier.getTypeMime()))
                .contentLength(contenu.length)
                .body(contenu);
    }

    public record ReponseCodeCourant(String code) {}
    public record ReponseDepotAdmin(UUID idDepot, String statut, List<ReponseFichier> fichiers) {}
    public record ReponseFichier(UUID idFichier, String nom, String typeMime, long taille) {}
}