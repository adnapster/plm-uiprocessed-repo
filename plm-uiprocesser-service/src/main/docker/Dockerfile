FROM anapsix/alpine-java
VOLUME /tmp
COPY plm-uiprocesser-service-1.1-RELEASED.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar