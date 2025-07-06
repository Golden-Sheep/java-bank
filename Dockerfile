# Etapa de build: compila o projeto com Maven e Java 17
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copia o wrapper e arquivos do Maven
COPY mvnw .
COPY .mvn/ .mvn

COPY pom.xml .

# Copia todos os módulos
COPY bank-common/ bank-common/
COPY bank-user/ bank-user/
COPY bank-account/ bank-account/
COPY bank-wallet/ bank-wallet/
COPY bank-transaction/ bank-transaction/
COPY bank-config/ bank-config/
COPY bank-api/ bank-api/
COPY bank-app/ bank-app/

# Compila o módulo bank-app e suas dependências (fat jar)
RUN ./mvnw clean package -DskipTests -pl bank-app -am

# Extrai o conteúdo do JAR Spring Boot gerado
WORKDIR /workspace/app/bank-app
RUN mkdir -p target/dependency && \
    cp target/bank-app-0.0.1-SNAPSHOT.jar target/dependency/app.jar && \
    cd target/dependency && \
    jar -xf app.jar

# Etapa de runtime
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/bank-app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "dev.jvops.bank.JavaBankApplication"]
