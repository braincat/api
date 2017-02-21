package com.structurizr.onpremisesapi.web.home;

import com.structurizr.annotation.UsedByPerson;
import com.structurizr.annotation.UsesComponent;
import com.structurizr.onpremisesapi.workspace.WorkspaceComponent;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@UsedByPerson(name = "Software Developer", description = "Views API information using")
public class HomePageServlet extends HttpServlet {

    private static final int GUID_LENGTH = 36;

    @UsesComponent(description = "Gets and puts workspace data using")
    private WorkspaceComponent workspaceComponent;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WorkspaceComponent workspaceComponent = WorkspaceComponent.create(config.getServletContext().getInitParameter("dataDirectory"));
        setWorkspaceComponent(workspaceComponent);
    }

    void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("workspaces", workspaceComponent.getWorkspaces());

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/views/home.jsp");
        dispatcher.forward(request,response);
    }

}