<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head>
        <title>OCR4all - <c:out value='${title}'/> (LAREX)</title>

        <script type="text/javascript">
            $(document).ready(function() {
                let imageMap;
                $.ajax({
                    url : "ajax/generic/getDirectImageMap",
                    type: "GET",
                    dataType: "json",
                    async : false,
                    success : function( data ) {
                        imageMap = JSON.stringify(data);
                        document.getElementById("imageMap").value = imageMap;
                    },
                });

                // create mimeType for each File
                let mimeTypeMap = {};
                let imap = JSON.parse(imageMap);
                for (const [key, value] of Object.entries(imap)) {
                    if(value instanceof Array) {
                        for(let s of value) {
                            let path = s;
                            if(typeof path === 'string'){
                                let imgExt = path.split('.').pop();
                                mimeTypeMap[path] = ("image/" + imgExt);
                            }
                        }
                    }
                }
                // Fill Form Data
                document.getElementById('fileMap').value = imageMap;
                document.getElementById('mimeMap').value = JSON.stringify(mimeTypeMap);
                // Prevent redirecting to Larex if image folder does not exist
                $("#larexForm").submit(function(e){
                    $.ajax({
                        url : "ajax/generic/checkImgTypeExistance",
                        type: "GET",
                        data: { "imageType" : "bin nrm desp" },
                        dataType: "json",
                        async : false,
                        success : function( dirExists ) {
                            if( dirExists === false){
                                $('#modal_alert').modal('open');
                                e.preventDefault();
                            }
                        },
                    });
                });
            });
        </script>
    </t:head>
    <t:body heading="${title} (LAREX)">
        <div class="container includes-list">
            <div class="section">
                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header active"><i class="material-icons">line_style</i><c:out value='${title}'/></div>
                        <div class="collapsible-body">
                            <s:larex></s:larex>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <div id="modal_alert" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    The  directory for selected image type does not exist.<br />
                    Use appropriate modules to create these images.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
    </t:body>
</t:html>
