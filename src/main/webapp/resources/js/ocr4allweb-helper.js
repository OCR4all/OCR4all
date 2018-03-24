/**
 * Includes project specific jQuery helper functionalities
 * 
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page (see head.tag)
 * 2. Needs to be included before other custom jQuery files
 */

// Open or close the given collapsible entry
function handleCollapsibleEntry(entryId, action, collapsibleEl) {
    collapsibleEl = collapsibleEl || $('.collapsible').eq(1);

    var activeCheck = false;
    if( action === 'close' )
        activeCheck = true;

    var collapsibleEntry = $(collapsibleEl).children('li').children('.collapsible-header').eq(entryId);
    if( $(collapsibleEntry).hasClass('active') === activeCheck ) {
        $(collapsibleEl).collapsible('open', entryId);
    }

    // Trigger change event in case the current page uses an imageList (resizing functionality)
    $(collapsibleEntry).change();
}

// Open the given collapsible entries and close the remaining ones
function openCollapsibleEntriesExclusively(entryIds, collapsibleEl) {
    collapsibleEl = collapsibleEl || $('.collapsible').eq(1);
    $.each($(collapsibleEl).children('li').children('.collapsible-header'), function(index, collapsibleEntry) {
        var action = 'open';
        if( $.inArray(index, entryIds) < 0 )
            action = 'close';

        handleCollapsibleEntry(index, action, collapsibleEl);
    });
}
