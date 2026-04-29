FROM bellsoft/liberica-openjdk-alpine:21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM bellsoft/liberica-openjre-alpine:21

WORKDIR /app

RUN apk add --no-cache tzdata curl
ENV TZ=Asia/Seoul

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/build/libs/app.jar app.jar

RUN mkdir -p /app/secrets && chown spring:spring /app/secrets

USER spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
