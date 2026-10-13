package com.studyhub.platform.modules.auth.service;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapper implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapper.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties applicationProperties;

    public AdminBootstrapper(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             ApplicationProperties applicationProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void run(String... args) {
        String adminEmail = applicationProperties.getAdmin().getBootstrapEmail().toLowerCase().trim();

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(applicationProperties.getAdmin().getBootstrapPassword()));
            admin.setFullName(applicationProperties.getAdmin().getBootstrapName());
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin);
            log.info("Bootstrapped initial system administrator account: {}", adminEmail);
        } else {
            log.info("Administrator account already exists: {}", adminEmail);
        }
    }
}
