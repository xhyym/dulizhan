# ---- 构建阶段 ----
ARG MAVEN_IMAGE=maven:3.9-eclipse-temurin-17
ARG JRE_IMAGE=eclipse-temurin:17-jre
FROM ${MAVEN_IMAGE} AS build
WORKDIR /build
ARG MAVEN_OPTS_ARGS=
COPY pom.xml .
# 先下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B ${MAVEN_OPTS_ARGS}
COPY src ./src
RUN mvn package -DskipTests -B ${MAVEN_OPTS_ARGS}

# ---- 运行阶段 ----
FROM ${JRE_IMAGE}
WORKDIR /app
RUN mkdir -p /app/uploads

COPY --from=build /build/target/admin-boot-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
