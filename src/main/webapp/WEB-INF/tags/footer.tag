<%@ tag description="Page Footer Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="hideOnPageLoad" required="true" %>
<jsp:useBean id="date" class="java.util.Date" />
<footer class="page-footer" <c:if test="${hideOnPageLoad == true}">style="display:none;"</c:if>>
    <div class="container">
        <div>
            <div class="row center-align">
                <div class="col l3 s6">
                    <a class="grey-text text-lighten-2" target="_blank" href="Team"><b>TEAM</b></a>
                </div>
                <div class="col l3 s6 offset-l6">
                    <a target="_blank" class="grey-text text-lighten-2" href="https://www.uni-wuerzburg.de/en/sonstiges/imprint-privacy-policy/"><b>IMPRINT</b></a>
                </div>
            </div>
            <div>
                <h6 class="grey-text dev-statement center-align text-lighten-2">
                    <b>
                        Developed at the Center for Philology and Digitality "Kallimachos" and<break/>
                        the Chair of Artificial Intelligence (Professor Dr. Frank Puppe) at the University Würzburg
                    </b>
                </h6>
            </div>
        </div>
    </div>
    <div class="container footer-copyright">
        <div class="row">
            <div class="col l3 s6 center-align">
                © 2017 - <fmt:formatDate value="${date}" pattern="yyyy" />
            </div>
            <div class="col l3 s6 offset-l6 center-align grey-text text-lighten-2">
                <a target="_blank" href="https://lists.uni-wuerzburg.de/mailman/listinfo/ocr4all">
                    Subscribe
                </a>
            </div>
        </div>
    </div>
</footer>
