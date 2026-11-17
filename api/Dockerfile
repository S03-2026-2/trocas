# Imagem da API de trocas (usada pelo Render e pelo docker-compose).
# Build em duas etapas: a primeira compila com Maven, a segunda leva só o .jar para uma imagem leve.

# ---------- Etapa 1: compila ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências antes de copiar o código: enquanto o pom.xml não mudar,
# o Docker reaproveita essa camada e os builds seguintes ficam bem mais rápidos.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: executa ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Não roda como root
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app

# Porta padrão local; no Render a aplicação escuta na porta da variável PORT (ver application.properties)
EXPOSE 8080

# Ajustes de JVM para o plano gratuito do Render (512 MB de RAM e pouca CPU):
# - MaxRAMPercentage: usa no máximo 75% da memória do container para o heap
# - UseSerialGC: coletor de lixo mais econômico para 1 CPU
# - TieredStopAtLevel=1: compilação JIT mais simples, deixa a subida bem mais rápida
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
