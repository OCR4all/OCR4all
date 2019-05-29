<%@ tag description="Page Body Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
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
                    <a class="project-name right">${projectName}</a>
                </div>
            </div>
        </nav>

        <div class="container">
            <a href="#" data-activates="slide-out" class="button-collapse top-nav"><i class="material-icons">menu</i></a>
        </div>

        <ul id="slide-out" class="side-nav">
            <li class="site-description">OCR 4 All</li>
            <li class="nav-separator"></li>
            <li><a href="${pageContext.request.contextPath}">Project Overview</a></li>
            <li class="nav-separator"></li>
            <li><a href="ProcessFlow">Process Flow</a></li>
            <li class="nav-separator"></li>
            <li><a href="Preprocessing">Preprocessing</a></li>
            <li><a href="Despeckling">Noise Removal</a></li>
            <li>
                <ul class="collapsible collapsible-expandable">
                    <li>
                        <a class="collapsible-header">Segmentation</a>
                        <div class="collapsible-body">
                            <ul>
                                <li><a href="SegmentationLarex">LAREX</a></li>
                                <li><a href="SegmentationDummy">Dummy</a></li>
                            <%--<li><a href="SegmentationPixelClassifier">Pixel Classifier</a></li>--%>
                            </ul>
                        </div>
                    </li>
                </ul>
            </li>

			<c:choose>
				<c:when test='${(not empty processingMode) && (processingMode == "Pagexml")}'>
				</c:when>
				<c:otherwise>
            <li><a href="RegionExtraction">Region Extraction</a></li>
				</c:otherwise>
			</c:choose>
            <li><a href="LineSegmentation">Line Segmentation</a></li>
            <li><a href="Recognition">Recognition</a></li>
			<c:choose>
				<c:when test='${(not empty processingMode) && (processingMode == "Pagexml")}'>
			<li><a href="GroundTruthProductionLarex">Ground Truth Production</a></li>
				</c:when>
				<c:otherwise>
            <li><a href="/GTC_Web?gtcDir=${fn:replace(projectDir, '\\', '/')}processing&dirType=pages" target="_blank">Ground Truth Production</a></li>
				</c:otherwise>
			</c:choose>
            <li><a href="Training">Training</a></li>
            <li><a href="Evaluation">Evaluation</a></li>
			<c:choose>
				<c:when test='${(not empty processingMode) && (processingMode == "Pagexml")}'>
            <li><a href="PostCorrectionLarex">Post Correction</a></li>
				</c:when>
				<c:otherwise>
            <li><a href="PostCorrection">Post Correction</a></li>
				</c:otherwise>
			</c:choose>
            <li><a href="ResultGeneration">Result Generation</a></li>
            <li class="nav-separator"></li>
        </ul>
    </header>
    <main>
        <c:choose>
            <%-- Prevent visiting module pages if a project adjustment is currently in progress --%>
            <c:when test="${not empty projectAdjustment && heading != 'Project Overview'}">
                <div class="container red-text">
                    <h4>Error</h4>
                    <p>${projectAdjustment}</p>
                </div>
            </c:when>
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
                        <!-- Execute failed -->
                        <div id="modal_executefailed" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    The execution of the ${heading} process failed.<br/>
                                    Please check for status errors and try to execute again later.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Process of the same type is still running -->
                        <div id="modal_sameprocesstype" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    The execution of the ${heading} process failed.<br/>
                                    A process of the same type is still running.<br/>
                                    Please wait until this process is finished and start the process again later.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Process conflicts with upcoming process -->
                        <div id="modal_processconflict" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    The execution of the ${heading} process failed.<br/>
                                    Another process that requires files provided by this process is still running.<br/>
                                    Please wait until this process is finished and start the process again later.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- ProcessFlow conflict -->
                        <div id="modal_processflowconflict" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    The execution of the ${heading} process failed.<br/>
                                    A ProcessFlow is still running.<br/>
                                    Please wait until the ProcessFlow is finished and start the process again later.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Selection error handling -->
                        <div id="modal_errorhandling" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    No pages were selected.<br/>
                                    Please select some pages to start the ${heading} process.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                        </div>
                        <!-- Input error handling-->
                        <div id="modal_inputerror" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    There exists an error in the input.<br/>
                                    Please fix it and try again.
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
                        <!-- Note that old process related files exist -->
                        <div id="modal_exists" class="modal">
                            <div class="modal-content">
                                <h4 class="red-text">Attention</h4>
                                <p>
                                    Some or all of the selected pages were already processed in a previous execution.<br/>
                                    If you agree these old process related files will be removed before the execution.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat ">Disagree</a>
                                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                         </div>
                        <!-- Check for process related files failed-->
                        <div id="modal_exists_failed" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    It could not be verified if old process related files exist.<br/>
                                    Due to this error, the process cannot be started to ensure data integrity.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                         </div>
                        <!-- Error in parameter passing (Error Code 400)-->
                        <div id="modal_settings_failed" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    Parameter were not passed correctly<br/>
                                    Due to this error, the process cannot be started to ensure data integrity.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                         </div>
                        <!-- Error in the execution (Error Code 500)-->
                        <div id="modal_execution_failed" class="modal">
                            <div class="modal-content red-text">
                                <h4>Error</h4>
                                <p>
                                    Error in the execution<br/>
                                    Due to this error, the process cannot be started to ensure data integrity.
                                </p>
                            </div>
                            <div class="modal-footer">
                                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                            </div>
                         </div>
                     </c:when>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </main>

    <%-- Hide footer on page load for image list pages (position will be calculated dynamically) --%>
    <t:footer hideOnPageLoad="${imageList}"></t:footer>
</body>
