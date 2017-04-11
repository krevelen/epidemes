MAINTAINER Rick van Krevelen "rick.van.krevelen@rivm.nl"

FROM maven:latest
# VOLUME /tmp
# VOLUME /app
VOLUME /root/.m2
EXPOSE 8080
ENTRYPOINT ["mvn","cargo:run","-Dmaven.test.skip=true"]
