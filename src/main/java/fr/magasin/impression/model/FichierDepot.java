package fr.magasin.impression.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fichier_depot")
public class FichierDepot {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(name = "nom_original", nullable = false)
    private String nomOriginal;

    @Column(name = "type_mime", nullable = false)
    private String typeMime;

    @Column(nullable = false)
    private long taille;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    @Column(name = "empreinte_sha256", length = 64)
    private String empreinteSha256;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @PrePersist
    void avantCreation() {
        if (creeLe == null) creeLe = Instant.now();
    }

    public UUID getId() { return id; }

    public Depot getDepot() { return depot; }
    public void setDepot(Depot depot) { this.depot = depot; }

    public String getNomOriginal() { return nomOriginal; }
    public void setNomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public long getTaille() { return taille; }
    public void setTaille(long taille) { this.taille = taille; }

    public String getCheminStockage() { return cheminStockage; }
    public void setCheminStockage(String cheminStockage) { this.cheminStockage = cheminStockage; }

    public String getEmpreinteSha256() { return empreinteSha256; }
    public void setEmpreinteSha256(String empreinteSha256) { this.empreinteSha256 = empreinteSha256; }
}
