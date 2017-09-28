<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function() {
                var inProgress = false;
                var progressInterval = null;
                var consoleOutput = "";

                // Initialize modals
                $('.modal').modal();

                // Fetch all modified parameters and return them appropriately
                function getParams() {
                    var params = { 'cmdArgs': [] };
                    $.each($('input[type="checkbox"]'), function() {
                        if( $(this).prop('checked') === true )
                            params['cmdArgs'].push($(this).attr('id'));
                    });
                    $.each($('input[type="text"]'), function() {
                        if( $(this).val() !== "" )
                            params['cmdArgs'].push($(this).attr('id'), $(this).val());
                    });
                    return params;
                }

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

                        if( initial !== false ) $('.collapsible').collapsible('open', 1);
                        // Update process bar
                        $('.determinate').attr("style", "width: " + data + "%");
                        // Update console output
                        $.get( "ajax/preprocessing/console" )
                        .done(function( data ) {
                            consoleOutput += data;
                            $('.console pre').html(consoleOutput).show();
                            // Scroll to bottom if user does not mouseover
                            if( !$('.console pre').is(":hover") )
                                $('.console pre').scrollTop($('.console pre').prop("scrollHeight"));
                        })
                        .fail(function( data ) {
                            $('.console pre').html("ERROR: Failed to load status").attr("class", "red-text");
                        })

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

                $("#execute").click(function() {
                    if( inProgress === true ) {
                        $('#modal_inprogress').modal('open');
                    }
                    else {
                        // Show status view
                        $('.collapsible').collapsible('open', 1);

                        $.post( "ajax/preprocessing/execute?" + jQuery.param(getParams()) )
                        .fail(function( data ) {
                            $('.status span').html("ERROR: Error during process execution").attr("class", "red-text");
                        })

                        // Update preprocessing status. Interval will be terminated in
                        // updateProgressBar(), if process is finished.
                        progressInterval = setInterval(updateStatus, 1000);
                    }
                });

                $("#cancel").click(function() {
                    if( inProgress !== true ) {
                        $('#modal_noprocess').modal('open');
                    }
                    else {
                        $.post( "ajax/preprocessing/cancel" )
                        .done(function( data ) {
                            $('#modal_successfulcancel').modal('open');
                        })
                        .fail(function( data ) {
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
                <ul class="collapsible" data-collapsible="accordion">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
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
                                                <input id="--threshold" type="text" />
                                                <label for="--threshold">Default: 0.5</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Zoom for page background estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--zoom" type="text" />
                                                <label for="--zoom">Default: 0.5</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Scale for estimating a mask over the text region</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--escale" type="text" />
                                                <label for="--escale">Default: 1.0</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Scale for estimating a mask over the text region</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--escale" type="text" />
                                                <label for="--escale">Default: 1.0</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Ignore this much of the border for threshold estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--bignore" type="text" />
                                                <label for="--bignore">Default: 0.1</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Percentage for filters</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--perc" type="text" />
                                                <label for="--perc">Default: 80</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Range for filters</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--range" type="text" />
                                                <label for="--range">Default: 20</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Skew angle estimation parameters (degrees)</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--maxskew" type="text" />
                                                <label for="--maxskew">Default: 2</label>
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
                                                <input id="--lo" type="text" />
                                                <label for="--lo">Default: 5</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Percentile for white estimation</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--hi" type="text" />
                                                <label for="--hi">Default: 90</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Steps for skew angle estimation (per degree)</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--skewsteps" type="text" />
                                                <label for="--skewsteps">Default: 8</label>
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
                            <div class="console"><pre></pre></div>
                        </div>
                    </li>
                </ul>

                <button id="execute" class="btn waves-effect waves-light">
                    Execute
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <!-- In progress information -->
                <div id="modal_inprogress" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>
                            There already exists an ongoing Preprocessing process.<br/>
                            Please wait until the current one is finished or cancel it.
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
