<%@ tag description="Recognition settings" pageEncoding="UTF-8" %>
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
                            <input id="--processes" type="number" />
                            <label for="--processes" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Line recognition models</p></td>
                    <td>
                        <select multiple="multiple" id="--checkpoint" name="--checkpoint[]"></select>
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
                    <td><p>Print additional information</p></td>
                    <td>
                        <p>
                            <input type="checkbox" class="filled-in" id="--verbose" checked="checked"/>
                            <label for="--verbose"></label>
                        </p>
                    </td>
                 </tr>
                 <tr>
                    <td><p>Number of lines to process in parallel (Batch size)</p></td>
                    <td>
                        <div class="input-field">
                            <input id="--batch_size" type="number" step="1" />
                            <label for="--batch_size" data-type="int" data-error="Has to be Int">Default: 1</label>
                        </div>
                    </td>
                </tr>
                 <tr>
                    <td><p>The voting algorithm to use</p></td>
                    <td>
                        <div class="input-field">
                            <select id="--voter" name="voter">
                                <option value="confidence_voter_default_ctc">confidence_voter_default_ctc</option>
                                <option value="confidence_voter_fuzzy_ctc">confidence_voter_fuzzy_ctc</option>
                                <option value="sequence_voter">sequence_voter</option>
                            </select>
                            <label></label>
                        </div>
                    </td>
                 </tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
