package com.structurizr.onpremisesapi.web;

import com.structurizr.annotation.UsesComponent;
import com.structurizr.onpremisesapi.workspace.WorkspaceComponent;

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
        String dataDirectory = getServletContext().getInitParameter("dataDirectory");

        try {
            dataDirectory = InitialContext.doLookup("java:comp/env/structurizr/api/dataDirectory");
        } catch (NamingException e) {
            // no environment entry was found
        }

        return dataDirectory;
    }

}
