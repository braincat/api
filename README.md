![Structurizr](docs/structurizr-banner.png)

# Structurizr API

This GitHub repository is a simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's [on-premises API feature](https://structurizr.com/help/on-premises-api). It supports the two basic operations required to ```GET``` and ```PUT``` workspaces, with workspace definitions being stored on the file system.

From a technical perspective, this implementation is a simple Java EE web application that can be run on a server such as Apache Tomcat, Jetty, etc.

## Data storage

Workspace data is stored on the file system, in the location defined by the ```dataDirectory``` parameter in the ```web.xml``` configuration file. By default, this is set to ```/usr/local/structurizr```.

This data directory contains one sub-directory per workspace, each of which is named to reflect the workspace ID. Underneath each workspace sub-directory are three files:

- workspace.json: the workspace definition as JSON
- key.txt: the Structurizr API key for the workspace
- secret.txt: the Structurizr API secret for the workspace

## HTTP vs HTTPS

Due to the [Same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy), the Structurizr API needs to be accessible using HTTPS. A self-signed certificate is sufficient if trusted (see below).

## Building from source

To build the Structurizr API from the source code, you need to clone this repository and run the Gradle build. Java 8 is required.

```
git clone https://github.com/structurizr/api.git structurizr-api
cd structurizr-api
./gradlew build assemble
```

The ```build/libs``` directory contains the assembled WAR file.

## Docker image

This repository also includes a Dockerfile that can be used to create a Docker image consisting of Java 8, Apache Tomcat 8.x and the Structurizr API web application. A pre-built Docker image is available on the [Docker Hub](https://hub.docker.com/r/structurizr/api/). Ports 8080 and 8443 expose HTTP and HTTPS respectively. You can pull a copy of the image using the following command.

```docker pull structurizr/api```

## TODO

- Generating a self-signed certificate.
- Trusting a self-signed certificate from Java, web browser, etc.
- Configuring the certificate in Apache Tomcat.
- Running the Docker image with an external data volume.
- Configuring the certificate for Docker.
- Preparing a workspace for local API access.
