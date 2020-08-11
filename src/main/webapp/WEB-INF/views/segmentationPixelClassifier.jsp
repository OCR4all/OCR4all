<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head imageList="true" inputParams="true" recModelSelect="true" processHandler="true">
        <title>OCR4all - Segmentation Pixel Classifier</title>

        <script type="text/javascript">
            function collectParamsAndExecute(selectedPages) {
                var ajaxParams = $.extend(
                    {
                        "pageIds[]" : selectedPages,
                        "imageType": $('#imageType').val()
                    },
                    getInputParams() );
                // Execute Segmentation process
                executeProcess(ajaxParams);
            }
            $(document).ready(function() {
                initializeRecModelSelect('#pixelclassifier--model', 'ajax/segmentationPixelClassifier/listModels');
                // Initialize process update and set options
                initializeProcessUpdate("segmentationPixelClassifier", [ 0 ], [ 2 ], true);

                $('#imageType').on('change', function() {
                    $('#bookname').val($('#imageType').val());
                    // Change ImageList depending on the imageType selected
                    reloadImageList($('#imageType').val(), true);
                });
                // Initialize image list
                $('#imageType').change();

                // Set available threads as default 
                $.get( "ajax/generic/threads" )
                .done(function( data ) {
                    if( !$.isNumeric(data) || Math.floor(data) != data || data < 0 )
                        return;

                    $('#--parallel').val(data).change();
                })

                $('button[data-id="execute"]').click(function() {
                    if( $('input[type="number"]').hasClass('invalid') ) {
                        $('#modal_inputerror').modal('open');
                        return;
                    }

                    var selectedPages = getSelectedPages();
                    if( selectedPages.length === 0 ) {
                        $('#modal_errorhandling').modal('open');
                        return;
                    }

                    $.post( "ajax/segmentation/exists", { "pageIds[]" : selectedPages } )
                    .done(function( data ){
                        if(data === false){
                            collectParamsAndExecute(selectedPages)
                        }
                        else{
                            $('#modal_exists').modal('open');
                        }
                    })
                    .fail(function( data ) {
                        $('#modal_exists_failed').modal('open');
                    });

                });

                $('button[data-id="cancel"]').click(function() {
                    cancelProcess();
                });
                $('#agree').click(function() {
                    collectParamsAndExecute(getSelectedPages())
                });
            });
        </script>
    </t:head>
    <t:body heading="Segmentation Pixel Classifier" imageList="true" processModals="true">
        <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings (General)</div>
                        <div class="collapsible-body">
                            <s:segmentationPixelClassifier settingsType="general"></s:segmentationPixelClassifier>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings (Advanced)</div>
                        <div class="collapsible-body">
                            <s:segmentationPixelClassifier settingsType="advanced"></s:segmentationPixelClassifier>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Pixel Classifier process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                            <div class="console">
                                 <ul class="tabs">
                                     <li class="tab" data-refid="consoleOut" class="active"><a href="#consoleOut">Console Output</a></li>
                                     <li class="tab" data-refid="consoleErr"><a href="#consoleErr">Console Error</a></li>
                                 </ul>
                                <div id="consoleOut"><pre></pre></div>
                                <div id="consoleErr"><pre></pre></div>
                            </div>
                        </div>
                    </li>
                </ul>

                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>
            </div>
        </div>
        <div id="modal_recaddmodel" class="modal">
            <div class="modal-content">
                <h4>Add new model</h4>
                <table>
                    <tr>
                        <td>Name:</td>
                        <td>
                            <div class="input-field">
                                <input type="text" id="recModelName" name="recModelName" class="validate" />
                                <label for="recModelName" data-error="Model name is empty or already exists"></label>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>Absolute file system path:</td>
                        <td>
                            <div class="input-field">
                                <input type="text" id="recModelPath" name="recModelPath" class="validate" />
                                <label for="recModelPath" data-error="Model path is empty or already exists"></label>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Cancel</a>
                <a href="#!" id="addRecModel" class="modal-action waves-effect waves-green btn-flat">Add</a>
            </div>
        </div>
    </t:body>
</t:html>
