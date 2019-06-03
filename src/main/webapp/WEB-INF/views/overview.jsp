<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:html>
    <t:head projectDataSel="true" processHandler="true">
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
                            <c:if test='${(not empty processingMode) && (processingMode == "Directory")}'>
                            { title: "Region Extraction", data: "segmentsExtracted" },
                            </c:if>
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
                            openCollapsibleEntriesExclusively([2]);

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
                    var ajaxParams = { "projectDir" : $('#projectDir').val(), "imageType" : $('#imageType').val(), "processingMode" : $('#processingMode').val() };
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
                                                 // Load datatable after the last process update is surely finished
                                                     datatable();
                                             }
                                         }
                                         else{
                                             openCollapsibleEntriesExclusively([0]);
                                             $('#modal_imageAdjust').modal({
                                                 dismissible: false
                                             });
                                             $('#modal_imageAdjust').modal('open');
                                         }
                                    });
                                }
                                else{
                                    // Unload project if directory structure is not valid
                                    $.get( "ajax/overview/invalidateSession" );

                                    openCollapsibleEntriesExclusively([0]);
                                    $('#modal_validateDir').modal('open');
                                }
                            });
                        }
                        else {
                            // Unload project if directory does not exist
                            $.get( "ajax/overview/invalidateSession" );

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

                $('button[data-id="loadProject"]').click(function() {
                    if( $.trim($('#projectDir').val()).length === 0 ) {
                        openCollapsibleEntriesExclusively([0]);
                        $('#projectDir').addClass('invalid').focus();
                    }
                    else {
                        // Only load project if no conversion process is running
                        setTimeout(function() {
                            if( !isProcessRunning() ) {
                                projectInitialization(false);
                            }
                            else {
                                $('#modal_inprogress').modal('open');
                            }
                        }, 500);
                    }
                });

                // Execute file rename only after the user agreed
                $('#directConvert, #backupAndConvert').click(function() {
                    // Initialize process handler (wait time, due to delayed AJAX process start)
                    setTimeout(function() {
                        initializeProcessUpdate("overview", [ 0 ], [ 1 ], false);
                    }, 500);

                    // Start process
                    var ajaxParams = {"backupImages" : ( $(this).attr('id') == 'backupAndConvert' )};
                    $.post( "ajax/overview/adjustProjectFiles", ajaxParams )
                    .done(function( data ) {
                        // Load datatable after the last process update is surely finished
                        setTimeout(function() {
                            datatable();
                        }, 2000);
                    })
                    .fail(function( data ) {
                        $('#modal_adjustImages_failed').modal('open');
                    });
                });
                $('#cancelConvert').click(function() {
                    setTimeout(function() {
                        // Unload project if user refuses the mandatory adjustments
                        if( !isProcessRunning() ) {
                            $.get( "ajax/overview/invalidateSession" );
                        }
                    }, 500);
                });

                $('button[data-id="cancelProjectAdjustment"]').click(function() {
                    cancelProcess();

                    // Unload project if user cancels the mandatory adjustments
                    setTimeout(function() {
                        $.get( "ajax/overview/invalidateSession" );
                    }, 500);
                });

                // Trigger overview table fetching on pageload
                if( $.trim($('#projectDir').val()).length !== 0 ) {
                    initializeProcessUpdate("overview", [ 0 ], [ 1 ], false);
                    setTimeout(function() {
                        // Load project only if no conversion process is currently running
                        if( !isProcessRunning() ) {
                            projectInitialization(true);
                        }
                    }, 500);
                } else {
                    openCollapsibleEntriesExclusively([0]);
                }
            });
        </script>
    </t:head>
    <t:body heading="Project Overview" processModals="true">
        <div class="container">
            <div class="section">
                <button data-id="loadProject" class="btn waves-effect waves-light" type="submit" name="action">
                    Load Project
                    <i class="material-icons right">send</i>
                </button>
                <button data-id="cancelProjectAdjustment" class="btn waves-effect waves-light" type="submit" name="action">
                    Cancel Project Adjustment
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="accordion">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <s:overview></s:overview>
                        </div>
                    </li>
                    <li style="display: block;">
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Project Overview process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">dehaze</i>Overview</div>
                        <div class="collapsible-body">
                            <table id="overviewTable" class="display centered" width="100%"></table>
                        </div>
                    </li>
                </ul>

                <button data-id="loadProject" class="btn waves-effect waves-light" type="submit" name="action">
                    Load Project
                    <i class="material-icons right">send</i>
                </button>
                <button data-id="cancelProjectAdjustment" class="btn waves-effect waves-light" type="submit" name="action">
                    Cancel Project Adjustment
                    <i class="material-icons right">cancel</i>
                </button>
            </div>
        </div>

        <div id="modal_imageAdjust" class="modal">
            <div class="modal-content">
                <h4 class="red-text">Attention</h4>
                    <p>
                        Some or all files do not match the required format of this software.<br />
                        <br />
                        The requirements are:<br />
                        1. All image files need to be in PNG format and have a ".png" file ending<br />
                        2. All image files need to be named accordingly: "0001.png, 0002.png, 0003.png, ..."<br />
                        <br />
                        To be able to load your project successfully the affected files need to be adjusted.<br />
                        Please choose one of the offered possibilities to continue.<br />
                        <br />
                        Short explanation of the different possibilities:<br />
                        1. <i>Convert files directly</i><br />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;All image files are adjusted automatically. The existing files will be replaced!<br />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Use at your own risk, e.g. if you already have a backup of your files or do not need one.<br />
                        2. <i>Backup and convert files</i><br />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;A backup of all image files will be done automatically before the adjustment.<br />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is the safe option because a backup is create before any changes are made.
                    </p>
            </div>
            <div class="modal-footer">
                <a href="#!" id="cancelConvert" class="modal-action modal-close waves-effect waves-green btn-flat ">Cancel</a>
                <a href="#!" id="directConvert" class="modal-action modal-close waves-effect waves-green btn-flat">Convert files directly</a>
                <a href="#!" id="backupAndConvert" class="modal-action modal-close waves-effect waves-green btn-flat">Backup and convert files</a>
            </div>
         </div>
        <div id="modal_adjustImages_failed" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                    <p>
                        Adjustment of image files to the required format failed.<br />
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
                        Please put the project related image files in a sub-directory named "input".<br />
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
