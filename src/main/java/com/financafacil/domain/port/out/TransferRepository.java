package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Transfer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
    Transfer save(Transfer transfer);
    Optional<Transfer> findById(UUID id, UUID userId);
    List<Transfer> findAllByUserId(UUID userId);
    void deleteById(UUID id, UUID userId);
}
