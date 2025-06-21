# 1. Java 21 환경 설정
FROM openjdk:21-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 복사
COPY ./service/user/build/libs/*.jar app.jar

# 4. 포트 오픈 (Spring Boot 내부 포트와 맞춰야 함)
EXPOSE 9000

# 5. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
