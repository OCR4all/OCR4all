<%@ tag description="SegmentationPixelClassifier settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
                <tr>
                    <td><p>Segmentation image type</p></td>
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
                    <td><p>Todo</p></td>
                    <td>
                        <div class="input-field">
                            <input id="Todo" />
                            <label for="Todo" data-type="Todo" data-error="Todo">Todo</label>
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
                    <td><p>Todo</p></td>
                    <td>
                        <div class="input-field">
                            <input id="Todo" />
                            <label for="Todo" data-type="Todo" data-error="Todo">Todo</label>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
