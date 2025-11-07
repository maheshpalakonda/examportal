# ---- build (Java 21) ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---- run (Java 21) ----
FROM eclipse-temurin:21-jre
ENV APP_USER=nonrootuser APP_UID=1000 JAVA_OPTS=""
RUN groupadd --gid ${APP_UID} ${APP_USER} \
 && useradd --uid ${APP_UID} --gid ${APP_UID} --shell /bin/bash --create-home ${APP_USER}
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
USER ${APP_USER}
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

