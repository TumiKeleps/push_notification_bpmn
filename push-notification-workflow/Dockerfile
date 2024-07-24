# FROM openjdk:21-slim
# VOLUME /tmp
# ADD target/*.jar app.jar
# ENV JAVA_OPTS "-Xmx192m -Xms192m -Djava.security.egd=file:///dev/./urandom -XX:+HeapDumpOnOutOfMemoryError "
# ENV TZ Africa/Johannesburg
# ENV dynamic_port 2000
# # NOT USE YET -XX:+UseG1GC
# ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar --server.port=${dynamic_port}" ]