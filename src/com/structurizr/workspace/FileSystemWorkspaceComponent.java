package com.structurizr.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A simple workspace component implementation that uses the local file system.
 */
public class FileSystemWorkspaceComponent implements WorkspaceComponent {

    private File dataDirectory;

    public FileSystemWorkspaceComponent(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public String getWorkspace(long workspaceId) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File file = new File(path, "workspace.json");

            return new String(Files.readAllBytes(file.toPath()), "UTF-8");
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Could not get workspace " + workspaceId, ioe);
        }
    }

    @Override
    public void putWorkspace(long workspaceId, String json) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File file = new File(path, "workspace.json");

            Files.write(file.toPath(), json.getBytes("UTF-8"));
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Could not put workspace " + workspaceId, ioe);
        }
    }

    @Override
    public String getApiKey(long workspaceId) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File file = new File(path, "key.txt");
            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()), "UTF-8").trim();
            } else {
                throw new WorkspaceComponentException("Could not find API key for workspace " + workspaceId);
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Error getting API key for workspace " + workspaceId, ioe);
        }
    }

    @Override
    public String getApiSecret(long workspaceId) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File file = new File(path, "secret.txt");
            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()), "UTF-8").trim();
            } else {
                throw new WorkspaceComponentException("Could not find API secret for workspace " + workspaceId);
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Error getting API secret for workspace " + workspaceId, ioe);
        }
    }

    private File getPathToWorkspace(long workspaceId) {
        File path = new File(dataDirectory, "" + workspaceId);
        if (!path.exists()) {
            path.mkdirs();
        }

        return path;
    }

}
