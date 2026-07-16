package com.financedomain.user.config;

import com.financedomain.user.bean.Admin;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== [Database Initializer] Vérification de l'existence des administrateurs ======");
        
        if (!adminRepository.existsByUsername("admin")) {
            System.out.println("[Database Initializer] Aucun administrateur par défaut trouvé. Création du compte 'admin'...");
            
            Admin admin = new Admin();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(TypeRole.ADMINISTRATOR);
            
            adminRepository.save(admin);
            System.out.println("[Database Initializer] Compte administrateur créé avec succès (Username: admin / Password: admin).");
        } else {
            System.out.println("[Database Initializer] Le compte administrateur 'admin' existe déjà.");
        }
        System.out.println("==========================================================================");
    }
}
