<%@ tag description="Help menu markup" pageEncoding="UTF-8" %>
<%@ attribute name="heading" required="true" %>

<script type="text/javascript">

    var mascotPath = "${pageContext.servletContext.contextPath}/resources/img/mascot.svg";

    $(document).ready(() => {
        $('button[data-id="redirectToExternalHelp"]').click(function () {
            window.open('https://github.com/OCR4all/getting_started', '_blank');
        });
        $('button[data-id="writeEmail"]').click(function () {
            window.open('mailto:maximilian.wehner@uni-wuerzburg.de');
        });

        const relativeUrlWithoutTrailingSlash = window.location.pathname.replace(/\/+$/, '');
        $.get('ajax/toursForCurrentUrl', {url: relativeUrlWithoutTrailingSlash}).done(function (tours) {

            firedEvents = new Array(tours.length);

            tours.forEach(function (tour) {
                const {id, topic, hasCompletedOnce, hotspot, overviewSlide, normalSlides, additionalHelpUrl} = tour;

                const buttonLabel = (hasCompletedOnce ? "Restart" : "Start") + " Tour";
                const buttonIcon = hasCompletedOnce ? "replay" : "play_arrow";
                const tourExtraClass = buttonLabel === "Restart Tour" ? " --is-done" : ""

                $('.help-item__tours').append(
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

                let $hotspot;

                if (!hotspot.isHidden) {
                    $hotspot = addHotspot(hotspot, id);
                    $hotspot.on('click', () => {
                        // with overview slide
                        const tour = initializeTour(id, normalSlides);

                        tour.addOverviewSlide(id, topic, overviewSlide.textContent, $hotspot);

                        tour.addNormalSlides(id, topic, additionalHelpUrl, normalSlides, true);

                        tour.on('cancel', removeHotspot.bind(null, $hotspot, id));

                        tour.start();
                    });
                }

                $(`button[data-id="startTour\${id}"]`).click(function () {
                    // without overview slide
                    if ($hotspot) removeHotspot($hotspot, id);

                    const tour = initializeTour(id, normalSlides);

                    tour.addNormalSlides(id, topic, additionalHelpUrl, normalSlides, false);

                    closeHelpMenu();
                    tour.start();
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
