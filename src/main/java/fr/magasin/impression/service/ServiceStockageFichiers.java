package fr.magasin.impression.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;

@Service
public class ServiceStockageFichiers {

    private static final Set<String> TYPES_INTERDITS_PREFIXE = Set.of("video/");

    /**
     * Lit le fichier uploadé, calcule le SHA-256 et renvoie les octets + le hash.
     * Plus aucune écriture sur le disque : tout passe en base.
     */
    public ResultatStockage lireEtHasher(MultipartFile fichier) {
        verifierFichier(fichier);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] contenu;

            try (InputStream in = fichier.getInputStream();
                 DigestInputStream dis = new DigestInputStream(in, digest)) {
                contenu = dis.readAllBytes();
            }

            String sha256 = HexFormat.of().formatHex(digest.digest());
            return new ResultatStockage(contenu, sha256);

        } catch (Exception e) {
            throw new RuntimeException("Echec lecture fichier", e);
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

    public record ResultatStockage(byte[] contenu, String sha256) {}
}