package com.financedomain.user.service;

import com.financedomain.user.bean.Admin;
import com.financedomain.user.dto.AdminRequest;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.repository.AdminRepository;
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

    public Admin createAdmin(AdminRequest request) {
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("L'identifiant '" + request.getUsername() + "' est déjà utilisé.");
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
            throw new IllegalArgumentException("Administrateur introuvable avec l'id : " + id);
        }
        adminRepository.deleteById(id);
    }
}
