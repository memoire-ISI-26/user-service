package com.financedomain.user.service;

import com.financedomain.user.bean.Admin;
import com.financedomain.user.dto.AdminRequest;
import com.financedomain.user.dto.PasswordUpdateRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin admin;
    private AdminRequest adminRequest;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setId(1L);
        admin.setFirstName("Admin");
        admin.setLastName("System");
        admin.setUsername("admin1");
        admin.setPassword("encoded_pass");
        admin.setRole(TypeRole.ADMINISTRATOR);

        adminRequest = new AdminRequest();
        adminRequest.setFirstName("Admin");
        adminRequest.setLastName("System");
        adminRequest.setUsername("admin1");
        adminRequest.setPassword("pass123");
    }

    @Test
    @DisplayName("Devrait créer un administrateur avec succès")
    void shouldCreateAdminSuccessfully() {
        when(adminRepository.existsByUsername("admin1")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        Admin created = adminService.createAdmin(adminRequest);

        assertNotNull(created);
        assertEquals("admin1", created.getUsername());
        assertEquals(TypeRole.ADMINISTRATOR, created.getRole());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("Devrait lever BadCreationFormatException si l'identifiant existe déjà")
    void shouldThrowBadCreationFormatExceptionWhenUsernameAlreadyExists() {
        when(adminRepository.existsByUsername("admin1")).thenReturn(true);

        assertThrows(BadCreationFormatException.class, () -> adminService.createAdmin(adminRequest));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("Devrait retourner la liste complète des administrateurs")
    void shouldGetAllAdmins() {
        when(adminRepository.findAll()).thenReturn(List.of(admin));

        List<Admin> admins = adminService.getAllAdmins();

        assertEquals(1, admins.size());
        assertEquals("admin1", admins.get(0).getUsername());
    }

    @Test
    @DisplayName("Devrait récupérer un administrateur par son ID")
    void shouldGetAdminById() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        Optional<Admin> found = adminService.getAdminById(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    @DisplayName("Devrait récupérer un administrateur par son nom d'utilisateur")
    void shouldGetAdminByUsername() {
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        Optional<Admin> found = adminService.getAdminByUsername("admin1");

        assertTrue(found.isPresent());
        assertEquals("admin1", found.get().getUsername());
    }

    @Test
    @DisplayName("Devrait supprimer un administrateur par ID")
    void shouldDeleteAdminSuccessfully() {
        when(adminRepository.existsById(1L)).thenReturn(true);
        doNothing().when(adminRepository).deleteById(1L);

        assertDoesNotThrow(() -> adminService.deleteAdmin(1L));
        verify(adminRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Devrait lever NullUserDataException lors de la suppression d'un administrateur inexistant")
    void shouldThrowNullUserDataExceptionWhenDeletingNonExistentAdmin() {
        when(adminRepository.existsById(99L)).thenReturn(false);

        assertThrows(NullUserDataException.class, () -> adminService.deleteAdmin(99L));
        verify(adminRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Devrait modifier le mot de passe d'un administrateur avec succès")
    void shouldUpdateAdminPasswordSuccessfully() {
        PasswordUpdateRequest passReq = new PasswordUpdateRequest("old_pass", "new_pass");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("old_pass", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.encode("new_pass")).thenReturn("new_encoded_pass");
        when(adminRepository.save(admin)).thenReturn(admin);

        assertDoesNotThrow(() -> adminService.updatePassword(1L, passReq));
        assertEquals("new_encoded_pass", admin.getPassword());
        verify(adminRepository).save(admin);
    }

    @Test
    @DisplayName("Devrait lever une exception si l'ancien mot de passe est incorrect")
    void shouldThrowExceptionWhenOldPasswordMismatch() {
        PasswordUpdateRequest passReq = new PasswordUpdateRequest("wrong_pass", "new_pass");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);

        assertThrows(BadCreationFormatException.class, () -> adminService.updatePassword(1L, passReq));
        verify(adminRepository, never()).save(any(Admin.class));
    }
}
