package com.nereden.api.infrastructure.config;

import com.nereden.api.domain.entity.User;
import com.nereden.api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    @Profile("dev")
    CommandLineRunner seedDevData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> seedTestUser(userRepository, passwordEncoder);
    }

    private void seedTestUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository.existsByEmail("test@nereden.com")) {
            return;
        }

        userRepository.save(User.builder()
                .email("test@nereden.com")
                .passwordHash(passwordEncoder.encode("Test1234"))
                .fullName("Test Kullanıcı")
                .build());

        log.info("Seeded dev test user: test@nereden.com / Test1234");
    }
}
