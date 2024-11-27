package org.event.gestionevenement.controller;

import org.event.gestionevenement.Repository.EvenementRepository;
import org.event.gestionevenement.Repository.ParticipationRepository;
import org.event.gestionevenement.entities.Evenement;
import org.event.gestionevenement.entities.Participation;
import org.event.gestionevenement.entities.Utilisateur;
import org.event.gestionevenement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class ParticipationController {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private UserDetailServiceImpl userService;
    @Autowired
    EmailService emailService;
    @Autowired
    EvaluationService evaluationService;
    @Autowired
    UserDetailServiceImpl userDetailService;
    @Autowired
    private EvenementServiceImpl evenementServiceImpl;

    //    @PostMapping("/participer/{id}")
//    public String participer(@PathVariable int id) {
//        try {
//            // Récupérer l'utilisateur connecté
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            Utilisateur user = userService.findByUsername(username);
//
//            // Vérifier si l'événement existe
//            Optional<Evenement> evenementOpt = evenementRepository.findById(id);
//            if (evenementOpt.isEmpty()) {
//                return "redirect:/?error=eventNotFound";
//            }
//            Evenement evenement = evenementOpt.get();
//
//            // Vérifier si l'utilisateur a déjà participé
//            if (participationRepository.existsByUserAndEvenement(user, evenement)) {
//                return "redirect:/?error=alreadyParticipated";
//            }
//
//            // Vérifier la capacité
//            if (evenement.getCapacite() <= 0) {
//                return "redirect:/?error=capacityReached";
//            }
//
//            // Ajouter la participation
//            Participation participation = new Participation();
//            participation.setUser(user);
//            participation.setEvenement(evenement);
//            participationRepository.save(participation);
//
//            // Réduire la capacité
//            evenement.setCapacite(evenement.getCapacite() - 1);
//            evenementRepository.save(evenement);
//
//            // Envoi d'email de confirmation
//            String subject = "Confirmation de participation";
//            String body = "Bonjour " + user.getUsername() + ",\n\n" +
//                    "Votre participation à l'événement '" + evenement.getTitre() + "' a été confirmée.";
//            emailService.sendEmail(user.getEmail(), subject, body);
//
//            return "redirect:/?success=participationAdded";
//        } catch (Exception e) {
//            e.printStackTrace(); // Log de l'erreur pour le débogage
//            return "redirect:/?error=internalError";
//        }
//    }
// Afficher form event
@GetMapping("/pageevaluate/{id}")
public String afficherFormulaireAjoutevale(@PathVariable int id, Model model) {
        // Récupérer l'utilisateur connecté à partir de Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Récupère le nom d'utilisateur (user connecté)
        model.addAttribute("username", username);
        Evenement evenement = evenementServiceImpl.getEvenement(id);
        model.addAttribute("evenement", evenement);
        return "evaluation";
    }
    @PostMapping("/waitingList/{id}")
    public String addToWaitingList(@PathVariable int id) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Utilisateur user = userService.findByUsername(username);
        user.setRegistrationDate(LocalDateTime.now());

        // Vérifier si l'événement existe
        Optional<Evenement> evenementOpt = evenementRepository.findById(id);
        if (evenementOpt.isEmpty()) {
            return "redirect:/?error=evenementNotFound";
        }
        Evenement evenement = evenementOpt.get();

        // Vérifier si la capacité est pleine
        if (evenement.getCapacite() > 0) {
            return "redirect:/?error=eventNotFull";
        }

        // Vérifier si l'utilisateur a déjà participé à l'événement
        if (participationRepository.existsByUserAndEvenement(user, evenement)) {
            return "redirect:/?error=alreadyParticipated";
        }

        // Vérifier si l'utilisateur est déjà en liste d'attente
        if (evenement.getWaitingList().contains(user)) {
            return "redirect:/?error=alreadyInWaitingList";
        }

        // Ajouter l'utilisateur à la liste d'attente
        evenement.getWaitingList().add(user);
        evenementRepository.save(evenement);

        return "redirect:/?success=waitingListAdded";
    }
    @PostMapping("/rate")
    public String rateEvent(@RequestParam(name = "id") int id,Model model,
                            @RequestParam(name = "rating") int rating,
                            RedirectAttributes redirectAttributes) {
        System.out.println("eval" + rating + " id " + id);

        // Récupérer l'utilisateur actuellement authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Nom d'utilisateur (email, username, etc.)

        // Récupérer l'utilisateur depuis votre service ou repository
        Utilisateur utilisateur =userDetailService.findByUsername(username);
        if (utilisateur == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
            return "redirect:/";
        }
        // Ajouter la notation
        boolean response = evaluationService.AddRating(rating, id, utilisateur.getId());
        if (response) {
            redirectAttributes.addFlashAttribute("successMessage", "Rating added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add rating.");
        }

        return "redirect:/";
    }

}

