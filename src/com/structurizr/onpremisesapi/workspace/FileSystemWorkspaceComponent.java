package com.structurizr.onpremisesapi.workspace;

import com.structurizr.annotation.UsesContainer;
import com.structurizr.onpremisesapi.domain.UUID;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple workspace component implementation that uses the local file system.
 */
@UsesContainer(name = "File System", description = "Gets information from")
class FileSystemWorkspaceComponent implements WorkspaceComponent {

    private File dataDirectory;

    FileSystemWorkspaceComponent(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public Collection<WorkspaceSummary> getWorkspaces() {
        Collection<WorkspaceSummary> workspaces = new ArrayList<>();

        File[] files = dataDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file != null && file.isDirectory() && file.getName().matches("\\d*")) {
                    long workspaceId = Long.parseLong(file.getName());
                    WorkspaceSummary workspace = new WorkspaceSummary(workspaceId);

                    try {
                        workspace.setKey(UUID.isUUID(getApiKey(workspaceId)));
                    } catch (WorkspaceComponentException e) {
                        workspace.setKey(false);
                    }

                    try {
                        workspace.setSecret(UUID.isUUID(getApiSecret(workspaceId)));
                    } catch (WorkspaceComponentException e) {
                        workspace.setSecret(false);
                    }

                    try {
                        workspace.setData(!getWorkspace(workspaceId).equals("{}"));
                    } catch (WorkspaceComponentException e) {
                        workspace.setData(false);
                    };

                    workspaces.add(workspace);
                }
            }
        }

        return workspaces;
    }

    @Override
    public boolean createWorkspace(long workspaceId, String key, String secret) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File keyPath = new File(path, "key.txt");
            File secretPath = new File(path, "secret.txt");

            if (!keyPath.exists() && !secretPath.exists()) {
                Files.write(keyPath.toPath(), key.getBytes("UTF-8"));
                Files.write(secretPath.toPath(), secret.getBytes("UTF-8"));

                return true;
            } else {
                return false;
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Could not create workspace " + workspaceId, ioe);
        }
    }

    @Override
    public String getWorkspace(long workspaceId) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File file = new File(path, "workspace.json");
            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()), "UTF-8");
            } else {
                return "{}";
            }
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
                throw new WorkspaceComponentException("Could not find API key at " + file.getCanonicalPath());
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
                throw new WorkspaceComponentException("Could not find API secret at " + file.getCanonicalPath());
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Error getting API secret for workspace " + workspaceId, ioe);
        }
    }

    @Override
    public RenderedImage getImage(long workspaceId, String name) throws WorkspaceComponentException {
        try {
            File path = getPathToWorkspace(workspaceId);
            File[] files = path.listFiles(file -> file.getName().equals(name));

            if (files != null && files.length == 1) {
                return ImageIO.read(files[0]);
            } else {
                return null;
            }
        } catch (IOException ioe) {
            throw new WorkspaceComponentException("Error getting resource named " + name + " from workspace " + workspaceId, ioe);
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
