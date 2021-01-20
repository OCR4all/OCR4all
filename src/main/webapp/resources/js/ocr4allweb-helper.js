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
    if (action === 'close')
        activeCheck = true;

    var collapsibleEntry = $(collapsibleEl).children('li').children('.collapsible-header').eq(entryId);
    if ($(collapsibleEntry).hasClass('active') === activeCheck) {
        $(collapsibleEl).collapsible('open', entryId);

        // Trigger change event in case the current page uses an imageList (resizing functionality)
        $(collapsibleEntry).change();
    }
}

// Open the given collapsible entries and close the remaining ones
function openCollapsibleEntriesExclusively(entryIds, collapsibleEl) {
    collapsibleEl = collapsibleEl || $('.collapsible').eq(1);
    $.each($(collapsibleEl).children('li').children('.collapsible-header'), function (index, collapsibleEntry) {
        var action = 'open';
        if ($.inArray(index, entryIds) < 0)
            action = 'close';

        handleCollapsibleEntry(index, action, collapsibleEl);
    });
}

function setCookie(name, value, days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "") + expires + "; path=/";
}

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function eraseCookie(name) {
    document.cookie = name + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}
