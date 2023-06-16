FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY build/install/* .

ARG projectName
ENV PROJECT_NAME=$projectName

RUN rm bin/*.bat
ENTRYPOINT "bin/$PROJECT_NAME" 