![Structurizr](docs/structurizr-banner.png)

# Structurizr API

This GitHub repository is a simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's [on-premises API feature](https://structurizr.com/help/on-premises-api). It supports the two basic operations required to get and put workspaces, with workspace definitions being stored on the file system.

This repository also includes a Dockerfile that can be used to create a standalone Docker image consisting of Java, Tomcat and the Structurizr API.