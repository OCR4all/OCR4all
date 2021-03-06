<%@ tag description="Larex settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<table class="compact">
    <tbody>
        <tr>
            <td>
                <form id="larexForm" action="/Larex/direct" method="POST" target="_blank">
                    <input type="hidden" id="bookpath" name="bookpath" value="${projectDir}" />
                    <input type="hidden" id="bookname" name="bookname" value="processing" />
                    <input type="hidden" id="websave" name="websave" value="false" />
                    <input type="hidden" id="localsave" name="localsave" value="bookpath" />
                    <input type="hidden" id="imagefilter" name="imagefilter" value="bin nrm desp" />
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
