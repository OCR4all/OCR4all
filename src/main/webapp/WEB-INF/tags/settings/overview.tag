<%@ tag description="Overview settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<table class="compact">
    <tbody>
        <tr>
            <td>
                <p>
                    Project data selection type
                </p>
            </td>
            <td>
                <div class="input-field col s3">
                    <i class="material-icons prefix">donut_small</i>
                    <c:choose>
                        <c:when test='${projectDataSelectionType == "fixedStructure"}'>
                            <c:set value='selected="selected"' var="fixedStructure"></c:set>
                        </c:when>
                        <c:otherwise><c:set value='selected="selected"' var="freeTextInput"></c:set></c:otherwise>
                    </c:choose>
                    <select id="projectDataSelectionType" name="projectDataSelectionType" class="suffix">
                        <option value="freeTextInput" ${freeTextInput}>Use free text input to provide directory paths</option>
                        <option value="fixedStructure" ${fixedStructure}>Use predefined project directory structure</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <p>
                    Project selection
                </p>
            </td>
            <td>
                <div class="input-field col s3">
                    <i class="material-icons prefix">library_books</i>
                    <select id="projectSelection" name="projectSelection" class="suffix">
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <p>
                    Project directory path
                </p>
            </td>
            <td>
                <div class="input-field">
                    <i class="material-icons prefix">folder</i>
                    <input id="projectDir" type="text" class="validate suffix" value="${projectDir}" />
                    <label for="projectDir" data-error="Directory path is empty or could not be accessed on the filesystem"></label>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                <p>Project image type</p>
            </td>
            <td>
                <div class="input-field col s3">
                    <i class="material-icons prefix">image</i>
                    <c:choose>
                        <c:when test='${imageType == "Gray"}'><c:set value='selected="selected"' var="graySel"></c:set></c:when>
                        <c:otherwise><c:set value='selected="selected"' var="binarySel"></c:set></c:otherwise>
                    </c:choose>
                    <select id="imageType" name="imageType" class="suffix">
                        <option value="Binary" ${binarySel}>Binary</option>
                        <option value="Gray" ${graySel}>Gray</option>
                    </select>
                    <label></label>
                </div>
            </td>
        </tr>
    </tbody>
</table>
