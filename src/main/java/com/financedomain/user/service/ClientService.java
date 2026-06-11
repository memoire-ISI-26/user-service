package com.financedomain.user.service;

import com.financedomain.user.bean.Client;
import com.financedomain.user.dto.ClientRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client createClient(ClientRequest request) {
        if (clientRepository.existsByNumber(request.getNumber())) {
            throw new IllegalArgumentException("Le numéro de téléphone '" + request.getNumber() + "' est déjà utilisé.");
        }

        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPassword(request.getPassword());
        client.setNumber(request.getNumber());
        client.setBirthdate(request.getBirthdate());
        client.setRole(TypeRole.CLIENT);

        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> getClientByNumber(String number) {
        return clientRepository.findByNumber(number);
    }

    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Client introuvable avec l'id : " + id);
        }
        clientRepository.deleteById(id);
    }
}
