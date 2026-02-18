package fr.magasin.impression.repository;

import fr.magasin.impression.model.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DepotRepository extends JpaRepository<Depot, UUID> { }
