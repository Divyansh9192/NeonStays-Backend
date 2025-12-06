# ---- Build Stage ----
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy only necessary files first (for caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (better caching)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the JAR
RUN ./mvnw clean package -DskipTests


# ---- Run Stage ----
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render provides PORT env automatically
EXPOSE 8080

# Make Spring use Render's PORT
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
