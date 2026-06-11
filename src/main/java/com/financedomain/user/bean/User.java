package com.financedomain.user.bean;

import com.financedomain.user.enums.TypeRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    protected Long id;

    @Column(name = "prenom")
    protected String firstName;

    @Column(name = "nom")
    protected String lastName;

    @Column(name = "mot_de_passe")
    protected String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private TypeRole role;
}
