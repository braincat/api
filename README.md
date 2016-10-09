![Structurizr](docs/structurizr-banner.png)

# Structurizr API

This GitHub repository is a simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's [on-premises API feature](https://structurizr.com/help/on-premises-api). It supports the two basic operations required to ```GET``` and ```PUT``` workspaces, with workspace definitions being stored on the file system.

From a technical perspective, this implementation is a simple Java EE web application that can be run on a server such as Apache Tomcat, Jetty, etc. The software architecture diagrams and documentation can be found at [https://structurizr.com/public/18571](https://structurizr.com/public/18571).

## Table of contents

1. [Data storage](#data-storage)
1. [Building from source](#building)
1. [Deploying into Apache Tomcat](#docker)
1. [Using the pre-built Docker image](#docker)
1. [Using the on-premises API feature](#using)

## Data storage

Workspace data is stored on the file system, in the location defined by the ```dataDirectory``` parameter in the ```web.xml``` configuration file. By default, this is set to ```/usr/local/structurizr```.

This data directory contains one sub-directory per workspace, each of which is named to reflect the workspace ID. Underneath each workspace sub-directory are three files:

- ```workspace.json``` - the workspace definition as JSON
- ```key.txt``` - the Structurizr API key for the workspace
- ```secret.txt``` - the Structurizr API secret for the workspace

## Building

To build the Structurizr API from the source code, you need to clone this repository and run the Gradle build. Java 8 is required.

```
git clone https://github.com/structurizr/api.git structurizr-api
cd structurizr-api
./gradlew build assemble
```

The ```build/libs``` directory will contain the assembled WAR file when the build is successful.

## Deploying

To deploy the Structurizr API into your Java EE server, follow the deployment instructions provided by the server vendor. For Apache Tomcat, the simplest method is to copy the WAR file to the ```$CATALINA_HOME/webapps``` directory.

Due to the [Same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy), the Structurizr API needs to be accessible using HTTPS. A self-signed certificate is sufficient. See [SSL/TLS Configuration HOW-TO](https://tomcat.apache.org/tomcat-8.0-doc/ssl-howto.html) for information about configuring HTTPS.

If deployment is successful, you should see a page like this when you navigate to the webapp URL in your web browser.

![Structurizr](docs/structurizr-api.png)

## Docker

This repository also includes a Dockerfile that can be used to create a Docker image consisting of Java 8, Apache Tomcat 8.x and the Structurizr API web application.

A __pre-built Docker image__ is available on the [Docker Hub](https://hub.docker.com/r/structurizr/api/). You can pull a copy of the image using the following command.

```
docker pull structurizr/api
```

You can then run the Docker image using a command like the following.

```
docker run -p 9999:8443 -v /Users/simon/structurizr:/usr/local/structurizr structurizr/api
```

### Publishing the HTTPS port

By default, the Docker container doesn't expose any ports, although Apache Tomcat is listening for HTTPS requests on port 8443. The ```-p 9999:8443``` parameter in the above command publishes this port, making it accessible outside of the container on port 9999.

### Configuring data storage

The Structurizr API is configured to use ```/usr/local/structurizr``` for data storage. On startup of the container, you need to mount a data volume so that the Structurizr API inside the container can store data outside of the container. Keeping the data stored outside of the container allows you to upgrade the container in the future, while retaining your data.

The ```-v /Users/simon/structurizr:/usr/local/structurizr``` parameter in the above command maps the local ```/Users/simon/structurizr``` directory to ```/usr/local/structurizr``` inside the container.

### Configuring HTTPS

To support HTTPS, Apache Tomcat within the Docker container is preconfigured to look for a Java keystore at ```/usr/local/structurizr/keystore.jks``` and if you start the container without providing a Java keystore, you will see the following error message.
```java.io.FileNotFoundException: /usr/local/structurizr/keystore.jks (No such file or directory)```

Although configuring an SSL certificate is out of the scope of this documentation, you can get started by generating a self-signed certificate using the following command.

```
keytool -genkey -alias tomcat -keyalg RSA -keystore /Users/simon/structurizr/keystore.jks
```

Enter ```password``` for the keystore password when prompted.

```
Enter keystore password:  
Re-enter new password: 
What is your first and last name?
  [Unknown]:  localhost
What is the name of your organizational unit?
  [Unknown]:  
What is the name of your organization?
  [Unknown]:  My organization
What is the name of your City or Locality?
  [Unknown]:  
What is the name of your State or Province?
  [Unknown]:  
What is the two-letter country code for this unit?
  [Unknown]:  JE
Is CN=localhost, OU=Unknown, O=My organization, L=Unknown, ST=Unknown, C=JE correct?
  [no]:  y

Enter key password for <tomcat>
	(RETURN if same as keystore password): 
```

After starting the Docker container, you should be able to navigate to, for example, [https://localhost:9999](https://localhost:9999) in your web browser and see the same screenshot as above. You will need to trust the certificate in your web browser if using a self-signed certificate.

## Using

To use the on-premises API feature from Structurizr, you need to be subscribed to the [On-premises Plan](https://structurizr.com/pricing) or the free trial.

### Creating a remote workspace

In order to use the on-premises API feature, you need to create a remote workspace that tells Structurizr where to find the local, on-premises API. This is just an empty workspace - none of your data will be stored here.

After signing in to Structurizr, create a new workspace by clicking the "Create a new empty workspace" button on your dashboard.

![Structurizr](docs/empty-workspace-1.png)

Using the Structurizr client library, you then need to update the workspace by specifying a remote API that should be used for getting and putting workspace data. For example, with Java.

```java
Workspace workspace = new Workspace("My workspace", "A description of my workspace");
workspace.setApi("https://localhost:9999");
StructurizrClient structurizrClient = new StructurizrClient("5855ca93-73f6-4736-95c5-d7b6e2f43c30", "6fc691f0-789a-4075-8c6a-707e80c0537c");
structurizrClient.putWorkspace(18561, workspace);
```

If this succeeded, you will see a message in the program output.

```
INFO  StructurizrClient - Putting workspace with ID 18561
INFO  StructurizrClient - {"message":"OK"}
```

![Structurizr](docs/empty-workspace-2.png)

### Preparing the local workspace

With the remote workspace created, you next need to prepare the local Structurizr API. You can do this by opening a command prompt and changing to the directory that the Structurizr API is using to store data. This directory needs to contain a sub-directory for the workspace, which itself needs to contain a ```key.txt``` and ```secret,txt``` file. The contents of these files must correspond to the values shown on your Structrizr dashboard. For example.

```
mkdir 18561
cd 18561
echo '5855ca93-73f6-4736-95c5-d7b6e2f43c30' > key.txt
echo '6fc691f0-789a-4075-8c6a-707e80c0537c' > secret.txt
```

### Using the local workspace

You can now use the Structurizr client library in the usual way, with the exception that the remote API URL needs to be specified when creating the ```StructurizrClient``` object.

```java
StructurizrClient structurizrClient = new StructurizrClient("https://localhost:9999", "5855ca93-73f6-4736-95c5-d7b6e2f43c30", "6fc691f0-789a-4075-8c6a-707e80c0537c");
Workspace workspace = new Workspace("My workspace", "A description of my workspace");
Model model = workspace.getModel();

Person user = model.addPerson("User", "A user of my software system.");
SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System", "My software system.");
user.uses(softwareSystem, "Uses");

ViewSet viewSet = workspace.getViews();
SystemContextView contextView = viewSet.createSystemContextView(softwareSystem, "Context", "A description of this diagram.");
contextView.addAllSoftwareSystems();
contextView.addAllPeople();

structurizrClient.putWorkspace(18561, workspace);
```

```
INFO  StructurizrClient - Putting workspace with ID 18561
INFO  StructurizrClient - {"message":"OK"}
```

SSL handshake errors are likely if using a self-signed certificate because the Structurizr client program runtime won't trust the certificate served by the Structurizr API server. If using the Java client, you can use ```javax.net.ssl.trustStore``` JVM option to point to your keystore. For example.

```
-Djavax.net.ssl.trustStore=/Users/simon/Desktop/structurizr/keystore.jks
```

## TODO

- Trusting a self-signed certificate from .NET.
