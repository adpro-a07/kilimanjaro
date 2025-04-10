FROM docker.io/library/eclipse-temurin:21-jdk-alpine@sha256:cafcfad1d9d3b6e7dd983fa367f085ca1c846ce792da59bcb420ac4424296d56 AS builder

WORKDIR /src/kilimanjaro
COPY . .
RUN ./gradlew clean bootjar

FROM docker.io/library/eclipse-temurin:21-jdk-alpine@sha256:cafcfad1d9d3b6e7dd983fa367f085ca1c846ce792da59bcb420ac4424296d56 AS runner

ARG USER_NAME=kilimanjaro_usr
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

RUN addgroup -g ${USER_GID} ${USER_NAME} \
    && adduser -h /opt/kilimanjaro -D -u ${USER_UID} -G ${USER_NAME} ${USER_NAME}

USER ${USER_NAME}
WORKDIR /opt/kilimanjaro
COPY --from=builder --chown=${USER_UID}:${USER_GID} /src/kilimanjaro/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]