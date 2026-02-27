package fr.magasin.impression.service;

import fr.magasin.impression.repository.DepotRepository;
import fr.magasin.impression.model.Depot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Purge automatique des dépôts de plus de 30 minutes.
 * Les fichiers associés sont supprimés en cascade (ON DELETE CASCADE).
 */
@Service
public class ServicePurgeDepots {

    private static final Logger log = LoggerFactory.getLogger(ServicePurgeDepots.class);
    private static final int DUREE_VIE_MINUTES = 30;

    private final DepotRepository depotRepository;

    public ServicePurgeDepots(DepotRepository depotRepository) {
        this.depotRepository = depotRepository;
    }

    /**
     * Exécuté toutes les 5 minutes.
     * Supprime tous les dépôts créés il y a plus de 30 min.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // toutes les 5 min
    @Transactional
    public void purgerDepotsExpires() {
        Instant limite = Instant.now().minus(DUREE_VIE_MINUTES, ChronoUnit.MINUTES);
        List<Depot> expires = depotRepository.findByCreeLeBeforeOrderByCreeLeAsc(limite);

        if (expires.isEmpty()) return;

        log.info("Purge auto : {} dépôt(s) de plus de {} min", expires.size(), DUREE_VIE_MINUTES);
        for (Depot d : expires) {
            log.info("  → Suppression dépôt {} (créé le {})", d.getCodePublic(), d.getCreeLe());
        }

        depotRepository.deleteAll(expires);
    }
}