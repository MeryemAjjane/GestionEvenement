package org.event.gestionevenement.controller;

import jakarta.validation.Valid;
import org.event.gestionevenement.Repository.EvenementRepository;
import org.event.gestionevenement.Repository.PaiementRepository;
import org.event.gestionevenement.entities.Evaluation;
import org.event.gestionevenement.entities.Evenement;
import org.event.gestionevenement.entities.Paiement;
import org.event.gestionevenement.entities.Utilisateur;
import org.event.gestionevenement.service.EvaluationService;
import org.event.gestionevenement.service.EvenementServiceImpl;
import org.event.gestionevenement.service.ParticipationService;
import org.event.gestionevenement.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class EvenementController {

    @Autowired
    private EvenementServiceImpl evenementService;
    @Autowired
    private UserDetailServiceImpl userDetailServiceImpl;
    @Autowired
    private ParticipationService participationService;

    @Autowired
    private EvenementRepository eventRep;
    @Autowired
    private PaiementRepository paiementRepository;
    @Autowired
    private EvaluationService evaluationService;


    // Afficher form event
    @GetMapping("/ajoutevent")
    public String afficherFormulaireAjout(Model model) {
        // Récupérer l'utilisateur connecté à partir de Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Récupère le nom d'utilisateur (user connecté)


        model.addAttribute("username", username);
        model.addAttribute("evenement", new Evenement());
        return "ajoutevent";
    }

    // submit event
    @PostMapping("/addevent")
    public String ajouterEvenement(@Valid @ModelAttribute Evenement evenement, Model model, BindingResult bindingResult) {
        try {
            // Récupérer l'utilisateur connecté à partir de Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();  // Récupère le nom d'utilisateur (user connecté)


            model.addAttribute("username", username);
            // Trouver l'utilisateur connecté
            Utilisateur user = userDetailServiceImpl.findByUsername(username);  // Assurez-vous d'avoir ce service
            model.addAttribute("user", user);
            // Associer l'événement à l'utilisateur connecté
            evenement.setUser(user);  // Assurez-vous que l'événement a un attribut "user" pour lier l'utilisateur

            // Ajouter l'événement à la base de données
            evenementService.addEvenement(evenement);

            return "redirect:/";  // Rediriger après l'ajout
    } catch(IllegalArgumentException e)

    {
        // Ajouter le message d'erreur pour l'afficher dans le formulaire
        model.addAttribute("errorMessage", e.getMessage());
        return "ajoutevent"; // Recharger la page avec les erreurs
    }
}

    // Afficher form edit event
    @GetMapping("/showeditevent/{id}")
    public String afficherFormulaireModification(@PathVariable int id, Model model) {
        // Récupérer l'utilisateur connecté à partir de Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Récupère le nom d'utilisateur (user connecté)


        model.addAttribute("username", username);
        Evenement evenement = evenementService.getEvenement(id);
        model.addAttribute("evenement", evenement);
        return "editevent";
    }

    // submit modification event
    @PostMapping("/editevent/{id}")
    public String modifierEvenement(@ModelAttribute("evenement") Evenement evenement, RedirectAttributes redirectAttributes) {
        try {
            evenementService.updateEvenement(evenement);
            redirectAttributes.addFlashAttribute("success", "L'événement a été modifié avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de la modification de l'événement.");
        }
        return "redirect:/";
    }

    // supprimer evenement
    @GetMapping("/deleteevent/{id}")
    public String deleteEvent(@PathVariable int id, RedirectAttributes redirectAttributes) {
        List<Utilisateur> participation= participationService.getParticipantsByEventId(id);
        Evenement evenement =evenementService.getEvenement(id);
        if (evenement != null && !participation.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vous ne pouvez pas supprimer un événement avec des participants.");
            return "redirect:/";
        }
        evenementService.deleteEvenement(id);
        redirectAttributes.addFlashAttribute("success", "L'événement a été supprimé avec succès.");
        return "redirect:/";
    }
    @GetMapping("/participants/{id}")
    public String afficherParticipants(@PathVariable int id, Model model) {
        // Récupérer l'utilisateur connecté à partir de Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Récupère le nom d'utilisateur (user connecté)


        model.addAttribute("username", username);
        Optional<Evenement> evenementOpt = eventRep.findById(id);
        if (evenementOpt.isEmpty()) {
            return "redirect:/admin?error=eventNotFound";
        }
        Evenement evenement = evenementOpt.get();
        List<Utilisateur> participants = participationService.getParticipantsByEventId(id);
        model.addAttribute("participants", participants);
        model.addAttribute("id", id); // pour afficher l'ID de l'événement si nécessaire
        model.addAttribute("eventCapacity", evenement.getCapacite());
        model.addAttribute("evenementtitle", evenement.getTitre());
        return "participants"; // Vue pour afficher les participants
    }

    @GetMapping("/waitinglist/{id}")
    public String showWaitingList(@PathVariable int id, Model model) {
        try {
            // Récupérer l'utilisateur connecté à partir de Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); // Récupère le nom d'utilisateur (user connecté)

            model.addAttribute("username", username);

            // Vérifie si l'événement existe
            Optional<Evenement> evenementOpt = eventRep.findById(id);
            if (evenementOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Événement introuvable.");
                return "redirect:/"; // Vue HTML pour afficher les erreurs
            }

            Evenement evenement = evenementOpt.get();

            // Trier la liste d'attente par ordre croissant de date d'enregistrement
            List<Utilisateur> sortedWaitingList = evenement.getWaitingList().stream()
                    .sorted(Comparator.comparing(Utilisateur::getRegistrationDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            model.addAttribute("waitingList", sortedWaitingList);
            model.addAttribute("eventTitle", evenement.getTitre());
            return "waitingList"; // La vue HTML qui affichera la liste d'attente

        } catch (Exception e) {
            // Capture les exceptions et affiche un message d'erreur simple
            model.addAttribute("errorMessage", "Une erreur est survenue lors de la récupération de la liste d'attente.");
            return "redirect:/"; // Vue HTML pour afficher les erreurs
        }
    }

    // Afficher la liste de payement
    @GetMapping("/showpayement")
    public String afficherPayement(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // récupère le nom d'utilisateur (user connecté)
        // Trouver l'utilisateur connecté
        Utilisateur user = userDetailServiceImpl.findByUsername(username); // Trouver l'utilisateur par son nom
        model.addAttribute("username", username);
        List<Paiement> paye=paiementRepository.findAll();
        model.addAttribute("paye", paye);
        return "listepayement";
    }
    @GetMapping("/showevaluation/{id}")
    public String afficherEvaluation(@PathVariable int id,Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // récupère le nom d'utilisateur (user connecté)
        // Trouver l'utilisateur connecté
        Utilisateur user = userDetailServiceImpl.findByUsername(username); // Trouver l'utilisateur par son nom
        model.addAttribute("username", username);
        Optional<Evenement> evenementOpt = eventRep.findById(id);
        Evenement evenement = evenementOpt.get();
        List<Evaluation> evaluations = evaluationService.getEvaluationByEventId(id);
        model.addAttribute("evenementtitle", evenement.getTitre());
        model.addAttribute("evaluations",evaluations);
        return "listevaluation";
    }



}