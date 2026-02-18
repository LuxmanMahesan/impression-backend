package fr.magasin.impression.controller;

import fr.magasin.impression.service.ServiceDepot;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/depots")
public class ControllerDepotPublic {

    private final ServiceDepot serviceDepot;

    public ControllerDepotPublic(ServiceDepot serviceDepot) {
        this.serviceDepot = serviceDepot;
    }

    @PostMapping("/demarrer")
    public ReponseDepotDemarre demarrer(@RequestBody RequeteCode requete) {
        UUID idDepot = serviceDepot.demarrerDepot(requete.code());
        return new ReponseDepotDemarre(idDepot);
    }

    @PostMapping(value = "/{idDepot}/fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReponseAjoutFichiers ajouterFichiers(
            @PathVariable UUID idDepot,
            @RequestParam("fichiers") List<MultipartFile> fichiers
    ) {
        List<UUID> ids = serviceDepot.ajouterFichiers(idDepot, fichiers);
        return new ReponseAjoutFichiers(ids);
    }

    @PostMapping("/{idDepot}/valider")
    public ReponseDepotValide valider(@PathVariable UUID idDepot) {
        UUID id = serviceDepot.validerDepot(idDepot);
        return new ReponseDepotValide(id);
    }

    public record RequeteCode(String code) {}
    public record ReponseDepotDemarre(UUID idDepot) {}
    public record ReponseAjoutFichiers(List<UUID> idsFichiers) {}
    public record ReponseDepotValide(UUID idDepot) {}
}
