package com.jairo.inventory.user.service;

import com.jairo.inventory.shared.security.AuthenticatedUser;
import com.jairo.inventory.shared.security.JwtService;
import com.jairo.inventory.user.domain.UserAccount;
import com.jairo.inventory.user.dto.AuthResponse;
import com.jairo.inventory.user.dto.LoginRequest;
import com.jairo.inventory.user.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AuthService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return Mono.fromCallable(() -> {
                    UserAccount user = repository.findByEmailIgnoreCase(request.email())
                            .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
                    if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                        throw new BadCredentialsException("Credenciales invalidas");
                    }
                    AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                            user.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getRoles().stream()
                                    .map(Enum::name)
                                    .sorted(Comparator.naturalOrder())
                                    .toList()
                    );
                    return new AuthResponse(
                            user.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            authenticatedUser.roles(),
                            jwtService.generateToken(authenticatedUser)
                    );
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public UserAccount getRequiredUser(UUID userId) {
        return repository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}
