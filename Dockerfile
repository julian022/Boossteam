# ===== STAGE 1: Build =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Cache de dependencias
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Código y build
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# ===== STAGE 2: Runtime =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Opcionales
ENV TZ=UTC
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75"

# Copia el jar construido
COPY --from=build /app/build/libs/*.jar app.jar

# La app escuchará en 8089 (lo pasaremos por env a Spring)
EXPOSE 8089

# Variables para inyectar credenciales/URL al correr
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""
ENV SERVER_PORT=8089

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
