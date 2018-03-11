<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="false" processHandler="true">
        <title>OCR4All - Segmentation</title>

        <script type="text/javascript">
            $(document).ready(function() {
                initializeProcessUpdate("segmentation", [ 0 ], [ 1 ], false);

                // Prevent redirecting to Larex if image folder does not exist
                $("#larexForm").submit(function(e){
                    $.ajax({
                        url : "ajax/generic/checkDir",
                        type: "GET",
                        data: { "imageType" : $('#imageType').val() },
                        async : false,
                        success : function( dirExists ) {
                            if( dirExists === false){
                                $('#modal_alert').modal('open');
                                e.preventDefault();
                            }
                        },
                    });
                });

                $('#imageType').on('change', function() {
                    $('#bookname').val($('#imageType').val());
                });
                $('#imageType').change();

                // Process handling (execute for all pages with current settings)
                $('button[data-id="execute"]').click(function() {
                    var ajaxParams =  { "imageType" : $('#imageType').val(), "replace" : $('#replace').prop('checked')};
                    executeProcess(ajaxParams);
                });
            });
        </script>
    </t:head>
    <t:body heading="Segmentation" processModals="true">
        <div class="container">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Apply Segmentation results
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">line_style</i>Segmentation</div>
                        <div class="collapsible-body">
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p>Segmentation image type</p></td>
                                        <td>
                                            <div class="input-field">
                                                <i class="material-icons prefix">image</i>
                                                <select id="imageType" name="imageType" class="suffix">
                                                    <option value="Despeckled">Despeckled</option>
                                                    <option value="Binary">Binary</option>
                                                </select>
                                                <label></label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p> Replace existing images </p></td>
                                        <td>
                                             <p>
                                                <input type="checkbox" class="filled-in" id="replace" />
                                                <label class ="tooltipped" data-position="right" data-html="true" data-delay="50" data-tooltip= "This checkbox is most likely only required if the project type (gray/binary) </br> has changed in the course of the session" for="replace"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                    <tr>
                                        <td>
                                            <form id="larexForm" action="/Larex/direct" method="POST" target="_blank">
                                                <input type="hidden" id="bookpath" name="bookpath" value="${projectDir}PreProc" />
                                                <input type="hidden" id="bookname" name="bookname" value="" />
                                                <input type="hidden" id="websave" name="websave" value="false" />
                                                <input type="hidden" id="localsave" name="localsave" value="bookpath" />
                                                <button data-id="openLarex" class="btn waves-effect waves-light" type="submit" name="action">
                                                    Open LAREX
                                                    <i class="material-icons right">chevron_right</i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No segmentation process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                </ul>

                <button data-id="execute" class="btn waves-effect waves-light">
                    Apply Segmentation results
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>
            </div>
        </div>

        <div id="modal_alert" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    The  directory for selected image type does not exist.<br />
                    Use appropriate modules to create these images.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
    </t:body>
</t:html>
