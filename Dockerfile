# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Primero solo el pom: Docker cachea las dependencias mientras el pom no cambie.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar (imagen liviana, solo JRE) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/mistermath.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
