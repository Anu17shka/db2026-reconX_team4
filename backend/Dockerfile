# ============================================================================
# TICKET-ADV146 — Spring Boot Dockerfile (multi-stage Maven -> JRE 25 slim)
#
# Stage 1: build the JAR with Temurin JDK 25 + Maven.
# Stage 2: jlink a custom minimal JRE containing only the modules this app
#          actually uses — the full eclipse-temurin:25-jre-alpine base alone
#          is ~224MB, which blows the <250MB budget once the ~95MB fat jar
#          is added. A trimmed jlink runtime keeps the final image lean.
# Stage 3: copy the custom JRE + the JAR onto a bare alpine base.
# ============================================================================

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -q -B dependency:go-offline || true
COPY src src
RUN ./mvnw -q -B -DskipTests package && \
    mv target/reconx-*.jar app.jar

# Module list derived via jdeps against the unpacked fat jar's BOOT-INF
# classpath, plus java.naming/java.xml/jdk.crypto.ec/java.management which
# jdeps' static analysis misses (JNDI, Liquibase XML changelogs, TLS EC
# certs, and JMX are all reached reflectively/via SPI).
FROM eclipse-temurin:25-jdk-alpine AS jlink
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.sql.rowset,java.transaction.xa,java.xml,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.net,jdk.unsupported \
    --strip-debug --no-man-pages --no-header-files --compress=2 \
    --output /opt/jre-minimal

FROM alpine:3.20
RUN addgroup -S reconx && adduser -S reconx -G reconx
ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"
COPY --from=jlink /opt/jre-minimal $JAVA_HOME
USER reconx
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
