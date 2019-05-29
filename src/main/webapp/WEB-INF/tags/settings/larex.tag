<%@ tag description="Larex settings" pageEncoding="UTF-8" %>
<table class="compact">
    <tbody>
        <tr>
            <td><p>Image type</p></td>
            <td>
                <div class="input-field">
                    <i class="material-icons prefix">image</i>
                    <select id="imageType" name="imageType" class="suffix">
                        <option value="Binary">Binary</option>
                        <option value="Despeckled">Despeckled</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
        <tr>
        <tr>
            <td>
                <form id="larexForm" action="/Larex/direct" method="POST" target="_blank">
                    <input type="hidden" id="bookpath" name="bookpath" value="${projectDir}" />
                    <input type="hidden" id="bookname" name="bookname" value="processing" />
                    <input type="hidden" id="websave" name="websave" value="false" />
                    <input type="hidden" id="localsave" name="localsave" value="bookpath" />
                    <input type="hidden" id="imagefilter" name="imagefilter" value="bin" />
                    <button data-id="openLarex" class="btn waves-effect waves-light" type="submit" name="action">
                        Open LAREX
                        <i class="material-icons right">chevron_right</i>
                    </button>
                </form>
            </td>
        </tr>
    </tbody>
</table>
