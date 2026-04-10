# ============================
# Stage 1: Build com Maven
# ============================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Cache de dependências (só baixa de novo se pom.xml mudar)
COPY pom.xml .
RUN apk add --no-cache maven \
    && mvn dependency:go-offline -B

# Copia código e compila
COPY src ./src
RUN mvn package -DskipTests -B

# ============================
# Stage 2: Runtime leve
# ============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

