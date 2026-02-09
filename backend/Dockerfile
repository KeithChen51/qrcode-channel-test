# 后端构建镜像
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /app
COPY pom.xml .
# 先下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# 运行镜像
FROM openjdk:8-jre-slim

WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制JAR包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
