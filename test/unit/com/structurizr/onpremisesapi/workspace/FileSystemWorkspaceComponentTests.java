package com.structurizr.onpremisesapi.workspace;

import com.structurizr.onpremisesapi.workspace.FileSystemWorkspaceComponent;
import com.structurizr.onpremisesapi.workspace.WorkspaceComponentException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FileSystemWorkspaceComponentTests {

    private File dataDirectory = new File("build/FileSystemWorkspaceComponentTests");
    private FileSystemWorkspaceComponent workspaceComponent = new FileSystemWorkspaceComponent(dataDirectory);

    @Test
    public void test_putAndGetWorkspace() throws Exception {

        String content = "Here is some JSON";
        workspaceComponent.putWorkspace(1, content);
        assertEquals("Here is some JSON", workspaceComponent.getWorkspace(1));
    }

    @Test
    public void test_getWorkspace_ThrowsAnException_WhenTheWorkspaceDoesNotExist() {
        try {
            workspaceComponent.getWorkspace(1);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not get workspace 1", e.getMessage());
        }
    }

    @Test
    public void test_getApiKey_ReturnsTheApiKey_WhenThereIsAnApiKey() throws Exception {
        File dir = new File(dataDirectory, "1");
        dir.mkdirs();
        File file = new File(dir, "key.txt");
        Files.write(file.toPath(), "2b1a855d-3825-4659-8ad2-79c2d96f8be2".getBytes());

        assertEquals("2b1a855d-3825-4659-8ad2-79c2d96f8be2", workspaceComponent.getApiKey(1));
    }

    @Test
    public void test_getApiKey_ThrowsAnException_WhenTheApiKeyDoesNotExist() {
        try {
            workspaceComponent.getApiKey(1);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not find API key for workspace 1", e.getMessage());
        }
    }

    @Test
    public void test_getApiSecret_ReturnsTheApiSecret_WhenThereIsAnApiSecret() throws Exception {
        File dir = new File(dataDirectory, "1");
        dir.mkdirs();
        File file = new File(dir, "secret.txt");
        Files.write(file.toPath(), "2b1a855d-3825-4659-8ad2-79c2d96f8be2".getBytes());

        assertEquals("2b1a855d-3825-4659-8ad2-79c2d96f8be2", workspaceComponent.getApiSecret(1));
    }

    @Test
    public void test_getApiSecret_ThrowsAnException_WhenTheApiSecretDoesNotExist() {
        try {
            workspaceComponent.getApiSecret(1);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not find API secret for workspace 1", e.getMessage());
        }
    }

    @After
    public void tearDown() {
        deleteDirectory(dataDirectory);
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }

}
