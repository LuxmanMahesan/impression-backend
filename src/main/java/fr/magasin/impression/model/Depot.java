package fr.magasin.impression.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "depot")
public class Depot {

    @Id
    @GeneratedValue
    private UUID id;

    /** Code court à 5 caractères affiché au client (ex: "A3K9Z") */
    @Column(name = "code_public", nullable = false, unique = true, length = 5)
    private String codePublic;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "valide_le")
    private Instant valideLe;

    @PrePersist
    void avantCreation() {
        if (creeLe == null) creeLe = Instant.now();
        if (statut == null) statut = "BROUILLON";
    }

    public UUID getId() { return id; }

    public String getCodePublic() { return codePublic; }
    public void setCodePublic(String codePublic) { this.codePublic = codePublic; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Instant getCreeLe() { return creeLe; }
    public Instant getValideLe() { return valideLe; }
    public void setValideLe(Instant valideLe) { this.valideLe = valideLe; }
}