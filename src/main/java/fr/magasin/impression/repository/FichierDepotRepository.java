package fr.magasin.impression.repository;

import fr.magasin.impression.model.FichierDepot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FichierDepotRepository extends JpaRepository<FichierDepot, UUID> {
    List<FichierDepot> findByDepot_Id(UUID depotId);
}
