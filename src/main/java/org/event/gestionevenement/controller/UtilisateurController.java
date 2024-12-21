package org.event.gestionevenement.controller;

import jakarta.validation.Valid;
import org.event.gestionevenement.Repository.RoleRepository;
import org.event.gestionevenement.Repository.UtilisateurRepository;
import org.event.gestionevenement.entities.Role;
import org.event.gestionevenement.entities.Utilisateur;
import org.event.gestionevenement.service.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class UtilisateurController {

    @Autowired
    private CompteService compteServ;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private RoleRepository roleRep;


    @GetMapping("/Inscrire")
    public String showRegisterForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur()); // Crée un nouvel objet utilisateur vide pour le formulaire
        return "register"; // Le nom de la page HTML pour le formulaire (register.html)
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute Utilisateur utilisateur, BindingResult bindingResult,Model model,
            @RequestParam(value = "organisateur", required = false) String isOrganisateur,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,@RequestParam(value = "companyName", required = false) String companyName) {

        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            // Ajouter les erreurs au modèle pour les afficher dans la vue
            model.addAttribute("errors", bindingResult.getAllErrors());
            if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
                model.addAttribute("confirmPasswordError", "La confirmation du mot de passe est different au mot de passe.");
            }
                return "register"; // Retourner à la vue du formulaire
        }
        // Générer un ID unique
        utilisateur.setId(UUID.randomUUID().toString());

        // Récupérer le rôle USER
        Role userRole = roleRep.findByRole("USER");

        // Si organisateur, ajouter ADMIN et enregistrer le nom de l'entreprise
        if (isOrganisateur != null) {
            Role adminRole = roleRep.findByRole("ADMIN");
            utilisateur.setRoles(List.of(userRole, adminRole)); // Attribuer les rôles USER et ADMIN
            utilisateur.setCompanyName(companyName); // Affecter le nom de l'entreprise
        } else {
            utilisateur.setRoles(List.of(userRole)); // Rôle par défaut USER
            utilisateur.setCompanyName(null); // Pas de nom d'entreprise si pas organisateur
        }

        // Sauvegarder l'utilisateur
        compteServ.ajouterUtilisateur(utilisateur);

        return "redirect:/login";
    }


}