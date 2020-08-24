<%@ tag description="Result Generation settings" pageEncoding="UTF-8" %>
<table class="compact">
    <tbody>
        <tr>
            <td><p>Result file type</p></td>
            <td>
                <div class="input-field">
                    <i class="material-icons prefix">insert_drive_file</i>
                    <select id="resultType" name="resultType" class="suffix">
                        <option value="txt">Text files</option>
                        <option value="xml">XML files</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
        <tr id="strategy-row">
            <td><p>Strategy</p></td>
            <td>
                <div class="input-field">
                    <i class="material-icons prefix">extension</i>
                    <select id="resultStrategy" name="resultStrategy" class="suffix">
                        <option value="fillUp">Default</option>
                        <option value="gt">Ground Truth only</option>
                        <option value="pred">Prediction only</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
    </tbody>
</table>
