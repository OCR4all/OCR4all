<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function(){
                $('.materialboxed').materialbox();
            });
        </script>

        <title>OCR4all_Web - Page Overview</title>
    </t:head>
    <t:body heading="Page Overview">
        <div class="container">
            <div class="section">
                <table class="striped centered">
                    <thead>
                        <tr>
                            <th>Page Identifier</th>
                            <th>Preprocessed</th>
                            <th>Segmented</th>
                            <th>Segments extracted</th>
                            <th>Lines extracted</th>
                            <th>Has GT</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>${pageOverview.pageId}</td>
                            <td><c:choose><c:when test="${pageOverview.preprocessed == 'true'}"><i class="material-icons green-text">check</i></c:when><c:otherwise><i class="material-icons red-text">clear</i></c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${pageOverview.segmented == 'true'}"><i class="material-icons green-text">check</i></c:when><c:otherwise><i class="material-icons red-text">clear</i></c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${pageOverview.segmentsExtracted == 'true'}"><i class="material-icons green-text">check</i></c:when><c:otherwise><i class="material-icons red-text">clear</i></c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${pageOverview.linesExtracted == 'true'}"><i class="material-icons green-text">check</i></c:when><c:otherwise><i class="material-icons red-text">clear</i></c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${pageOverview.hasGT == 'true'}"><i class="material-icons green-text">check</i></c:when><c:otherwise><i class="material-icons red-text">clear</i></c:otherwise></c:choose></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="section">
                <div class="row">
                    <div class="col s6">
                        <h4>Images</h4>
                        <ul class="collapsible popout" data-collapsible="accordion">
                            <li>
                                <div class="collapsible-header"><i class="material-icons">image</i>Original</div>
                                <div class="collapsible-body">
                                    <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Original}">
                                </div>
                            </li>
                            <li>
                                <div class="collapsible-header active"><i class="material-icons">image</i>Binary</div>
                                <div class="collapsible-body">
                                    <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Binary}">
                                </div>
                            </li>
                            <li>
                                <div class="collapsible-header"><i class="material-icons">image</i>Gray</div>
                                <div class="collapsible-body">
                                    <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Gray}">
                                </div>
                            </li>
                            <li>
                                <div class="collapsible-header"><i class="material-icons">image</i>Despeckled</div>
                                <div class="collapsible-body">
                                    <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Despeckled}">
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div class="col s6">
                        <h4>Segments</h4>
                        <ul class="collapsible popout" data-collapsible="accordion">
                            <c:forEach items="${segments}" var="seg">
                            <li>
                                <div class="collapsible-header"><i class="material-icons">art_track</i>${seg.key}</div>
                                <div class="collapsible-body">
                                    <ul class="collapsible popout" data-collapsible="accordion">
                                        <c:forEach var="line" items="${seg.value}">
                                            <li>
                                                <div class="collapsible-header"><i class="material-icons">short_text</i>${line}</div>
                                                <div class="collapsible-body">
                                                </div>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </t:body>
</t:html>
