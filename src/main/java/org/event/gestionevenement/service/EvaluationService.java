package org.event.gestionevenement.service;

import org.event.gestionevenement.Repository.EvaluationRepository;
import org.event.gestionevenement.Repository.EvenementRepository;
import org.event.gestionevenement.Repository.UtilisateurRepository;
import org.event.gestionevenement.entities.Evaluation;
import org.event.gestionevenement.entities.Evenement;
import org.event.gestionevenement.entities.Participation;
import org.event.gestionevenement.entities.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvaluationService {
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    EvenementRepository evenementRepository;
    @Autowired
    EvaluationRepository evaluationRepository;
    public boolean AddRating(int rating, int event_id, String user_id) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        Evenement evenement = evenementRepository.findById(event_id).orElse(null);
        Utilisateur utilisateur = utilisateurRepository.findById(String.valueOf(user_id)).orElse(null);
        if (evenement == null || utilisateur == null) {
            return false;
        }
        Optional<Evenement> evenementOpt = evenementRepository.findById(event_id);
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(user_id);

        if (!evenementOpt.isPresent() || !utilisateurOpt.isPresent()) {
            return false;
        }
        Evaluation newEval = Evaluation.builder()
                .utilisateur(utilisateur)
                .evenement(evenement)
                .rating(rating)
                .build();
        evaluationRepository.save(newEval);
        return true;

    }


    public List<Evaluation> getEvaluationByEventId(int id) {
        return evaluationRepository.findByEvenementId(id);
    }
}
