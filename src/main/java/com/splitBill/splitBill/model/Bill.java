package com.splitBill.splitBill.model;

import jakarta.persistence.*;
import lombok.*;
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

    // HAPUS INI BRAY!!!
    // @Column(name = "resto_id", nullable = false)
    // private UUID restoId;

    private String note;

    @Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "service_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal servicePercent = BigDecimal.ZERO;

    @Column(name = "bill_date")
    private LocalDateTime billDate = LocalDateTime.now();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillParticipant> participants = new ArrayList<>();

    // INI YANG TETEP ADA — INI YANG PENTING!
    @ManyToOne
    @JoinColumn(name = "resto_id")
    private Resto resto; // gak perlu nullable = true lagi

    // Kalau lu butuh restoId buat apa-apa, pake ini aja:
    public UUID getRestoId() {
        return resto != null ? resto.getId() : null;
    }
}