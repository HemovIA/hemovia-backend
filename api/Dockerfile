# syntax=docker/dockerfile:1

# ---------- 1º estágio: compila e roda os testes unitários ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia primeiro só o que define as dependências, para aproveitar o cache de camadas
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Testes unitários rodam aqui; se algum falhar, a imagem não é gerada.
# Os testes marcados com @Tag("integration") precisam do PostgreSQL e rodam na esteira (CI).
RUN ./mvnw -B package -DexcludedGroups=integration

# ---------- 2º estágio: leva só o artefato ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Não roda como root
RUN addgroup -S hemovia && adduser -S hemovia -G hemovia
COPY --from=build /app/target/*.jar app.jar
USER hemovia

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
