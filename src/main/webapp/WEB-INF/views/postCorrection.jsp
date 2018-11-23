<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head processHandler="true">
        <title>OCR4All - Post Correction</title>

        <script type="text/javascript">
            $(document).ready(function() {
                initializeProcessUpdate("postCorrection", [ 0 ], [ 0 ], true);

                // Process handling
                $('button[data-id="execute"]').click(function() {
                    $('#modal_delete_before_insert').modal('open');
                });
                $('#agree').click(function() {
                    // Execute postCorrection process
                    executeProcess({});
                });
            });
        </script>
    </t:head>
    <t:body heading="Post Correction" processModals="true">
        <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Import OCR Results
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="openNashi" class="btn waves-effect waves-light">
                    Open Nashi
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Post Correction process running</span></p></div>
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
                    Import OCR Results
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="openNashi" class="btn waves-effect waves-light">
                    Open Nashi
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>
            </div>
        </div>

        <!-- Nashi reimport information -->
        <div id="modal_delete_before_insert" class="modal">
            <div class="modal-content">
                <h4 class="red-text">Attention</h4>
                <p>
                    To ensure integrity the data will be reimported to Nashi.<br />
                    If you agree all book related data will be deleted in Nashi before the new import.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat ">Disagree</a>
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
    </t:body>
</t:html>
