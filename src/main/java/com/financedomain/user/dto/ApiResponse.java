package com.financedomain.user.dto;

import lombok.*;

/**
 * Wrapper générique pour toutes les réponses de l'API.
 * Contient les données métier ainsi que le champ "execution_chain"
 * qui indique le port de l'instance ayant traité la requête.
 * Ce champ permet de vérifier le bon fonctionnement du load balancing :
 * les ports doivent s'alterner à chaque invocation lorsque plusieurs
 * instances sont lancées.
 *
 * @param <T> le type de données retournées
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {

    private T data;
    private String execution_chain;

}
