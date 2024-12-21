package org.event.gestionevenement.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
public class Utilisateur {
    @Id
    private String id;
    @Column(unique = true)
    @NotEmpty(message = "Le nom ne peut pas être vide")
    private String username;
    @NotEmpty(message = "Le mot de passe est obligatoire")
    @Size(min = 4, message = "Le mot de passe doit contenir au moins 4 caractères")
    private String password;
    @NotEmpty(message = "Email est obligatoire")
    @Email(message = "Veuillez fournir une adresse e-mail valide")
    private String email;
    private String nomPrenom;
    @Column(length = 14)
    private String numTele;
    @Column(nullable = true, length = 50)
    private String companyName;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;
    private LocalDateTime registrationDate;




}
