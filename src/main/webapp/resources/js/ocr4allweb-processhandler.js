/**
 * Includes jQuery functionality for project specific process handling
 * This covers the continuous retrieval of the process status and, if required, its console output/error
 *
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page (see head.tag)
 *    In this project it can be included by passing processHandler="true" to the head.tag include
 * 2. Add "initializeProcessUpdate(controller, collapsibleOpenStandard, collapsibleOpenOnAction, updateConsole)" function call to the target page (when document ready)
 * 3. Call "executeProcess(ajaxParams)" to start process execution
 * 4. Call "cancelProcess()" to cancel the current process execution
 */

var globalInProgress = false;
var globalProgressInterval = null;
var globalConsoleStream = { "out" : "", "err" : "" };

var globalController = "";
var globalCollapsibleOpenStandard = [];
var globalCollapsibleOpenOnAction = [];
var globalUpdateConsole = false;

// Function to stop a running process update
function stopProcessUpdate(message, messageClass) {
    message = message || false;
    messageClass = messageClass || false;

    globalInProgress = false;
    clearInterval(globalProgressInterval);
    if( message != false ) {
        $('.status span').html(message);
        if( messageClass != false )
            $('.status span').attr("class", messageClass);
    }
}

// Handle tab behavior
function selectActiveTab() {
    // Set console output to active tab
    // Workaround with setTimeout, due to a bug in materialize JS
    // Without using this the indicator width is not calculated correctly
    setTimeout(function( ) { $('ul.tabs').tabs('select_tab', $('.console div.active').attr('id')); }, 200);
}

// Remove error notification icon if user clicks on consoleErr tab
$(document).ready(function() {
    $('li.tab a').on('click', function() {
        var clickedTabIcon = $(this).find('i.material-icons');
        if( clickedTabIcon.length > 0 ) {
            clickedTabIcon.remove();
            $('ul.tabs').tabs('select_tab', $(this).parent('li').attr('data-refid'));
        }
    });
});

// Function to handle process console
function updateProcessConsole(streamType, tabId) {
    $.get( "ajax/" + globalController + "/console", { "streamType" : streamType } )
    .done(function( data ) {
        if( data === '' ) return;

        // Prevents duplicate output due to complete console output storage
        if( data === globalConsoleStream[streamType])
            return;

        globalConsoleStream[streamType] = data;
        $('#' + tabId + ' pre').html(data);

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
    });
}

// Function to handle status on processflow page exclusively
// This is a minimal version of updateProcessStatus() function
function updateProcessFlowStatus(process) {
    // Update progress in status collapsible
    $.get( "ajax/" + process + "/progress" )
    .done(function( data ) {
        progress = data;
        if( Math.floor(data) != data || !$.isNumeric(data) ) {
            $('li[data-id="' + process + '"] .status span').html("ERROR: Invalid AJAX response").attr("class", "red-text");
            return;
        }

        if( progress < 0 ) {
            $('li[data-id="' + process + '"] .determinate').attr("style", "width: 0%");
            return;
        }

        // Update process bar
        $('li[data-id="' + process + '"] .determinate').attr("style", "width: " + progress + "%");
        if( progress < 100 ) {
            $('li[data-id="' + process + '"] .status span').html("Ongoing").attr("class", "orange-text");
            // Open status collapsible first
            openCollapsibleEntriesExclusively([ 2 ]);
            // Open collapsible of process that is currently executed next
            var statusCollapsible = $('.collapsible[data-id="status"]');
            var processCollapsibleEntryId = $(statusCollapsible).find('li[data-id="' + process + '"]').index();
            openCollapsibleEntriesExclusively([ processCollapsibleEntryId ], statusCollapsible);
        }
        else {
            $('li[data-id="' + process + '"] .status span').html("Completed").attr("class", "green-text");
        }
    })
    .fail(function( data ) {
        $('li[data-id="' + process + '"] .status span').html("ERROR: Failed to load status").attr("class", "red-text");
    });
}

//Function to display cancel information of current process on processflow page exclusively
function displayProcessFlowCancel(success) {
    success = success || true;

    var currentProcessStatusSpan = $('ul[data-id="status"] li.active .status span');
    if( currentProcessStatusSpan !== undefined ) {
        if( success === true ) {
            $(currentProcessStatusSpan).html("Process cancelled");
        }
        else {
            $(currentProcessStatusSpan).html("ERROR: Error during process cancelling").attr("class", "red-text");
        }
    }
}

// Function to handle process progress
function updateProcessStatus(initial) {
    initial = initial || false;

    // Update progress in status collapsible
    $.get( "ajax/" + globalController + "/progress" )
    .done(function( data ) {
        progress = data;
        if( Math.floor(data) != data || !$.isNumeric(data) ) {
            if( initial === true )  openCollapsibleEntriesExclusively(globalCollapsibleOpenStandard);
            if( initial === false ) stopProcessUpdate("ERROR: Invalid AJAX response", "red-text");
            return;
        }

        if( data < 0 ) {
            if( initial === true ) {
                openCollapsibleEntriesExclusively(globalCollapsibleOpenStandard);

                // Update status continuously. Interval can be terminated with stopProcessUpdate() if needed.
                globalProgressInterval = setInterval(updateProcessStatus, 1000);
            }
            if( initial === false ) stopProcessUpdate();
            // No ongoing process
            $('.determinate').attr("style", "width: 0%");
            return;
        }

        var ongoingProgress = globalInProgress;
        if( globalInProgress === false ) {
            globalInProgress = true;
            $('.status span').html("Ongoing").attr("class", "orange-text");
        }

        if( ongoingProgress === false )
            openCollapsibleEntriesExclusively(globalCollapsibleOpenOnAction);

        // Update process bar
        $('.determinate').attr("style", "width: " + data + "%");

        if( globalUpdateConsole === true ) {
            // Update console output
            updateProcessConsole("out", "consoleOut");
            updateProcessConsole("err", "consoleErr");
            selectActiveTab();
        }

        // Terminate interval loop
        if( data >= 100 ) {
            stopProcessUpdate("Completed", "green-text");
        }

        // Update status continuously. Interval can be terminated with stopProcessUpdate() if needed.
        if( initial === true )
            globalProgressInterval = setInterval(updateProcessStatus, 1000);
    })
    .fail(function( data ) {
        stopProcessUpdate("ERROR: Failed to load status", "red-text");
    });
}

// Function to start process update
function startProcessUpdate() {
    // Update status without interval first to trigger collapsible behaviour first
    updateProcessStatus(true);
}

// Function to execute a process with given AJAX parameters
function executeProcess(ajaxParams) {
    if( globalInProgress === true ) {
        $('#modal_inprogress').modal('open');
    }
    else {
        // Needs to be outside of AJAX block
        // 'done' functionality is only triggered after process is finished
        startProcessUpdate();

        $.post( "ajax/" + globalController + "/execute", ajaxParams )
        .fail(function( jqXHR, data ) {
            switch(jqXHR.status) {
            case 530:
                $('#modal_inprogress').modal('open');
                stopProcessUpdate("ERROR: The process is still running", "red-text");
                break;
            case 535:
                $('#modal_sameprocesstype').modal('open');
                stopProcessUpdate("ERROR: A process with the same type is still running", "red-text");
                break;
            case 536:
                $('#modal_processconflict').modal('open');
                stopProcessUpdate("ERROR: The process execution conflicts with a running process", "red-text");
                break;
            case 537:
                $('#modal_processflowconflict').modal('open');
                stopProcessUpdate("ERROR: The process execution conflicts with the ProcessFlow", "red-text");
                break;
            default:
                $('#modal_executefailed').modal('open');
                stopProcessUpdate("ERROR: Error during process execution", "red-text");
                break;
            }

            // Execute collapsible change after 1 second to prevent issues with updateProcessStatus AJAX call
            setTimeout(function() { openCollapsibleEntriesExclusively(globalCollapsibleOpenOnAction); }, 1000);
        });
    }
}

// Cancel a process
function cancelProcess() {
    if( globalInProgress !== true ) {
        $('#modal_noprocess').modal('open');
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
}

// Call this function after document is ready
function initializeProcessUpdate(controller, collapsibleOpenStandard, collapsibleOpenOnAction, updateConsole) {
    // Set global variables to avoid function parameter passing
    globalController = controller;
    globalCollapsibleOpenStandard = collapsibleOpenStandard;
    globalCollapsibleOpenOnAction = collapsibleOpenOnAction;
    globalUpdateConsole = updateConsole;

    startProcessUpdate();
}
