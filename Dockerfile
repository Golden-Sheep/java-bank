# Etapa de build: compila o projeto com Maven e Java 17
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copia os arquivos do projeto
COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ src

# Gera o JAR (sem rodar testes)
RUN ./mvnw clean package -DskipTests

# Extrai as dependências e classes do JAR para uso na próxima imagem
RUN mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# Etapa de runtime: imagem leve com apenas o JRE
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Define caminho das dependências vindas do build
ARG DEPENDENCY=/workspace/app/target/dependency

# Copia dependências e classes para a imagem final
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Define o ponto de entrada da aplicação
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "dev.jvops.bank.JavaBankApplication"]
