package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.User;
import com.financafacil.domain.port.out.UserRepository;
import com.financafacil.infrastructure.persistence.jpa.UserJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.UserJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserJpaMapper mapper;

    @Override public User save(User user) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(user))); }
    @Override public Optional<User> findByEmail(String email) { return jpaRepository.findByEmail(email).map(mapper::toDomain); }
    @Override public Optional<User> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    @Override public boolean existsByEmail(String email) { return jpaRepository.existsByEmail(email); }
}
