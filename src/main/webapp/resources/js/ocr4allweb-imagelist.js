/**
 * Includes jQuery functionality for project specific image lists on the right side of the web page
 *
 * Things that need to be done to use this:
 * 1. Include this file and viewport.js in the head of the target page (see head.tag)
 *    In this project it can be included by passing imageList="true" to the head.tag include
 * 2. Add "initializeImageList(imageType)" function call to the target page (when document ready)
 * 3. Include ul-Tag with id="imageList" (see body.tag)
 *    In this project it can be included by passing imageList="true" to the body.tag include
 */

var globalImageType = "";

function getSelectedPages() {
    var selectedPages = [];
    $.each($('input[type="checkbox"]').not('#selectAll'), function() {
        if( $(this).is(':checked') )
            selectedPages.push($(this).attr('data-pageid'));
    });
    return selectedPages;
}

function activateLinkHightlighting() {
    $('#imageList').on('click', 'a', function() {
        $('.image-list li>a.active').removeClass('active');
        var markEl = this;
        // If preview images are shown, only mark them
        if( $(this).parent('li').find('a>img').is(":visible") )
            markEl = $(this).parent('li').find('a>img').parent('a');
        $(markEl).addClass('active');
    });
}

var lastChecked = null;
// Feature to select multiple checkboxes while holding down the shift key
// See: https://stackoverflow.com/questions/659508/how-can-i-shift-select-multiple-checkboxes-like-gmail/659571#659571
function initializeMultiCheckboxSelection() {
    $('#imageList input[type="checkbox"]').on('click', function(event) {
        if( !lastChecked ) {
            lastChecked = this;
            return;
        }

        if( event.shiftKey ) {
            var checkBoxes = $('#imageList input[type="checkbox"]').not('#selectAll');
            var start = $(checkBoxes).index($(this));
            var end =   $(checkBoxes).index($(lastChecked));

            $(checkBoxes).slice(Math.min(start, end), Math.max(start, end) + 1).prop('checked', $(lastChecked).is(':checked'));
        }

        $(this).change();
        lastChecked = this;
    })
}

// Fetch all available pages and add them to the list (without the actual images)
function fetchPageList() {
    $.get( "ajax/generic/pagelist", { "imageType" : globalImageType } )
    .done(function( data ) {
        $.each(data, function(id, pageId) {
            var li = '<li>';
            li    += '<input type="checkbox" class="filled-in" id="page' + pageId + '" data-pageid="' + pageId + '" />';
            li    += '<label for="page' + pageId + '"></label>';
            li    += '<a href="#!" data-pageid="' + pageId + '" class="page-text">Page ' + pageId + '</a><br />';
            li    += '<a href="#!" data-pageid="' + pageId + '" ><img width="100" src="" style="display: none;" /></a>';
            li    += '</li>';
            $('#imageList').append(li);

            // Direct checkbox events need to be initialized after their creation
            initializeMultiCheckboxSelection();
        });
    })
    .fail(function( data ) {
        var li = '<li class="red-text">';
        li    += 'Error: Could not load page list';
        li    += '</li>';
        $('#imageList').append(li);
    })
}

// Load visible page images to the list
function loadVisiblePages() {
    // Fetch page images that are currently shown on the site
    var firstVisibleImageLinks = $('#imageList li>a').not('.page-text').withinviewport();
    $.each(firstVisibleImageLinks, function(index, aEl) {
        var imgEl = $(aEl).find('img');
        // Check if the image is already loaded
        if( $(imgEl).attr('src') !== '' )
            return;

        // Load page images via Ajax
        $.get( "ajax/image/page", { "imageId" : globalImageType, "pageId" : $(aEl).attr('data-pageid'), "width" : 150 } )
        .done(function( data ) {
            $(imgEl).attr('src', "data:image/jpeg;base64, " + data);
        });
    });
}

// Call this function after document is ready
function initializeImageList(imageType, enableLinkHighlighting) {
    globalImageType = imageType;

    enableLinkHighlighting = enableLinkHighlighting || false;
    if( enableLinkHighlighting === true )
        activateLinkHightlighting();

    fetchPageList();
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

        $('#imageList input[type="checkbox"]').prop('checked', checked);
    });
    $('#imageList').on('change', $('input[type="checkbox"]').not('#selectAll'), function() {
        var checkedCount  = 0;
        var checkBoxCount = 0;
        $.each($('#imageList input[type="checkbox"]').not('#selectAll'), function(index, el) {
            if( $(el).is(':checked') )
                checkedCount++;
            checkBoxCount++;
        });

        $('#selectAll').prop('indeterminate', false);
        $('#selectAll').prop('checked', false);
        if( checkedCount === checkBoxCount ) {
            $('#selectAll').prop('checked', true);
        }
        else if( checkedCount > 0 ) {
        	console.log("here");
        	$('#selectAll').prop('indeterminate', true);
        }
    });

    // Show/hide preview images 
    $('.image-list-trigger').on('click', function() {
        if( $(this).hasClass('active') ) {
            $(this).removeClass('active');
            $('#imageList img').hide();
        }
        else {
            $(this).addClass('active');
            $('#imageList img').show();
            loadVisiblePages();
        }
    });

    $('#imageList').on('scrollStop', function() {
    	console.log("here");
        if( $('.image-list-trigger').hasClass('active') )
            loadVisiblePages();
    });

    var scrollTimeout = null;
    // If the user stops scrolling for 2 seconds, load currently visible pages 
    $('#imageList').scroll(function() {
        clearTimeout(scrollTimeout);
        if( $('.image-list-trigger').hasClass('active') ) {
            scrollTimeout = setTimeout(function() {
                loadVisiblePages();
            }, 2000);
        }
    });
});
