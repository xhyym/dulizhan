# ---- 构建阶段：容器内用 Maven 编译，无需本机安装 Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# 先拷 pom 预热依赖缓存，源码变动时无需重新下载依赖
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/uploads

# 通配拷贝，避免 jar 名硬编码（artifactId=dulizhan → dulizhan-1.0.0-SNAPSHOT.jar）
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
