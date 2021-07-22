FROM azul/zulu-openjdk-alpine:11.0.10 AS builder

RUN ["/usr/lib/jvm/zulu11/bin/jlink", "--compress=2", "--no-header-files", "--strip-debug", "--no-man-pages", \
     "--module-path", "/usr/lib/jvm/zulu11/jmods", \
     "--add-modules", "java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,jdk.unsupported,jdk.crypto.ec,jdk.jdwp.agent", \
     "--output", "/jlinked"]

FROM alpine:3.11.3

ARG FINAL_NAME

COPY --from=builder /jlinked /opt/java/

ADD $FINAL_NAME.jar /app.jar

COPY entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh && dos2unix /entrypoint.sh

ENTRYPOINT exec /entrypoint.sh
