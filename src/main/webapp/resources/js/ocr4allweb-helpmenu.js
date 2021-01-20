var $helpMenu;
var $overlay;

function fadeInBackgroundOverlay() {
    $overlay = $('<div id="sidenav-overlay" style="opacity: 0"></div>');
    $('body').append($overlay);
    $overlay.velocity({opacity: 1}, {
        duration: 300,
        queue: false,
        easing: 'easeOutQuad'
    });
    $overlay.on('click', closeHelpMenu);
}

function fadeOutBackgroundOverlay() {
    $overlay = $('#sidenav-overlay');
    $overlay.velocity({opacity: 0}, {
        duration: 200,
        queue: false,
        easing: 'easeOutQuad',
        complete: function () {
            $(this).remove();
        }
    });
}

function openHelpMenu(event) {
    event.stopPropagation();
    $helpMenu.css('transform', 'translateX(0)');
    fadeInBackgroundOverlay();
}

function closeHelpMenu() {
    $helpMenu.css('transform', 'translateX(100%)');
    fadeOutBackgroundOverlay();
}

function hideHotspot(hotspot, tourId) {
    $(hotspot).fadeOut();
    const oldHotspotCookie = getCookie("hiddenHotspots");

    if (!oldHotspotCookie) {
        setCookie("hiddenHotspots", tourId, 365)
    } else {
        const oldCookieAlreadyContainsCurrentId = oldHotspotCookie.split("---").map(Number).includes(tourId);
        if (!oldCookieAlreadyContainsCurrentId) setCookie("hiddenHotspots", oldHotspotCookie + "---" + tourId, 365)
    }
}

$(document).ready(() => {
    $helpMenu = $('.help-menu');

    const openHelpMenuBtn = $('.help-icon');
    openHelpMenuBtn.on('click', openHelpMenu);

    const closeHelpMenuBtn = $('.close-help-icon');
    closeHelpMenuBtn.on('click', closeHelpMenu);
})
