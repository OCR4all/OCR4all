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
                        <option value="docx">DOCX files</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
        <tr id="strategy-row">
            <td style="width: 500px;"><p>
                Strategy
                <br />
                <span class="userInfo">Default prefers ground truth but falls back to prediction if the former doesn't exist</span>
            </p></td>
            <td>
                <div class="input-field">
                    <select id="resultStrategy" name="resultStrategy" class="suffix">
                        <option value="fillUp">Default</option>
                        <option value="gt">Ground Truth only</option>
                        <option value="pred">Prediction only</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
        <tr id="emptyLines-row">
            <td>
                <p>
                    Preserve empty lines
                    <br />
                    <span class="userInfo">If activated lines without text will get preserved</span>
                </p>
            </td>
            <td>
                <form action="#">
                    <p>
                        <input type="checkbox" id="preserveEmptyLines" />
                        <label for="preserveEmptyLines"></label>
                    </p>
                </form>
            </td>
        </tr>
        <tr id="pageDelimiter-row">
            <td>
                <p>
                    Add page delimiter
                    <br />
                    <span class="userInfo">If activated page delimiter of the format '### PageId' are inserted.</span>
                </p>
            </td>
            <td>
                <form action="#">
                    <p>
                        <input type="checkbox" checked="checked" id="addPageDelimiter" />
                        <label for="addPageDelimiter"></label>
                    </p>
                </form>
            </td>
        </tr>
    </tbody>
</table>
