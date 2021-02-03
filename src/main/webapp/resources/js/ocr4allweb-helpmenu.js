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

function ensureRightTabIsOpen(attachTo) {
    return new Promise((resolve) => {
        const $attachTo = $(attachTo);
        if (!$attachTo.length) return resolve();
        const parentAccordion = $attachTo.closest('.collapsible');
        if (!parentAccordion.length) return resolve();
        parentAccordion.children().each((idx, tab) => {
            const $tab = $(tab)
            if ($tab.find($attachTo).length) {
                if ($tab.hasClass('active')) return resolve();
                /*parentAccordion.collapsible({
                    onOpen: function() {
                        console.log("opened?")
                        return resolve();
                    }
                });*/
                parentAccordion.collapsible('open', idx);
                setTimeout(resolve, 400)
            }
        })
    })
}

function createTour(defaultOptionOverwrites = {}) {
    const defaultOptions = {
        useModalOverlay: true,
        defaultStepOptions: {
            cancelIcon: {
                enabled: true
            },
            classes: 'slide',
            modalOverlayOpeningPadding: 10,
            scrollTo: {behavior: 'smooth', block: 'center'},
            popperOptions: {
                modifiers: [
                    {
                        name: 'offset',
                        options: {
                            offset: [0, 15]
                        }
                    },
                    {
                        name: 'preventOverflow',
                        options: {
                            altAxis: true,
                            padding: 10
                        },
                    }
                ]
            }
        }
    };

    return new Shepherd.Tour(deepMerge(defaultOptions, defaultOptionOverwrites));
}

function checkClassConstantly(className, $attachTo, step) {
    const interval = setInterval(() => {
        if (!Shepherd.activeTour || !$(Shepherd.activeTour.currentStep.el).is(step.el)) {
            clearInterval(interval);
            return;
        }

        const hasClass = $attachTo.hasClass(className);

        if (hasClass) {
            step.hide();
        } else if (!hasClass && !step.isOpen()) {
            step.show();
        }
    }, 500)
}

Shepherd.Tour.prototype.addOverviewSlide = function (tourId, topic, textContent, $hotspot) {

    const content = `
            ${textContent}
            <div class="mascot">
                <img alt="OCR4all mascot" src=${mascotPath}>
            </div>
            <div class="learnings-overview" >
                What you will learn: <br/>
                <span class="learnings-overview__topic">${topic}</span>
            </div>`

    this.addStep({
        title: '',
        text: content,
        attachTo: {
            element: $hotspot[0],
            on: 'auto'
        },
        canClickTarget: false,
        buttons: [
            {
                action() {
                    return this.cancel();
                },
                classes: 'button-red',
                text: 'Dismiss'
            },
            {
                action() {
                    removeHotspot($hotspot, tourId);
                    return this.next();
                },
                classes: 'button-green',
                text: 'Start Tour'
            }
        ],
        when: {
            show() {
                addProgressBar($(Shepherd.activeTour.currentStep.el), 0);
            }
        },
        classes: '--with-icon',
    });
}

Shepherd.Tour.prototype.addNormalSlides = function (tourId, topic, additionalHelpUrl, normalSlides, hasOverviewSlide) {
    normalSlides.forEach((normalSlide, idx) => {
        const {attachTo, textContent, hideIfClass, endIfEvent, endIfHint} = normalSlide;

        const isFirst = idx === 0;
        const isLast = idx === normalSlides.length - 1;
        const $attachTo = $(attachTo);

        const nextSlideIfEvent = $attachTo.length && endIfEvent;
        const hideSlideIfClassName = $attachTo.length && hideIfClass;

        const step = this.addStep({
            title: topic,
            text: textContent,
            attachTo: {
                element: attachTo,
                on: 'auto'
            },
            canClickTarget: true,
            buttons: [
                {
                    action() {
                        return this.back();
                    },
                    classes: `button-red ${isFirst ? ' button-hidden' : ''}`,
                    text: 'Back'
                },
                {
                    action() {
                        window.open(additionalHelpUrl, '_blank');
                    },
                    classes: 'button-blue',
                    text: "Additional Help",
                },
                {
                    action() {
                        if (nextSlideIfEvent) {
                            alert(endIfHint);
                        } else return this.next();
                    },
                    classes: `button-green ${nextSlideIfEvent ? ' button-greyed-out' : ''}`,
                    text: isLast ? "Close tour" : "Next",
                }
            ],
            when: {
                show() {
                    const activeTour = Shepherd.activeTour;
                    const currentStep = activeTour.currentStep;
                    const $currentStep = $(currentStep.el);

                    const currentProgress = hasOverviewSlide ?
                        Math.round(activeTour.steps.indexOf(activeTour.currentStep) / (activeTour.steps.length - 1) * 100) :
                        Math.round((activeTour.steps.indexOf(activeTour.currentStep) + 1) / activeTour.steps.length * 100);

                    addProgressBar($currentStep, currentProgress);

                    if (hideSlideIfClassName) checkClassConstantly(hideIfClass, $attachTo, step);
                    if (nextSlideIfEvent) {
                        $attachTo.on(endIfEvent, () => this.next())
                    }
                }
            },
            beforeShowPromise: ensureRightTabIsOpen.bind(null, attachTo),
            classes: '',
        });

        $(document).on("click", (e) => {
            console.log(e.target);
        })
    })
}

function initializeTour() {
    const tour = createTour();

    Shepherd.on('cancel', function () {
        // show help menu hint on first cancel
        if (!getCookie("hasSeenHelpMenuHint")) {
            setCookie("hasSeenHelpMenuHint", true, 365);

            // start help menu hint tour
            const helpMenuTour = createTour({
                defaultStepOptions: {
                    cancelIcon: {
                        enabled: false
                    }
                }
            });

            const content = `
                That's no problem!<br/>
                In case you change your
                mind, you can <span class="darkblue"> always find all
                available tours for the current page in this menu! </span>
            <div class="mascot">
                <img alt="OCR4all mascot" src=${mascotPath}>
            </div>
    `

            helpMenuTour.addStep({
                title: 'Important hint',
                text: content,
                attachTo: {
                    element: $('.help-icon .material-icons')[0],
                    on: 'auto'
                },
                buttons: [
                    {
                        action() {
                            return this.next();
                        },
                        classes: 'button-green',
                        text: 'Got it!'
                    }
                ],
                classes: '--with-icon --single-button',
            });

            helpMenuTour.start();
        }
    });

    return tour;
}

function addProgressBar(current, progressInPercent) {
    const adaptedProgress = progressInPercent === 0 ? progressInPercent + 1 : progressInPercent;
    const progressBar = $(`
            <div class="tour-progress">
                <div class="tour-progress__filler" style="width: ${adaptedProgress}%" />
                ${progressInPercent}%
            </div>
        `);
    const footer = current.find('.shepherd-footer');
    progressBar.insertBefore(footer);
}

function addHotspot(hotspot, tourId) {
    const {selectorToAttach, leftValue} = hotspot;

    const hotspotHtml = $(`
        <button data-id="offerTour${tourId}" class="hotspot">
            <div class="hotspot__inner">
                Tour
            </div>
        </button>
        `)

    let attachTo;
    if (selectorToAttach && (attachTo = $(selectorToAttach)).length) {
        if (leftValue) {
            hotspotHtml.css('left', leftValue);
        }
        attachTo.css('position', 'relative').append(hotspotHtml);
    } else {
        // put the hotspot in the left lane, vertically centered
        hotspotHtml.css('left', '30px');
        $('body').append(hotspotHtml);
    }

    return $(`button[data-id="offerTour${tourId}`);
}

function removeHotspot($hotspot, tourId) {
    $hotspot.fadeOut();
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
