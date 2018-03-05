<%@ tag description="Page Body Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="heading" required="true" %>
<%@ attribute name="imageList" required="false" %>
<%@ attribute name="processModals" required="false" %>
<body>
    <header>
        <nav class="top-nav">
            <div class="container">
                <div class="nav-wrapper">
                    <a class="page-title">${heading}</a>
                    <a class="page-title right">| OCR 4 All</a>
                </div>
            </div>
        </nav>

        <div class="container">
            <a href="#" data-activates="slide-out" class="button-collapse top-nav"><i class="material-icons">menu</i></a>
        </div>

        <ul id="slide-out" class="side-nav">
            <li class="site-description">OCR 4 All</li>
            <li><a href="${pageContext.request.contextPath}">Project Overview</a></li>
            <li><a href="Preprocessing">Preprocessing</a></li>
            <li><a href="Despeckling">Despeckling</a></li>
            <li><a href="Segmentation">Segmentation</a></li>
            <li><a href="RegionExtraction">Region Extraction</a></li>
            <li><a href="LineSegmentation">Line Segmentation</a></li>
            <li><a href="/GTC_Web" target="_blank">Ground Truth Correction</a></li>
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
            <c:otherwise>
                <c:choose>
                    <%-- Adds page image list on the right side of the page --%>
                    <c:when test="${not empty imageList}">
                        <ul id="imageList" class="side-nav image-list">
                            <li class="heading"><i class="material-icons image-list-trigger">remove_red_eye</i>Pages</li>
                            <li class="select-all">
                                <input type="checkbox" class="" id="selectAll" />
                                <label for="selectAll"></label>
                                Select all
                            </li>
                        </ul>
                    </c:when>
                </c:choose>

                <jsp:doBody />

                <c:choose>
                    <%-- Process specific modals that can be included if needed --%>
                    <c:when test="${not empty processModals}">
                        <!-- In progress information -->
                        <div id="modal_inprogress" class="modal">
                            <div class="modal-content">
                                <h4>Information</h4>
                                <p>
                                    There already is a running ${heading} process.<br/>
                                    Please wait until it is finished or cancel it.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Error handling -->
                        <div id="modal_errorhandling" class="modal">
                            <div class="modal-content red-text">
                                <h4>Information</h4>
                                <p>
                                    No pages were selected.<br/>
                                    Please select some pages to start the ${heading} process.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- No current process information -->
                        <div id="modal_noprocess" class="modal">
                            <div class="modal-content">
                                <h4>Information</h4>
                                <p>There exists no ongoing ${heading} process.</p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Successful cancel information -->
                        <div id="modal_successfulcancel" class="modal">
                            <div class="modal-content">
                                <h4>Information</h4>
                                <p>The ${heading} process was cancelled successfully.</p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Failed cancel information -->
                        <div id="modal_failcancel" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>The ${heading} process could not be cancelled.</p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                    </c:when>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </main>
</body>
