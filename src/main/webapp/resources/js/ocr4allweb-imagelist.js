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
    $.each($('#imageList input[type="checkbox"]').not('#selectFilter'), function() {
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
    // Add event listener on li element instead of directly to checkbox in order to allow shift click in firefox.
    // A click on checkboxes in firefox is not triggered, if a modifier is active (shift, alt, ctrl)
    const doesProvideMod = !(navigator.userAgent.indexOf("Firefox") >= 0);
    $('#imageList>li').on('click', (doesProvideMod ? 'input[type="checkbox"]' : 'label'), function(event) {
        const checkbox = doesProvideMod ? this : $(this).siblings('input[type="checkbox"]')[0];
        if( !lastChecked ) {
            lastChecked = checkbox;
            return;
        }

        if( event.shiftKey ) {
            var checkBoxes = $('#imageList input[type="checkbox"]').not('#selectFilter');
            var start = $(checkBoxes).index($(checkbox));
            var end =   $(checkBoxes).index($(lastChecked));

            $(checkBoxes).slice(Math.min(start, end), Math.max(start, end) + 1).prop('checked', $(lastChecked).is(':checked'));
        }

        $(checkbox).change();
        lastChecked = checkbox;
    });
}

// Removes all available pages from the image list
// The select all checkbox and the title of the image list remains
function emptyImageList() {
    $('li[data-id="pagelistImage"]').each(function() {
        this.remove();
    });
}

// Builds the image list based on given page Ids
function buildImageList(pageIds) {
    $.each(pageIds, function(id, pageId) {
        var li = '<li data-id="pagelistImage">';
        li    += '<input type="checkbox" class="filled-in" id="page' + pageId + '" data-pageid="' + pageId + '" />';
        li    += '<label for="page' + pageId + '"></label>';
        li    += '<a href="#!" data-pageid="' + pageId + '" class="page-text">Page ' + pageId + '</a><br />';
        li    += '<a href="#!" data-pageid="' + pageId + '" ><img width="100" src="" style="display: none;" /></a>';
        li    += '</li>';
        $('#imageList').append(li);
    });

    // Direct checkbox events need to be initialized after their creation
    initializeMultiCheckboxSelection();

    // In case of reload, reset selectAll checkbox first
    if( $('#selectFilter').prop('indeterminate') === true || $('#selectFilter').prop('checked') === true )
        $('#selectFilter').click();

    // Select all pages (e.g. as default on page load)
    $('#selectFilter').click();

    // In case of reload and shown images, restore this setting after image list switch is finished
    var imageListTrigger = $('.image-list-trigger');
    if( $(imageListTrigger).hasClass('active') ) {
        $(imageListTrigger).removeClass('active');
        $(imageListTrigger).click();
    }
}

// Fetch all available pages and add them to the list (without the actual images)
function fetchPageList(staticPageIds) {
    // Load pages via AJAX
    if( staticPageIds === false ) {
        $.get( "ajax/generic/pagelist", { "imageType" : globalImageType } )
        .done(function( data ) {
            buildImageList(data);
        })
        .fail(function( data ) {
            var li = '<li class="red-text">';
            li    += 'Error: Could not load page list';
            li    += '</li>';
            $('#imageList').append(li);
        });
    }
    // Use static page ids
    else {
        buildImageList(staticPageIds);
    }
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
        .always(function( data ) {
            // Remove broken image icon first
            var nextEl = $(imgEl).next();
            if( nextEl.length !== 0 ) {
                $(nextEl).remove();
            }
        })
        .done(function( data ) {
            $(imgEl).attr('src', "data:image/jpeg;base64, " + data);
        })
        .fail(function( data ) {
            $(imgEl).after('<i class="material-icons image-list-broken-image" data-info="broken-image">broken_image</i>');
        });
    });
}

//Workaround to adjust height of image list
//Cannot be done with CSS properly (due to dynamic content changes with AJAX)
function resizeImageList() {
    var mainHeight = $('main').height();
    $('#imageList').height('auto');
    if( mainHeight > $('#imageList').outerHeight(true) ) {
        var newPosition = mainHeight - ($('#imageList').outerHeight(true) - $('#imageList').height());
        $('#imageList').height(newPosition);
    }

    // Move footer below image list
    var footerPos = $('#imageList').outerHeight(true) + $('#imageList').position().top;
    $('footer').attr('style', 'display:block;position:absolute;width:100%;top:' + footerPos + 'px;');
}

// Call this function to reload an existing image list
function reloadImageList(imageType, enableLinkHighlighting, staticPageIds) {
    emptyImageList();
    initializeImageList(imageType, enableLinkHighlighting, staticPageIds);
    resizeImageList();
}

// Call this function after document is ready
function initializeImageList(imageType, enableLinkHighlighting, staticPageIds) {
    globalImageType = imageType;

    enableLinkHighlighting = enableLinkHighlighting || false;
    if( enableLinkHighlighting === true )
        activateLinkHightlighting();

    staticPageIds = staticPageIds || false;
    fetchPageList(staticPageIds);
}

$(document).ready(function() {
    // Resize image list when collapsible is opened/closed
    // Most pages are built with collapsible in this project, so this is the most used case
    // Other change/click/... events need to be set in the target JSP
    $('.collapsible-header').on('click change', function() {
        $('footer').hide(); // Prevent short overlapping of elements
        setTimeout(resizeImageList, 500);
    });

    // Checkbox handling (select all functionality)
    $('#selectFilter').on('change', function() {
        let selectMode = $("#select-filter-option").val();

        let checked = false;

        if( $(this).is(':checked') )
            checked = true;

        switch (selectMode) {
            case "all":
                $('#imageList input[type="checkbox"]').not("#selectFilter").prop('checked', checked);
                break;
            case "even":
                $('#imageList input[type="checkbox"]:even').not("#selectFilter").prop('checked', checked);
                break;
            case "odd":
                $('#imageList input[type="checkbox"]:odd').not("#selectFilter").prop('checked', checked);
                break;
        }
    });

    $('#imageList').on('change', $('input[type="checkbox"]').not('#selectFilter'), function() {
        let selectMode = $("#select-filter-option").val();

        $('#selectFilter').prop('indeterminate', false);
        $('#selectFilter').prop('checked', false);

        function compareItemToChecked(items, checked){
            if(items.length === checked.length)
                $('#selectFilter').prop('checked', true);
            else if(checked.length > 0)
                $('#selectFilter').prop('indeterminate', true);
        }

        switch (selectMode) {
            case "all":
                let all_items = $('#imageList input[type="checkbox"]').not('#selectFilter');
                let all_checked = $('#imageList input[type="checkbox"]:checked').not('#selectFilter');

                compareItemToChecked(all_items, all_checked);
                break;
            case "even":
                let even_items = $('#imageList input[type="checkbox"]:even').not('#selectFilter');
                let even_checked = $('#imageList input[type="checkbox"]:even:checked').not('#selectFilter');

                compareItemToChecked(even_items, even_checked);
                break;
            case "odd":
                let odd_items = $('#imageList input[type="checkbox"]:odd').not('#selectFilter');
                let odd_checked = $('#imageList input[type="checkbox"]:odd:checked').not('#selectFilter');

                compareItemToChecked(odd_items, odd_checked);
                break;
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

    // Initial resizing if page changes shorty after loading
    $('footer').hide(); // Prevent short overlapping of elements
    setTimeout(function() { resizeImageList(); }, 500);
});
