FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083

# Run: env vars from compose override. Secrets from /run/secrets if present.
# Production: use env vars. Dev: use secret files.
CMD ["sh", "-c", "\
  [ -f /run/secrets/google_client_id ] && export GOOGLE_CLIENT_ID=$(cat /run/secrets/google_client_id); \
  [ -f /run/secrets/google_client_secret ] && export GOOGLE_CLIENT_SECRET=$(cat /run/secrets/google_client_secret); \
  [ -f /run/secrets/twilio_account_sid ] && export TWILIO_ACCOUNT_SID=$(cat /run/secrets/twilio_account_sid); \
  [ -f /run/secrets/twilio_auth_token ] && export TWILIO_AUTH_TOKEN=$(cat /run/secrets/twilio_auth_token); \
  [ -f /run/secrets/twilio_whatsapp_from ] && export TWILIO_WHATSAPP_FROM=$(cat /run/secrets/twilio_whatsapp_from); \
  exec java -jar app.jar"]
