# syntax=docker/dockerfile:1
# ---- Build stage: compila todo o backend (multi-modulo) com Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia o projeto inteiro (todas as bibliotecas + pdv)
COPY . .

# Gera o quarkus-app em pdv/target/quarkus-app
RUN mvn -B -DskipTests clean package

# ---- Runtime stage: JRE enxuto com o pacote Quarkus ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /workspace/pdv/target/quarkus-app/ /app/

ENV QUARKUS_HTTP_HOST=0.0.0.0
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]
