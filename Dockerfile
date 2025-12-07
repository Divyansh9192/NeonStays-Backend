# ---- Build Stage ----
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Render assigns a port like 10000, 34622, etc.
# EXPOSE MUST MATCH THE PORT SPRING BOOT ACTUALLY USES
EXPOSE 10000

# Bind Spring Boot to the Render-assigned $PORT
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
