package com.financedomain.user.service;

import com.financedomain.user.bean.Admin;
import com.financedomain.user.dto.AdminRequest;
import com.financedomain.user.dto.PasswordUpdateRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.exception.BadCreationFormatException;
import com.financedomain.user.exception.NullUserDataException;
import com.financedomain.user.repository.AdminRepository;
import com.financedomain.user.dto.TrackingEvent;
import com.financedomain.user.proxy.TrackingProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TrackingProxy trackingProxy;

    public Admin createAdmin(AdminRequest request) {
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new BadCreationFormatException("L'identifiant '" + request.getUsername() + "' est déjà utilisé.");
        }

        Admin admin = new Admin();
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setUsername(request.getUsername());
        admin.setRole(TypeRole.ADMINISTRATOR);

        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new NullUserDataException("Administrateur introuvable avec l'id : " + id);
        }
        adminRepository.deleteById(id);
    }

    public void updatePassword(Long id, PasswordUpdateRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new NullUserDataException("Administrateur introuvable avec l'id : " + id));

        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new BadCreationFormatException("L'ancien mot de passe est incorrect.");
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);

        // Tracking
        sendTrackingEvent("PASSWORD_UPDATE", admin.getUsername(), String.valueOf(admin.getId()), "ADMINISTRATOR", "Changement de mot de passe administrateur.");
    }

    private void sendTrackingEvent(String eventType, String username, String userId, String userRole, Object payload) {
        try {
            TrackingEvent event = TrackingEvent.builder()
                    .eventType(eventType)
                    .msisdn(username) // Use username as key identifier since admin has no phone msisdn in table
                    .userId(userId)
                    .userRole(userRole)
                    .sourceService("user-service")
                    .payload(payload)
                    .timestamp(java.time.Instant.now())
                    .build();
            trackingProxy.collectEvent(event, "INTERNAL");
        } catch (Exception e) {
            System.err.println("Erreur de tracking admin: " + e.getMessage());
        }
    }
}
