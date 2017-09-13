<%@ tag description="Page Body Tag" pageEncoding="UTF-8" %>
<%@ attribute name="heading" required="true" %>
<body>
    <header>
        <nav class="top-nav">
            <div class="container">
                <div class="nav-wrapper">
                    <a class="page-title">${heading}</a>
                </div>
            </div>
        </nav>

        <ul id="slide-out" class="side-nav fixed">
            <li class="site-description">OCR 4 All Web</li>
            <li><a href="${pageContext.request.contextPath}">Project Overview</a></li>
            <li><a href="#!">Preprocessing</a></li>
            <li><a href="#!">Despeckling</a></li>
            <li><a href="#!">Segmentation</a></li>
            <li><a href="#!">Region Extraction</a></li>
            <li><a href="#!">Line Segmentation</a></li>
            <li><a href="#!">Ground Truth Creation</a></li>
            <li><a href="#!">Line Selection For Training</a></li>
            <li><a href="#!">Train/Test Split</a></li>
            <li><a href="#!">Training</a></li>
            <li><a href="#!">Character Recognition</a></li>
        </ul>
    </header>

    <main>
        <jsp:doBody />
    </main>
</body>
