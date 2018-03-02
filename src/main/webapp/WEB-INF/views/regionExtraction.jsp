<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>Region Extractor</title>

        <script type="text/javascript">
            $(document).ready(function() {
                initializeImageList("OCR");
                initializeProcessUpdate("regionExtraction", [ 0 ], [ 1 ], false);

                // Process handling (execute despeckling for all pages with current settings)
                $('button[data-id="execute"]').click(function() {
                    var selectedPages = getSelectedPages();
                    if( selectedPages.length === 0 ) {
                        $('#modal_errorhandling').modal('open');
                        return;
                    }

                    var ajaxParams = { "spacing" : $('input[id="spacing"]').val(), "usespacing" : $('input[id=usespacing]').prop('checked'),
                    		           "avgbackground" : $('input[id=avgbackground]').prop('checked'), "pageIds[]" : selectedPages };
                    // Execute Preprocessing process
                    executeProcess(ajaxParams);
                    });
                $('button[data-id="cancel"]').click(function() {
                    cancelProcess();
                });
            });
        </script>
    </t:head>
    <t:body heading="Region Extraction" imageList="true" processModals="true">
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
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p> Use average background</p></td>
                                        <td>
                                             <p>
                                                <input type="checkbox" class="filled-in" id="avgbackground" />
                                                <label for="avgbackground"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p> Use spacing</p></td>
                                        <td>
                                             <p>
                                                <input type="checkbox" class="filled-in" id="usespacing" checked="checked"/>
                                                <label for="usespacing"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Spacing</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="spacing" type="number" value="10" />
                                                <label for="spacing" data-type="int" data-error="Has to be integer" >Default: 10 </label>
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
