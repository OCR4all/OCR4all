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
                            <input id="preprocessing--maxskew" data-setting="--maxskew" type="number" step="0.1" />
                            <label for="preprocessing--maxskew" data-type="float" data-error="Has to be float">Default: 2 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Number of parallel threads for program execution</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--parallel" data-setting="--parallel" type="number" />
                            <label for="preprocessing--parallel" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
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
                            <input type="checkbox" data-setting="--nocheck" class="filled-in" id="preprocessing--nocheck" />
                            <label for="preprocessing--nocheck"></label>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Threshold, determines lightness</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--threshold" data-setting="--threshold" type="number" step="0.01" />
                            <label for="preprocssing--threshold" data-type="float" data-error="Has to be float">Default: 0.5 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Zoom for page background estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--zoom" data-setting="--zoom" type="number" step="0.01" />
                            <label for="preprocessing--zoom" data-type="float" data-error="Has to be float">Default: 0.5 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Scale for estimating a mask over the text region</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--escale" data-setting="--escale" type="number" step="0.1" />
                            <label for="preprocessing--escale" data-type="float" data-error="Has to be float">Default: 1.0 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Ignore this much of the border for threshold estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--bignore" data-setting="--bignore" type="number" step="0.01" />
                            <label for="preprocessing--bignore" data-type="float" data-error="Has to be float">Default: 0.1 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Percentage for filters</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--perc" data-setting="--perc" type="number" />
                            <label for="preprocessing--perc" data-type="float" data-error="Has to be float">Default: 80 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Range for filters</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--range" data-setting="--range" type="number" />
                            <label for="preprocessing--range" data-type="int" data-error="Has to be integer">Default: 20 (Integer value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Force grayscale processing even if image seems binary</p></td>
                    <td>
                        <p>
                            <input type="checkbox" class="filled-in" data-setting="--gray" id="preprocessing--gray" />
                            <label for="preprocessing--gray"></label>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Percentile for black estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--lo" data-setting="--lo" type="number" step="0.1" />
                            <label for="preprocessing--lo" data-type="float" data-error="Has to be float">Default: 5 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Percentile for white estimation</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--hi" data-setting="--hi" type="number" />
                            <label for="preprocessing--hi" data-type="float" data-error="Has to be float">Default: 90 (Float value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Steps for skew angle estimation (per degree)</p></td>
                    <td>
                        <div class="input-field">
                            <input id="preprocessing--skewsteps" data-setting="--skewsteps" type="number" />
                            <label for="preprocessing--skewsteps" data-type="int" data-error="Has to be integer">Default: 8 (Integer value)</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
