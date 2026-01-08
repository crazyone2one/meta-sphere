FROM alpine/java:21-jdk
MAINTAINER jingll <the2ndindec@gmail.com>
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone
WORKDIR /app
VOLUME /app/files
ADD backend-1.0.0.jar metasphere.jar
EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "metasphere.jar"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar metasphere.jar"]