<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head processHandler="true" inputParams="true">
        <title>OCR4All - Training</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Initialize process update and set options
                initializeProcessUpdate("training", [ 0 ], [ 2 ], true);
                // No progress calculation possible, so hide the progress-bar
                $('.progress').hide();

                // Fill model dummy select with already existing models
                $.get( 'ajax/recognition/listModels' )
                .done(function( data ) {
                    $.each(data, function(key, value) {
                        var optionEl = $("<option></option>").attr("value", value).text(key);
                        $("#pretrainingDummySelect").append(optionEl);
                    });
                    // Update select with newly added values
                    $("#pretrainingDummySelect").material_select();
                });


                var currentHardwareType = "";
                // Handle hardware type dropdown menu including default settings
                $('#hardwareType').on('change', function() {
                    var type = $(this).val();
                    // Prevent further actions if the type did not change
                    if( type == currentHardwareType )
                        return;

                    if( type == "GPU" ) {
                        // In case of GPU usage the parallel setting should always be "1"
                        $('#training--max_parallel_models').val(1).change().prop('disabled', true);
                    }
                    else {
                        // Restore default setting
                        $('#training--max_parallel_models').val("").prop('disabled', false).focusin().focusout();
                    }
                });


                // Helper function to get current amount of folds (uses default value as fallback)
                function getCurrentFolds() {
                    var folds = parseInt($('#training--n_folds').val()) || 0;
                    if( folds <= 0 ) folds = parseInt($('#defaultFolds').val()) || 0;
                    return folds;
                }

                // Helper functions for dynamic generation of pretraining selection elements
                function clonePretrainingElement(idSuffix, addAfterElement, spanText) {
                    var newElementId = 'pretrainingTr' + idSuffix;
                    var modelEl = $('#pretrainingDummyTr').clone().prop('id', newElementId);
                    $(addAfterElement).after(modelEl);
                    $('#' + newElementId).find('select').prop('id', 'pretrainingModelSelect' + idSuffix);
                    $('#' + newElementId).find('span').first().html(spanText);
                    $('#' + newElementId).show();

                    // Reinitialize all model selects (to provide a unique select functionality for each select)
                    $('select[data-id="pretrainingModelSelect"]').material_select();
                }

                var currentPretrainingType = "";
                // Handle pretraining dropdown menus for the new models
                $('#pretrainingType').on('change', function() {
                    var type = $(this).val();
                    // Prevent further actions if the type did not change
                    if( type == currentPretrainingType )
                        return;

                    // First remove all dynamic pretraining elements
                    $('tr[data-id="pretrainingTr"]').not('#pretrainingDummyTr').remove();
                    // Then create them according to the selected type
                    if( type == "single_model" ) {
                        clonePretrainingElement("", $('#pretrainingDummyTr'), "All");
                    }
                    else if( type == "multiple_models" ) {
                        var folds = getCurrentFolds();
                        if( folds <= 0 ) return;

                        for(; folds > 0; folds--) {
                            clonePretrainingElement(folds, $('#pretrainingDummyTr'), folds);
                        }
                    }
                });

                // Adjust pretraining dropdown menu count if folds change
                $('#training--n_folds').on('change', function() {
                    var pretrainingType = $('#pretrainingType').val();
                    if( pretrainingType != "multiple_models" )
                        return;

                    var newFolds = getCurrentFolds();
                    if( newFolds <= 0 ) return;
                    var currentFolds = $('tr[data-id="pretrainingTr"]').not('#pretrainingDummyTr').length;

                    if( newFolds < currentFolds ) {
                        // Remove unnecessary pretraining elements
                        for(; currentFolds > newFolds; currentFolds--) {
                            $('#pretrainingTr' + currentFolds).remove();
                        }
                    }
                    else if( newFolds > currentFolds ) {
                        // Add missing pretraining elements
                        for(++currentFolds; currentFolds <= newFolds; currentFolds++) {
                            clonePretrainingElement(currentFolds, $('tr[data-id="pretrainingTr"]').last(), currentFolds);
                        }
                    }
                });


                // Module specufic function to get the input parameters
                function getExtendedInputParams() {
                    // Fetch basic input parameters
                    var ajaxParams = getInputParams();

                    // Add the specific training identifier
                    ajaxParams = $.extend(ajaxParams, { "trainingId" : $('#trainingId').val() });

                    // Check if pretraining arguments need to be added
                    var pretrainingType = $('#pretrainingType').val();
                    if( pretrainingType == "from_scratch" )
                        return ajaxParams;

                    if( $('tr[data-id="pretrainingTr"]').not('#pretrainingDummyTr').length <= 0 )
                        return ajaxParams;

                    // Build new weights parameter out of dyamically created select elements
                    var params = ['--weights'];
                    $.each($('tr[data-id="pretrainingTr"]').not('#pretrainingDummyTr').find('select'), function(index, element) {
                        params.push($(this).val());
                    });

                    // Add new parameter to existing ones
                    ajaxParams["cmdArgs"] = $.merge(ajaxParams["cmdArgs"], params);

                    return ajaxParams;
                }

                $('button[data-id="execute"]').click(function() {
                    if( $('input[type="number"]').hasClass('invalid') ) {
                        $('#modal_inputerror').modal('open');
                        return;
                    }

                    // In case of a custom training identifier, check for existance first
                    if ($('#trainingId').val() !== "") {
                        $.get( "ajax/training/exists?" , { "trainingId" : $('#trainingId').val() })
                        .done(function( data ) {
                            if( data === false ) {
                                // Execute Training process (training identifier does not exist)
                                executeProcess(getExtendedInputParams());
                            }
                            else {
                                $('#modal_exists_training').modal('open');
                            }
                        })
                        .fail(function( data ) {
                            $('#modal_exists_failed').modal('open');
                        });
                    }
                    else {
                        // Execute Training process directly
                        executeProcess(getExtendedInputParams());
                    }
                });
                $('button[data-id="cancel"]').click(function() {
                    cancelProcess();
                });
                $('#agree').click(function() {
                    // Execute Training process
                    executeProcess(getExtendedInputParams());
                });
            });
        </script>
    </t:head>
    <t:body heading="Training" processModals="true">
        <div class="container">
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
                            <s:training settingsType="general"></s:training>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings (Advanced)</div>
                        <div class="collapsible-body">
                            <s:training settingsType="advanced"></s:training>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Training process running</span></p></div>
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

        <!-- A directory with the given training identifier already exists -->
        <div id="modal_exists_training" class="modal">
            <div class="modal-content red-text">
                <h4 class="red-text">Attention</h4>
                <p>
                    There already exist related files for the given training identifier.<br/>
                    If you agree, these old process related files will be removed before the execution.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Disagree</a>
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
    </t:body>
</t:html>
