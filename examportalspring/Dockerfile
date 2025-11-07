# ---- build (Java 21) ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---- run (Java 21) ----
FROM eclipse-temurin:21-jre

# ✅ Create non-root user properly (Debian-based image)
RUN addgroup --system nonrootuser && adduser --system --ingroup nonrootuser nonrootuser

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
USER nonrootuser
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

