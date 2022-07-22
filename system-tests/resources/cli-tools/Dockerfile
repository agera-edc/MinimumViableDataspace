# -buster is required to have apt available
FROM openjdk:17-slim-buster

WORKDIR /app

# Copy Registration Service client jar
COPY ./registration-service-cli.jar /app
COPY ./entrypoint.sh /app

RUN ["chmod", "+x", "/app/entrypoint.sh"]

ENTRYPOINT [ "/app/entrypoint.sh"]
