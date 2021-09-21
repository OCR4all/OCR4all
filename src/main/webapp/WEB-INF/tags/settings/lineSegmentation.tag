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
                            Maximum # whitespace column separators
                            <br />
                            <span class="userWarning">Should be set to '-1' if no column separation is desired/required.</span>
                        </p>

                    </td>
                    <td>
                        <div class="input-field">
                            <input id="lineSegmentation--maxcolseps" data-setting="--max-whiteseps" type="number" step="1" value="-1" />
                            <label for="lineSegmentation--maxcolseps" data-type="int" data-error="Has to be int">Default: -1</label>
                        </div>
                    </td>
                </tr>
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
                    <td>
                        <p>
                            Bounding rectangle
                        </p>
                    </td>
                    <td>
                        <p>
                            <input type="checkbox" id="lineSegmentation--bounding-rectangle" data-setting="--bounding-rectangle"/>
                            <label for="lineSegmentation--bounding-rectangle"></label>
                        </p>
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
        <ul class="collapsible" data-collapsible="accordion">
            <li>
                <div class="collapsible-header">Limits</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Minimum scale permitted</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="lineSegmentation--minscale" data-setting="--minscale" type="number" step="1" />
                                        <label for="lineSegmentation--minscale" data-type="int" data-error="Has to be integer">Default: 12</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Maximum # lines permitted</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="lineSegmentation--maxlines" data-setting="--maxlines" type="number" step="10" />
                                        <label for="lineSegmentation--maxlines" data-type="int" data-error="Has to be integer">Default: 300</label>
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
                                <td>
                                	<p>
                                		The basic scale of the document (roughly, xheight)
										<br />
										<span class="userWarning">Will automatically be estimated if 0 or negative.</span>
                                	</p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Non-standard scaling of horizontal parameters</p></td>
                                <td>
                                    <div class="input-field">
                                    <input id="--hscale" data-setting="--hscale" type="number" step="0.1" />
                                    <label for="--hscale" data-type="float" data-error="Has to be float">Default: 1</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Non-standard scaling of vertical parameters</p></td>
                                <td>
                                    <div class="input-field">
                                    <input id="lineSegmentation--vscale" data-setting="--vscale" type="number" step="0.1" />
                                    <label for="lineSegmentation--vscale" data-type="float" data-error="Has to be float">Default: 1</label>
                                    </div>
                                </td>
                            </tr>
							<tr>
								<td>
									<p>
										Filter strength for individual characters when creating a textline)
										<br />
										<span class="userWarning">Smaller values will filter out less characters (based on size)</span>
									</p>

								</td>
								<td>
									<div class="input-field">
										<input id="lineSegmentation--filter-strength" data-setting="--filter-strength" type="number" step="0.001"/>
										<label for="lineSegmentation--filter-strength" data-type="float" data-error="Has to be a float">Default: 1</label>
									</div>
								</td>
							</tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Region skew estimate parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
						   <tr>
								<td><p>Maximum estimated skew of a region</p></td>
								<td>
									<div class="input-field">
										<input id="lineSegmentation--maxskew" data-setting="--maxskew" type="number" step="0.001"/>
										<label for="lineSegmentation--maxskew" data-type="float" data-error="Has to be a float">Default: 2.0</label>
									</div>
								</td>
							</tr>
							<tr>
								<td><p>Steps between 0 and +/-maxskew to estimate the possible skew of a region.</p></td>
								<td>
									<div class="input-field">
										<input id="lineSegmentation--skewsteps" data-setting="--skewsteps" type="number" step="1"/>
										<label for="lineSegmentation--skewsteps" data-type="int" data-error="Has to be a float">Default: 8</label>
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
                                        <input id="lineSegmentation--threshold" data-setting="--threshold" type="number" step="0.1" />
                                        <label for="lineSegmentation--threshold" data-type="float" data-error="Has to be float">Default: 0.2</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">Black column separators parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Maximum black column separators </p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="lineSegmentation--maxseps" data-setting="--max-blackseps" type="number" step="0.1" />
                                        <label for="lineSegmentation--maxseps" data-type="float" data-error="Has to be float">Default: 0</label>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Widen black separators (to account for warping)</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="lineSegmentation--sepwiden" data-setting="--widen-blackseps" type="number" step="10" />
                                        <label for="lineSegmentation--sepwiden" data-type="int" data-error="Has to be integer">Default: 10</label>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </li>
            <li>
                <div class="collapsible-header">White column separators parameters</div>
                <div class="collapsible-body">
                    <table class="compact">
                        <tbody>
                            <tr>
                                <td><p>Minimum column height (units=scale)</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="lineSegmentation--csminheight" data-setting="--minheight-whiteseps" type="number" step="1"/>
                                        <label for=lineSegmentation--csminheight data-type="int" data-error="Has to be integer">Default: 10</label>
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
								<td><p>Use gaussian instead of uniform</p></td>
								<td>
									<p>
										<input type="checkbox" class="filled-in" id="lineSegmentation--usegauss" data-setting="--usegauss"/>
										<label for="lineSegmentation--usegauss"></label>
									</p>
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
                                <td><p>Remove ImageRegions from the image before processing TextRegions for TextLines</p></td>
                                <td>
                                        <p>
                                            <input type="checkbox" data-setting="--remove-images" class="filled-in" id="lineSegmentation--remove_images"/>
                                            <label for="lineSegmentation--remove_images"></label>
                                        </p>
                                </td>
                            </tr>
                            <tr>
                                <td><p>Max iterations for smearing mask to polygon</p></td>
                                <td>
                                    <div class="input-field">
                                        <input id="lineSegmentation--fail-save" data-setting="--fail-save" type="number" step="1"/>
                                        <label for=lineSegmentation--fail-save data-type="int" data-error="Has to be integer">Default: 50</label>
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
