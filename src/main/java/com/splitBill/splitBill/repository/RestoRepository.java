package com.splitBill.splitBill.repository;

import com.splitBill.splitBill.model.Resto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestoRepository extends JpaRepository<Resto, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<Resto> findByDeletedFalse();
    Optional<Resto> findByIdAndDeletedFalse(UUID id);
}