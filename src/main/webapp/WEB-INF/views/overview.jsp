<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:html>
    <t:head projectDataSel="true" processHandler="true">
        <title>OCR4all - Project Overview</title>

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
                            { title: "Line Segmentation", data: "linesExtracted" },
                            { title: "Recognition", data: "recognition" },
                            { title: "Ground Truth", data: "groundtruth" },
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
                function projectInitialization(newPageVisit, allowLegacy=false) {
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
                                            // Check if the project still contains legacy files
                                            $.get("ajax/overview/isLegacy")
                                            .done(function(data) {
                                                if( !allowLegacy && data === true ){
                                                    $('#modal_legacy').modal({
                                                        dismissible: true
                                                    });
                                                    $('#modal_legacy').modal('open');
                                                } else {
                                                    // Check if dir only houses pdf files and no images
                                                    $.get("ajax/overview/checkpdf")
                                                        .done(function(data) {
                                                        if( data === true) {
                                                            openCollapsibleEntriesExclusively([0]);
                                                            $('#modal_convertpdf').modal({
                                                                dismissible: false
                                                            });
                                                            $('#modal_convertpdf').modal('open');
                                                        }
                                                        else {
                                                            // Load datatable after the last process update is surely finished
                                                            datatable();

                                                            // Dynamically change loaded project display as site isn't getting reloaded after project loading anymore
                                                            $.get("ajax/overview/getProjectName")
                                                                .done(function(projectName){
                                                                    const $projectName = $('.project-name span');

                                                                    $projectName.text(projectName);

                                                                    const projectLoadedEvent = new CustomEvent("projectLoaded", {
                                                                        bubbles: true
                                                                    });
                                                                    $projectName[0].dispatchEvent(projectLoadedEvent);

                                                                    // Load datatable after the last process update is surely finished
                                                                    datatable();
                                                                })
                                                        }
                                                    });
                                                }
                                            });
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

                $("[data-id='exportGTC']").click(function(){
                    localStorage.setItem('export_gt_clicked', 'True');
                    $("[data-id='exportGTC']").removeClass("pulse");
                    let export_gt_info_shown = localStorage.getItem('export_gt_info_shown');
                    if(!export_gt_info_shown){
                        $("#export_gt_info").modal("open");
                        localStorage.setItem('export_gt_info_shown', 'True');
                    }
                })

                function checkExportGTClicked(){
                    let export_gt_clicked = localStorage.getItem("export_gt_clicked");
                    if(!export_gt_clicked){
                        $("[data-id='exportGTC']").addClass("pulse");
                    }else{
                        $("[data-id='exportGTC']").removeClass("pulse");
                    }
                }

                checkExportGTClicked();


                function exportData(newPageVisit, completeDir, pages, binary, gray) {
                    var ajaxParams = { "projectDir" : $('#projectDir').val(), "imageType" : $('#imageType').val(), "processingMode" : $('#processingMode').val() };
                    // Check if directory exists
                    $.get( "ajax/overview/checkDir?",
                        // Only force new session if project loading is triggered by user
                        $.extend(ajaxParams, {"resetSession" : false})
                    )
                        .done(function( data ) {
                            if( data === true ) {
                                $.get( "ajax/overview/validate?" )
                                    .done(function( data ) {
                                        if( data === true ) {
                                                var ajaxParams = {"completeDir" : completeDir, "pages" : pages, "binary" : binary, "gray" : gray};
                                                $.post( "ajax/overview/exportGtc", ajaxParams )
                                                    .done(function( data ) {
                                                        setTimeout(function() {
                                                            datatable();
                                                        }, 2000);
                                                        $('#gtc_spinner').addClass('hiddendiv');
                                                        $('#zip_path').text("> " + data);
                                                        $('#modal_export_finished').modal('open');
                                                    })
                                                    .fail(function( data ) {
                                                        $('#modal_exportgtc_failed').modal('open');
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
                    // Enable Cancel Project Adjustment
                    $('button[data-id="cancelProjectAdjustment"]').removeClass('disabled');

                    // Initialize process handler (wait time, due to delayed AJAX process start)
                    setTimeout(function() {
                        initializeProcessUpdate("overview", [ 0 ], [ 1 ], false);
                    }, 500);

                    // Start process
                    var ajaxParams = {"backupImages" : ( $(this).attr('id') == 'backupAndConvert' )};
                    $.post( "ajax/overview/adjustProjectFiles", ajaxParams )
                    .done(function( data ) {
                        // Disable Cancel Project Adjustment
                        $('button[data-id="cancelProjectAdjustment"]').addClass('disabled');
                        // Load datatable after the last process update is surely finished
                        setTimeout(function() {
                            datatable();
                        }, 2000);
                    })
                    .fail(function( data ) {
                        // Disable Cancel Project Adjustment
                        $('button[data-id="cancelProjectAdjustment"]').addClass('disabled');
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
                $('#cancelConvertPdf').click(function() {
                    setTimeout(function() {
                    }, 500);
                });
                $('#convertToPdf, #convertToPdfWithBlanks').click(function() {
                    $('button[data-id="cancelProjectAdjustment"]').removeClass('disabled');
                    // Initialize process handler (wait time, due to delayed AJAX process start)
                    setTimeout(function() {
                        initializeProcessUpdate("overview", [ 0 ], [ 1 ], false);
                    }, 500);

                    // Start converting PDF
                    var ajaxParams = {"deleteBlank" : ( $(this).attr('id') == 'convertToPdf' ), "dpi" : document.getElementById('dpi').value};
                    $.post( "ajax/overview/convertProjectFiles", ajaxParams )
                        .done(function( data ) {
                            $('button[data-id="cancelProjectAdjustment"]').addClass('disabled');
                            // Load datatable after the last process update is surely finished
                            setTimeout(function() {
                                datatable();
                            }, 2000);
                        })
                        .fail(function( data ) {
                            $('button[data-id="cancelProjectAdjustment"]').addClass('disabled');
                        });
                });

                // Convert legacy project
                $('#convertLegacy').click(function() {
                    $("#legacy-convert-preloader").show();

                    setTimeout(function() {
                        initializeProcessUpdate("overview", [ 0 ], [ 1 ], false);
                    }, 500);

                    var ajaxParams = {"backupLegacy" : document.getElementById('backupCheckbox').checked};
                    $.post("ajax/overview/convertLegacyProject", ajaxParams)
                        .done(function() {
                            $("#legacy-convert-preloader").hide();
                            $("#modal_legacy_convert").modal("close");
                            Materialize.toast("Project successfully converted!", 1000, "green");
                            // Load datatable after the last process update is surely finished
                            setTimeout(function() {
                                datatable();
                            }, 500);
                        })
                        .fail(function() {
                            $("#legacy-convert-preloader").hide();
                            Materialize.toast("Project can't be converted!", 2000, "red");
                            $.get( "ajax/overview/invalidateSession" );
                        });
                })
                $('#continueLegacy').click(function() {
                    setTimeout(function() {
                        // Unload project if user refuses the mandatory adjustments
                        if( !isProcessRunning() ) {
                            projectInitialization(true, true);
                        }
                    }, 500);
                });
                $('#exportAllPages, #exportPages').click(function() {
                    const $this = $(this);
                    $('#gtc_spinner').removeClass('hiddendiv')
                    // Initialize process handler (wait time, due to delayed AJAX process start)
                    setTimeout(function() {
                        if( !isProcessRunning() ) {
                            exportData(false,($this.attr('id') == 'exportAllPages'),
                                document.getElementById('pageNo').value,
                                document.getElementById('binaryCheckbox').checked,
                                document.getElementById('grayCheckbox').checked);
                        }
                        else {
                            $('#modal_inprogress').modal('open');
                        }
                    }, 500);
                });

                $('button[data-id="cancelProjectAdjustment"]').click(function() {
                    $('button[data-id="cancelProjectAdjustment"]').addClass('disabled');
                    //cancel only in file conversion progress
                    if(globalInProgress) {
                        cancelProcess();
                    }
                    else {
                        $.post( "ajax/" + globalController + "/cancel" )
                            .done(function( data ) {
                                stopProcessUpdate("Process cancelled", "");
                                $('#modal_successfulcancel').modal('open');
                            })
                            .fail(function( data ) {
                                stopProcessUpdate("ERROR: Error during process cancelling", "red-text");
                                $('#modal_failcancel').modal('open');
                            });
                    }
                    // Unload project if user cancels the mandatory adjustments
                    setTimeout(function() {
                        $.get( "ajax/overview/invalidateSession" );
                    }, 500);
                });

                $('button[data-id="exportGTC"]').click(function() {
                    $.get("ajax/overview/checkGtc" )
                        .done(function( data ) {
                            if(data === true) {
                                $('#modal_exportgtc').modal('open');
                            }
                            else {
                                $('#modal_noGtcFound').modal('open');
                            }
                        })
                            .fail(function ( data ) {
                                console.log("failed check")
                                $('#modal_noGtcFound').modal('open');
                            });

                    setTimeout(function() {
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
                //checking if dpi input value si valid and disabling button if not
                $('#dpi').on('input', function(e) {
                    if(!this.checkValidity()){
                        $('#convertToPdf').addClass("disabled");
                        $('#convertToPdfWithBlanks').addClass("disabled");
                    }else{
                        $('#convertToPdf').removeClass("disabled");
                        $('#convertToPdfWithBlanks').removeClass("disabled");
                    }
                });
                $('#pageNo').on('input', function(e) {
                    if(!this.checkValidity()){
                        $('#exportPages').addClass("disabled");
                    }else{
                        $('#exportPages').removeClass("disabled");
                    }
                });
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
                <button data-id="cancelProjectAdjustment" class="btn waves-effect waves-light disabled" type="submit" name="action">
                    Cancel Project Adjustment
                    <i class="material-icons right">cancel</i>
                </button>
                <button data-id="exportGTC" class="btn waves-effect waves-light secondary-btn" type="submit" name="action">
                    Export GT
                    <i class="material-icons right">arrow_downward</i>
                </button>
                <div class="preloader-wrapper small active" style="float:right; margin-right: 8px;">
                    <div id="gtc_spinner" class="spinner-layer spinner-blue-only hiddendiv">
                        <div class="circle-clipper left">
                            <div class="circle"></div>
                        </div><div class="gap-patch">
                        <div class="circle"></div>
                    </div><div class="circle-clipper right">
                        <div class="circle"></div>
                    </div>
                    </div>
                </div>

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
                <button data-id="cancelProjectAdjustment" class="btn waves-effect waves-light disabled" type="submit" name="action">
                    Cancel Project Adjustment
                    <i class="material-icons right">cancel</i>
                </button>
            </div>
        </div>
        <div id="modal_legacy_convert" class="modal">
            <div class="modal-content">
                <h4 class="red-text">Convert legacy project</h4>
                <p>The project you are about to load, includes files of an old version of OCR4all.</p>
                <form action="#">
                    <p>
                        <input type="checkbox" id="backupCheckbox" class="filled-in" />
                        <label for="backupCheckbox">Backup legacy project files.</label>
                    </p>
                </form>
                <div id="legacy-convert-preloader" class="progress">
                    <div class="indeterminate"></div>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#!" id="convertLegacy" class="waves-effect waves-green btn">Convert</a>
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
        <div id="modal_legacy" class="modal">
            <div class="modal-content">
                <h4 class="red-text">Warning: Legacy files found</h4>
                <p>The project you are about to load, includes files of an old version of OCR4all.</p>
                <p>Opening and editing your project will not delete any legacy data from your project,
                    but existing legacy data from "Line Segmentations", "Recognitions" and "Ground Truth Productions" will not be accessible any more.</p>
                <p>If you need this legacy data, please consider installing our OCR4all "legacy" version.</p>
                <p>Selecting "Convert" will allow you to convert a "legacy" project to a "latest" project. The conversion process may have adverse impacts on existing line segmentation!</p>
                <p>Selecting "Continue" will continue the loading of the project, but may not able to use every previously existing data.</p>
            </div>
            <div class="modal-footer">
                <a href="#modal_legacy_convert" class="modal-close modal-trigger waves-effect waves-green btn">Convert</a>
                <a href="#!" id="continueLegacy" class="modal-action modal-close waves-effect waves-green btn-flat">Continue</a>
            </div>
        </div>
        <div id="modal_convertpdf" class="modal">
            <div class="modal-content">
                <h4 class="red-text">Convert PDF files</h4>
                <table class="compact">
                    <tbody>
                    <tr>
                        <td><p>
                            The required PNG format was not found in the input folder.<br />
                            A PDF document was found instead.
                            <br />
                            <br />
                            To be able to load your project successfully the PDF needs to be converted to separate PNG files.<br />
                            Please choose one of the offered possibilities to continue.<br /></p></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td><p>
                            The default value of the DPI used when rendering is set to 300: <br />
                            Please note that a higher DPI corresponds to a higher rendering time.
                            <br />
                            <br />
                            This may take a while.</p></td>
                        <td>
                            <br />
                            <div class="input-field">
                                <input id="dpi" type="number" value="300" min="50" max="2000" step="10"/>
                                <label for="dpi" data-type="int" data-error="Has to be integer">Rendering DPI:</label>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <a href="#!" id="cancelConvertPdf" class="modal-action modal-close waves-effect waves-green btn-flat ">Cancel</a>
                <a href="#!" id="convertToPdf" class="modal-action modal-close waves-effect waves-green btn-flat">Convert PDF and delete blank pages</a>
                <a href="#!" id="convertToPdfWithBlanks" class="modal-action modal-close waves-effect waves-green btn-flat">convert pdf and leave blank pages</a>
            </div>
        </div>
        <div id="modal_exportgtc" class="modal">
            <div class="modal-content">
                <div class="row">
                    <div class="col s11">
                        <h4 class="red-text">Export GT</h4>
                    </div>
                    <div class="col s1">
                        <a class="btn-floating waves-effect waves-light modal-trigger indigo darken-2" href="#export_gt_info"><i class="material-icons">info_outline</i></a>
                    </div>
                </div>
                <table class="compact">
                    <tbody>
                    <tr>
                        <td>
                            <p>
                                Would you like to export the Ground Truth Data to a .zip file ?<br />
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <p>Please choose which image type you would like to zip:</p>
                            <div>
                                <input type="checkbox" id="binaryCheckbox" name="Binary" checked="checked">
                                <label for="binaryCheckbox">Binary</label>
                            </div>

                            <div>
                                <input type="checkbox" id="grayCheckbox" name="Gray" checked="checked">
                                <label for="grayCheckbox">Gray</label>
                            </div>
                        </td>
                        <td>
                            <div class="col s12">
                                <div class="input-field inline">
                                    <input id="pageNo" type="text" pattern="^(\d+\s*([\-]?\s*\d+)?)((,|;){1}(\d+\s*([\-]?\s*\d+)?))*$">
                                    <label for="pageNo">Pages to Export</label>
                                    <span class="helper-text" style="color:gray;font-weight:lighter">e.g. 1-4,6,8-10</span>
                                </div>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <a href="#!" id="cancelExport" class="modal-action modal-close waves-effect waves-green btn-flat ">Cancel</a>
                <a href="#!" id="exportPages" class="modal-action modal-close waves-effect waves-green btn-flat">Export Pages</a>
                <a href="#!" id="exportAllPages" class="modal-action modal-close waves-effect waves-green btn-flat">Export All Pages</a>
            </div>
        </div>
        <div id="modal_noGtcFound" class="modal">
            <div class="modal-content red-text">
                <div class="row">
                    <div class="col s11">
                        <h4>No Ground Truth Data was found</h4>
                    </div>
                    <div class="col s1">
                        <a class="btn-floating waves-effect waves-light modal-trigger indigo darken-2" href="#export_gt_info"><i class="material-icons">info_outline</i></a>
                    </div>
                </div>
                <p>
                    Please be sure to load a project with Ground Truth Data before exporting.
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
            </div>
        </div>
        <div id="export_gt_info" class="modal">
            <div class="modal-content">
                <h4>Export GT Information</h4>
                <p>An important cornerstone of OCR4all are the <a href="https://github.com/Calamari-OCR/calamari_models" target="_blank">available mixed models</a> which can be used for pretraining or to perform OCR out of the box.
                    These models heavily rely on extensive and diverse Ground Truth which is cumbersome to produce.<br/>
                    If you consider contributing your GT to the training of mixed models please contact us at <a href="mailto:ocr4all@uni-wuerzburg.de">ocr4all@uni-wuerzburg.de</a>.
                    Thank you very much!</p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Close</a>
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
        <div id="modal_exportgtc_failed" class="modal">
            <div class="modal-content red-text">
                <h4>Error</h4>
                <p>
                    Due to an unexpected Error Ground Truth Data could not be exported.<br />
                    Please reload the Project.<br />
                </p>
            </div>
            <div class="modal-footer">
                <a href="#!" id='agree' class="modal-action modal-close waves-effect waves-green btn-flat">OK</a>
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
        <div id="modal_export_finished" class="modal">
            <div class="modal-content">
                <h4 class="blue-text">Ground Truth Export finished</h4>
                <p>
                    The zip file with the ground truth data and current timestamp can now be found in your current project folder:
                </p>
                <p id="zip_path"></p>
            </div>
            <div class="modal-footer">
                <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Ok</a>
            </div>
        </div>
    </t:body>
</t:html>
