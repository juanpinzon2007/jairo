package com.jairo.inventory.user.service;

import com.jairo.inventory.user.domain.UserAccount;
import com.jairo.inventory.user.dto.CreateUserRequest;
import com.jairo.inventory.user.dto.UserResponse;
import com.jairo.inventory.user.repository.UserAccountRepository;
import jakarta.persistence.EntityExistsException;
import java.util.TreeSet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public UserService(UserAccountRepository repository,
                       PasswordEncoder passwordEncoder,
                       TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
    }

    public Flux<UserResponse> findAll() {
        return Mono.fromCallable(() -> repository.findAll().stream().map(this::toResponse).toList())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<UserResponse> create(CreateUserRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    if (repository.existsByEmailIgnoreCase(request.email())) {
                        throw new EntityExistsException("Ya existe un usuario con ese correo");
                    }
                    UserAccount user = new UserAccount();
                    user.setFullName(request.fullName());
                    user.setEmail(request.email());
                    user.setPasswordHash(passwordEncoder.encode(request.password()));
                    user.setRoles(request.roles());
                    return toResponse(repository.save(user));
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isActive(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toCollection(TreeSet::new)),
                user.getCreatedAt()
        );
    }
}
