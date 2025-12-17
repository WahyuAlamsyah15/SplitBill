package com.splitBill.splitBill.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false)
    private FeeType taxType = FeeType.PERCENT;

    @Column(name = "tax_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private FeeType serviceType = FeeType.PERCENT;
    
    @Column(name = "service_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal serviceValue = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "bill_date", updatable = false)
    private LocalDateTime billDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillParticipant> participants = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resto_id", nullable = false)
    private Resto resto;
}