<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="true">
        <title>OCR4All - Despeckling</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Load image list
                initializeImageList("Binary", true);
                // Additional resizing event to adjust image list height
                $('#originalImg').on('load', function () {
                    setTimeout(resizeImageList, 500);
                });

                var despeckledAjaxReq = null;
                // Function to load page image on demand via AJAX
                function loadPageImage(divEl, pageId, imageType) {
                    var ajaxUrl = (imageType == 'Despeckled') ? "ajax/image/preview/despeckled" : "ajax/image/page";
                    var ajaxParams = { "pageId" : pageId, "imageId" : imageType, "width" : 960 };
                    if( imageType == 'Despeckled' ) {
                        $.extend(ajaxParams, { "maxContourRemovalSize" : $('input[name="maxContourRemovalSize"]').val() });
                        $.extend(ajaxParams, { "illustrationType" : $('select[name="illustrationType"]').val() });

                        // Abort last despeckling AJAX request
                        // This prevents issues with delayed image loading
                        if( despeckledAjaxReq !== null )
                            despeckledAjaxReq.abort();
                        despeckledAjaxReq = null;
                    }

                    // Handle preloader to indicate that the despeckled image loading is still in progress
                    $(divEl).find('img').first().attr('src', '');
                    $(divEl).find('i[data-info="broken-image"]').first().remove();
                    var preloaderId = (imageType == 'Despeckled') ? 'despeckledPreloader' : 'binaryPreloader'; 
                    $('#' + preloaderId).removeClass('hide');

                    var imageAjaxReq = $.get( ajaxUrl, ajaxParams )
                    .done(function( data ) {
                        if( data === '' ) {
                            $(divEl).find('img').first().after('<i class="material-icons" data-info="broken-image">broken_image</i>');
                        }
                        else {
                            $(divEl).find('img').first().attr('src', 'data:image/jpeg;base64, ' + data);
                        }

                        // Cleanup preloader (loading process finished)
                        var preloaderId = (imageType == 'Despeckled') ? 'despeckledPreloader' : 'binaryPreloader'; 
                        $('#' + preloaderId).addClass('hide');
                    })
                    .fail(function( data ) {
                        $(divEl).find('img').first().after('<i class="material-icons" data-info="broken-image">broken_image</i>');

                        // Cleanup preloader (loading process finished)
                        var preloaderId = (imageType == 'Despeckled') ? 'despeckledPreloader' : 'binaryPreloader'; 
                        $('#' + preloaderId).addClass('hide');
                    })

                    if( imageType == 'Despeckled' )
                        despeckledAjaxReq = imageAjaxReq;
                }

                // Handle onclick event for pages in page image list
                $('#imageList').on('click', 'a', function() {
                    // Show image preview
                    if( !$('.collapsible-header:eq(1)').hasClass('active') ) {
                        $('.collapsible-header:eq(1)').addClass('active');
                        $('.collapsible').collapsible({accordion: false});
                    }

                    // Load full size images
                    loadPageImage($('#originalImg').parent('div'),   $(this).attr('data-pageid'), "Binary");
                    loadPageImage($('#despeckledImg').parent('div'), $(this).attr('data-pageid'), "Despeckled");
                });

                // Update despeckled image preview when settings are changed
                $('input, select').on('change', function() {
                    if( $('.image-list li>a.active').length === 1 )
                        loadPageImage($('#despeckledImg').parent('div'), $('.image-list li>a.active').attr('data-pageid'), "Despeckled");
                });

                var inProgress = false;
                var progressInterval = null;
                // Function to handle despeckling progress
                function updateStatus(initial) {
                    initial = initial || false;

                    // Update despeckling progress in status collapsible
                    $.get( "ajax/despeckling/progress" )
                    .done(function( data ) {
                        progress = data;
                        if( Math.floor(data) != data || !$.isNumeric(data) ) {
                            if( initial !== false ) $('.collapsible').collapsible('open', 0);
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("ERROR: Invalid AJAX response").attr("class", "red-text");
                            return;
                        }

                        if( data < 0 ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            // No ongoing preprocessing
                            $('.determinate').attr("style", "width: 0%");
                            return;
                        }

                        if( inProgress === false ) {
                            inProgress = true;
                            $('.status span').html("Ongoing").attr("class", "orange-text");
                        }

                        if( initial !== false ) $('.collapsible').collapsible('open', 2);
                        // Update process bar
                        $('.determinate').attr("style", "width: " + data + "%");

                        // Terminate interval loop
                        if( data >= 100 ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("Completed").attr("class", "green-text");
                        }
                    })
                    .fail(function( data ) {
                        inProgress = false;
                        clearInterval(progressInterval);
                        $('.status span').html("ERROR: Failed to load status").attr("class", "red-text");
                    })
                }
                // Initial call to set progress variable
                updateStatus(true);
                progressInterval = setInterval(updateStatus, 1000);


                // Process handling (execute despeckling for all pages with current settings)
                $('button[data-id="execute"]').click(function() {
                    if( inProgress === true ) {
                        $('#modal_inprogress').modal('open');
                    }
                    else {
                        var selectedPages = getSelectedPages();
                        if( selectedPages.length === 0 ) {
                            $('#modal_errorhandling').modal('open');
                            return;
                        }

                        // Show status (and hide image preview)
                        if( $('.collapsible').find('.collapsible-header').eq(1).hasClass('active') )
                            $('.collapsible').find('.collapsible-header').eq(1).click();
                        if( !$('.collapsible').find('.collapsible-header').eq(2).hasClass('active') )
                            $('.collapsible').find('.collapsible-header').eq(2).click();
                        $(window).scrollTop(0);

                        var ajaxParams = { "maxContourRemovalSize" : $('input[name="maxContourRemovalSize"]').val(), "pageIds[]" : selectedPages };
                        $.post( "ajax/despeckling/execute", ajaxParams )
                        .fail(function( data ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("ERROR: Error during process execution").attr("class", "red-text");
                        })

                        // Update despeckling status. Interval will be terminated in
                        // updateStatus(), if process is finished.
                        progressInterval = setInterval(updateStatus, 1000);
                    }
                });
                $('button[data-id="cancel"]').click(function() {
                    if( inProgress !== true ) {
                        $('#modal_noprocess').modal('open');
                    }
                    else {
                        $.post( "ajax/despeckling/cancel" )
                        .done(function( data ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("No Preprocessing process running").attr("class", "");
                            $('#modal_successfulcancel').modal('open');
                        })
                        .fail(function( data ) {
                            inProgress = false;
                            clearInterval(progressInterval);
                            $('.status span').html("ERROR: Error during process cancelling").attr("class", "red-text");
                            $('#modal_failcancel').modal('open');
                        })
                    }
                });
            });
        </script>
    </t:head>
    <t:body heading="Despeckling" imageList="true">
        <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute for selected pages
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header active"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p>Maximal size for removing contours</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="maxContourRemovalSize" name="maxContourRemovalSize" value="100" type="number" />
                                                <label for="maxContourRemovalSize" data-type="float" data-error="Has to be float (. sep)">Float value</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Illustration type</p></td>
                                        <td>
                                            <div class="input-field">
                                                <select id="illustrationType" name="illustrationType">
                                                    <option value="standard">Show binary image</option>
                                                    <option value="marked">Show binary image including removed speckles</option>
                                                </select>
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">image</i>Image Preview</div>
                        <div class="collapsible-body">
                            <div class="row center">
                                <div class="col s1"></div>
                                <div class="col s4">
                                    <h5>Binary</h5>
                                    <img id="originalImg" class="materialboxed" width="100%" src="" />
                                    <div id="binaryPreloader" class="preloader-wrapper active small hide">
                                        <div class="spinner-layer spinner-blue-only">
                                            <div class="circle-clipper left">
                                                <div class="circle"></div>
                                            </div>
                                            <div class="gap-patch">
                                                <div class="circle"></div>
                                            </div>
                                            <div class="circle-clipper right">
                                                <div class="circle"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col s2"></div>
                                <div class="col s4">
                                    <h5>Despeckled</h5>
                                    <img id="despeckledImg" class="materialboxed" width="100%" src="" />
                                    <div id="despeckledPreloader" class="preloader-wrapper active small hide">
                                        <div class="spinner-layer spinner-blue-only">
                                            <div class="circle-clipper left">
                                                <div class="circle"></div>
                                            </div>
                                            <div class="gap-patch">
                                                <div class="circle"></div>
                                            </div>
                                            <div class="circle-clipper right">
                                                <div class="circle"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col s1"></div>
                            </div>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Despeckling process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                </ul>

                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute for selected pages
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <!-- In progress information -->
                <div id="modal_inprogress" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>
                            There already is a running Despeckling process.<br/>
                            Please wait until it is finished or cancel it.
                        </p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- Error handling -->
                <div id="modal_errorhandling" class="modal">
                    <div class="modal-content red-text">
                        <h4>Information</h4>
                        <p>
                            No pages were selected.<br/>
                            Please select pages to despeckle and try again.
                        </p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- No current process information -->
                <div id="modal_noprocess" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>There exists no ongoing Despeckling process.</p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- Successful cancel information -->
                <div id="modal_successfulcancel" class="modal">
                    <div class="modal-content">
                        <h4>Information</h4>
                        <p>The Despeckling process was cancelled successfully.</p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
                <!-- Failed cancel information -->
                <div id="modal_failcancel" class="modal">
                    <div class="modal-content red-text">
                        <h4>Error</h4>
                        <p>The Despeckling process could not be cancelled.</p>
                    </div>
                    <div class="modal-footer">
                        <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat">Agree</a>
                    </div>
                </div>
            </div>
        </div>
    </t:body>
</t:html>
