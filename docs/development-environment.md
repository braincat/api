This section describes the development environment required for contributing and building the Structurizr API.

## Building

To build the Structurizr API from the source code, you need to clone this repository and run the Gradle build. Java 8 is required.

```
git clone https://github.com/structurizr/api.git structurizr-api
cd structurizr-api
./gradlew build assemble
```

The ```build/libs``` directory will contain the assembled WAR file when the build is successful.