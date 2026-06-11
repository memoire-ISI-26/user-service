package com.financedomain.user.bean;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrateur")
@PrimaryKeyJoinColumn(name = "id_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Admin extends User {

    @Column(name = "identifiant")
    private String username;
}
