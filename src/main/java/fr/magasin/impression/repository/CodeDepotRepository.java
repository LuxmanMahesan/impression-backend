package fr.magasin.impression.repository;

import fr.magasin.impression.model.CodeDepot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeDepotRepository extends JpaRepository<CodeDepot, Short> {
}
