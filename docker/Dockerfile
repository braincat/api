FROM tomcat:8-jre8
MAINTAINER Structurizr <help@structurizr.com>
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/ROOT"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/docs"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/examples"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/manager"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/host-manager"]

COPY server.xml /usr/local/tomcat/conf
COPY ROOT.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]