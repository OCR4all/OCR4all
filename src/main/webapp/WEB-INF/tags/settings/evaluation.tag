<%@ tag description="Preprocessing settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td><p>Number of parallel threads for program execution</p></td>
                    <td>
                        <div class="input-field">
                            <input id="evaluation--num_threads" data-setting="--num_threads" type="number" />
                            <label for="evaluation--num_threads" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Output this many top confusion</p></td>
                    <td>
                        <div class="input-field">
                            <input id="evaluation--n_confusions" data-setting="--n_confusions" type="number" step="1"/>
                            <label for="evaluation--n_confusions" data-type="int" data-error="Has to be int">Default: 10</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
</c:choose>
