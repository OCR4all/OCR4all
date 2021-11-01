<%@ tag description="Larex settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<table class="compact">
    <tbody>
        <tr>
            <td>
                <form id="larexForm" action="/Larex/directLibrary" method="POST" target="_blank">
                    <input type="hidden" id="fileMap" name="fileMap" value="" />
                    <input type="hidden" id="mimeMap" name="mimeMap" value="" />
                    <input type="hidden" id="metsFilePath" name="metsFilePath" value="" />
                    <input type="hidden" id="customFlag" name="customFlag" value="false" />
                    <input type="hidden" id="customFolder" name="customFolder" value="" />
					<c:if test="${not empty modes}" >
                    <input type="hidden" id="modes" name="modes" value="${modes}" />
					</c:if>
                    <button data-id="openLarex" class="btn waves-effect waves-light" type="submit" name="action">
                        Open LAREX
                        <i class="material-icons right">chevron_right</i>
                    </button>
                </form>
            </td>
        </tr>
    </tbody>
</table>
