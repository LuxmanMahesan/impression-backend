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
        String codePublic = serviceDepot.demarrerDepot(requete.code());
        log.info("<<< Dépôt créé : {}", codePublic);
        return new ReponseDepotDemarre(codePublic);
    }

    @PostMapping(value = "/{codePublic}/fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReponseAjoutFichiers ajouterFichiers(
            @PathVariable String codePublic,
            @RequestParam("fichiers") List<MultipartFile> fichiers
    ) {
        log.info(">>> POST /api/depots/{}/fichiers — {} fichier(s)", codePublic, fichiers.size());
        List<UUID> ids = serviceDepot.ajouterFichiers(codePublic, fichiers);
        log.info("<<< {} fichier(s) ajouté(s)", ids.size());
        return new ReponseAjoutFichiers(ids);
    }

    @PostMapping("/{codePublic}/valider")
    public ReponseDepotValide valider(@PathVariable String codePublic) {
        log.info(">>> POST /api/depots/{}/valider", codePublic);
        String code = serviceDepot.validerDepot(codePublic);
        log.info("<<< Dépôt validé : {}", code);
        return new ReponseDepotValide(code);
    }

    public record RequeteCode(String code) {}
    public record ReponseDepotDemarre(String idDepot) {}
    public record ReponseAjoutFichiers(List<UUID> idsFichiers) {}
    public record ReponseDepotValide(String idDepot) {}
}