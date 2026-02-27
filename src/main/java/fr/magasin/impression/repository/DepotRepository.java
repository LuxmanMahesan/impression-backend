package fr.magasin.impression.repository;

import fr.magasin.impression.model.Depot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepotRepository extends JpaRepository<Depot, UUID> {

    Optional<Depot> findByCodePublic(String codePublic);

    boolean existsByCodePublic(String codePublic);

    List<Depot> findAllByOrderByCreeLeDesc();

    /** Pour la purge auto : dépôts créés avant la date limite */
    List<Depot> findByCreeLeBeforeOrderByCreeLeAsc(Instant limite);
}