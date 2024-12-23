package org.event.gestionevenement.service;

import org.event.gestionevenement.Repository.EvenementRepository;
import org.event.gestionevenement.Repository.ParticipationRepository;
import org.event.gestionevenement.Repository.UtilisateurRepository;
import org.event.gestionevenement.entities.Evenement;
import org.event.gestionevenement.entities.Participation;
import org.event.gestionevenement.entities.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvenementServiceImpl implements EvenementService {
    @Autowired
    private EvenementRepository evenementRepository;
    private UtilisateurRepository utilisateurRepository;
    private ParticipationRepository participationRepository;

    @Override
    public Evenement addEvenement(Evenement evenement) {
        if (evenement.getTitre() == null || evenement.getTitre().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (evenement.getLieu() == null || evenement.getLieu().isEmpty()) {
            throw new IllegalArgumentException("Le lieu est obligatoire");
        }
        if (evenement.getDescription() == null || evenement.getDescription().isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        if (evenement.getPrix()==0|| evenement.getPrix()<0) {
            throw new IllegalArgumentException("Le prix doit etre superieur a 0");
        }
        if (evenement.getCapacite()==0|| evenement.getCapacite()<0) {
            throw new IllegalArgumentException("La capacite doit etre superieur a 0");
        }
        // Validation de la date (si la date est avant la date actuelle)
        if (evenement.getDate() == null) {
            throw new IllegalArgumentException("La date est obligatoire");
        } else if (evenement.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de evenement ne peut pas etre dans le passe.");
        }


        return evenementRepository.save(evenement);
    }

    @Override
    public Evenement updateEvenement(Evenement evenement) {
        Optional<Evenement> existingEvenement = evenementRepository.findById(evenement.getId());
        if (existingEvenement.isPresent()) {
            Evenement evenementToUpdate = existingEvenement.get();
            evenementToUpdate.setTitre(evenement.getTitre());
            evenementToUpdate.setDescription(evenement.getDescription());
            evenementToUpdate.setLieu(evenement.getLieu());
            evenementToUpdate.setDate(evenement.getDate());
            evenementToUpdate.setCapacite(evenement.getCapacite());
            evenementToUpdate.setType(evenement.getType());
            evenementToUpdate.setPrix(evenement.getPrix());
            return evenementRepository.save(evenementToUpdate);
        } else {
            throw new RuntimeException("Événement non trouvé avec l'ID : " + evenement.getId());
        }
    }


    @Override
    public Evenement getEvenement(int id) {
        return evenementRepository.findById(id).get();
    }

    @Override
    public List<Evenement> getAllEvenement() {
        System.out.println(evenementRepository.findAll());
        return evenementRepository.findAll();
    }

    @Override
    public void deleteEvenement(int id) {
        if (evenementRepository.existsById(id)) {
            evenementRepository.deleteById(id);
        }
    }

    // Récupérer les événements créés par un utilisateur spécifique
    public List<Evenement> findByUser(Utilisateur user) {
        return evenementRepository.findByUser(user);  // Utiliser la méthode de repository pour filtrer par utilisateur
    }


   }




