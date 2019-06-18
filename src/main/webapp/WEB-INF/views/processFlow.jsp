<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head imageList="true" inputParams="true" processHandler="true" recModelSelect="true">
        <title>OCR4All - Centralized Process Flow</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Initialize recognition model selection
                initializeRecModelSelect('#recognition--checkpoint');

                // Load image list
                initializeImageList("Original");

                // Open process selection on default
                $('.collapsible').eq(1).collapsible('open', 0);

                function getProcessesToExecute() {
                    var processesToExecute = [];
                    $.each($('#processSelection input[type="checkbox"]'), function() {
                        if( $(this).is(':checked') )
                            processesToExecute.push($(this).attr('data-id'));
                    });
                    return processesToExecute;
                }

                function getProcessSettings(processesToExecute) {
                    var processSettings = {};
                    $.each(processesToExecute, function(index, process) {
                        switch(process) {
                            case "preprocessing":
                            case "lineSegmentation":
                            case "recognition":
                                // Pass appropriate settings element to select parameters from
                                processSettings[process] = getInputParams($('[data-id="settings"]').find('[data-id="' + process + '"]'));
                                break;
                            case "despeckling":
                                processSettings[process] =
                                    { "maxContourRemovalSize" : $('input[name="maxContourRemovalSize"]').val() };
                                break;
                            case "segmentationDummy":
                                processSettings[process] =
                                    { "imageType" : $('#imageType').val(), "replace" : $('#replace').prop('checked') };
                                break;
                            case "regionExtraction":

                                processSettings[process] = {
                                    "spacing" : $('input[id="spacing"]').val(), "usespacing" : $('input[id=usespacing]').prop('checked'),
                                    "maxskew" : $('input[id="regionExtraction--maxskew"]').val(),
                                    "skewsteps" : $('input[id="regionExtraction--skewsteps"]').val(),
                                    "parallel" : $('.collapsible[data-id="settings"] li[data-id="regionExtraction"]').find($('[data-setting="--parallel"]')).val()
                                };
                                break;
                            default: break;
                        }
                    });
                    return processSettings;
                }

                // Handle all parallel settings at once
                $('#parallelGlobal').on('change', function() {
                    var parallelSetting = $('#parallelGlobal').val();
                    $('[data-setting="--parallel"]').val(parallelSetting).change();
                    $('li[data-id="recognition"]').find('[data-setting="--processes"]').val(parallelSetting).change();
                });
                // Set available threads as default 
                $.get( "ajax/generic/threads" )
                .done(function( data ) {
                    if( !$.isNumeric(data) || Math.floor(data) != data || data < 0 )
                        return;

                    $('#parallelGlobal').val(data).change();
                });

                var currentProcessInterval = null;
                var lastExecutedProcess = "";
                // Handles progress updates of all processes
                function initiateProgressHandling() {
                    $.get( "ajax/processFlow/current" )
                    .done(function( process ) {
                        // Update all processes on initial page load or execution
                        if( lastExecutedProcess === "" && process !== "" ) {
                            $.each($('#processSelection input[type="checkbox"]'), function() {
                                updateProcessFlowStatus($(this).attr('data-id'));
                            });
                        }

                        if( process === "" ) {
                            // No Process is currently executed
                            // Stop continuous AJAX requests to update the progress
                            clearInterval(currentProcessInterval);

                            // Final update for last executed process (should set status to complete)
                            if( lastExecutedProcess !== "" ) {
                                updateProcessFlowStatus(lastExecutedProcess);
                            }
                        }
                        else if( process !== lastExecutedProcess ) {
                            if( lastExecutedProcess === "" ) {
                                // Continuous progress update needs to be initiated
                                currentProcessInterval = setInterval(initiateProgressHandling, 1000);
                            }
                            else {
                                // Final update of the last exectued process status
                                updateProcessFlowStatus(lastExecutedProcess);
                            }

                            // Initial progress update for newly executed processes
                            updateProcessFlowStatus(process);
                        }
                        else {
                            // Update progress continously
                            updateProcessFlowStatus(process);
                        }

                        lastExecutedProcess = process;
                    });
                }
                function executeProcessFlow(selectedPages, processesToExecute) {
                    // Execute processflow with explicit JSON content setting
                    // Otherwise the request cannot map the transferred data correctly
                    $.ajax({
                        headers: { 
                            'Accept': 'application/json',
                            'Content-Type': 'application/json' 
                        },
                        'type': 'POST',
                        'url': "ajax/processFlow/execute",
                        'data': JSON.stringify({
                            "pageIds" : selectedPages,
                            "processesToExecute" : processesToExecute,
                            "processSettings" :  getProcessSettings(processesToExecute)
                        }),
                    })
                    .done(function( data ) {
                        // Show notification in case the user stays on the page til the end of execution
                        $('#modal_pfsuccessfull').modal('open');
                    })
                    .fail(function( jqXHR, data ) {
                        switch(jqXHR.status) {
                            case 400: $('#modal_settings_failed').modal('open'); break;
                            case 500: $('#modal_execution_failed').modal('open'); break;
                            case 530: $('#modal_inprogress').modal('open'); break;
                            case 531: $('#modal_misssubprocdata').modal('open'); break;
                            case 533: $('#modal_nosettings').modal('open'); break;
                            case 536: $('#modal_dupexecsubproc').modal('open'); break;
                            default:  $('#modal_executefailed').modal('open'); break;
                        }
                    });

                    // Trigger progress handling of processflow processes
                    setTimeout(initiateProgressHandling, 1000);	
                }
                // Start processflow execution
                $('button[data-id="execute"]').click(function() {
                    var selectedPages = getSelectedPages();
                    if( selectedPages.length === 0 ) {
                        $('#modal_errorhandling').modal('open');
                        return;
                    }

                    var processesToExecute = getProcessesToExecute();
                    if( processesToExecute.length === 0 ) {
                        $('#modal_noprocsel').modal('open');
                        return;
                    }

                    validateCheckpoints();
                    // If Recognition should be executed, verify that a model is selected
                    if( $.inArray("recognition", processesToExecute) !== -1 && $('.ms-list').hasClass('invalid')) {
                        $('#modal_checkpointerror').modal('open');
                        return;
                    }

                    $.post( "ajax/processFlow/exists", { "pageIds[]" : selectedPages, "processes[]" : processesToExecute } )
                    .done(function( data ){
                        if(data === false){
                            executeProcessFlow(selectedPages, processesToExecute);
                        }
                        else{
                            $('#modal_exists').modal('open');
                        }
                    })
                    .fail(function( data ) {
                        $('#modal_exists_failed').modal('open');
                    });
                });

                // Cancel/finalize processflow execution
                $('button[data-id="cancel"], button[data-id="finalize"]').click(function() {
                    var finalize = ($(this).attr('data-id') == 'finalize') ? true : false;
                    var ajaxParams = finalize ? {} : { 'terminate': true };
                    var modalId = finalize ? 'modal_pfsuccessfulfinalize' : 'modal_successfulcancel';
                    $.post( "ajax/processFlow/cancel", ajaxParams)
                    .done(function( data ) {
                        initiateProgressHandling();
                        if( finalize === false )
                            displayProcessFlowCancel();

                        $('#' + modalId).modal('open');
                    })
                    .fail(function( jqXHR, data ) {
                        if( jqXHR.status == 534 ) {
                            $('#modal_noprocess').modal('open');
                        }
                        else {
                            displayProcessFlowCancel(false);
                            $('#modal_failcancel').modal('open');
                        }
                    });
                });

                $('#agree').click(function() {
                    var selectedPages = getSelectedPages();
                    if( selectedPages.length === 0 ) {
                        $('#modal_errorhandling').modal('open');
                        return;
                    }

                    var processesToExecute = getProcessesToExecute();
                    if( processesToExecute.length === 0 ) {
                        $('#modal_noprocsel').modal('open');
                        return;
                    }
                    executeProcessFlow(selectedPages, processesToExecute);
                });
                // Check if processflow execution is running
                initiateProgressHandling();
            });
        </script>
    </t:head>
    <t:body heading="Centralized Process Flow" imageList="true" processModals="true">
        <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="finalize" class="btn waves-effect waves-light">
                    Finalize current process and exit
                    <i class="material-icons right">cancel</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">format_indent_increase</i>Process selection</div>
                        <div class="collapsible-body">
                            <table id="processSelection" class="compact clinched">
                                <tbody>
                                    <tr>
                                        <td><p>Preprocessing</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runPreprocessingProcess" data-id="preprocessing" checked="checked" />
                                                <label for="runPreprocessingProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Noise Removal</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runDespecklingProcess" data-id="despeckling" />
                                                <label for="runDespecklingProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Segmentation (Dummy)</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runSegmentationDummyProcess" data-id="segmentationDummy" checked="checked" />
                                                <label for="runSegmentationDummyProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
									<c:choose>
										<c:when test='${(not empty processingMode) && (processingMode == "Pagexml")}'>
										</c:when>
										<c:otherwise>
                                    <tr>
                                        <td><p>Region Extraction</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runRegionExtractionProcess" data-id="regionExtraction" checked="checked" />
                                                <label for="runRegionExtractionProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
										</c:otherwise>
									</c:choose>
                                    <tr>
                                        <td><p>Line Segmentation</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runLineSegmentationProcess" data-id="lineSegmentation" checked="checked" />
                                                <label for="runLineSegmentationProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Recognition</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runRecognitionProcess" data-id="recognition" checked="checked" />
                                                <label for="runRecognitionProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p>Number of parallel threads for program execution</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="parallelGlobal" type="number" step="1" />
                                                <label for="parallelGlobal" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>

                            <ul class="collapsible" data-collapsible="expandable" data-id="settings">
                                <li data-id="preprocessing">
                                    <div class="collapsible-header"><i class="material-icons">settings</i>Preprocessing</div>
                                    <div class="collapsible-body">
                                        <ul class="collapsible" data-collapsible="expandable">
                                            <li>
                                                <div class="collapsible-header active"><i class="material-icons">settings</i>General</div>
                                                <div class="collapsible-body">
                                                    <s:preprocessing settingsType="general"></s:preprocessing>
                                                </div>
                                            </li>
                                            <li>
                                                <div class="collapsible-header"><i class="material-icons">settings</i>Advanced</div>
                                                <div class="collapsible-body">
                                                    <s:preprocessing settingsType="advanced"></s:preprocessing>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>
                                </li>
                                <li data-id="despeckling">
                                    <div class="collapsible-header"><i class="material-icons">settings</i>Noise Removal</div>
                                    <div class="collapsible-body">
                                        <s:despeckling></s:despeckling>
                                    </div>
                                </li>
                                <li data-id="segmentationDummy">
                                    <div class="collapsible-header"><i class="material-icons">settings</i>Segmentation (Dummy)</div>
                                    <div class="collapsible-body">
                                        <s:segmentationDummy></s:segmentationDummy>
                                    </div>
                                </li>
									<c:choose>
										<c:when test='${(not empty processingMode) && (processingMode == "Pagexml")}'>
										</c:when>
										<c:otherwise>
                                <li data-id="regionExtraction">
                                    <div class="collapsible-header"><i class="material-icons">settings</i>Region Extraction</div>
                                    <div class="collapsible-body">
                                        <s:regionExtraction></s:regionExtraction>
                                    </div>
                                </li>
										</c:otherwise>
									</c:choose>
                                <li data-id="lineSegmentation">
                                    <div class="collapsible-header"><i class="material-icons">settings</i>Line Segmentation</div>
                                    <div class="collapsible-body">
                                        <ul class="collapsible" data-collapsible="expandable">
                                            <li>
                                                <div class="collapsible-header active"><i class="material-icons">settings</i>General</div>
                                                <div class="collapsible-body">
                                                    <s:lineSegmentation settingsType="general"></s:lineSegmentation>
                                                </div>
                                            </li>
                                            <li>
                                                <div class="collapsible-header"><i class="material-icons">settings</i>Advanced</div>
                                                <div class="collapsible-body">
                                                    <s:lineSegmentation settingsType="advanced"></s:lineSegmentation>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>
                                </li>
                                <li data-id="recognition">
                                    <div class="collapsible-header"><i class="material-icons">settings</i>Recognition</div>
                                    <div class="collapsible-body">
                                        <ul class="collapsible" data-collapsible="expandable">
                                            <li>
                                                <div class="collapsible-header active"><i class="material-icons">settings</i>General</div>
                                                <div class="collapsible-body">
                                                    <s:recognition settingsType="general"></s:recognition>
                                                </div>
                                            </li>
                                            <li>
                                                <div class="collapsible-header"><i class="material-icons">settings</i>Advanced</div>
                                                <div class="collapsible-body">
                                                    <s:recognition settingsType="advanced"></s:recognition>
                                                </div>
                                            </li>
                                        </ul>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <ul class="collapsible" data-collapsible="expandable" data-id="status">
                                <li data-id="preprocessing">
                                    <div class="collapsible-header"><i class="material-icons">info_outline</i>Preprocessing</div>
                                    <div class="collapsible-body">
                                        <div class="status"><p>Status: <span>No Preprocessing process running</span></p></div>
                                        <div class="progress">
                                            <div class="determinate"></div>
                                        </div>
                                    </div>
                                </li>
                                <li data-id="despeckling">
                                    <div class="collapsible-header"><i class="material-icons">info_outline</i>Noise Removal</div>
                                    <div class="collapsible-body">
                                        <div class="status"><p>Status: <span>No Noise Removal process running</span></p></div>
                                        <div class="progress">
                                            <div class="determinate"></div>
                                        </div>
                                    </div>
                                </li>
                                <li data-id="segmentationDummy">
                                    <div class="collapsible-header"><i class="material-icons">info_outline</i>Segmentation (Dummy)</div>
                                    <div class="collapsible-body">
                                        <div class="status"><p>Status: <span>No Segmentation (Dummy) process running</span></p></div>
                                        <div class="progress">
                                            <div class="determinate"></div>
                                        </div>
                                    </div>
                                </li>
								<c:choose>
									<c:when test='${(not empty processingMode) && (processingMode == "Pagexml")}'>
									</c:when>
									<c:otherwise>
                                <li data-id="regionExtraction">
                                    <div class="collapsible-header"><i class="material-icons">info_outline</i>Region Extraction</div>
                                    <div class="collapsible-body">
                                        <div class="status"><p>Status: <span>No Region Extraction process running</span></p></div>
                                        <div class="progress">
                                            <div class="determinate"></div>
                                        </div>
                                    </div>
                                </li>
									</c:otherwise>
								</c:choose>
                                <li data-id="lineSegmentation">
                                    <div class="collapsible-header"><i class="material-icons">info_outline</i>Line Segmentation</div>
                                    <div class="collapsible-body">
                                        <div class="status"><p>Status: <span>No Line Segmentation process running</span></p></div>
                                        <div class="progress">
                                            <div class="determinate"></div>
                                        </div>
                                    </div>
                                </li>
                                <li data-id="recognition">
                                    <div class="collapsible-header"><i class="material-icons">info_outline</i>Recognition</div>
                                    <div class="collapsible-body">
                                        <div class="status"><p>Status: <span>No Recognition process running</span></p></div>
                                        <div class="progress">
                                            <div class="determinate"></div>
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </li>
                </ul>

                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="finalize" class="btn waves-effect waves-light">
                    Finalize current process and exit
                    <i class="material-icons right">cancel</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>
            </div>
        </div>

        <!-- Process Flow successfully finished -->
        <div id="modal_pfsuccessfull" class="modal">
            <div class="modal-content">
                <h4>Information</h4>
                <p>
                    Process Flow execution successfully finished.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <!-- Process Flow successfully finalize -->
        <div id="modal_pfsuccessfulfinalize" class="modal">
            <div class="modal-content">
                <h4>Information</h4>
                <p>
                    Process Flow execution finalized successfully.<br />
                    The current process will be finished.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <!-- No processes selected -->
        <div id="modal_noprocsel" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    No processes were selected.<br/>
                    Please select some processes to start the Process Flow.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <!-- Error in checkpoint (model) selection -->
        <div id="modal_checkpointerror" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    No models were selected in the Recognition settings.<br/>
                    Open "Settings" and then "Recognition" to see and change the list of models.<br/>
                    Please select at least one model and execute the Process Flow again.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <!-- Duplicate subprocesses execution -->
        <div id="modal_dupexecsubproc" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    A subprocess that should be executed was already running.<br />
                    The process flow was therefore terminated.<br />
                    Please ensure that all subprocesses are finished before the process flow is executed.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <!-- Subprocess lacking of data -->
        <div id="modal_misssubprocdata" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    The upcoming subprocess was lacking of input data.<br />
                    This can occur if the previous subprocess has not provided the necessary data.<br />
                    Please check for errors in this provided data.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <!-- No process settings -->
        <div id="modal_nosettings" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    No settings were passed for one of the processed which shall be executed.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
    </t:body>
</t:html>
