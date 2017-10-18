<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <title>OCR4All - Preprocessing</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Set available threads as default 
                $.get( "ajax/preprocessing/threads" )
                .done(function( data ) {
                    if( !$.isNumeric(data) || Math.floor(data) != data || data < 0 )
                        return;

                    $('#--parallel').val(data).change();
                })

                // Error handling for parameter input fields
                $('input[type="number"]').on('change', function() {
                    var num = $(this).val();
                    if( !$.isNumeric(num) ) {
                        if( num !== "" ) {
                            $(this).addClass('invalid').focus();
                        }
                        else {
                            $(this).removeClass('invalid');
                        }
                    }
                    else if( Math.floor(num) != num ) {
                        if( $(this).attr('data-type') === "int" ) 
                            $(this).addClass('invalid').focus();
                    }
                    else {
                        $(this).removeClass('invalid');
                    }
                });

                // Fetch all modified parameters and return them appropriately
                function getParams() {
                    var params = { 'cmdArgs': [] };
                    $.each($('input[type="checkbox"]'), function() {
                        if( $(this).prop('checked') === true )
                            params['cmdArgs'].push($(this).attr('id'));
                    });
                    $.each($('input[type="number"]'), function() {
                        if( $(this).val() !== "" )
                            params['cmdArgs'].push($(this).attr('id'), $(this).val());
                    });
                    console.log(params);
                    return params;
                }

                // Handle tab behaviour
                function selectActiveTab() {
                    // Set console output to active tab
                    // Workaround with setTimeout, due to a bug in materialize JS
                    // Without using this the indicator width is not calculated correctly
                    setTimeout(function( ) { $('ul.tabs').tabs('select_tab', $('.console div.active').attr('id')); }, 200);
                }
                // Remove error notification icon if user clicks on consoleErr tab
                $('li.tab a').on('click', function() {
                    var clickedTabIcon = $(this).find('i.material-icons');
                    if( clickedTabIcon.length > 0 ) {
                        clickedTabIcon.remove();
                        $('ul.tabs').tabs('select_tab', $(this).parent('li').attr('data-refid'));
                    }
                });

                var inProgress = false;
                var progressInterval = null;
                var consoleStream = { "out" : "", "err" : "" };
                // Function to handle preprocessing console
                function updateConsole(streamType, tabId) {
                    $.get( "ajax/preprocessing/console", { "streamType" : streamType } )
                    .done(function( data ) {
                        if( data === '' ) return;

                        if( streamType == 'out' ) {
                            // Console Out is incremental
                            consoleStream['out'] += data;
                            $('#' + tabId + ' pre').html(consoleStream['out']);
                        }
                        else {
                            // Console Err is complete
                            if( data === consoleStream['err'])
                                return;

                            consoleStream['err'] += data;
                            $('#' + tabId + ' pre').html(data);
                        }

                        if( !$('#' + tabId + ' pre').is(":visible") )
                            $('#' + tabId + ' pre').show();

                        // Scroll to bottom if user does not mouseover
                        if( !$('#' + tabId + ' pre').is(":hover") )
                            $('#' + tabId + ' pre').scrollTop($('#' + tabId + ' pre').prop("scrollHeight"));

                        // Show error notification if error console is updated and the tab not active
                        if( streamType === "err" && !$('#' + tabId).hasClass('active') ) {
                            if( $('ul.tabs li[data-refid="' + tabId + '"] a').find('i').length === 0 )
                                $('ul.tabs li[data-refid="' + tabId + '"] a').append('<i class="material-icons" data-info="report">report</i>');
                        }
                    })
                    .fail(function( data ) {
                        $('#' + tabId + ' pre').html("ERROR: Failed to load console").attr("class", "red-text");
                    })
                }
                // Function to handle preprocessing progress
                function updateStatus(initial) {
                    initial = initial || false;

                    // Update preprocessing progress in status collapsible
                    $.get( "ajax/preprocessing/progress" )
                    .done(function( data ) {
                        progress = data;
                        if( Math.floor(data) != data || !$.isNumeric(data) ) {
                            if( initial !== false ) $('.collapsible').collapsible('open', 0);
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("ERROR: Invalid AJAX response").attr("class", "red-text");
                            return;
                        }

                        if( data < 0 ) {
                            if( initial !== false ) $('.collapsible').collapsible('open', 0);
                            inProgress = false;
                            clearInterval(progressInterval);
                            // No ongoing preprocessing
                            $('.determinate').attr("style", "width: 0%");
                            return;
                        }

                        if( inProgress === false ) {
                            inProgress = true;
                            $('.status span').html("Ongoing").attr("class", "orange-text");
                        }

                        if( initial !== false ) {
                            $('.collapsible').collapsible('open', 2);
                            selectActiveTab();
                        }
                        // Update process bar
                        $('.determinate').attr("style", "width: " + data + "%");
                        // Update console output
                        updateConsole("out", "consoleOut");
                        updateConsole("err", "consoleErr");

                        // Terminate interval loop
                        if( data >= 100 ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("Completed").attr("class", "green-text");
                        }
                    })
                    .fail(function( data ) {
                        inProgress = false;
                        clearInterval(progressInterval);
                        $('.status span').html("ERROR: Failed to load status").attr("class", "red-text");
                    })
                }
                // Initial call to set progress variable
                updateStatus(true);
                progressInterval = setInterval(updateStatus, 1000);

                $('button[data-id="execute"]').click(function() {
                    if( inProgress === true ) {
                        $('#modal_inprogress').modal('open');
                    }
                    else {
                        if( $('input[type="number"]').hasClass('invalid') ){
                            $('#modal_errorhandling').modal('open');	
                            return;
                        }

                        // Show status view and hide others
                        if( $('.collapsible').find('li').eq(0).hasClass('active') )
                            $('.collapsible').collapsible('open', 0);
                        if( $('.collapsible').find('li').eq(1).hasClass('active') )
                            $('.collapsible').collapsible('open', 1);
                        if( !$('.collapsible').find('li').eq(2).hasClass('active') ) {
                            $('.collapsible').collapsible('open', 2);
                            selectActiveTab();
                        }

                        $.post( "ajax/preprocessing/execute?" + jQuery.param(getParams()) )
                        .fail(function( data ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("ERROR: Error during process execution").attr("class", "red-text");
                        })

                        // Update preprocessing status. Interval will be terminated in
                        // updateStatus(), if process is finished.
                        progressInterval = setInterval(updateStatus, 1000);
                    }
                });

                $('button[data-id="cancel"]').click(function() {
                    if( inProgress !== true ) {
                        $('#modal_noprocess').modal('open');
                    }
                    else {
                        $.post( "ajax/preprocessing/cancel" )
                        .done(function( data ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("No Preprocessing process running").attr("class", "");
                            $('#modal_successfulcancel').modal('open');
                        })
                        .fail(function( data ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("ERROR: Error during process cancelling").attr("class", "red-text");
                            $('#modal_failcancel').modal('open');
                        })
                    }
                });
            });
        </script>
    </t:head>
    <t:body heading="Preprocessing">
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
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p>Skew angle estimation parameters (degrees)</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--maxskew" type="number" step="0.1" />
                                                <label for="--maxskew" data-type="float" data-error="Has to be float">Default: 2 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Number of parallel threads for program execution</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--parallel" type="number" />
                                                <label for="--parallel" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings (Advanced)</div>
                        <div class="collapsible-body">
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p>Disable error checking on inputs</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="--nocheck" />
                                                <label for="--nocheck"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Threshold, determines lightness</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--threshold" type="number" step="0.01" />
                                                <label for="--threshold" data-type="float" data-error="Has to be float">Default: 0.5 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Zoom for page background estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--zoom" type="number" step="0.01" />
                                                <label for="--zoom" data-type="float" data-error="Has to be float">Default: 0.5 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Scale for estimating a mask over the text region</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--escale" type="number" step="0.1" />
                                                <label for="--escale" data-type="float" data-error="Has to be float">Default: 1.0 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Ignore this much of the border for threshold estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--bignore" type="number" step="0.01" />
                                                <label for="--bignore" data-type="float" data-error="Has to be float">Default: 0.1 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Percentage for filters</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--perc" type="number" />
                                                <label for="--perc" data-type="float" data-error="Has to be float">Default: 80 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Range for filters</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--range" type="number" />
                                                <label for="--range" data-type="int" data-error="Has to be integer">Default: 20 (Integer value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Force grayscale processing even if image seems binary</p></td>
                                        <td>
                                            <p>
                                                <input type="checkbox" class="filled-in" id="--gray" />
                                                <label for="--gray"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Percentile for black estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--lo" type="number" step="0.1" />
                                                <label for="--lo" data-type="float" data-error="Has to be float">Default: 5 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Percentile for white estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--hi" type="number" />
                                                <label for="--hi" data-type="float" data-error="Has to be float">Default: 90 (Float value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Steps for skew angle estimation (per degree)</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--skewsteps" type="number" />
                                                <label for="--skewsteps" data-type="int" data-error="Has to be integer">Default: 8 (Integer value)</label>
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Preprocessing process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                            <div class="console">
                                 <ul class="tabs">
                                     <li class="tab" data-refid="consoleOut"><a href="#consoleOut">Console Output</a></li>
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

                <!-- In progress information -->
                <div id="modal_inprogress" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>
                            There already is a running Preprocessing process.<br/>
                            Please wait until it is finished or cancel it.
                        </p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- Error handling-->
                <div id="modal_errorhandling" class="modal">
                    <div class="modal-content red-text">
                        <h4>Information</h4>
                        <p>
                            There exists an error in the input.<br/>
                            Please fix it and try again.
                        </p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- No current process information -->
                <div id="modal_noprocess" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>There exists no ongoing Preprocessing process.</p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- Successful cancel information -->
                <div id="modal_successfulcancel" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>The Preprocessing process was cancelled successfully.</p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- Failed cancel information -->
                <div id="modal_failcancel" class="modal">
                    <div class="modal-content red-text">
                        <h4>Error</h4>
                        <p>The Preprocessing process could not be cancelled.</p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
            </div>
        </div>
    </t:body>
</t:html>
