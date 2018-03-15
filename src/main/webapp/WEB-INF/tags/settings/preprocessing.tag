<%@ tag description="Preprocessing settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td><p>Skew angle estimation parameters (degrees)</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--maxskew" type="number" step="0.1" />
                            <label for="--maxskew" data-type="float" data-error="Has to be float">Default: 2 (Float value)</label>
                        </div>
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
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td><p>Disable error checking on inputs</p></td>
                    <td>
                        <p>
                            <input type="checkbox" class="filled-in" id="--nocheck" />
                            <label for="--nocheck"></label>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Threshold, determines lightness</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--threshold" type="number" step="0.01" />
                            <label for="--threshold" data-type="float" data-error="Has to be float">Default: 0.5 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Zoom for page background estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--zoom" type="number" step="0.01" />
                            <label for="--zoom" data-type="float" data-error="Has to be float">Default: 0.5 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Scale for estimating a mask over the text region</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--escale" type="number" step="0.1" />
                            <label for="--escale" data-type="float" data-error="Has to be float">Default: 1.0 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Ignore this much of the border for threshold estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--bignore" type="number" step="0.01" />
                            <label for="--bignore" data-type="float" data-error="Has to be float">Default: 0.1 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Percentage for filters</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--perc" type="number" />
                            <label for="--perc" data-type="float" data-error="Has to be float">Default: 80 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Range for filters</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--range" type="number" />
                            <label for="--range" data-type="int" data-error="Has to be integer">Default: 20 (Integer value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Force grayscale processing even if image seems binary</p></td>
                    <td>
                        <p>
                            <input type="checkbox" class="filled-in" id="--gray" />
                            <label for="--gray"></label>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Percentile for black estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--lo" type="number" step="0.1" />
                            <label for="--lo" data-type="float" data-error="Has to be float">Default: 5 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Percentile for white estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--hi" type="number" />
                            <label for="--hi" data-type="float" data-error="Has to be float">Default: 90 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Steps for skew angle estimation (per degree)</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--skewsteps" type="number" />
                            <label for="--skewsteps" data-type="int" data-error="Has to be integer">Default: 8 (Integer value)</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
