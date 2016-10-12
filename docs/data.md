Workspace data is stored on the file system, in the location defined by the ```dataDirectory``` parameter in the ```web.xml``` configuration file of the API Application. By default, this is set to ```/usr/local/structurizr```.

This data directory contains one sub-directory per workspace, each of which is named to reflect the workspace ID. Underneath each workspace sub-directory are three files:

- ```workspace.json``` - the workspace definition as JSON
- ```key.txt``` - the Structurizr API key for the workspace
- ```secret.txt``` - the Structurizr API secret for the workspace