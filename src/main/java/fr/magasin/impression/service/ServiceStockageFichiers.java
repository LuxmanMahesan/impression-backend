package fr.magasin.impression.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
public class ServiceStockageFichiers {

    private static final Set<String> TYPES_INTERDITS_PREFIXE = Set.of("video/");

    @Value("${app.stockage.racine:stockage}")
    private String racine;

    public ResultatStockage enregistrer(UUID idDepot, MultipartFile fichier) {
        verifierFichier(fichier);

        String nomOriginal = fichier.getOriginalFilename() == null ? "fichier" : fichier.getOriginalFilename();
        String nomNettoye = nomOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");

        UUID idFichier = UUID.randomUUID();
        Path dossierDepot = Paths.get(racine, "depots", idDepot.toString());
        Path destination = dossierDepot.resolve(idFichier + "_" + nomNettoye);

        try {
            Files.createDirectories(dossierDepot);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = fichier.getInputStream();
                 DigestInputStream dis = new DigestInputStream(in, digest)) {
                Files.copy(dis, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            String sha256 = HexFormat.of().formatHex(digest.digest());
            return new ResultatStockage(destination.toAbsolutePath().toString(), sha256);

        } catch (Exception e) {
            throw new RuntimeException("Echec stockage fichier", e);
        }
    }

    private void verifierFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }
        String type = fichier.getContentType() == null ? "" : fichier.getContentType();
        for (String prefixe : TYPES_INTERDITS_PREFIXE) {
            if (type.startsWith(prefixe)) {
                throw new IllegalArgumentException("Type de fichier interdit");
            }
        }
    }

    public record ResultatStockage(String chemin, String sha256) {}
}
