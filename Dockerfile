FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m"
ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]