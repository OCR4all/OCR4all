<%@ tag description="Page Body Tag" pageEncoding="UTF-8" %>
<%@ attribute name="heading" required="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<body>
    <header>
        <nav class="top-nav">
            <div class="container">
                <div class="nav-wrapper">
                    <a class="page-title">${heading}</a>
                </div>
            </div>
        </nav>

        <div class="container">
            <a href="#" data-activates="slide-out" class="button-collapse top-nav hide-on-large-only"><i class="material-icons">menu</i></a>
        </div>

        <ul id="slide-out" class="side-nav fixed">
            <li class="site-description">OCR 4 All Web</li>
            <li><a href="${pageContext.request.contextPath}">Project Overview</a></li>
            <li><a href="Preprocessing">Preprocessing</a></li>
            <li><a href="Despeckling">Despeckling</a></li>
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
        <c:choose>
            <%-- In case of an error in the controller, show its message and hide site content --%>
            <c:when test="${not empty error}">
                <div class="container red-text">
                    <h4>Error</h4>
                    <p>${error}</p>
                </div>
            </c:when>
            <%-- If no error exists, display site content --%>
            <c:otherwise><jsp:doBody /></c:otherwise>
        </c:choose>
    </main>
</body>
