package com.financedomain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountCreationRequest {

    @JsonProperty("id_user")
    private long idUser;

    private String number;
    private String currency;
}
