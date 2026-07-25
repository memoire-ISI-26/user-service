package com.financedomain.user.service;

import com.financedomain.user.bean.Client;
import com.financedomain.user.dto.AccountCreationRequest;
import com.financedomain.user.dto.ClientRequest;
import com.financedomain.user.dto.PasswordUpdateRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.exception.AccountAlreadyExistException;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.proxy.WalletProxy;
import com.financedomain.user.repository.ClientRepository;
import com.financedomain.user.dto.TrackingEvent;
import com.financedomain.user.proxy.TrackingProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WalletProxy walletProxy;

    @Autowired
    private TrackingProxy trackingProxy;

    public Client createClient(ClientRequest request) {
        if (clientRepository.existsByNumber(request.getNumber())) {
            throw new AccountAlreadyExistException("Le numéro de téléphone '" + request.getNumber() + "' est déjà utilisé.");
        }

        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setNumber(request.getNumber());
        client.setBirthdate(request.getBirthdate());
        client.setRole(TypeRole.CLIENT);

        Client savedClient = clientRepository.save(client);

        try {
            walletProxy.createAccount(new AccountCreationRequest(
                    savedClient.getId(),
                    savedClient.getNumber(),
                    "XOF"
            ));
        } catch (BadCreationFormatException e) {
            throw new BadCreationFormatException("Échec de la création du compte portefeuille associé dans wallet-service : " + e.getMessage());
        }

        return savedClient;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Cacheable(value = "clients", key = "#id")
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @Cacheable(value = "clientsByNumber", key = "#number")
    public Optional<Client> getClientByNumber(String number) {
        return clientRepository.findByNumber(number);
    }

    @Caching(evict = {
        @CacheEvict(value = "clients", key = "#id"),
        @CacheEvict(value = "clientsByNumber", allEntries = true)
    })
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new NullUserDataException("Client introuvable avec l'id : " + id);
        }
        clientRepository.deleteById(id);
    }

    @Caching(evict = {
        @CacheEvict(value = "clients", key = "#id"),
        @CacheEvict(value = "clientsByNumber", allEntries = true)
    })
    public void updatePassword(Long id, PasswordUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NullUserDataException("Client introuvable avec l'id : " + id));

        if (!passwordEncoder.matches(request.getOldPassword(), client.getPassword())) {
            throw new BadCreationFormatException("L'ancien mot de passe est incorrect.");
        }

        client.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.save(client);

    }

    private void sendTrackingEvent(String eventType, String msisdn, String userId, String userRole, Object payload) {
        try {
            TrackingEvent event = TrackingEvent.builder()
                    .eventType(eventType)
                    .msisdn(msisdn)
                    .userId(userId)
                    .userRole(userRole)
                    .sourceService("user-service")
                    .payload(payload)
                    .timestamp(java.time.Instant.now())
                    .build();
            trackingProxy.collectEvent(event, "INTERNAL");
        } catch (Exception e) {
            System.err.println("Erreur de tracking client: " + e.getMessage());
        }
    }
}
