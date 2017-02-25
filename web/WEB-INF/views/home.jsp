<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Structurizr API</title>

    <link href="static/css/bootstrap-3.3.2.min.css" rel="stylesheet" media="screen" />
    <link href="static/css/bootstrap-theme-3.3.2.min.css" rel="stylesheet" media="screen" />
    <link href="static/css/structurizr.css" rel="stylesheet" media="screen" />

    <link rel="icon" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" href="/static/img/apple-touch-icon.png" />

    <script src="static/js/jquery-2.0.3.min.js"></script>
    <script src="static/js/lodash-3.10.1.min.js"></script>
    <script src="static/js/backbone-1.2.1.min.js"></script>
    <script src="static/js/bootstrap-3.3.2.min.js"></script>
</head>
<body>
    <div class="section">
        <div class="container">
            <div class="hidden">
                <h1>Structurizr</h1>
                <p>
                    Visualise, document and explore your software architecture
                </p>
            </div>
            <div class="row">
                <div class="col-md-1"></div>
                <div class="col-md-10 centered">
                    <img src="static/img/structurizr-logo.png" alt="Structurizr" class="img-responsive" />
                </div>
                <div class="col-md-1"></div>
            </div>
        </div>
    </div>

    <div class="section">
        <div class="container centered">
            <h1>Structurizr API</h1>
            <p>
                This web application is a simple implementation of the Structurizr API, which is designed to be run on-premises to support Structurizr's <a href="https://structurizr.com/help/on-premises-api">on-premises API feature</a>.
                The API URL is <code>${apiUrl}</code>.
            </p>

            <h2>Workspaces</h2>
            <c:choose>
            <c:when test="${not empty workspaces}">
            <p>
                Workspace data is being stored at <code>${initParam['dataDirectory']}</code> and here is a summary of the workspaces that have been configured.
            </p>

            <br />

            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th class="centered" width="25%">Workspace ID</th>
                    <th class="centered" width="25%">API key</th>
                    <th class="centered" width="25%">API secret</th>
                    <th class="centered" width="25%">Data</th>
                </tr>
                </thead>
                <c:forEach var="workspace" items="${workspaces}">
                    <tr>
                        <td class="centered"><a href="https://structurizr.com/workspace/${workspace.id}">${workspace.id}</a></td>
                        <td class="centered"><c:choose><c:when test="${workspace.key}"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></c:when><c:otherwise><span class="glyphicon glyphicon-remove" aria-hidden="true"></c:otherwise></c:choose></td>
                        <td class="centered"><c:choose><c:when test="${workspace.secret}"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></c:when><c:otherwise><span class="glyphicon glyphicon-remove" aria-hidden="true"></c:otherwise></c:choose></td>
                        <td class="centered"><c:choose><c:when test="${workspace.data}"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></c:when><c:otherwise><span class="glyphicon glyphicon-remove" aria-hidden="true"></c:otherwise></c:choose></td>
                    </tr>
                </c:forEach>
            </table>
            </c:when>
            <c:otherwise>
            <p>
                Workspace data is being stored at <code>${initParam['dataDirectory']}</code> but there are no workspaces configured yet.
            </p>
            </c:otherwise>
            </c:choose>
        </div>
    </div>

    <footer>
        <a href="https://structurizr.com/public/18571/documentation">Documentation</a>
        |
        <a href="https://github.com/structurizr/api">Source code</a>
    </footer>
</body>
</html>
