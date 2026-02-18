package fr.magasin.impression.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "code_depot")
public class CodeDepot {

    @Id
    private Short id;

    @Column(nullable = false, length = 20)
    private String valeur;

    @Column(name = "date_generation", nullable = false)
    private LocalDate dateGeneration;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }

    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }

    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }
}
