# ---- 构建阶段 ----
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# 先下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ---- 运行阶段 ----
FROM --platform=linux/amd64 eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/uploads

COPY --from=build /build/target/admin-boot-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
