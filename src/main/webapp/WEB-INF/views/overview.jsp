<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head projectDataSel="true">
        <title>OCR4All - Project Overview</title>

        <!-- jQuery DataTables -->
        <link rel="stylesheet" type="text/css" href="resources/css/datatables.min.css">
        <script type="text/javascript" charset="utf8" src="resources/js/datatables.min.js"></script>

        <script type="text/javascript">
            $(document).ready(function() {
                // Initialize project data selection
                initializeProjectDataSelection('ajax/overview/listProjects');

                var datatableReloadIntveral = null;
                // Responsible for initializing and updating datatable contents
                function datatable(){
                    // Allow reinitializing DataTable with new data
                    if( $.fn.DataTable.isDataTable("#overviewTable") ) {
                        $('#overviewTable').DataTable().clear().destroy();
                    }

                    var overviewTable = $('#overviewTable').DataTable( {
                        ajax : {
                            "type"   : "GET",
                            "url"    : "ajax/overview/list",
                            "dataSrc": function (data) { return data; },
                            "error"  : function() {
                                openCollapsibleEntriesExclusively([0]);
                                $('#projectDir').addClass('invalid').focus();
                                // Prevent datatable from reloading an invalid directory
                                clearInterval(datatableReloadIntveral);
                            }
                        },
                        columns: [
                            { title: "Page Identifier", data: "pageId" },
                            { title: "Preprocessing", data: "preprocessed" },
                            { title: "Noise Removal", data: "despeckled"},
                            { title: "Segmentation", data: "segmented" },
                            { title: "Region Extraction", data: "segmentsExtracted" },
                            { title: "Line Segmentation", data: "linesExtracted" },
                            { title: "Recognition", data: "recognition" },
                        ],
                        createdRow: function( row, data, index ){
                            $('td:first-child', row).html('<a href="pageOverview?pageId=' + data.pageId + '">' + data.pageId + '</a>');
                            $.each( $('td:not(:first-child)', row), function( idx, td ) {
                                if( $(td).html() === 'true' ) {
                                    $(td).html('<i class="material-icons green-text">check</i>');
                                }
                                else {
                                    $(td).html('<i class="material-icons red-text">clear</i>');
                                }
                            });
                        },
                        initComplete: function() {
                            openCollapsibleEntriesExclusively([1]);

                            // Initialize select input
                            $('select').material_select();

                            // Update overview continuously
                            datatableReloadIntveral = setInterval( function() {
                                overviewTable.ajax.reload(null, false);
                            }, 10000);
                        },
                    });
                }

                // Responsible for verification and loading of the project
                function projectInitialization(newPageVisit) {
                    var ajaxParams = { "projectDir" : $('#projectDir').val(), "imageType" : $('#imageType').val() };
                    // Check if directory exists
                    $.get( "ajax/overview/checkDir?",
                           // Only force new session if project loading is triggered by user
                           $.extend(ajaxParams, {"resetSession" : !newPageVisit})
                    )
                    .done(function( data ) {
                        if( data === true ) {
                            $.get( "ajax/overview/validate?" )
                            .done(function( data ) {
                                if( data === true ) {

                                    // Check if filenames match project specific naming convention
                                    $.get( "ajax/overview/validateProject?", ajaxParams )
                                    .done(function( data ) {
                                         if( data === true ) {
                                             // Two scenarios for loading overview page:
                                             // 1. Load or reload new project: Page needs reload to update GTC_Web link in navigation
                                             // 2. Load project due to revisiting overview page: Only datatable needs to be initialized
                                             if( newPageVisit == false ) {
                                                 location.reload();
                                             }
                                             else {
                                                 datatable();
                                             }
                                         }
                                         else{
                                             $('#modal_filerename').modal({
                                                 dismissible: false
                                             });
                                             $('#modal_filerename').modal('open');
                                         }
                                    });
                                }
                                else{
                                    $('#modal_validateDir').modal('open');
                                }
                            });
                        }
                        else {
                            openCollapsibleEntriesExclusively([0]);
                            $('#projectDir').addClass('invalid').focus();
                            // Prevent datatable from reloading an invalid directory
                            clearInterval(datatableReloadIntveral);

                            $('#modal_checkDir_failed').modal('open');
                        }
                    })
                    .fail(function( data ) {
                        $('#modal_checkDir_failed').modal('open');
                    });
                }

                $("button").click(function() {
                    if( $.trim($('#projectDir').val()).length === 0 ) {
                        openCollapsibleEntriesExclusively([0]);
                        $('#projectDir').addClass('invalid').focus();
                    }
                    else {
                        projectInitialization(false);
                    }
                });

                // invalidates Session
                $('#disagree').click(function() {
                    $.get( "ajax/overview/invalidateSession" )
                });

                // Execute file rename only after the user agreed
                $('#agree').click(function() {
                    $.get( "ajax/overview/adjustProjectFiles" )
                    .done(function( data ) {
                        datatable();
                    })
                    .fail(function( data ) {
                        $('#modal_exists_failed').modal('open');
                    });
                });

                // Trigger overview table fetching on pageload
                if( $.trim($('#projectDir').val()).length !== 0 ) {
                    projectInitialization(true);
                } else {
                    openCollapsibleEntriesExclusively([0]);
                }
            });
        </script>
    </t:head>
    <t:body heading="Project Overview">
        <div class="container">
            <div class="section">
                <button class="btn waves-effect waves-light" type="submit" name="action">
                    Load Project
                    <i class="material-icons right">send</i>
                </button>

                <ul class="collapsible" data-collapsible="accordion">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <s:overview></s:overview>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">dehaze</i>Overview</div>
                        <div class="collapsible-body">
                            <table id="overviewTable" class="display centered" width="100%"></table>
                        </div>
                    </li>
                </ul>

                <button class="btn waves-effect waves-light" type="submit" name="action">
                    Load Project
                    <i class="material-icons right">send</i>
                </button>
            </div>
        </div>

        <div id="modal_filerename" class="modal">
            <div class="modal-content">
                <h4 class="red-text">Attention</h4>
                    <p>
                        Some or all files do not match the required naming convention for this tool.<br />
                        If you agree the affected files will be renamed automatically.
                    </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat ">Disagree</a>
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
         </div>
        <div id="modal_filerename_failed" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                    <p>
                        Renaming of the image files failed.<br />
                        Due to this error the project could not be loaded.
                    </p>
            </div>
            <div class="modal-footer">
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
         </div>
        <div id="modal_validateDir" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                    <p>
                        The selected project directory does not have the required structure.<br />
                        Please put the project related image files in a sub-directory named "Original".<br />
                        Until then the project cannot be loaded successfully.
                    </p>
            </div>
            <div class="modal-footer">
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
         </div>
        <div id="modal_checkDir_failed" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                    <p>
                        The specified project directory could not accessed.<br />
                        Due to this error the project could not be loaded.
                    </p>
            </div>
            <div class="modal-footer">
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
         </div>
    </t:body>
</t:html>
