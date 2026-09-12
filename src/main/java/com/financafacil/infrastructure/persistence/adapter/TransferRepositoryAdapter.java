package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Transfer;
import com.financafacil.domain.port.out.TransferRepository;
import com.financafacil.infrastructure.persistence.jpa.TransferJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.TransferJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepository {
    private final TransferJpaRepository jpaRepository;
    private final TransferJpaMapper mapper;

    @Override
    public Transfer save(Transfer transfer) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transfer)));
    }

    @Override
    public Optional<Transfer> findById(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<Transfer> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID userId) {
        jpaRepository.deleteByIdAndUserId(id, userId);
    }
}
