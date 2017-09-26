<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function() {
                var inProgress = false;
                var progressInterval = null;

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

                function updateProgressBar(initial) {
                    initial = initial || false;

                    $.get( "ajax/preprocessing/progress" )
                    .done(function( data ) {
                        progress = data;
                        if( Math.floor(data) != data || !$.isNumeric(data) ) {
                            if( initial !== false ) $('.collapsible').collapsible('open', 0);
                            inProgress = false;
                            clearInterval(progressInterval);
                            return; //TODO: Error handling
                        }

                        if( data < 0 ) {
                            if( initial !== false ) $('.collapsible').collapsible('open', 0);
                            inProgress = false;
                            clearInterval(progressInterval);
                            // No ongoing preprocessing
                            $('.determinate').attr("style", "width: 0%");
                            return;
                        }

                        if( initial !== false ) $('.collapsible').collapsible('open', 1);
                        // Update process bar
                        $('.determinate').attr("style", "width: " + data + "%");
                        inProgress = true;

                        // Terminate interval loop
                        if( data >= 100 ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                        }
                    })
                    .fail(function( data ) {
                        inProgress = false;
                        clearInterval(progressInterval);
                        //TODO: Error handling
                    })
                }
                // Initial call to set progress variable
                updateProgressBar(true);
                progressInterval = setInterval(updateProgressBar, 1000);

                $("button").click(function() {
                    if( inProgress === true ) {
                        //TODO: Error handling
                    }
                    else {
                        // Show status view
                        $('.collapsible').collapsible('open', 1);

                        // Update preprocessing status. Interval will be terminated in
                        // updateProgressBar(), if process is finished.
                        progressInterval = setInterval(updateProgressBar, 1000);
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
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                            <div class="console"><pre></pre></div>
                        </div>
                    </li>
                </ul>

                <button class="btn waves-effect waves-light" type="submit" name="action">
                    Start
                    <i class="material-icons right">send</i>
                </button>
            </div>
        </div>
    </t:body>
</t:html>
