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

    /**
     * Liste blanche des types MIME autorisés (fichiers imprimables uniquement).
     */
    private static final Set<String> TYPES_AUTORISES = Set.of(
            // PDF
            "application/pdf",
            // Images
            "image/jpeg",
            "image/png",
            "image/bmp",
            "image/tiff",
            "image/gif",
            "image/webp",
            // Word
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // Excel
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            // PowerPoint
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // Texte brut
            "text/plain",
            // LibreOffice / OpenDocument
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation"
    );

    /**
     * Extensions autorisées (vérification supplémentaire par nom de fichier).
     */
    private static final Set<String> EXTENSIONS_AUTORISEES = Set.of(
            ".pdf", ".jpg", ".jpeg", ".png", ".bmp", ".tiff", ".tif", ".gif", ".webp",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".odt", ".ods", ".odp"
    );

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

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Echec lecture fichier", e);
        }
    }

    private void verifierFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        // Vérifier le type MIME
        String type = fichier.getContentType() == null ? "" : fichier.getContentType().toLowerCase();
        if (!TYPES_AUTORISES.contains(type)) {
            throw new IllegalArgumentException(
                    "Type de fichier non autorisé (" + type + "). " +
                            "Seuls les fichiers imprimables sont acceptés : PDF, images, Word, Excel, PowerPoint."
            );
        }

        // Vérifier l'extension
        String nom = fichier.getOriginalFilename();
        if (nom != null && nom.contains(".")) {
            String extension = nom.substring(nom.lastIndexOf('.')).toLowerCase();
            if (!EXTENSIONS_AUTORISEES.contains(extension)) {
                throw new IllegalArgumentException(
                        "Extension de fichier non autorisée (" + extension + ")."
                );
            }
        }
    }

    public record ResultatStockage(byte[] contenu, String sha256) {}
}