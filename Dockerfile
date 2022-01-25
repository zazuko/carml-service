FROM docker.io/library/openjdk:11-bullseye

EXPOSE 8080

RUN apt update \
  && apt install -y maven \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .
RUN mvn clean package

WORKDIR /app/service/target
RUN unzip meecrowave-meecrowave-distribution.zip

WORKDIR /app/service/target/meecrowave-distribution

CMD [ "./bin/meecrowave.sh", "run" ]
