# ---- Build Stage ----
FROM maven:3.8.8-eclipse-temurin-11 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Build application
COPY src ./src
RUN mvn package -DskipTests -B -q

# ---- Runtime Stage ----
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S wms && adduser -S wms -G wms

# Copy jar
COPY --from=builder /build/target/ct-tibet-wms-1.0.0.jar app.jar

# Create log directory
RUN mkdir -p /app/logs && chown -R wms:wms /app

USER wms

EXPOSE 48888

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
