<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>OCR4All - Segmentation (LAREX)</title>

        <script type="text/javascript">
            $(document).ready(function() {
                initializeImageList("Binary", true);

                initializeProcessUpdate("segmentationLarex", [ 0 ], [ 1 ], false);

                // Prevent redirecting to Larex if image folder does not exist
                $("#larexForm").submit(function(e){
                    $.ajax({
                        url : "ajax/generic/checkDir",
                        type: "GET",
                        data: { "imageType" : $('#imageType').val() },
                        async : false,
                        success : function( dirExists ) {
                            if( dirExists === false){
                                $('#modal_alert').modal('open');
                                e.preventDefault();
                            }
                        },
                    });
                });

                $('#imageType').on('change', function() {
                    $('#bookname').val($('#imageType').val());
                    // Change ImageList depending on the imageType selected
                    reloadImageList($('#imageType').val(), true);
                });
                $('#imageType').change();

                // Process handling (execute for all pages with current settings)
                $('button[data-id="execute"]').click(function() {
                    var selectedPages = getSelectedPages();
                    if( selectedPages.length === 0 ) {
                        $('#modal_errorhandling').modal('open');
                        return;
                    }
                    $.get( "ajax/segmentationLarex/exists?", { "pageIds[]" : selectedPages } )
                    .done(function( data ) {
                        if(data === false) {
                            var ajaxParams =  { "pageIds[]" : selectedPages, "imageType" : $('#imageType').val(), "replace" : $('#replace').prop('checked')};
                            // Execute segmentation process
                            executeProcess(ajaxParams);
                        }
                        else{
                            $('#modal_exists').modal('open');
                        }
                    });
                });
                $('#agree').click(function() {
                    var selectedPages = getSelectedPages();
                    var ajaxParams =  { "pageIds[]" : selectedPages, "imageType" : $('#imageType').val()};
                    // Execute segmentation process
                    executeProcess(ajaxParams);
                });
            });
        </script>
    </t:head>
    <t:body heading="Segmentation (LAREX)" imageList="true" processModals="true">
        <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Apply Segmentation results
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">line_style</i>Segmentation</div>
                        <div class="collapsible-body">
                            <s:segmentationLarex></s:segmentationLarex>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Segmentation process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                </ul>

                <button data-id="execute" class="btn waves-effect waves-light">
                    Apply Segmentation results
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>
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
