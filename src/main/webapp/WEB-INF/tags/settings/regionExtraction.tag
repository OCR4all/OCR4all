<%@ tag description="Region Extraction settings" pageEncoding="UTF-8" %>
<table class="compact">
    <tbody>
        <tr>
            <td><p> Use average background</p></td>
            <td>
                 <p>
                    <input type="checkbox" class="filled-in" id="avgbackground" />
                    <label for="avgbackground"></label>
                </p>
            </td>
        </tr>
        <tr>
            <td><p> Use spacing</p></td>
            <td>
                 <p>
                    <input type="checkbox" class="filled-in" id="usespacing" checked="checked"/>
                    <label for="usespacing"></label>
                </p>
            </td>
        </tr>
        <tr>
            <td><p>Spacing</p></td>
            <td>
                <div class="input-field">
                    <input id="spacing" type="number" value="10" />
                    <label for="spacing" data-type="int" data-error="Has to be integer" >Default: 10 </label>
                </div>
            </td>
        </tr>
    </tbody>
</table>
