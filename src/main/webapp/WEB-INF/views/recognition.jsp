<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head imageList="true" inputParams="true" recModelSelect="true" processHandler="true">
        <title>OCR4All - Recognition</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Initialize recognition model selection
                initializeRecModelSelect('#--checkpoints');

                // Load image list
                $.get( "ajax/recognition/getValidPageIds")
                .done(function( data ) {
                    initializeImageList("OCR", false, data);
                });

                // Initialize process update and set options
                initializeProcessUpdate("recognition", [ 0 ], [ 2 ], true);

                // Set available threads as default 
                $.get( "ajax/generic/threads" )
                .done(function( data ) {
                    if( !$.isNumeric(data) || Math.floor(data) != data || data < 0 )
                        return;

                    $('#--parallel').val(data).change();
                });

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
                    $.get( "ajax/recognition/exists?", { "pageIds[]" : selectedPages } )
                    .done(function( data ){
                        if(data === false){
                            var ajaxParams = $.extend( { "pageIds[]" : selectedPages }, getInputParams() );
                            // Execute recongition process
                            executeProcess(ajaxParams);
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
                    var selectedPages = getSelectedPages();
                    var ajaxParams = $.extend( { "pageIds[]" : selectedPages }, getInputParams() );
                    // Execute recognition process
                    executeProcess(ajaxParams);
                });
            });
        </script>
    </t:head>
    <t:body heading="Recognition" imageList="true" processModals="true">
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
                            <s:recognition settingsType="general"></s:recognition>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings (Advanced)</div>
                        <div class="collapsible-body">
                            <s:recognition settingsType="advanced"></s:recognition>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Recognition process running</span></p></div>
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
    </t:body>
</t:html>
