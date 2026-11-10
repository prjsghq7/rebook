# Render 배포용: Maven으로 war를 빌드한 뒤 JRE 17 이미지에서 실행합니다.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/*.war app.war
# 무료 인스턴스(메모리 512MB)에 맞춘 JVM 설정
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
CMD ["java", "-jar", "app.war"]
