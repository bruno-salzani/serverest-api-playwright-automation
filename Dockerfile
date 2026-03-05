FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
ENV BASE_URL=https://serverest.dev
CMD ["mvn", "test"]
