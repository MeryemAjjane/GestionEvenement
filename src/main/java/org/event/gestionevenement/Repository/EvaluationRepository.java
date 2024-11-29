package org.event.gestionevenement.Repository;

import org.event.gestionevenement.entities.Evaluation;
import org.event.gestionevenement.entities.Evenement;
import org.event.gestionevenement.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    List<Evaluation> findByEvenementId(int evenementId);
    boolean existsByUtilisateurAndEvenement(Utilisateur utilisateur, Evenement evenement);
}
