package com.math;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada de MisterMath.
 *
 * <p>MisterMath es una API didáctica que resuelve operaciones simples entre dos números.
 * Lo interesante no es la matemática, sino <b>cómo</b> se resuelve: el cliente elige en el
 * payload qué patrón de diseño (Singleton, Factory, Strategy o Builder) ejecuta el cálculo,
 * y toda la aplicación está organizada con CQRS y los principios SOLID.</p>
 *
 * <p>{@link ConfigurationPropertiesScan} registra automáticamente los {@code record} anotados con
 * {@code @ConfigurationProperties} (JWT, usuario, caché).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MisterMathApplication {

    /**
     * Arranca el contexto de Spring.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(MisterMathApplication.class, args);
    }
}
