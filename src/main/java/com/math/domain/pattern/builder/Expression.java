package com.math.domain.pattern.builder;

import com.math.domain.operation.MathOperation;
import com.math.domain.pattern.factory.MathOperationFactory;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <b>Patrón Builder.</b>
 *
 * <p>Una {@code Expression} es un objeto <b>inmutable</b> ({@code num1 operation num2}). En vez de
 * un constructor con muchos parámetros, se arma paso a paso con un {@link Builder} fluido y
 * {@link Builder#build()} valida que no falte nada antes de crearla:</p>
 *
 * <pre>
 *  Expression expression = Expression.builder()
 *          .left(new BigDecimal("10"))
 *          .operator("*")
 *          .right(new BigDecimal("4"))
 *          .build();          // ← valida aquí
 *  expression.evaluate();     // 40
 * </pre>
 *
 * <p>El builder está escrito a mano para que se vea cómo funciona. En los DTOs del proyecto se
 * usa {@code @Builder} de Lombok, que genera exactamente este mismo código.</p>
 */
public final class Expression {

    private final BigDecimal left;
    private final String operator;
    private final BigDecimal right;
    private final MathOperation operation;

    /** Privado: solo el {@link Builder} puede crear expresiones. */
    private Expression(Builder builder) {
        this.left = builder.left;
        this.operator = builder.operator;
        this.right = builder.right;
        this.operation = MathOperationFactory.create(builder.operator);
    }

    /**
     * Inicia la construcción de una expresión.
     *
     * @return un builder vacío
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Evalúa la expresión.
     *
     * @return el resultado de {@code left operator right}
     */
    public BigDecimal evaluate() {
        return operation.apply(left, right);
    }

    /**
     * Representación legible, por ejemplo {@code "10 * 4"}.
     *
     * @return la expresión como texto
     */
    @Override
    public String toString() {
        return left.toPlainString() + " " + operator + " " + right.toPlainString();
    }

    /**
     * Constructor paso a paso de {@link Expression}. Cada método devuelve {@code this} para
     * poder encadenar llamadas (interfaz fluida).
     */
    public static final class Builder {

        private BigDecimal left;
        private String operator;
        private BigDecimal right;

        private Builder() {
        }

        /**
         * @param left primer operando
         * @return este mismo builder
         */
        public Builder left(BigDecimal left) {
            this.left = left;
            return this;
        }

        /**
         * @param operator símbolo de la operación
         * @return este mismo builder
         */
        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        /**
         * @param right segundo operando
         * @return este mismo builder
         */
        public Builder right(BigDecimal right) {
            this.right = right;
            return this;
        }

        /**
         * Valida y crea la expresión inmutable.
         *
         * @return la expresión lista para evaluar
         * @throws NullPointerException si falta alguna parte
         * @throws com.math.domain.exception.UnsupportedOperationSymbolException si el operador no existe
         */
        public Expression build() {
            Objects.requireNonNull(left, "left operand is required");
            Objects.requireNonNull(operator, "operator is required");
            Objects.requireNonNull(right, "right operand is required");
            return new Expression(this);
        }
    }
}
