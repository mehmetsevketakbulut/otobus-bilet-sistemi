# 1. Aşama: Maven kullanarak projeyi derle
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# Pom dosyasını kopyala
COPY pom.xml .
# Bağımlılıkları indir (cache mekanizması için)
RUN mvn dependency:go-offline -B
# Kaynak kodları kopyala ve derle
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Aşama: Derlenen jar dosyasını çalıştır (Daha hafif bir imaj kullanılır)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
