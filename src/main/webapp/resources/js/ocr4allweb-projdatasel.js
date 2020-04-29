/**
 * Includes project specific jQuery project data selection functionalities
 * 
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page (see head.tag)
 *    In this project it can be included by passing projDataSel="true" to the head.tag include
 * 2. Add "initializeProjectDataSelection(ajaxURL)" function call to the target page (when document ready)
 */

var globalAjaxURL = "";

// Display only the given selection type of project related data directories
function displaySelectionType(selectionType) {
    if( selectionType === "select" ) {
        $('input[data-projdataselrefid]').parents('tr').hide();
        $('select[data-projdatasel]').parents('tr').show();
    }
    else {
        $('select[data-projdatasel]').parents('tr').hide();
        $('input[data-projdataselrefid]').parents('tr').show();
        var projectDataSelectionType = $('select[data-projdataseltype]');
        if( projectDataSelectionType.length !== 0 ) {
            $('select[data-projdataseltype]').val('freeTextInput');
            $('select[data-projdataseltype]').material_select();
        }
    }
}

// Load project data via AJAX request and create option elements
// In case that no data was found or AJAX request failed, use text input as fallback
function loadProjDataOptions() {
    $.get( globalAjaxURL )
    .done(function( data ) {
        if( jQuery.isEmptyObject(data) ) {
            displaySelectionType('input');
            //TODO: Show modal with information
            return;
        }

        // (Re)build the select element with new values
        // Keep current selected value after reloading the data
        $.each($('select[data-projdatasel]'), function(idx, selEl) {
            $(selEl).empty();
            var currrentDirectoryPath = $('input[data-projdataselrefid="' + $(selEl).attr('id') + '"]').val();
            $.each(data, function(key, value) {
                var optionEl = $("<option></option>").attr("value", value).text(key);
                if( value === currrentDirectoryPath )
                    $(optionEl).attr("selected", "selected");
                $(selEl).append(optionEl);
            });
        });
        $('select[data-projdatasel]').change().material_select();

        displaySelectionType('select');
    })
    .fail(function(data) {
        displaySelectionType('input');
        //TODO: Show modal with information
    });
}

// Modify project data selection type (e.g. on overview page)
function handleProcessDataSelection() {
    var projectDataSelectionType = $('select[data-projdataseltype]').val();
    if( projectDataSelectionType === 'fixedStructure' ) {
        loadProjDataOptions();
    }
    else {
        displaySelectionType('input');
    }
}

function initializeProjectDataSelection(ajaxURL) {
    globalAjaxURL = ajaxURL;
    loadProjDataOptions();
}

$(document).ready(function() {
    // Always use projectDir input as entry point
    // Therefore update it when changing the project data via dropdown method
    $('select[data-projdatasel]').on('change', function() {
        $('input[data-projdataselrefid="' + $(this).attr('id') + '"]').val($(this).val());
    });

    // Trigger to manage if there exists an element to change the selection type dynamically
    $('select[data-projdataseltype]').on('change', function() {
        handleProcessDataSelection();
    });

    //Calls the models Version warning, if it wasnt already displayed
    if(window.localStorage.getItem('OCR4all_webVersionWarning') === null){
        $('#modal_version_warning').modal();
        setTimeout(function(){ $('#modal_version_warning').modal('open'); }, 1000);
        window.localStorage.setItem('OCR4all_webVersionWarning', true);
    }
});