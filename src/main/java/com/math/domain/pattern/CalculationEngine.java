package com.math.domain.pattern;

import java.math.BigDecimal;

/**
 * Un "motor" que resuelve {@code num1 (operation) num2} aplicando un patrón de diseño concreto.
 *
 * <h2>SOLID aplicado aquí</h2>
 * <ul>
 *   <li><b>O - Open/Closed:</b> para agregar un patrón nuevo se crea otro {@code @Component}
 *       que implemente esta interfaz. {@link CalculationEngineResolver} lo descubre solo, sin
 *       modificar una línea.</li>
 *   <li><b>L - Liskov:</b> los cuatro motores son intercambiables: reciben lo mismo y devuelven
 *       lo mismo; solo cambia el camino interno.</li>
 *   <li><b>D - Dependency Inversion:</b> el command handler depende de esta abstracción, nunca
 *       de {@code SingletonCalculationEngine} o {@code FactoryCalculationEngine}.</li>
 * </ul>
 */
public interface CalculationEngine {

    /**
     * Patrón que implementa este motor; el resolver lo usa como llave.
     *
     * @return el patrón atendido
     */
    CalculationPattern pattern();

    /**
     * Resuelve la operación.
     *
     * @param num1   primer número
     * @param symbol símbolo de la operación ({@code + - * / % ^})
     * @param num2   segundo número
     * @return resultado más la bitácora de pasos del patrón
     */
    CalculationOutcome calculate(BigDecimal num1, String symbol, BigDecimal num2);
}
