# --- build stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Pre-fetch dependencies for better layer caching.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# --- runtime stage --------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

RUN addgroup -S iam && adduser -S iam -G iam
USER iam

WORKDIR /app

COPY --from=build /workspace/target/iam-portfolio-*.jar /app/app.jar

# Copy the RAG corpus so DocumentLoader finds it inside the container.
COPY --chown=iam:iam docs/RAG_CORPUS /app/docs/RAG_CORPUS

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --retries=20 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
