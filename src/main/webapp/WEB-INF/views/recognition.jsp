<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>OCR4All - Recognition</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Load image list
                $.get( "ajax/recognition/getImageIds")
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
                                        <td><p>Line recognition model</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--model" type="text" step="0.1" />
                                                <label for="--model" data-type="text">Path to the model</label>
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
                            <ul class="collapsible" data-collapsible="accordion">
                                <li>
                                    <div class="collapsible-header">Line dewarping (usually contained in model)</div>
                                    <div class="collapsible-body">
                                        <table class="compact">
                                            <tbody>
                                                <tr>
                                                    <td><p>No line estimation</p></td>
                                                    <td>
                                                        <p>
                                                            <input type="checkbox" class="filled-in" id="--nolineest"/>
                                                            <label for="--nolineest"></label>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>target line height (overrides recognizer)</p></td>
                                                    <td>
                                                        <div class="input-field">
                                                            <input id="--height" type="number" step="0.1" />
                                                            <label for="--height" data-type="float" data-error="Has to be integer">Default: -1</label>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </li>
                                <li>
                                    <div class="collapsible-header">Recognition</div>
                                    <div class="collapsible-body">
                                        <table class="compact">
                                            <tbody>
                                                <tr>
                                                    <td><p>extra blank padding to the left and right of text line</p></td>
                                                    <td>
                                                        <div class="input-field">
                                                            <input id="--pad" type="number" step="1" />
                                                            <label for="--pad" data-type="float" data-error="Has to be integer">Default: 16</label>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Don't normalize the textual output from the recognizer</p></td>
                                                    <td>
                                                        <p>
                                                            <input type="checkbox" class="filled-in" id="--nonormalize"/>
                                                            <label for="--nonormalize"></label>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Output LSTM locations for characters</p></td>
                                                    <td>
                                                        <p>
                                                            <input type="checkbox" class="filled-in" id="--llocs"/>
                                                            <label for="--llocs"></label>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Output extended llocs</p></td>
                                                    <td>
                                                        <p>
                                                            <input type="checkbox" class="filled-in" id="--llocsext"/>
                                                            <label for="--llocsext"></label>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Output aligned LSTM locations for characters</p></td>
                                                    <td>
                                                         <p>
                                                            <input type="checkbox" class="filled-in" id="--alocs"/>
                                                            <label for="--alocs"></label>
                                                         </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Output probabilities for each letter</p></td>
                                                    <td>
                                                        <p>
                                                            <input type="checkbox" class="filled-in" id="--probabilities"/>
                                                            <label for="--probabilities"></label>
                                                        </p>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </li>
                                <li>
                                    <div class="collapsible-header">Error measures</div>
                                    <div class="collapsible-body">
                                        <table class="compact">
                                            <tbody>
                                                <tr>
                                                    <td><p>Estimate error rate only</p></td>
                                                    <td>
                                                        <p>
                                                            <input type="checkbox" class="filled-in" id="--estrate"/>
                                                            <label for="--estrate"></label>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Estimate confusion matrix </p></td>
                                                    <td>
                                                        <div class="input-field">
                                                        <input id="--estconf" type="number" step="1" />
                                                        <label for="--estconf" data-type="float" data-error="Has to be integer">Default: 20</label>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>String comparison used for error rate estimate</p></td>
                                                    <td>
                                                        <div class="input-field">
                                                            <input id="--compare" type="text" />
                                                            <label for="--compare" data-type="text" >Default: nospace</label>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><p>Context for error reporting</p></td>
                                                    <td>
                                                        <div class="input-field">
                                                            <input id="--context" type="number" step="0.1" />
                                                            <label for="--context" data-type="float" data-error="Has to be integer">Default: 0</label>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </li>
                              </ul>
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
