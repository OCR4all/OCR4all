var $helpMenu;
var $overlay;

/*
    Opens the helpmenu by sliding it out and adding a background overlay
 */
function openHelpMenu(event) {
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
    event.stopPropagation();
    $helpMenu.css('transform', 'translateX(0)');
    fadeInBackgroundOverlay();
}

/*
    Closes the helpmenu by sliding it back in and removing the background overlay
 */
function closeHelpMenu() {
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
    $helpMenu.css('transform', 'translateX(100%)');
    fadeOutBackgroundOverlay();
}

/*
    Adds all relevant listeners to the current open slide
 */
function addListeners(slide, step) {
    return Promise.all([
        ensureHideSlideWhileDropdownIsOpenListener(slide, false, step),
        ensureEndEventListener(slide, false)
    ])
}

/*
    Removes all added listeners from the open slide whenever it becomes inactive (next, back, cancel)
 */
function removeListeners(slide) {
    return Promise.all([
        ensureHideSlideWhileDropdownIsOpenListener(slide, true),
        ensureEndEventListener(slide, true)
    ])
}

/*
    Returns $attachTo together with the underlying select element, in case $attachTo is the wrapping input for a select.
    If $attachTo is not a wrapper element for a material select, an array with only $attachTo is returned
 */
function withCompoundSelect($attachTo) {
    const compoundSelectId = $attachTo.attr('data-activates')?.replace('select-options-', '');
    const $compoundSelect = $(`select[data-select-id="${compoundSelectId}"]`);

    return [$attachTo, $compoundSelect].filter(el => el.length);
}

/*
    In case there are multiple tabs on the page (e.g. Settings, Status and Overview), ensures that the tab that contains
    attachTo is opened before a slide gets active
 */
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

/*
    Helper function for addListener/removeListener. Ensures that the listener for manually showing the next slide on
    a certain event is added (cleanup = false) or removed (cleanup = true)
 */
function ensureEndEventListener(slide, cleanup) {
    return new Promise((resolve) => {
        if (!slide) return resolve();
        const {attachTo, endIfEvent} = slide;
        const $attachTo = $(attachTo);
        if (!$attachTo.length || !endIfEvent) return resolve();
        const thisSpecificEndIfEvent = endIfEvent + ".shepherd"; // namespace the event for jQuery on and off listeners
        const $elements = withCompoundSelect($attachTo);
        $elements.forEach(($element, idx) => {
            if (cleanup) {
                $element.off(thisSpecificEndIfEvent);
            } else {
                $element.off(thisSpecificEndIfEvent).on(thisSpecificEndIfEvent, () => {
                    return Shepherd.activeTour.next();
                });
            }
            const isLast = idx === $elements.length - 1;
            if (isLast) return resolve();
        });
    })
}

/*
    Helper function for addListener/removeListener. Ensures that the current slide gets hidden whenever a dropdown is open,
    and gets shown whenever the dropdown is closed again. Adds the listener on cleanup = false and removes it on cleanup = true
 */
function ensureHideSlideWhileDropdownIsOpenListener(slide, cleanup, step) {
    const thisSpecificOpenEvent = 'open.shepherd'; // namespace the event for jQuery on and off listeners
    const thisSpecificCloseEvent = 'close.shepherd'; // namespace the event for jQuery on and off listeners
    return new Promise((resolve) => {
        if (!slide) return resolve();
        const {attachTo} = slide;
        const $attachTo = $(attachTo);
        if (!$attachTo.length) return resolve();
        if (cleanup) {
            $attachTo.off(thisSpecificOpenEvent).off(thisSpecificCloseEvent);
        } else {
            $attachTo.off(thisSpecificOpenEvent).on(thisSpecificOpenEvent, () => {
                step.hide();
            });
            $attachTo.off(thisSpecificCloseEvent).on(thisSpecificCloseEvent, () => {
                step.show();
            });
        }
        return resolve();
    })
}

/*
    Creates the shepherd tour object
 */
function createTour(defaultOptionOverwrites = {}) {
    const defaultOptions = {
        useModalOverlay: !isChrome(),
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

/*
    Creates the shepherd tour object and sets up relevant listeners (on tour cancel, on tour complete)
 */
function initializeTour(tourId, normalSlides) {
    function showHelpMenuHintTour() {
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
                mind, you can <b> always find all
                available tours for the respective page in this menu! </b>
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
                        // we have to cancel it, otherwise Shepherd.on('complete') will fire unintentionally
                        return this.cancel();
                    },
                    classes: 'button-green',
                    text: 'Got it!'
                }
            ],
            classes: '--with-icon --single-button',
        });

        helpMenuTour.start();
    }
    function getActiveSlide(slides) {
        const activeSlideIdx = Shepherd.activeTour.steps.indexOf(Shepherd.activeTour.currentStep);
        return slides[activeSlideIdx];
    }

    const tour = createTour();

    firedShowIfEvents[tourId] = new Array(normalSlides.length).fill(false);

    Shepherd.on('cancel', async function () {
        await removeListeners(getActiveSlide(normalSlides));
        // show help menu hint on first cancel
        if (!getCookie("hasSeenHelpMenuHint")) {
            setCookie("hasSeenHelpMenuHint", true);
            showHelpMenuHintTour();
        }
    });

    Shepherd.on('complete', async function () {
        await removeListeners(getActiveSlide(normalSlides));
        appendToCookie("completedTours", "---", tourId);
    })

    return tour;
}

/*
    Adds an overview slide to the tour that calls this function
 */
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

/*
    Adds normal slides to the tour that calls this function
 */
Shepherd.Tour.prototype.addNormalSlides = function (tourId, topic, additionalHelpUrl, normalSlides, hasOverviewSlide) {
    normalSlides.forEach((slide, idx) => {
        const {attachTo, showIfEvent, endIfEvent, endIfHint, textContent, mediaPlacement, mediaUrl, mediaType} = slide;

        const isFirst = idx === 0;
        const isLast = idx === normalSlides.length - 1;

        const attachToExists = $(attachTo).length;
        const showSlideIfClassName = attachToExists && showIfEvent;
        const nextSlideIfEvent = attachToExists && endIfEvent;

        const slideHasMedia = mediaPlacement && mediaUrl && mediaType;

        const step = this.addStep({
            title: topic,
            text: slideHasMedia ? `
                <span class="text">${textContent}</span>
                <${mediaType.toLowerCase()} class="media" src="${mediaUrl}" controls></${mediaType.toLowerCase()}>
            ` : textContent,
            attachTo: {
                element: attachTo,
                on: 'auto'
            },
            canClickTarget: true,
            buttons: [
                {
                    async action() {
                        await removeListeners(slide);
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
                    async action() {
                        if (nextSlideIfEvent) {
                            return alert(endIfHint);
                        }
                        await removeListeners(slide);
                        return this.next();
                    },
                    classes: `button-green ${nextSlideIfEvent ? ' button-greyed-out' : ''}`,
                    text: isLast ? "Close tour" : "Next",
                }
            ],
            when: {
                show: function () {
                    const activeTour = Shepherd.activeTour;
                    const currentStep = activeTour.currentStep;
                    const $currentStep = $(currentStep.el);

                    const currentProgress = hasOverviewSlide ?
                        Math.round(activeTour.steps.indexOf(activeTour.currentStep) / (activeTour.steps.length - 1) * 100) :
                        Math.round((activeTour.steps.indexOf(activeTour.currentStep) + 1) / activeTour.steps.length * 100);

                    addProgressBar($currentStep, currentProgress);
                }
            },
            beforeShowPromise: function () {
                return new Promise(async (resolve) => {

                    await ensureRightTabIsOpen(attachTo);
                    await addListeners(slide, step);

                    if (showSlideIfClassName) {
                        const showIfEventHasAlreadyBeenFired = firedShowIfEvents[tourId][idx];
                        $(attachTo).on(showIfEvent, () => {
                            firedShowIfEvents[tourId][idx] = true;
                            return resolve();
                        })
                        if (showIfEventHasAlreadyBeenFired) {
                            return resolve();
                        }
                    } else return resolve();
                })
            },
            classes: getClasses(slideHasMedia, mediaPlacement, isLast)
        });
    })
}

/*
    Adds a progress bar to the current slide
 */
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

/*
    Gets the classes to add to the normal slide
 */
function getClasses(slideHasMedia, mediaPlacement, isLast) {
    let classes = [];
    if (slideHasMedia) {
        classes.push("--with-media");
        classes.push(`--${mediaPlacement.toLowerCase()}`)
    }
    if (isLast) {
        classes.push("--is-last");
    }
    return classes.join(" ");
}

/*
    Adds a hotspot to the desired position and returns the jQuery object for it
 */
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

/*
    Removes a hotspot and sets a cookie that indicates, that the hotspot should be hidden from now on
 */
function removeHotspot($hotspot, tourId) {
    $hotspot.fadeOut();
    appendToCookie("hiddenHotspots", "---", tourId);
}

$(document).ready(() => {
    $helpMenu = $('.help-menu');

    const openHelpMenuBtn = $('.help-icon');
    openHelpMenuBtn.on('click', openHelpMenu);

    const closeHelpMenuBtn = $('.close-help-icon');
    closeHelpMenuBtn.on('click', closeHelpMenu);
})
