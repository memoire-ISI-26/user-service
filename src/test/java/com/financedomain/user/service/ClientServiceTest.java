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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletProxy walletProxy;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientRequest clientRequest;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(10L);
        client.setFirstName("Mamadou");
        client.setLastName("Diallo");
        client.setNumber("771234567");
        client.setPassword("encoded_pass");
        client.setBirthdate("1995-05-15");
        client.setRole(TypeRole.CLIENT);

        clientRequest = new ClientRequest();
        clientRequest.setFirstName("Mamadou");
        clientRequest.setLastName("Diallo");
        clientRequest.setNumber("771234567");
        clientRequest.setPassword("pass123");
        clientRequest.setBirthdate("1995-05-15");
    }

    @Test
    @DisplayName("Devrait créer un client et appeler le wallet-service pour la création de compte")
    void shouldCreateClientSuccessfullyAndCreateWalletAccount() {
        when(clientRepository.existsByNumber("771234567")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(walletProxy.createAccount(any(AccountCreationRequest.class))).thenReturn(ResponseEntity.ok().build());

        Client created = clientService.createClient(clientRequest);

        assertNotNull(created);
        assertEquals("771234567", created.getNumber());
        assertEquals(TypeRole.CLIENT, created.getRole());
        verify(clientRepository).save(any(Client.class));
        verify(walletProxy).createAccount(any(AccountCreationRequest.class));
    }

    @Test
    @DisplayName("Devrait lever AccountAlreadyExistException si le numéro est déjà enregistré")
    void shouldThrowAccountAlreadyExistExceptionWhenNumberAlreadyExists() {
        when(clientRepository.existsByNumber("771234567")).thenReturn(true);

        assertThrows(AccountAlreadyExistException.class, () -> clientService.createClient(clientRequest));
        verify(clientRepository, never()).save(any(Client.class));
        verifyNoInteractions(walletProxy);
    }

    @Test
    @DisplayName("Devrait retourner la liste de tous les clients")
    void shouldGetAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(client));

        List<Client> clients = clientService.getAllClients();

        assertEquals(1, clients.size());
        assertEquals("771234567", clients.get(0).getNumber());
    }

    @Test
    @DisplayName("Devrait trouver un client par son ID")
    void shouldGetClientById() {
        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));

        Optional<Client> found = clientService.getClientById(10L);

        assertTrue(found.isPresent());
        assertEquals(10L, found.get().getId());
    }

    @Test
    @DisplayName("Devrait trouver un client par son numéro de téléphone")
    void shouldGetClientByNumber() {
        when(clientRepository.findByNumber("771234567")).thenReturn(Optional.of(client));

        Optional<Client> found = clientService.getClientByNumber("771234567");

        assertTrue(found.isPresent());
        assertEquals("771234567", found.get().getNumber());
    }

    @Test
    @DisplayName("Devrait supprimer un client avec succès")
    void shouldDeleteClientSuccessfully() {
        when(clientRepository.existsById(10L)).thenReturn(true);
        doNothing().when(clientRepository).deleteById(10L);

        assertDoesNotThrow(() -> clientService.deleteClient(10L));
        verify(clientRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Devrait lever NullUserDataException si le client à supprimer n'existe pas")
    void shouldThrowNullUserDataExceptionWhenDeletingNonExistentClient() {
        when(clientRepository.existsById(99L)).thenReturn(false);

        assertThrows(NullUserDataException.class, () -> clientService.deleteClient(99L));
        verify(clientRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Devrait mettre à jour le mot de passe d'un client")
    void shouldUpdateClientPasswordSuccessfully() {
        PasswordUpdateRequest passReq = new PasswordUpdateRequest("old_pass", "new_pass");

        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("old_pass", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.encode("new_pass")).thenReturn("new_encoded_pass");
        when(clientRepository.save(client)).thenReturn(client);

        assertDoesNotThrow(() -> clientService.updatePassword(10L, passReq));
        assertEquals("new_encoded_pass", client.getPassword());
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Devrait lever BadCreationFormatException si l'ancien mot de passe du client est erroné")
    void shouldThrowExceptionWhenUpdatingPasswordWithInvalidOldPassword() {
        PasswordUpdateRequest passReq = new PasswordUpdateRequest("wrong_pass", "new_pass");

        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);

        assertThrows(BadCreationFormatException.class, () -> clientService.updatePassword(10L, passReq));
        verify(clientRepository, never()).save(any(Client.class));
    }
}
