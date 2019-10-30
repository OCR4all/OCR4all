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
                            $('#modal_fin').modal('open');
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
    <t:body heading="Segmentation Import" imageList="false" processModals="true">
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

                <form id="larexForm" action="/Larex/direct" method="POST" target="_blank">
                    <input type="hidden" id="bookpath" name="bookpath" value="${projectDir}" />
                    <input type="hidden" id="bookname" name="bookname" value="processing" />
                    <input type="hidden" id="websave" name="websave" value="false" />
                    <input type="hidden" id="localsave" name="localsave" value="bookpath" />
                    <input type="hidden" id="imagefilter" name="imagefilter" value="bin" />
                    <input type="hidden" id="title" name="title" value="Import Correction" />
                    <input type="hidden" id="modes" name="modes" value="edit lines text" />
                    <button data-id="openLarex" class="btn waves-effect waves-light" type="submit" name="action">
                        Open LAREX
                        <i class="material-icons right">chevron_right</i>
                    </button>
                </form>
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
                    Currently only ABBYY FineReader 10 XML scheme is supported<br />
                    Please be aware that only the official XML scheme can be converted.
                    <br />
                    To Import the OCR XML save the files in the Input folder and press the "IMPORT" button.
                    After converting open LAREX and set the subtype in each region and confirm the
                    Ground Truth Data (which can be done in the "Lines" section in LAREX).
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Ok</a>
            </div>
        </div>
        <div id="modal_fin" class="modal">
            <div class="modal-content blue-grey-text">
                <h4>Please note:</h4>
                <p>
                    After converting the subtypes of each Region has to be set and the Ground Truth has to be confirmed with Larex.
                    <form id="larexForm" action="/Larex/direct" method="POST" target="_blank">
                        <input type="hidden" id="bookpath" name="bookpath" value="${projectDir}" />
                        <input type="hidden" id="bookname" name="bookname" value="processing" />
                        <input type="hidden" id="websave" name="websave" value="false" />
                        <input type="hidden" id="localsave" name="localsave" value="bookpath" />
                        <input type="hidden" id="imagefilter" name="imagefilter" value="bin" />
                        <input type="hidden" id="title" name="title" value="Import Correction" />
                        <input type="hidden" id="modes" name="modes" value="edit lines text" />
                        <button data-id="openLarex" class="btn waves-effect waves-light" type="submit" name="action">
                            Open LAREX
                            <i class="material-icons right">chevron_right</i>
                        </button>
                    </form>
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
