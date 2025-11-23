package com.splitBill.splitBill.repository;

import com.splitBill.splitBill.model.BillParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BillParticipantRepository extends JpaRepository<BillParticipant, UUID> {
}