# Stage 1: Build the application
FROM openjdk:21-ea-1-jdk-oracle AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: Create the final, lightweight image
FROM openjdk:21-ea-1-jre-oracle
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]