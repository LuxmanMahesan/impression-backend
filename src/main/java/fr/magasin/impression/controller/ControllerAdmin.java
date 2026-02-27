package fr.magasin.impression.controller;

import fr.magasin.impression.repository.DepotRepository;
import fr.magasin.impression.repository.FichierDepotRepository;
import fr.magasin.impression.model.Depot;
import fr.magasin.impression.model.FichierDepot;
import fr.magasin.impression.service.ServiceCodeDepot;
import fr.magasin.impression.service.ServiceDepot;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class ControllerAdmin {

    private final ServiceCodeDepot serviceCodeDepot;
    private final ServiceDepot serviceDepot;
    private final DepotRepository depotRepository;
    private final FichierDepotRepository fichierDepotRepository;

    public ControllerAdmin(ServiceCodeDepot serviceCodeDepot,
                           ServiceDepot serviceDepot,
                           DepotRepository depotRepository,
                           FichierDepotRepository fichierDepotRepository) {
        this.serviceCodeDepot = serviceCodeDepot;
        this.serviceDepot = serviceDepot;
        this.depotRepository = depotRepository;
        this.fichierDepotRepository = fichierDepotRepository;
    }

    @GetMapping("/code-courant")
    public ReponseCodeCourant codeCourant() {
        return new ReponseCodeCourant(serviceCodeDepot.obtenirCodeCourant());
    }

    /** Liste tous les dépôts (plus récent d'abord) */
    @GetMapping("/depots")
    public List<ReponseDepotResume> listerDepots() {
        return serviceDepot.listerTousLesDepots().stream()
                .map(d -> {
                    long nbFichiers = fichierDepotRepository.findByDepot_Id(d.getId()).size();
                    return new ReponseDepotResume(
                            d.getCodePublic(),
                            d.getStatut(),
                            d.getCreeLe(),
                            d.getValideLe(),
                            nbFichiers
                    );
                })
                .toList();
    }

    /** Détail d'un dépôt par code public */
    @GetMapping("/depots/{codePublic}")
    public ReponseDepotAdmin depot(@PathVariable String codePublic) {
        Depot depot = depotRepository.findByCodePublic(codePublic.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Depot introuvable"));

        List<FichierDepot> fichiers = fichierDepotRepository.findByDepot_Id(depot.getId());

        List<ReponseFichier> liste = fichiers.stream()
                .map(f -> new ReponseFichier(f.getId(), f.getNomOriginal(), f.getTypeMime(), f.getTaille()))
                .toList();

        return new ReponseDepotAdmin(depot.getCodePublic(), depot.getStatut(), liste);
    }

    @GetMapping("/depots/{codePublic}/fichiers/{idFichier}/telechargement")
    public ResponseEntity<byte[]> telecharger(@PathVariable String codePublic, @PathVariable UUID idFichier) {
        Depot depot = depotRepository.findByCodePublic(codePublic.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Depot introuvable"));

        FichierDepot fichier = fichierDepotRepository.findById(idFichier)
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable"));

        if (!fichier.getDepot().getId().equals(depot.getId())) {
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

    /** Supprime un dépôt et tous ses fichiers */
    @DeleteMapping("/depots/{codePublic}")
    public ReponseSuppressionDepot supprimer(@PathVariable String codePublic) {
        serviceDepot.supprimerDepot(codePublic);
        return new ReponseSuppressionDepot(codePublic, "Dépôt supprimé");
    }

    public record ReponseCodeCourant(String code) {}
    public record ReponseDepotResume(String codePublic, String statut, Instant creeLe, Instant valideLe, long nbFichiers) {}
    public record ReponseDepotAdmin(String idDepot, String statut, List<ReponseFichier> fichiers) {}
    public record ReponseFichier(UUID idFichier, String nom, String typeMime, long taille) {}
    public record ReponseSuppressionDepot(String codePublic, String message) {}
}