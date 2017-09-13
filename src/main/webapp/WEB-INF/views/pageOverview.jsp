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
                <h4>Images</h4>
                <div id="images" class="col s12">
                    <div class="row">
                        <div class="col s3">
                            <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Original}">
                            <p>Original</p>
                        </div>
                        <div class="col s3">
                            <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Binary}">
                            <p>Binary</p>
                        </div>
                        <div class="col s3">
                           <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Despeckled}">
                            <p>Despeckled</p>
                        </div>
                        <div class="col s3">
                            <img class="materialboxed" width="100%" src="data:image/jpeg;base64, ${image.Gray}">
                            <p>Gray</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </t:body>
</t:html>
