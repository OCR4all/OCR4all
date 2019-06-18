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
    <c:when test="${settingsType == 'advanced'}">
<table class="compact">
	<tbody>
		<tr>
			<td><p>Skip empty ground truth data</p></td>
			<td>
				<input type="checkbox" data-setting="--skip_empty_gt" class="filled-in" id="preprocessing--skip_empty_gt" checked/>
				<label for="preprocessing--skip_empty_gt"></label>
			</td>
		</tr>
		<tr>
			<td><p>Non existing file handling mode</p></td>
			<td>
				<div class="input-field">
					<select id="evaluation--non_existing_file_handling_mode" data-setting="--non_existing_file_handling_mode" name="eval_non_mode">
						<option value="skip">Skip files</option>
						<option value="empty">Treat as empty files</option>
						<option value="error">Throw an error</option>
					</select>
					<label></label>
				</div>
			</td>
		</tr>
	</tbody>
</table>
    </c:when>
</c:choose>
