package com.math.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data: la implementación ({@code save}, {@code findById}, ...) la genera
 * Spring en tiempo de ejecución.
 */
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
}
