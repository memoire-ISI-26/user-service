package com.financedomain.user.config;

import com.financedomain.user.bean.Admin;
import com.financedomain.user.enums.TypeRole;
import com.financedomain.user.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final static String ADMIN = "admin";

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("====== [Database Initializer] Vérification de l'existence des administrateurs ======");
        
        if (!adminRepository.existsByUsername(ADMIN)) {
            log.info("[Database Initializer] Aucun administrateur par défaut trouvé. Création du compte 'admin'...");
            
            Admin admin = new Admin();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setUsername(ADMIN);
            admin.setPassword(passwordEncoder.encode(ADMIN));
            admin.setRole(TypeRole.ADMINISTRATOR);
            
            adminRepository.save(admin);
            log.info("[Database Initializer] Compte administrateur créé avec succès (Username: admin / Password: admin).");
        } else {
            log.info("[Database Initializer] Le compte administrateur 'admin' existe déjà.");
        }
        log.info("==========================================================================");
    }
}
