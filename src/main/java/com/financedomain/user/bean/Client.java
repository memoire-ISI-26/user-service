package com.financedomain.user.bean;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "id_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Client extends User {

    @Column(name = "telephone", unique = true, nullable = false, length = 12)
    private String number;

    @Column(name = "date_de_naissance", nullable = false)
    private String birthdate;
}
