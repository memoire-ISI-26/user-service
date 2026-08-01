package com.financedomain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financedomain.user.bean.Admin;
import com.financedomain.user.dto.AdminRequest;
import com.financedomain.user.dto.PasswordUpdateRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper;
    private Admin sampleAdmin;
    private AdminRequest adminRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();

        sampleAdmin = new Admin();
        sampleAdmin.setId(1L);
        sampleAdmin.setFirstName("Super");
        sampleAdmin.setLastName("Admin");
        sampleAdmin.setUsername("superadmin");
        sampleAdmin.setRole(TypeRole.ADMINISTRATOR);

        adminRequest = new AdminRequest();
        adminRequest.setFirstName("Super");
        adminRequest.setLastName("Admin");
        adminRequest.setUsername("superadmin");
        adminRequest.setPassword("pass123");
    }

    @Test
    @DisplayName("Devrait créer un administrateur (201 Created)")
    void shouldCreateAdminAndReturn201() throws Exception {
        when(adminService.createAdmin(any(AdminRequest.class))).thenReturn(sampleAdmin);

        mockMvc.perform(post("/users/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("superadmin"));
    }

    @Test
    @DisplayName("Devrait retourner 409 Conflict si le nom d'utilisateur administrateur existe déjà")
    void shouldReturn409ConflictWhenAdminUsernameExists() throws Exception {
        when(adminService.createAdmin(any(AdminRequest.class)))
                .thenThrow(new BadCreationFormatException("Identifiant déjà utilisé"));

        mockMvc.perform(post("/users/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Devrait retourner 401 Unauthorized pour getAllAdmins sans rôle")
    void shouldReturn401WhenRoleIsNull() throws Exception {
        mockMvc.perform(get("/users/admin/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Devrait retourner 403 Forbidden pour getAllAdmins si le rôle n'est pas ADMINISTRATOR")
    void shouldReturn403WhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/users/admin/list")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait retourner 200 OK avec la liste des administrateurs pour le rôle ADMINISTRATOR")
    void shouldReturn200OKWhenRoleIsAdmin() throws Exception {
        when(adminService.getAllAdmins()).thenReturn(List.of(sampleAdmin));

        mockMvc.perform(get("/users/admin/list")
                        .header("X-User-Role", "ADMINISTRATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Devrait retourner un administrateur par ID (200 OK)")
    void shouldReturnAdminById() throws Exception {
        when(adminService.getAdminById(1L)).thenReturn(Optional.of(sampleAdmin));

        mockMvc.perform(get("/users/admin/1")
                        .header("X-User-Role", "ADMINISTRATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("superadmin"));
    }

    @Test
    @DisplayName("Devrait retourner 404 Not Found si l'administrateur n'existe pas")
    void shouldReturn404WhenAdminNotFound() throws Exception {
        when(adminService.getAdminById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/admin/99")
                        .header("X-User-Role", "ADMINISTRATOR"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Devrait autoriser la recherche d'administrateur par username pour le rôle INTERNAL")
    void shouldReturnAdminByUsernameForInternalRole() throws Exception {
        when(adminService.getAdminByUsername("superadmin")).thenReturn(Optional.of(sampleAdmin));

        mockMvc.perform(get("/users/admin/username/superadmin")
                        .header("X-User-Role", "INTERNAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("superadmin"));
    }

    @Test
    @DisplayName("Devrait supprimer un administrateur (204 No Content)")
    void shouldDeleteAdminAndReturn204() throws Exception {
        doNothing().when(adminService).deleteAdmin(1L);

        mockMvc.perform(delete("/users/admin/1")
                        .header("X-User-Role", "ADMINISTRATOR"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Devrait mettre à jour le mot de passe de l'administrateur connecté (200 OK)")
    void shouldUpdatePasswordSuccessfully() throws Exception {
        PasswordUpdateRequest passReq = new PasswordUpdateRequest("old", "new");
        doNothing().when(adminService).updatePassword(eq(1L), any(PasswordUpdateRequest.class));

        mockMvc.perform(put("/users/admin/password")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passReq)))
                .andExpect(status().isOk());
    }
}
