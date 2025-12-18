package com.splitBill.splitBill.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "tenant_id"})
})
@Data
@NoArgsConstructor
@SQLDelete(sql = "UPDATE restos SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Resto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    @OneToMany(mappedBy = "resto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bill> bills = new ArrayList<>();
}