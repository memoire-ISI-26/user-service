package com.financedomain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financedomain.user.bean.Client;
import com.financedomain.user.dto.ClientRequest;
import com.financedomain.user.dto.PasswordUpdateRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.exception.AccountAlreadyExistException;
import com.financedomain.user.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @Mock
    private Environment environment;

    @InjectMocks
    private ClientController clientController;

    private ObjectMapper objectMapper;
    private Client sampleClient;
    private ClientRequest clientRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
        objectMapper = new ObjectMapper();

        lenient().when(environment.getProperty("local.server.port", "unknown")).thenReturn("8081");

        sampleClient = new Client();
        sampleClient.setId(10L);
        sampleClient.setFirstName("Mamadou");
        sampleClient.setLastName("Diallo");
        sampleClient.setNumber("771234567");
        sampleClient.setBirthdate("1995-05-15");
        sampleClient.setRole(TypeRole.CLIENT);

        clientRequest = new ClientRequest();
        clientRequest.setFirstName("Mamadou");
        clientRequest.setLastName("Diallo");
        clientRequest.setNumber("771234567");
        clientRequest.setPassword("pass123");
        clientRequest.setBirthdate("1995-05-15");
    }

    @Test
    @DisplayName("Devrait créer un client et retourner 201 Created avec ApiResponse")
    void shouldCreateClientAndReturn201() throws Exception {
        when(clientService.createClient(any(ClientRequest.class))).thenReturn(sampleClient);

        mockMvc.perform(post("/users/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.number").value("771234567"));
    }

    @Test
    @DisplayName("Devrait retourner 409 Conflict si le numéro de téléphone existe déjà")
    void shouldReturn409ConflictWhenClientNumberExists() throws Exception {
        when(clientService.createClient(any(ClientRequest.class)))
                .thenThrow(new AccountAlreadyExistException("Numéro déjà utilisé"));

        mockMvc.perform(post("/users/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Devrait retourner 401 Unauthorized pour getAllClients sans rôle")
    void shouldReturn401WhenRoleIsNull() throws Exception {
        mockMvc.perform(get("/users/client/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Devrait retourner 403 Forbidden si un CLIENT demande la liste de tous les clients")
    void shouldReturn403WhenRoleIsClient() throws Exception {
        mockMvc.perform(get("/users/client/list")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait retourner 200 OK avec la liste des clients pour un ADMINISTRATOR")
    void shouldReturn200OKWhenRoleIsAdmin() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of(sampleClient));

        mockMvc.perform(get("/users/client/list")
                        .header("X-User-Role", "ADMINISTRATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Devrait autoriser un CLIENT à consulter son propre profil par ID (200 OK)")
    void shouldReturnClientByIdWhenClientRequestsOwnProfile() throws Exception {
        when(clientService.getClientById(10L)).thenReturn(Optional.of(sampleClient));

        mockMvc.perform(get("/users/client/10")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.number").value("771234567"));
    }

    @Test
    @DisplayName("Devrait retourner 403 Forbidden si un CLIENT demande le profil d'un autre client par ID")
    void shouldReturn403WhenClientRequestsOtherProfile() throws Exception {
        mockMvc.perform(get("/users/client/99")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait retourner le bean Client directement sans enveloppe ApiResponse pour le rôle INTERNAL")
    void shouldReturnRawClientForInternalRole() throws Exception {
        when(clientService.getClientByNumber("771234567")).thenReturn(Optional.of(sampleClient));

        mockMvc.perform(get("/users/client/number/771234567")
                        .header("X-User-Role", "INTERNAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("771234567"));
    }

    @Test
    @DisplayName("Devrait supprimer un client (204 No Content)")
    void shouldDeleteClientAndReturn204() throws Exception {
        doNothing().when(clientService).deleteClient(10L);

        mockMvc.perform(delete("/users/client/10")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Devrait mettre à jour le mot de passe d'un client (200 OK)")
    void shouldUpdatePasswordAndReturn200() throws Exception {
        PasswordUpdateRequest passReq = new PasswordUpdateRequest("old", "new");
        doNothing().when(clientService).updatePassword(eq(10L), any(PasswordUpdateRequest.class));

        mockMvc.perform(put("/users/client/password")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passReq)))
                .andExpect(status().isOk());
    }
}
