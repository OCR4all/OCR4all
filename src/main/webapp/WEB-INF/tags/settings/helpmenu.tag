<%@ tag description="Help menu markup" pageEncoding="UTF-8" %>
<%@ attribute name="heading" required="true" %>

<script type="text/javascript">

    const initTour = () => {
        const tour = new Shepherd.Tour({
            defaultStepOptions: {
                cancelIcon: {
                    enabled: true
                },
                classes: 'tour',
                scrollTo: {behavior: 'smooth', block: 'center'}
            }
        });

        Shepherd.on('cancel', showHelpMenuHint);

        return tour;
    }

    const createProgressBar = (progressInPercent) => {
        const adaptedProgress = progressInPercent === 0 ? progressInPercent + 1 : progressInPercent;
        return $(`
            <div class="tour-progress">
                <div class="tour-progress__filler" style="width: \${adaptedProgress}%" />
                \${progressInPercent}%
            </div>
        `);
    }

    const showHelpMenuHint = () => {
        if (!getCookie("hasSeenHelpMenuHint")) {
            setCookie("hasSeenHelpMenuHint", true, 365);
            alert("help menu hint!");
        }
    }

    const createOverviewSlide = (tourId, tour, topic, textContent, hotspot) => {

        const content = `
            \${textContent}
            <div class="mascot">
                <img alt="OCR4all mascot" src="${pageContext.servletContext.contextPath}/resources/img/mascot.svg">
            </div>
            <div class="learnings-overview" >
                What you will learn: <br/>
                <span class="learnings-overview__topic">\${topic}</span>
            </div>`

        tour.addStep({
            title: '',
            text: content,
            attachTo: {
                element: hotspot,
                on: 'auto'
            },
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
                        hideHotspot(hotspot, tourId);
                        return this.next();
                    },
                    classes: 'button-green',
                    text: 'Start Tour'
                }
            ],
            when: {
                show() {
                    const $this = $(Shepherd.activeTour.currentStep.el);
                    const footer = $this.find('.shepherd-footer');
                    createProgressBar(0).insertBefore(footer);
                }
            },
            id: 'overview',
        });
    }

    $(document).ready(() => {
        $('button[data-id="redirectToExternalHelp"]').click(function () {
            window.open('https://github.com/OCR4all/getting_started', '_blank');
        });
        $('button[data-id="writeEmail"]').click(function () {
            window.open('mailto:maximilian.wehner@uni-wuerzburg.de');
        });

        const relativeUrlWithoutTrailingSlash = window.location.pathname.replace(/\/+$/, '');
        $.get('ajax/toursForCurrentUrl', {url: relativeUrlWithoutTrailingSlash}).done(function (tours) {
            console.log(tours);
            const tourContainer = $('.help-item__tours');

            tours.forEach(function (tour) {
                const {id, topic, hasCompletedOnce, hotspot, overviewSlide} = tour;

                const buttonLabel = (hasCompletedOnce ? "Restart" : "Start") + " Tour";
                const buttonIcon = hasCompletedOnce ? "replay" : "play_arrow";
                const tourExtraClass = buttonLabel === "Restart Tour" ? " --is-done" : ""

                tourContainer.append(
                    $(`<div class="help-tour\${tourExtraClass}">
                        <div class="help-tour__label">
                            Topic:
                        </div>
                        <div class="help-tour__topic">
                            \${topic}
                            <span class="help-tour__checkmark material-icons">check_circle_outline</span>
                        </div>
                        <div class="help-tour__action">
                            <button data-id="startTour\${id}" class="btn waves-effect waves-light">
                                \${buttonLabel}
                                <i class="material-icons right">\${buttonIcon}</i>
                            </button>
                        </div>
                    </div>
                    `)
                );

                if (!hotspot.isHidden) {
                    const {selectorToAttach, xSelectorOffsetInPx, attachFallback} = hotspot;

                    const hotspotHtml = $(
                    `<button data-id="offerTour\${id}" class="hotspot">
                        <div class="hotspot__inner">
                            Tour
                        </div>
                    </button>
                    `)

                    if (selectorToAttach) {
                        const attachTo = $(selectorToAttach);
                        attachTo.css('position', 'relative').append(hotspotHtml);
                        if (xSelectorOffsetInPx) {
                            $(".hotspot").css('left', xSelectorOffsetInPx + "px");
                        }
                    } else {
                        // ...
                    }

                    $(`button[data-id="offerTour\${id}"]`).click(function () {
                        // with overview slide
                        const tour = initTour();

                        createOverviewSlide(id, tour, topic, overviewSlide.textContent, this);

                        tour.on('cancel', () => {
                            hideHotspot(this, id);
                        });

                        tour.start();

                    });
                }

                $(`button[data-id="startTour\${id}"]`).click(function () {
                    // without overview slide
                });
            })
        })
    })
</script>

<div class="help-menu">
    <div class="help-menu__container">
        <div class="help-menu__header">
            <span>Help</span>
            <a href="javascript:void(0)" class="close-help-icon">&times;</a>
        </div>
        <div class="help-menu__item">
            <div class="help-item__header">
                <h5>Get help in the online manual</h5>
                <p>The online manual covers all topics in-depth.</p>
            </div>
            <div class="help-item__action">
                <button data-id="redirectToExternalHelp" class="btn waves-effect waves-light" type="button">
                    Take me there!
                    <i class="material-icons right">launch</i>
                </button>
            </div>
        </div>
        <div class="help-menu__item">
            <div class="help-item__header">
                <h5>Contact our support team</h5>
                <p>Have a special problem? Don’t know further? We’re happy to hear from you!</p>
            </div>
            <div class="help-item__action">
                <button data-id="writeEmail" class="btn waves-effect waves-light" type="button">
                    Write an email
                    <i class="material-icons right">email</i>
                </button>
            </div>
        </div>
        <div class="help-menu__item">
            <div class="help-item__header --with-icon">
                <h5>Take a guided tour</h5>
                <p>Available tours for this specific page (<span class="darkblue">${heading}</span>):</p>
                <img alt="OCR4all mascot" src="${pageContext.servletContext.contextPath}/resources/img/mascot.svg">
            </div>
            <div class="help-item__tours"></div>
        </div>
    </div>
</div>
