FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/uploads

# 本地编译好的 JAR，直接 COPY（不在容器内编译，省去 Maven 依赖下载）
COPY target/admin-boot-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
