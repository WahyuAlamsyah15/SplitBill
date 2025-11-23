package com.splitBill.splitBill.repository;

import com.splitBill.splitBill.model.ItemAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemAssignmentRepository extends JpaRepository<ItemAssignment, UUID> {
    @Query("SELECT a FROM ItemAssignment a WHERE a.billItem.bill.id = :billId")
    List<ItemAssignment> findAllByBillItem_Bill_Id(@Param("billId") UUID billId);

    Optional<ItemAssignment> findByBillItemIdAndParticipantId(UUID billItemId, UUID participantId);
}