package com.jairo.inventory.user.config;

import com.jairo.inventory.user.domain.Role;
import com.jairo.inventory.user.domain.UserAccount;
import com.jairo.inventory.user.repository.UserAccountRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!repository.existsByEmailIgnoreCase("admin@inventory.local")) {
                UserAccount admin = new UserAccount();
                admin.setFullName("Administrador General");
                admin.setEmail("admin@inventory.local");
                admin.setPasswordHash(passwordEncoder.encode("Admin123*"));
                admin.setRoles(Set.of(Role.ADMIN));
                repository.save(admin);
            }

            if (!repository.existsByEmailIgnoreCase("user@inventory.local")) {
                UserAccount user = new UserAccount();
                user.setFullName("Operador Inventario");
                user.setEmail("user@inventory.local");
                user.setPasswordHash(passwordEncoder.encode("User123*"));
                user.setRoles(Set.of(Role.USER));
                repository.save(user);
            }
        };
    }
}
