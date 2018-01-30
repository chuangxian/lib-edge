FROM daocloud.io/myzd/gradle-builder:v20170706 as build
MAINTAINER Yuanhai He <i@bestmike007.com>

COPY . /srv
RUN curl -sSL http://git.oschina.net/bestmike007/files/raw/master/setenv.sh | sh -s; \
    gradle --no-daemon build -x test

FROM daocloud.io/myzd/openjdk-jre:v20170824-2

COPY --from=build /srv/build/libs/*.jar /srv/
ENTRYPOINT ["java", "-server", "-jar", "/srv/java-demo-0.0.1-SNAPSHOT.jar"]