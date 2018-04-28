<%@ tag description="Page Footer Tag" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="hideOnPageLoad" required="true" %>
<jsp:useBean id="date" class="java.util.Date" />
<footer class="page-footer" <c:if test="${hideOnPageLoad == true}">style="display:none;"</c:if>>
    <div class="container">
        <div class="row">
            <div class="col l6 s12">
                <h5 class="white-text">Authors</h5>
                <div class="row">
                    <div class="col l6 s6">
                        <p>
                            <b>Christian Reul</b> - 
                            <i>OCR processes</i><br />
                            EMail: <a href="mailto:christian.reul@uni-wuerzburg.de">christian.reul@uni-wuerzburg.de</a>
                        </p>
                    </div>
                </div>
                <div class="row">
                    <div class="col l6 s6">
                        <p>
                            <b>Dennis Christ</b> - 
                            <i>Developer</i><br />
                            EMail: <a href="mailto:dennis.christ@stud-mail.uni-wuerzburg.de">dennis.christ@stud-mail.uni-wuerzburg.de</a>
                        </p>
                    </div>
                    <div class="col l6 s6">
                        <p>
                            <b>Alexander Hartelt</b> - 
                            <i>Developer</i><br />
                            EMail: <a href="mailto:alexander.hartelt@stud-mail.uni-wuerzburg.de">alexander.hartelt@stud-mail.uni-wuerzburg.de</a>
                        </p>
                    </div>
                </div>
            </div>
            <div class="col l4 offset-l2 s12">
                <h5 class="white-text">Links</h5>
                <ul>
                    <li><a target="_blank" class="grey-text text-lighten-3" href="https://www.uni-wuerzburg.de/en/sonstiges/imprint-privacy-policy/">Imprint</a></li>
                </ul>
            </div>
        </div>
    </div>
    <div class="footer-copyright">
        <div class="container">
            © 2017 - <fmt:formatDate value="${date}" pattern="yyyy" />
        </div>
    </div>
</footer>
