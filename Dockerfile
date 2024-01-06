# from: https://www.docker.com/blog/9-tips-for-containerizing-your-spring-boot-code/

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
COPY temp ./temp

CMD ["./mvnw", "spring-boot:run"]