package com.structurizr.onpremises.web;

import com.structurizr.annotation.UsesComponent;
import com.structurizr.onpremises.workspace.WorkspaceComponent;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public abstract class AbstractServlet extends HttpServlet {

    @UsesComponent(description = "Gets and puts workspace data using")
    private WorkspaceComponent workspaceComponent;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WorkspaceComponent workspaceComponent = WorkspaceComponent.create(getDataDirectory());
        setWorkspaceComponent(workspaceComponent);
    }

    public WorkspaceComponent getWorkspaceComponent() {
        return workspaceComponent;
    }

    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    protected String getDataDirectory() {
        return getConfigurationParameter("structurizr/dataDirectory", "structurizr.dataDirectory", "STRUCTURIZR_DATA_DIRECTORY", "/usr/local/structurizr");
    }

    private String getConfigurationParameter(String jndiEnvironmentEntryName, String systemPropertyName, String environmentVariableName, String defaultValue) {
        String value = getEnvironmentEntry(jndiEnvironmentEntryName);
        if (value == null) {
            value = getSystemProperty(systemPropertyName);
            if (value == null) {
                value = getEnvironmentVariable(environmentVariableName);
                if (value == null) {
                    value = defaultValue;
                }
            }
        }

        return value;
    }

    private String getEnvironmentEntry(String name) {
        try {
            return InitialContext.doLookup("java:comp/env/" + name);
        } catch (NamingException e) {
            return null;
        }
    }

    private String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }

    private String getSystemProperty(String name) {
        return System.getProperty(name);
    }

}
