<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head imageList="true" processHandler="true">
        <title>OCR4all - Noise Removal</title>

        <script type="text/javascript">
            $(document).ready(function() {
                // Load image list
                initializeImageList("Binary", true);

                // Initialize process update and set options
                initializeProcessUpdate("despeckling", [ 0 ], [ 0, 2 ], false);

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
                    handleCollapsibleEntry(1, 'open');

                    // Load full size images
                    loadPageImage($('#originalImg').parent('div'),   $(this).attr('data-pageid'), "Binary");
                    loadPageImage($('#despeckledImg').parent('div'), $(this).attr('data-pageid'), "Despeckled");
                });

                // Update despeckled image preview when settings are changed
                $('input, select').on('change', function() {
                    if( $('.image-list li>a.active').length === 1 )
                        loadPageImage($('#despeckledImg').parent('div'), $('.image-list li>a.active').attr('data-pageid'), "Despeckled");
                });

                // Process handling (execute despeckling for all pages with current settings)
                $('button[data-id="execute"]').click(function() {
                    var selectedPages = getSelectedPages();
                    if( selectedPages.length === 0 ) {
                        $('#modal_errorhandling').modal('open');
                        return;
                    }
                    $.post( "ajax/despeckling/exists", { "pageIds[]" : selectedPages } )
                    .done(function( data ){
                        if(data === false){
                            var ajaxParams = { "maxContourRemovalSize" : $('input[name="maxContourRemovalSize"]').val(), "pageIds[]" : selectedPages };
                            // Execute despeckling process
                            executeProcess(ajaxParams);
                        }
                        else{
                            $('#modal_exists').modal('open');
                        }
                    })
                    .fail(function( data ) {
                        $('#modal_exists_failed').modal('open');
                    });
                });
                $('button[data-id="cancel"]').click(function() {
                    cancelProcess();
                });
                $('#agree').click(function() {
                    var selectedPages = getSelectedPages();
                    var ajaxParams = { "maxContourRemovalSize" : $('input[name="maxContourRemovalSize"]').val(), "pageIds[]" : selectedPages };
                    // Execute despeckling process
                    executeProcess(ajaxParams);
                });
            });
        </script>
    </t:head>
    <t:body heading="Noise Removal" imageList="true" processModals="true">
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
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings</div>
                        <div class="collapsible-body">
                            <s:despeckling></s:despeckling>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">image</i>Image Preview</div>
                        <div class="collapsible-body">
                            <p class="center">For a preview of Noise Removal output with the current settings, click on a page in the image list on the right</p>
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
                                    <h5>Noise Removal</h5>
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
                            <div class="status"><p>Status: <span>No Noise Removal process running</span></p></div>
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
            </div>
        </div>
    </t:body>
</t:html>
