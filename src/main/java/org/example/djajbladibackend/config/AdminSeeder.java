package org.example.djajbladibackend.config;

import org.example.djajbladibackend.factory.UserFactory;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Ensures a default Admin user exists with a known password on every startup.
 * If the admin already exists, the password is re-synced to the configured value.
 * Credentials are externalized via environment variables.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@djajbladi.ma}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123456}")
    private String adminPassword;

    @Value("${app.admin.fullname:Administrateur DjajBladi}")
    private String adminFullName;

    public AdminSeeder(UserRepository userRepository, UserFactory userFactory,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Optional<User> existing = userRepository.findByEmail(adminEmail);

        if (existing.isPresent()) {
            User admin = existing.get();
            if (admin.getRole() != RoleEnum.Admin) {
                admin.setRole(RoleEnum.Admin);
            }
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("Admin user password synced: {}", adminEmail);
            return;
        }

        User admin = userFactory.createAdmin(adminFullName, adminEmail, adminPassword);
        userRepository.save(admin);
        log.info("Default admin user created: {}", adminEmail);
    }
}
