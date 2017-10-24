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

    if( $('.collapsible').find('li').find('.collapsible-header').eq(entryId).hasClass('active') === activeCheck )
        $('.collapsible').collapsible('open', entryId);
}

// Open the given collapsible entries and close the remaining ones
function openCollapsibleEntriesExclusively(entryIds) {
    $.each($('.collapsible').find('li').find('.collapsible-header'), function(index, collapsibleEntry) {
        var action = 'open';
        if( $.inArray(index, entryIds) < 0 )
            action = 'close';

        handleCollapsibleEntry(index, action);
    });
}
