package com.math.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Tabla {@code transactions} de PostgreSQL: request, response y fecha.
 *
 * <pre>
 *  transactions
 *  ┌────────────┬─────────────┬──────────────────────────────┐
 *  │ id         │ BIGSERIAL   │ PK                           │
 *  │ request    │ TEXT        │ payload recibido (JSON)      │
 *  │ response   │ TEXT        │ resultado devuelto (JSON)    │
 *  │ created_at │ TIMESTAMPTZ │ lo llena Hibernate al insertar│
 *  └────────────┴─────────────┴──────────────────────────────┘
 * </pre>
 *
 * <p>Lombok: {@code @Getter/@Setter} generan accesores, {@code @NoArgsConstructor} es el
 * constructor vacío que exige JPA y {@code @Builder} permite crearla de forma fluida.</p>
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String request;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
