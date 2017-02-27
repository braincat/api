package com.structurizr.onpremisesapi.web.home;

import com.structurizr.annotation.UsedByPerson;
import com.structurizr.onpremisesapi.web.AbstractServlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles requests for the home page.
 */
@UsedByPerson(name = "Software Developer", description = "Views API information using")
public class HomePageServlet extends AbstractServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("workspaces", getWorkspaceComponent().getWorkspaces());
        String apiUrl = request.getRequestURL().toString();
        if (apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length()-1);
        }
        request.setAttribute("apiUrl", apiUrl);
        request.setAttribute("dataDirectory", getDataDirectory());

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/views/home.jsp");
        dispatcher.forward(request,response);
    }

}