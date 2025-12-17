package com.splitBill.splitBill.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "item_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"bill_item_id", "participant_id"}))
@Data
@NoArgsConstructor
public class ItemAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_item_id", nullable = false)
    private BillItem billItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private BillParticipant participant;

    @Column(name = "quantity_taken", nullable = false)
    private int quantityTaken = 0;
}