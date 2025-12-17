package com.splitBill.splitBill.repository;

import com.splitBill.splitBill.model.Bill;
import com.splitBill.splitBill.model.Resto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {

    @Query(value = """
        SELECT 
            bi.id, bi.name, bi.price, ba.quantity_taken, bp.name as participant_name
        FROM bill_items bi
        JOIN item_assignments ba ON bi.id = ba.bill_item_id
        JOIN bill_participants bp ON ba.participant_id = bp.id
        WHERE bi.bill_id = :billId
        """, nativeQuery = true)
    List<Object[]> findItemAssignmentsForSplit(@Param("billId") UUID billId);

    @Query("SELECT b FROM Bill b JOIN FETCH b.resto WHERE b.tenantId = :tenantId")
    List<Bill> findAllWithRestoByTenantId(@Param("tenantId") String tenantId);

    Optional<Bill> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByResto(Resto resto);

    long countByResto_Id(UUID restoId);
}