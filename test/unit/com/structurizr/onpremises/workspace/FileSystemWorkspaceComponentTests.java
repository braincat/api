package com.structurizr.onpremises.workspace;

import com.structurizr.Workspace;
import com.structurizr.io.json.JsonWriter;
import org.junit.After;
import org.junit.Test;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import static org.junit.Assert.*;

public class FileSystemWorkspaceComponentTests {

    private File dataDirectory = new File("build/FileSystemWorkspaceComponentTests");
    private FileSystemWorkspaceComponent workspaceComponent = new FileSystemWorkspaceComponent(dataDirectory);

    @Test
    public void test_createWorkspace_ReturnsFalse_WhenTheAPIKeyAlreadyExists() throws Exception {
        File dir = new File(dataDirectory, "1");
        dir.mkdirs();
        File file = new File(dir, "key.txt");
        Files.write(file.toPath(), "2b1a855d-3825-4659-8ad2-79c2d96f8be2".getBytes());

        assertFalse(workspaceComponent.createWorkspace(1, "key", "secret"));
    }

    @Test
    public void test_createWorkspace_ReturnsFalse_WhenTheAPISecretAlreadyExists() throws Exception {
        File dir = new File(dataDirectory, "1");
        dir.mkdirs();
        File file = new File(dir, "secret.txt");
        Files.write(file.toPath(), "2b1a855d-3825-4659-8ad2-79c2d96f8be2".getBytes());

        assertFalse(workspaceComponent.createWorkspace(1, "key", "secret"));
    }

    @Test
    public void test_createWorkspace_SavesTheAPIKeyAndSecret_WhenTheAPIKeyAndSecretDoNotExist() throws Exception {
        assertTrue(workspaceComponent.createWorkspace(1, "key", "secret"));
        assertEquals("key", workspaceComponent.getApiKey(1));
        assertEquals("secret", workspaceComponent.getApiSecret(1));
    }

    @Test
    public void test_putAndGetWorkspace() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setThumbnail("Thumbnail");

        JsonWriter jsonWriter = new JsonWriter(false);
        StringWriter stringWriter = new StringWriter();
        jsonWriter.write(workspace, stringWriter);

        String content = stringWriter.toString();
        workspaceComponent.putWorkspace(1, content);

        Properties properties = new Properties();
        File propertiesFile = new File(new File(dataDirectory, "1"), "workspace.properties");
        properties.load(new FileReader(propertiesFile));
        assertEquals("Name", properties.getProperty("name"));
        assertEquals("Description", properties.getProperty("description"));
        assertEquals("Thumbnail", properties.getProperty("thumbnail"));

        assertEquals(content, workspaceComponent.getWorkspace(1));
    }

    @Test
    public void test_getWorkspace_ReturnsAnEmptyJsonString_WhenTheWorkspaceDoesNotExist() {
        try {
            String json = workspaceComponent.getWorkspace(1);
            assertEquals("{}", json);
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
    public void test_getApiKey_ThrowsAnException_WhenTheApiKeyDoesNotExist() throws Exception {
        try {
            workspaceComponent.getApiKey(1);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not find API key at " + dataDirectory.getCanonicalPath() + File.separator + "1" + File.separator + "key.txt", e.getMessage());
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
    public void test_getApiSecret_ThrowsAnException_WhenTheApiSecretDoesNotExist() throws Exception {
        try {
            workspaceComponent.getApiSecret(1);
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("Could not find API secret at " + dataDirectory.getCanonicalPath() + File.separator + "1" + File.separator + "secret.txt", e.getMessage());
        }
    }

    @Test
    public void test_getImage_ReturnsNull_WhenTheWorkspaceDoesNotExist() throws Exception {
        assertNull(workspaceComponent.getImage(1234, "image.png"));
    }

    @Test
    public void test_getImage_ReturnsNull_WhenTheImageDoesNotExist() throws Exception {
        File dir = new File(dataDirectory, "1");
        dir.mkdirs();

        assertNull(workspaceComponent.getImage(1, "image.png"));
    }

    @Test
    public void test_getImage_ReturnsAnImage_WhenTheImageDoesExist() throws Exception {
        File dir = new File(dataDirectory, "1");
        dir.mkdirs();

        File source = new File("test/unit/com/structurizr/onpremises/image.png");
        File destination = new File(dir, "image.png");
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

        RenderedImage image = workspaceComponent.getImage(1, "image.png");
        assertNotNull(image);
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
