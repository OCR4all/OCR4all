/**
 * Includes jQuery functionality for project specific image lists on the right side of the web page
 *
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page
 * 2. Add "initializeImageList(imageType)" function call to the target page (when document ready)
 * 3. Include ul-Tag with id="imageList" and appropriate style (see body.tag)
 *    In this project it can be included by passing imageList="true" to the body.tag include
 */

function getSelectedPages() {
    var selectedPages = [];
    $.each($('input[type="checkbox"]').not('#selectAll'), function() {
        if( $(this).is(':checked') )
            selectedPages.push($(this).attr('data-pageid'));
    });
    return selectedPages;
}

var skip = 0;
var limit = 10;
var imageListAjaxInProgress = false;
var imageListInterval = null;
// Continuously fetch all page images and add them to the list
function fetchListImages(imageType) {
    if( imageListAjaxInProgress )
        return;
    imageListAjaxInProgress = true;

    $.get( "ajax/image/list", { "imageType" : imageType, "skip" : skip, "limit" : limit, "width" : 150 } )
    .done(function( data ) {
        $.each(data, function(pageId, pageImage) {
            var li = '<li>';
            li    += 'Page ' + pageId;
            li    += '<a href="#!" data-pageid="' + pageId + '"><img width="100" src="data:image/jpeg;base64, ' + pageImage + '" /></a>';
            li    += '<input type="checkbox" class="filled-in" id="page' + pageId + '" data-pageid="' + pageId + '" />';
            li    += '<label for="page' + pageId + '"></label>';
            li    += '</li>';
            $('#imageList').append(li);
        });
        // Update counter and enable next load
        skip += limit;
        imageListAjaxInProgress = false;
        // Stop loading of remaining images (all images fetched)
        if( data === '' || jQuery.isEmptyObject(data) ) {
            clearInterval(imageListInterval);
            imageListAjaxInProgress = false;
        }
    })
    .fail(function( data ) {
        var li = '<li class="red-text">';
        li    += 'Error: Could not load page images';
        li    += '</li>';
        $('#imageList').append(li);
        // Stop loading of remaining images 
        clearInterval(imageListInterval);
        imageListAjaxInProgress = false;
    })
}

// Call this function after document is ready
function initializeImageList(imageType) {
    imageListInterval = setInterval(function() { fetchListImages(imageType) }, 100);
}

// Workaround to adjust height of image list
// Cannot be done with CSS properly (due to dynamic content changes with AJAX)
function resizeImageList() {
    var mainHeight = $('main').height();
    $('#imageList').height('auto');
    if( mainHeight > $('#imageList').height() ) {
        $('#imageList').height(mainHeight);
    }
}

$(document).ready(function() {
    // Resize image list when collapsible is opened/closed
    // Most pages are built with collapsible in this project, so this is the most used case
    // Other change/click/... events need to be set in the target JSP
    $('.collapsible-header').on('click', function() {
        setTimeout(resizeImageList, 500);
    });

    // Checkbox handling (select all functioanlity)
    $('#selectAll').on('change', function() {
        var checked = false;
        if( $(this).is(':checked') )
            checked = true;

        $('input[type="checkbox"]').prop('checked', checked);
    });
    $('#imageList').on('change', $('input[type="checkbox"]').not('#selectAll'), function() {
        var checked = true;
        $.each($('input[type="checkbox"]').not('#selectAll'), function(index, el) {
            if( !$(el).is(':checked') )
                checked = false;
        });

        $('#selectAll').prop('checked', checked);
    });
});
