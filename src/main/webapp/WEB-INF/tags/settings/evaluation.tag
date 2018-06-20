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
                            <label for="evaluation--n_confusions" data-type="int" data-error="Has to be int">Default: 1</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td><p>Kind of comparison (exact, nospace, letdig, letters, digits, inc)</p></td>
                    <td>
                        <div class="input-field">
                            <input id="evaluation--kind" data-setting="--kind" type="text" />
                            <label for="evaluation--kind" data-type="text">Default: exact</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Don't use missing or empty output files in the calculation</p></td>
                    <td>
                        <p>
                            <input type="checkbox" data-setting="--skipmissing" class="filled-in" id="evaluation--skipmissing" />
                            <label for="evaluation--skipmissing"></label>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Output all confusions to this file</p></td>
                    <td>
                        <div class="input-field">
                            <input id="evaluation--allconf" data-setting="--allconf" type="text" />
                            <label for="evaluation--allconf" data-type="text">Default: None</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Output per-file errors to this file</p></td>
                    <td>
                        <div class="input-field">
                            <input id="evaluation--perfile" data-setting="--perfile" type="text" />
                            <label for="evaluation--perfile" data-type="text">Default: None</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Context for confusion matrix</p></td>
                    <td>
                        <div class="input-field">
                            <input id="evaluation--context" data-setting="--context" type="number" />
                            <label for="evaluation--context" data-type="int" >Default: 0</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
