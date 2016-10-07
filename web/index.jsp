<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Structurizr API</title>

    <link href="/static/css/bootstrap-3.3.2.min.css" rel="stylesheet" media="screen" />
    <link href="/static/css/bootstrap-theme-3.3.2.min.css" rel="stylesheet" media="screen" />
    <link href="/static/css/structurizr.css" rel="stylesheet" media="screen" />

    <script src="/static/js/jquery-2.0.3.min.js"></script>
    <script src="/static/js/lodash-3.10.1.min.js"></script>
    <script src="/static/js/backbone-1.2.1.min.js"></script>
    <script src="/static/js/bootstrap-3.3.2.min.js"></script>
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
                    <img src="/static/img/structurizr-logo.png" alt="Structurizr" class="img-responsive" />
                </div>
                <div class="col-md-1"></div>
            </div>
        </div>
    </div>

    <div class="section">
        <div class="container centered">
            <h2>Structurizr API</h2>
            <p>
                Workspace data is being stored at <code>${initParam['dataDirectory']}</code>
            </p>
        </div>
    </div>
</body>
</html>
