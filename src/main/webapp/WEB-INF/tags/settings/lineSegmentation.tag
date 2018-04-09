<%@ tag description="Line Segmentation settings" pageEncoding="UTF-8" %>
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
                    <td><p>Use gaussian instead of uniform</p></td>
                    <td>
                             <p>
                                <input type="checkbox" class="filled-in" id="--usegauss" checked="checked"/>
                                <label for="--usegauss"></label>
                             </p>
                    </td>
                </tr>
                <tr>
                    <td>
                        <p>
                            Maximum # whitespace column separators 
                            <br />
                            <span style="color:red;font-size:9pt" >The value of this option should be set to '-1' when LAREX was used as the segmentation process</span>
                        </p>
                        
                    </td>
                    <td>
                        <div class="input-field">
                            <input id="--maxcolseps" type="number" step="0.1" value="3" />
                            <label for="--maxcolseps" data-type="float" data-error="Has to be float">Default: 3</label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><p>Output grayscale lines as well</p></td>
                    <td>
                             <c:if test='${imageType == "Gray"}'><c:set value='checked="checked"' var="setCheckboxImageType"></c:set></c:if>
                             <p>
                                 <input type="checkbox" class="filled-in" id="--gray" ${setCheckboxImageType}/>
                                 <label for="--gray"></label>
                             </p>
                    </td>
                </tr>
                <tr>
                    <td><p>Number of parallel threads for program execution</p></td>
                    <td>
                         <div class="input-field">
                             <input id="--parallel" type="number" step="1" />
                             <label for=--parallel data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
        <ul class="collapsible" data-collapsible="accordion">
            <li>
                <div class="collapsible-header">Error checking</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Zoom for page background estimation, smaller=faster</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--zoom" type="number" step="0.1" />
                                        <label for="--zoom" data-type="float" data-error="Has to be float">Default: 0.5</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Limits</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Minimum scale permitted</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--minscale" type="number" step="1" />
                                        <label for="--minscale" data-type="int" data-error="Has to be integer">Default: 12</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Maximum # lines permitted</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--maxlines" type="number" step="10" />
                                        <label for="--maxlines" data-type="int" data-error="Has to be integer">Default: 300</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Scale parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>The basic scale of the document (roughly, xheight) </p></td>
                                <td>
                                    <div class="input-field">
                                    <input id="--scale" type="number" step="0.1" />
                                    <label for="--scale" data-type="float" data-error="Has to be float">Default: 0</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Non-standard scaling of horizontal parameters</p></td>
                                <td>
                                    <div class="input-field">
                                    <input id="--hscale" type="number" step="0.1" />
                                    <label for="--hscale" data-type="float" data-error="Has to be float">Default: 1</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Non-standard scaling of vertical parameters</p></td>
                                <td>
                                    <div class="input-field">
                                    <input id="--vscale" type="number" step="0.1" />
                                    <label for="--vscale" data-type="float" data-error="Has to be float">Default: 1</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Line parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Baseline threshold </p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--threshold" type="number" step="0.1" />
                                        <label for="--threshold" data-type="float" data-error="Has to be float">Default: 0.2</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Noise threshold for removing small components from lines</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--noise" type="number" step="1" />
                                        <label for="--noise" data-type="int" data-error="Has to be integer">Default: 8</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Column parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Maximum black column separators </p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--maxseps" type="number" step="0.1" />
                                        <label for="--maxseps" data-type="float" data-error="Has to be float">Default: 2</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Widen black separators (to account for warping)</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--sepwiden" type="number" step="10" />
                                        <label for="--sepwiden" data-type="int" data-error="Has to be integer">Default: 10</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Also check for black column separators</p></td>
                                <td>
                                        <p>
                                            <input type="checkbox" class="filled-in" id="--blackseps"/>
                                            <label for="--blackseps"></label>
                                        </p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Whitespace column separators</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Minimum column height (units=scale)</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--csminheight" type="number" step="1"/>
                                        <label for=--csminheight data-type="int" data-error="Has to be integer">Default: 10</label>
                                    </div>
                               </td>
                            </tr>
                            <tr>
                                <td><p>Minimum aspect ratio for column separators</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--csminaspect" type="number" step="0.1" />
                                        <label for=--csminaspect data-type="float" data-error="Has to be float">Default: 1.1</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Output parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Padding for extracted lines </p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--pad" type="number" step="0.1" />
                                        <label for="--pad" data-type="float" data-error="Has to be float">Default: 3</label>
                                    </div>
                                </td>
                             </tr>
                             <tr>
                                <td><p>Expand mask for grayscale extraction</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="--expand" type="number" step="0.1" />
                                        <label for=--expand data-type="float" data-error="Has to be float">Default: 3</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Other parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                               <tr>
                                <td><p>Be less verbose</p></td>
                                <td>
                                        <p>
                                            <input type="checkbox" class="filled-in" id="--quit"/>
                                            <label for="--quit"></label>
                                        </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Enable debug mode</p></td>
                                <td>
                                        <p>
                                            <input type="checkbox" class="filled-in" id="--debug"/>
                                            <label for="--debug"></label>
                                        </p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
        </ul>
    </c:when>
</c:choose>
