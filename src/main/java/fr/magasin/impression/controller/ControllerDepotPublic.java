package fr.magasin.impression.controller;

import fr.magasin.impression.service.ServiceDepot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/depots")
public class ControllerDepotPublic {

    private static final Logger log = LoggerFactory.getLogger(ControllerDepotPublic.class);

    private final ServiceDepot serviceDepot;

    public ControllerDepotPublic(ServiceDepot serviceDepot) {
        this.serviceDepot = serviceDepot;
    }

    @PostMapping("/demarrer")
    public ReponseDepotDemarre demarrer(@RequestBody RequeteCode requete) {
        log.info(">>> POST /api/depots/demarrer — code reçu : [{}]", requete.code());
        UUID idDepot = serviceDepot.demarrerDepot(requete.code());
        log.info("<<< Dépôt créé : {}", idDepot);
        return new ReponseDepotDemarre(idDepot);
    }

    @PostMapping(value = "/{idDepot}/fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReponseAjoutFichiers ajouterFichiers(
            @PathVariable UUID idDepot,
            @RequestParam("fichiers") List<MultipartFile> fichiers
    ) {
        log.info(">>> POST /api/depots/{}/fichiers — {} fichier(s)", idDepot, fichiers.size());
        List<UUID> ids = serviceDepot.ajouterFichiers(idDepot, fichiers);
        log.info("<<< {} fichier(s) ajouté(s)", ids.size());
        return new ReponseAjoutFichiers(ids);
    }

    @PostMapping("/{idDepot}/valider")
    public ReponseDepotValide valider(@PathVariable UUID idDepot) {
        log.info(">>> POST /api/depots/{}/valider", idDepot);
        UUID id = serviceDepot.validerDepot(idDepot);
        log.info("<<< Dépôt validé : {}", id);
        return new ReponseDepotValide(id);
    }

    public record RequeteCode(String code) {}
    public record ReponseDepotDemarre(UUID idDepot) {}
    public record ReponseAjoutFichiers(List<UUID> idsFichiers) {}
    public record ReponseDepotValide(UUID idDepot) {}
}