package com.splitBill.splitBill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.splitBill.splitBill.model.ItemAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemAssignmentRepository extends JpaRepository<ItemAssignment, UUID> {
    Optional<ItemAssignment> findByBillItemIdAndParticipantId(UUID billItemId, UUID participantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ItemAssignment ia WHERE ia.billItem.bill.id = :billId")
    void deleteAllByBillId(UUID billId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ItemAssignment ia WHERE ia.participant.id = :participantId")
    void deleteAllByParticipantId(UUID participantId);
}