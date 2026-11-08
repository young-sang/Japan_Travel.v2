# 백엔드 이미지 (D-085). Gradle 로 jar 를 만들고 JRE 17 로 실행한다.
# 래퍼(gradlew) 대신 Gradle 이 들어 있는 공식 이미지를 쓴다 — 래퍼는 빌드 때마다 GitHub 에서
# 배포판을 받는데, 그 주소가 504 를 돌려주면 이미지를 만들 수 없다. 버전은 래퍼와 같은 8.14.
# 테스트는 로컬 MySQL 이 필요해(D-062) 이미지 빌드에서는 돌리지 않는다.
FROM gradle:8.14-jdk17 AS build
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY src src
RUN gradle bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
