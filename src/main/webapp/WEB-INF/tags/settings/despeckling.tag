<%@ tag description="Despeckling settings" pageEncoding="UTF-8" %>
<table class="compact">
    <tbody>
        <tr>
            <td><p>Maximal size for removing contours</p></td>
            <td>
                <div class="input-field">
                    <input id="maxContourRemovalSize" name="maxContourRemovalSize" value="100" type="number" />
                    <label for="maxContourRemovalSize" data-type="float" data-error="Has to be float (. sep)">Float value</label>
                </div>
            </td>
        </tr>
        <tr>
            <td><p>Illustration type</p></td>
            <td>
                <div class="input-field">
                    <select id="illustrationType" name="illustrationType">
                        <option value="marked">Show binary image including removed speckles</option>
                        <option value="standard">Show binary image</option>
                    </select>
                </div>
            </td>
        </tr>
    </tbody>
</table>
