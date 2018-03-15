<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>OCR4All - Centralized Process Flow</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Load image list
                initializeImageList("Original");

                // Open process selection on default
                $('.collapsible').collapsible('open', 0);

                function getProcessesToExecute() {
                    var processesToExecute = [];
                    $.each($('#processSelection input[type="checkbox"]'), function() {
                        if( $(this).is(':checked') )
                            processesToExecute.push($(this).attr('data-id'));
                    });
                    return processesToExecute;
                }

                var currentProcessInterval = null;
                var lastExecutedProcess = "";
                // Handles progress updates of all processes
                function initiateProgressHandling() {
                    $.get( "ajax/processFlow/current" )
                    .done(function( process ) {
                        // Update all processes on initial page load or execution
                        if( lastExecutedProcess === "" ) {
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

                    var ajaxParams = { "pageIds[]" : selectedPages, "processesToExecute[]" : processesToExecute };
                    $.post( "ajax/processFlow/execute", ajaxParams )
                    .done(function( data ) {
                        // Show notification in case the user stays on the page til the end of execution
                        $('#modal_pfsuccessfull').modal('open');
                    })
                    .fail(function( jqXHR, data ) {
                        switch(jqXHR.status) {
                            case 530: $('#modal_dupexecsubproc').modal('open'); break;
                            case 531: $('#modal_misssubprocdata').modal('open'); break;
                            case 532: $('#modal_inprogress').modal('open'); break;
                            default:  $('#modal_executefailed').modal('open'); break;
                        }
                    });

                    // Trigger progress handling of processflow processes
                    setTimeout(initiateProgressHandling, 1000);
                });

                // Cancel/stop processflow execution
                $('button[data-id="cancel"]').click(function() {
                    $.post( "ajax/processFlow/cancel" )
                    .done(function( data ) {
                        $('#modal_successfulcancel').modal('open');
                    })
                    .fail(function( jqXHR, data ) {
                        if( jqXHR.status == 534 ) {
                            $('#modal_noprocess').modal('open');
                        }
                        else {
                            $('#modal_failcancel').modal('open');
                        }
                    });
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
                                        <td><p>Segmentation</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runSegmentationProcess" data-id="segmentation" checked="checked" />
                                                <label for="runSegmentationProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Region Extraction</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="runRegionExtractionProcess" data-id="regionExtraction" checked="checked" />
                                                <label for="runRegionExtractionProcess"></label>
                                            </p>
                                        </td>
                                    </tr>
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
                    <li data-id="preprocessing">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status (Preprocessing)</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Preprocessing process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                    <li data-id="despeckling">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status (Noise Removal)</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Noise Removal process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                    <li data-id="segmentation">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status (Segmentation)</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Segmentation process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                    <li data-id="regionExtraction">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status (Region Extraction)</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Region Extraction process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                    <li data-id="lineSegmentation">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status (Line Segmentation)</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Line Segmentation process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                    <li data-id="recognition">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status (Recognition)</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Recognition process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
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
    </t:body>
</t:html>
