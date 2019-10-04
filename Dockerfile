ARG APP_INSIGHTS_AGENT_VERSION=2.4.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.2

# Mandatory!
ENV APP ia-case-notifications-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

COPY lib/applicationinsights-agent-2.4.1.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 8093

CMD [ "ia-case-notifications-api.jar" ]
