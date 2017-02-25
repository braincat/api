# Development Environment

This section describes the development environment required to build the Structurizr API.

## Getting the source code

The source code is open source and available on GitHub at [https://github.com/structurizr/api](https://github.com/structurizr/api).

Assuming that you have ```git``` installed, use the following command to clone the repo.

```
git clone https://github.com/structurizr/api.git structurizr-api
```

## Building

To build the Structurizr API from the source code, simply run the Gradle build (Java 8 is required).

```
cd structurizr-api
./gradlew build
```

The ```build/libs``` directory will contain the assembled WAR file (e.g. ```structurizr-api-x.y.war```) when the build is successful.