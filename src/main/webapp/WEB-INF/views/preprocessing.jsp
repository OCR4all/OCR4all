<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>OCR4All - Preprocessing</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Load image list
                initializeImageList("Original");

                // Initialize process update and set options
                initializeProcessUpdate("preprocessing", [ 0 ], [ 2 ], true);

                // Set available threads as default 
                $.get( "ajax/generic/threads" )
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
                function getInputParams() {
                    var params = { 'cmdArgs': [] };
                    // Exclude checkboxes in pagelist (will be passed separately)
                    $.each($('input[type="checkbox"]').not('[data-pageid]').not('#selectAll'), function() {
                        if( $(this).prop('checked') === true )
                            params['cmdArgs'].push($(this).attr('id'));
                    });
                    $.each($('input[type="number"]'), function() {
                        if( $(this).val() !== "" )
                            params['cmdArgs'].push($(this).attr('id'), $(this).val());
                    });
                    return params;
                }

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

                    var ajaxParams = $.extend( { "pageIds[]" : selectedPages }, getInputParams() );
                    // Execute Preprocessing process
                    executeProcess(ajaxParams);
                });

                $('button[data-id="cancel"]').click(function() {
                    cancelProcess();
                });
            });
        </script>
    </t:head>
    <t:body heading="Preprocessing" imageList="true" processModals="true">
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
