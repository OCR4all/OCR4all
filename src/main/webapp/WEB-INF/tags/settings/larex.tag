<%@ tag description="Larex settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<table class="compact">
    <tbody>
        <tr>
            <td>
                <form id="larexForm" action="/Larex/direct2" method="POST" target="_blank">
                    <input type="hidden" id="imageMap" name="imageMap" value="" />
                    <input type="hidden" id="xmlMap" name="xmlMap" value="" />
                    <input type="hidden" id="bookname" name="bookname" value="processing" />
                    <input type="hidden" id="websave" name="websave" value="true" />
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
            <td>
                <button id="openLarexDirect" class="btn waves-effect waves-light" type="submitDirect" name="action">
                Open LAREX Direct
                <i class="material-icons right">chevron_right</i>
            </button></td>
        </tr>
    </tbody>
</table>
