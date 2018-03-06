/**
 * Includes project specific jQuery helper functionalities
 * 
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page (see head.tag)
 * 2. Needs to be included before other custom jQuery files
 */

// Open or close the given collapsible entry
function handleCollapsibleEntry(entryId, action) {
    var activeCheck = false;
    if( action === 'close' )
        activeCheck = true;

    var collapsibleEl = $('.collapsible').eq(0).children('li').children('.collapsible-header').eq(entryId);
    if( $(collapsibleEl).hasClass('active') === activeCheck ) {
        $('.collapsible').eq(0).collapsible('open', entryId);
    }

    // Trigger change event in case the current page uses an imageList (resizing functionality)
    $(collapsibleEl).change();
}

// Open the given collapsible entries and close the remaining ones
function openCollapsibleEntriesExclusively(entryIds) {
    $.each($('.collapsible').eq(0).children('li').children('.collapsible-header'), function(index, collapsibleEntry) {
        var action = 'open';
        if( $.inArray(index, entryIds) < 0 )
            action = 'close';

        handleCollapsibleEntry(index, action);
    });
}
