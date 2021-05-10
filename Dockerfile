FROM openjdk:11.0.11-jdk
COPY build/libs/Keymanager-GRPC-0.1-all.jar /etc/keymanager-GRPC.jar
WORKDIR /etc
EXPOSE 50051
CMD ["java", "-jar", "keymanager-GRPC.jar"]