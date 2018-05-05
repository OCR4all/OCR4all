<%@ tag description="Recognition settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td><p>Disable error checking on inputs</p></td>
                    <td>
                        <p>
                            <input type="checkbox" class="filled-in" id="--nocheck" checked="checked"/>
                            <label for="--nocheck"></label>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Number of parallel threads for program execution</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--parallel" type="number" />
                            <label for="--parallel" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Line recognition models</p></td>
                    <td>
                        <select multiple="multiple" id="recModels" name="recModels[]"></select>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
        <ul class="collapsible" data-collapsible="accordion">
            <li>
                <div class="collapsible-header">Line dewarping (usually contained in model)</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>No line estimation</p></td>
                                <td>
                                    <p>
                                        <input type="checkbox" class="filled-in" id="--nolineest"/>
                                        <label for="--nolineest"></label>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>target line height (overrides recognizer)</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--height" type="number" step="0.1" />
                                        <label for="--height" data-type="float" data-error="Has to be float">Default: -1</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Recognition</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>extra blank padding to the left and right of text line</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--pad" type="number" step="1" />
                                        <label for="--pad" data-type="int" data-error="Has to be integer">Default: 16</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Don't normalize the textual output from the recognizer</p></td>
                                <td>
                                    <p>
                                        <input type="checkbox" class="filled-in" id="--nonormalize"/>
                                        <label for="--nonormalize"></label>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Output LSTM locations for characters</p></td>
                                <td>
                                    <p>
                                        <input type="checkbox" class="filled-in" id="--llocs"/>
                                        <label for="--llocs"></label>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Output extended llocs</p></td>
                                <td>
                                    <p>
                                        <input type="checkbox" class="filled-in" id="--llocsext"/>
                                        <label for="--llocsext"></label>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Output aligned LSTM locations for characters</p></td>
                                <td>
                                     <p>
                                        <input type="checkbox" class="filled-in" id="--alocs"/>
                                        <label for="--alocs"></label>
                                     </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Output probabilities for each letter</p></td>
                                <td>
                                    <p>
                                        <input type="checkbox" class="filled-in" id="--probabilities"/>
                                        <label for="--probabilities"></label>
                                    </p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Error measures</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Estimate error rate only</p></td>
                                <td>
                                    <p>
                                        <input type="checkbox" class="filled-in" id="--estrate"/>
                                        <label for="--estrate"></label>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Estimate confusion matrix </p></td>
                                <td>
                                    <div class="input-field">
                                    <input id="--estconf" type="number" step="1" />
                                    <label for="--estconf" data-type="int" data-error="Has to be integer">Default: 20</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>String comparison used for error rate estimate</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--compare" type="text" />
                                        <label for="--compare" data-type="text" >Default: nospace</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Context for error reporting</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--context" type="number" step="0.1" />
                                        <label for="--context" data-type="float" data-error="Has to be float">Default: 0</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
        </ul>
    </c:when>
</c:choose>
