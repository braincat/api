package com.structurizr.onpremisesapi.workspace;

import com.structurizr.annotation.UsesContainer;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * A simple workspace component implementation that uses the local file system.
 */
@UsesContainer(name = "File System", description = "Stores information on")
class FileSystemWorkspaceComponent implements WorkspaceComponent {

    private File dataDirectory;

    public FileSystemWorkspaceComponent(File dataDirectory) {
        this.dataDirectory = dataDirectory;
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
