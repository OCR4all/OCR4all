<%@ tag description="Page Head Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="imageList" required="false" %>
<%@ attribute name="inputParams" required="false" %>
<%@ attribute name="processHandler" required="false" %>
<%@ attribute name="projectDataSel" required="false" %>
<%@ attribute name="recModelSelect" required="false" %>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <!--Import Google Icon Font-->
    <link href="${pageContext.servletContext.contextPath}/resources/css/materialicons.css" rel="stylesheet">
    <!--Import materialize.css-->
    <link type="text/css" rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/materialize.min.css"  media="screen,projection"/>

    <!--Let browser know website is optimized for mobile-->
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <!--Import jQuery before materialize.js-->
    <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/materialize.min.js"></script>

    <!--Project ocr4all related-->
    <link type="text/css" rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/ocr4allweb.css">
    <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/ocr4allweb-helper.js"></script>

    <!-- Hot fix patches -->
    <script type="text/javascript">
        /* Fix Chrome select drop down close on first click.
         * Needed for materialize css v0.100.2 (fixed in v1.0.0 and higher)
         * https://github.com/InfomediaLtd/angular2-materialize/issues/444#issuecomment-497063955
         */
        $(document).on("click",".select-wrapper",(e) => e.stopPropagation());
    </script>
    <c:choose>
        <%-- Include JS files to provide image list functionality --%>
        <c:when test="${not empty imageList}">
            <script src="${pageContext.servletContext.contextPath}/resources/js/withinviewport.js"></script>
            <script src="${pageContext.servletContext.contextPath}/resources/js/jquery.withinviewport.js"></script>
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/ocr4allweb-imagelist.js"></script>
        </c:when>
    </c:choose>

    <c:choose>
        <%-- Include JS files to provide module input parameter functionality --%>
        <c:when test="${not empty inputParams}">
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/ocr4allweb-inputparams.js"></script>
        </c:when>
    </c:choose>

    <c:choose>
        <%-- Include JS files to provide process handling functionality --%>
        <c:when test="${not empty processHandler}">
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/ocr4allweb-processhandler.js"></script>
        </c:when>
    </c:choose>

    <c:choose>
        <%-- Include JS files to provide project data selection functionality --%>
        <c:when test="${not empty projectDataSel}">
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/ocr4allweb-projdatasel.js"></script>
        </c:when>
    </c:choose>

    <c:choose>
        <%-- Include JS files to provide recognition model selection functionality --%>
        <c:when test="${not empty recModelSelect}">
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/jquery.quicksearch.min.js"></script>
            <link type="text/css" rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/multi-select.css">
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/jquery.multi-select.min.js"></script>
            <script type="text/javascript" src="${pageContext.servletContext.contextPath}/resources/js/ocr4allweb-recmodelselect.js"></script>
        </c:when>
    </c:choose>

    <!--Collapsible elements (e.g. side navigation)-->
    <script type="text/javascript">
        $(document).ready(function() {
            /* Regularly ping backend to avoid session time out during long running jobs like training, recognition or
             * line segmentation
             */
            setInterval(function(){
                $.get('ajax/team/sysenv');
            }, 8400);
            // Navigation button (used if screen size is too small)
            $(".button-collapse").sideNav();

            // Sidebar active handler
            let pageTitle = $('.page-title').first().text();
            $(".side-nav li a").each(function() {
                if( $(this).text() == pageTitle )
                    $(this).parent().addClass("selected");
            });

            // Initialize select form elements (multi-select elements will be loaded separately)
            $('select').not('select[multiple]').material_select();

            // Initialize modals
            $('.modal').modal();

            // Initialize tabs
            $('ul.tabs').tabs();

            // Expand all side navigation collapsibles (to show nested elements)
            $('.side-nav .collapsible-header').addClass('active');
            $('.side-nav .collapsible').collapsible();

            $('#open_menu').click(function() {
                $('#slide-out').removeClass('hiddendiv');
            });
        });

    </script>

    <jsp:doBody />
</head>
