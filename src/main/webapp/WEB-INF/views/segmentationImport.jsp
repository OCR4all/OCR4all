<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>OCR4All - Segmentation Import</title>

        <script type="text/javascript">
            $(document).ready(function() {
                initializeProcessUpdate("segmentationImport", [ 0 ], [ 1 ], false);

                $('#imageType').on('change', function() {
                    $('#bookname').val($('#imageType').val());
                    // Change ImageList depending on the imageType selected
                    reloadImageList($('#imageType').val(), true);
                });
                // Initialize image list
                $('#imageType').change();

                // Process handling (execute for all pages with current settings)
                $('button[data-id="execute"]').click(function() {
                    // Start process
                    var ajaxParams = {"sourcePath" : $('#importFile').val(), "outputPath" : "here"};
                    $.post( "ajax/segmentationImport/execute", ajaxParams )
                        .done(function( data ) {
                            // Load datatable after the last process update is surely finished
                            setTimeout(function() {
                                datatable();
                            }, 2000);
                        })
                        .fail(function( data ) {
                            $('#modal_error').modal('open');
                        });
                });
                $('#agree').click(function() {
                    var selectedPages = getSelectedPages();
                    var ajaxParams =  { "pageIds[]" : selectedPages, "imageType" : $('#imageType').val()};
                    // Execute segmentation process
                    executeProcess(ajaxParams);
                });
                $('button[data-id="info"]').click(function() {
                    $('#modal_info').modal('open');
                });
            });
        </script>
    </t:head>
    <t:body heading="Segmentation Import" imageList="true" processModals="true">
        <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Import
                    <i class="material-icons right">arrow_upward</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>
                <button data-id="info" class="btn-floating waves-effect waves-light grey darken-3"><i class="material-icons">
                    info
                </i></button>
                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <s:segmentationImport></s:segmentationImport>
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
                    Import
                    <i class="material-icons right">arrow_upward</i>
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
        <div id="modal_info" class="modal">
            <div class="modal-content blue-grey-text">
                <h4>Import Instructions</h4>
                <p>
                    Currently only selected XML formats are supported: ABBYY FineReader 10 XML, ALTO XML and hOCR XML<br />
                    Please be aware that only the official XML schemes can be converted.
                    <br />
                    To Import the OCR XML, either save it in the input folder or enter the path of the file.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Ok</a>
            </div>
        </div>
        <div id="modal_error" class="modal">
            <div class="modal-content red-text">
                <h4>FATAL ERROR</h4>
                <p>
                    Oh God, something went horribly wrong!<br />
                    Not great, not terrible.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Ok</a>
            </div>
        </div>
    </t:body>
</t:html>
