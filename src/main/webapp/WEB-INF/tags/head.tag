<%@ tag description="Page Head Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="imageList" required="false" %>
<%@ attribute name="processHandler" required="false" %>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <!--Import Google Icon Font-->
    <link href="resources/css/materialicons.css" rel="stylesheet">
    <!--Import materialize.css-->
    <link type="text/css" rel="stylesheet" href="resources/css/materialize.min.css"  media="screen,projection"/>

    <!--Let browser know website is optimized for mobile-->
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <!--Import jQuery before materialize.js-->
    <script type="text/javascript" src="resources/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="resources/js/materialize.min.js"></script>

    <!--Project OCR4All_Web related-->
    <link type="text/css" rel="stylesheet" href="resources/css/ocr4allweb.css">
    <script type="text/javascript" src="resources/js/ocr4allweb-helper.js"></script>

    <c:choose>
        <%-- Include JS files to provide image list functionality --%>
        <c:when test="${not empty imageList}">
            <script src="resources/js/withinviewport.js"></script>
            <script src="resources/js/jquery.withinviewport.js"></script>
            <script type="text/javascript" src="resources/js/ocr4allweb-imagelist.js"></script>
        </c:when>
    </c:choose>

    <c:choose>
        <%-- Include JS files to provide process handling functionality --%>
        <c:when test="${not empty processHandler}">
            <script type="text/javascript" src="resources/js/ocr4allweb-processhandler.js"></script>
        </c:when>
    </c:choose>

    <!--Collapsible elements (e.g. side navigation)-->
    <script type="text/javascript">
        $(document).ready(function() {
            // Navigation button (used if screen size is too small)
            $(".button-collapse").sideNav();

            // Sidebar active handler
            var pageTitle = $('.page-title').text();
            $(".side-nav li a").each(function() {
                if( $(this).text() == pageTitle )
                    $(this).parent().addClass("active");
            })

            // Initialize select form elements
            $('select').material_select();

            // Initialize modals
            $('.modal').modal();

            // Initialize tabs
            $('ul.tabs').tabs(); 
        });
    </script>

    <jsp:doBody />
</head>
