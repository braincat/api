<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Structurizr API</title>

    <link href="static/css/bootstrap-3.3.2.min.css" rel="stylesheet" media="screen" />
    <link href="static/css/bootstrap-theme-3.3.2.min.css" rel="stylesheet" media="screen" />
    <link href="static/css/structurizr.css" rel="stylesheet" media="screen" />
    <link href="static/css/open-sans.css" rel="stylesheet" media="screen" />

    <link rel="icon" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" href="/static/img/apple-touch-icon.png" />

    <script src="static/js/jquery-2.0.3.min.js"></script>
    <script src="static/js/lodash-3.10.1.min.js"></script>
    <script src="static/js/backbone-1.2.1.min.js"></script>
    <script src="static/js/bootstrap-3.3.2.min.js"></script>
</head>
<body>

    <div id="topNavigation">
        <nav class="navbar navbar-inverse navbar-fixed-top structurizrBackgroundDarker" style="border: none;" role="navigation">
            <div class="container-fluid">
                <!-- Brand and toggle get grouped for better mobile display -->
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="/"><span class="glyphicon glyphicon-home" aria-hidden="true"></span></a>
                </div>

                <!-- Collect the nav links, forms, and other content for toggling -->
                <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                    <ul class="nav navbar-nav">
                        <li class="hidden-sm"><a href="https://structurizr.com/help/about">About</a></li>
                        <li class="hidden-sm"><a href="https://structurizr.com/help/visualise">Visualise</a></li>
                        <li class="hidden-sm"><a href="https://structurizr.com/help/document">Document</a></li>
                        <li class="hidden-sm"><a href="https://structurizr.com/help/explore">Explore</a></li>
                        <li class="hidden-sm"><a href="https://structurizr.com/help/compare">Compare</a></li>
                        <li><a href="https://structurizr.com/help/examples">Examples</a></li>
                        <li><a href="https://structurizr.com/help">Help</a></li>
                    </ul>
                </div><!-- /.navbar-collapse -->
            </div><!-- /.container-fluid -->
        </nav>
    </div>

    <div class="content">
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
                        <img src="/static/img/structurizr-banner.png" alt="Structurizr" class="img-responsive" style="margin-top: 20px" />
                    </div>
                    <div class="col-md-1"></div>
                </div>
            </div>
        </div>

        <div class="section">
            <div class="container centered">
                <p>
                    Workspace data: <code>${dataDirectory}</code>
                    <br />
                    API URL: <code>${apiUrl}</code>
                </p>
            </div>
        </div>

        <div class="section">
            <div class="container">
                <h2>Workspaces</h2>
                <c:choose>
                <c:when test="${not empty workspaces}">
                <p>
                    Here is a summary of your workspaces.
                </p>

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
                            <td class="centered">${workspace.id}</td>
                            <td class="centered"><c:choose><c:when test="${workspace.key}"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></c:when><c:otherwise><span class="glyphicon glyphicon-remove" aria-hidden="true"></c:otherwise></c:choose></td>
                            <td class="centered"><c:choose><c:when test="${workspace.secret}"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></c:when><c:otherwise><span class="glyphicon glyphicon-remove" aria-hidden="true"></c:otherwise></c:choose></td>
                            <td class="centered"><c:choose><c:when test="${workspace.data}"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></c:when><c:otherwise><span class="glyphicon glyphicon-remove" aria-hidden="true"></c:otherwise></c:choose></td>
                        </tr>
                    </c:forEach>
                </table>
                </c:when>
                <c:otherwise>
                    <p>
                        There are no workspaces.
                    </p>
                </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div id="footer" class="structurizrBackgroundDark">
            <div class="container">
                <p>
                    <a href="https://structurizr.com/public/18571/documentation">Documentation</a>
                    |
                    <a href="https://github.com/structurizr/api">Source code</a>
                </p>

                <p>
                    <a href="mailto:help@structurizr.com">help@structurizr.com</a> |
                    <a href="https://structurizr.slack.com">Slack</a> |
                    <a href="https://groups.google.com/d/forum/structurizr">Google Group</a> |
                    <a href="https://github.com/structurizr">GitHub</a> |
                    <a href="https://twitter.com/structurizr">Twitter</a> |
                    <a href="https://facebook.com/structurizr">Facebook</a> |
                    <a href="https://medium.com/@structurizr">Blog</a> |
                    Version 0.4
                    <br />
                    Copyright &copy; Structurizr Limited
                </p>
            </div>
        </div>
    </div>
</body>
</html>
