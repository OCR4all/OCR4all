<%@ tag description="Line Segmentation settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td>
                        <p>
                            Image processing scale 
                            <br />
                            <span class="userWarning">Will be estimated from the image if left empty</span>
                        </p>
                        
                    </td>
                    <td>
                        <div class="input-field">
                            <input id="lineSegmentation--scale" data-setting="--scale" type="number" />
                            <label for="lineSegmentation--scale" data-type="float" data-error="Has to be a float">Default: -1</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Precision of the polygon surrounding the textline. (Smaller values result in better precision)</p></td>
                    <td>
                         <div class="input-field">
                             <input id="lineSegmentation--tolerance" data-setting="--tolerance" type="number" step="0.001" />
                             <label for="lineSegmentation--tolerance" data-type="float" data-error="Has to be float">Default: 1.0</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Number of parallel threads for program execution</p></td>
                    <td>
                         <div class="input-field">
                             <input id="lineSegmentation--parallel" data-setting="--parallel" type="number" step="1" />
                             <label for="lineSegmentation--parallel" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
    </c:when>
</c:choose>
