package com.splitBill.splitBill.repository;

import com.splitBill.splitBill.model.Resto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestoRepository extends JpaRepository<Resto, UUID> {
    
    List<Resto> findByTenantId(String tenantId);
    
    Optional<Resto> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);

    @Modifying
    @Query("DELETE FROM Resto r WHERE r.id = :id")
    void physicalDeleteById(@Param("id") UUID id);
}