<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function() {
                // Workaround to adjust height of image list
                // Cannot be done with CSS properly (due to dynamic content changes with AJAX)
                function resizeImageList() {
                    var mainHeight = $('main').height();
                    $('#imageList').height('auto');
                    if( mainHeight > $('#imageList').height() ) {
                        $('#imageList').height(mainHeight);
                    }
                }
                $('#originalImg').on('load', function () {
                    setTimeout(resizeImageList, 500);
                });
                $('.collapsible-header').on('click', function() {
                    setTimeout(resizeImageList, 500);
                });

                // Checkbox handling (select all functioanlity)
                $('#selectAll').on('change', function() {
                    var checked = false;
                    if( $(this).is(':checked') )
                        checked = true;

                    $('input[type="checkbox"]').prop('checked', checked);
                });
                $('#imageList').on('change', $('input[type="checkbox"]').not('#selectAll'), function() {
                    var checked = true;
                    $.each($('input[type="checkbox"]').not('#selectAll'), function(index, el) {
                        if( !$(el).is(':checked') )
                            checked = false;
                    });

                    $('#selectAll').prop('checked', checked);
                });

                // Fetch all page images and add them to the list
                $.get( "ajax/image/list", { "imageType" : "Binary", "width" : 150 } )
                .done(function( data ) {
                    $.each(data, function(pageId, pageImage) {
                        var li = '<li>';
                        li    += 'Page ' + pageId;
                        li    += '<a href="#!" data-pageid="' + pageId + '"><img width="100" src="data:image/jpeg;base64, ' + pageImage + '" /></a>';
                        li    += '<input type="checkbox" class="filled-in" id="page' + pageId + '" />';
                        li    += '<label for="page' + pageId + '"></label>';
                        li    += '</li>';
                        $('#imageList').append(li);
                    });
                })
                .fail(function( data ) {
                    var li = '<li class="red-text">';
                    li    += 'Error: Could not load page images';
                    li    += '</li>';
                    $('#imageList').append(li);
                })

                // Function to load page image on demand via AJAX
                function loadPageImage(divEl, pageId, imageType) {
                    $.get( "ajax/image/page", { "pageId" : pageId, "imageId" : imageType, "width" : 395 } )
                    .done(function( data ) {
                        if( data === '' ) {
                            $(divEl).find('img').first().attr('src', '');
                            $(divEl).find('i[data-info="broken-image"]').first().remove();
                            $(divEl).find('img').first().after('<i class="material-icons" data-info="broken-image">broken_image</i>');
                        }
                        else {
                            $(divEl).find('i[data-info="broken-image"]').first().remove();
                            $(divEl).find('img').first().attr('src', 'data:image/jpeg;base64, ' + data);
                        }
                    })
                    .fail(function( data ) {
                        $(divEl).find('img').first().attr('src', '');
                        $(divEl).find('i[data-info="broken-image"]').first().remove();
                        $(divEl).find('img').first().after('<i class="material-icons" data-info="broken-image">broken_image</i>');
                    })
                }

                // Handle onclick event for pages in page image list
                $('#imageList').on('click', 'a', function(e) {
                    $('.collapsible-header').last().addClass('active');
                    $('.collapsible').collapsible({accordion: false});

                    // Load full size images
                    loadPageImage($('#originalImg').parent('div'),   $(this).attr('data-pageid'), "Binary");
                    loadPageImage($('#despeckledImg').parent('div'), $(this).attr('data-pageid'), "Despeckled");
                });

                // Initialize select form
                $('select').material_select();
            });
        </script>
    </t:head>
    <t:body heading="Despeckling">
        <ul id="imageList" class="side-nav image-list">
            <li class="heading">Pages</li>
            <li>
                Select all:
                <input type="checkbox" class="filled-in" id="selectAll" />
                <label for="selectAll"></label>
            </li>
        </ul>

        <div class="container includes-list">
            <div class="section">
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
                                                <input id="maxContourRemovalSize" value="100" type="number" />
                                                <label for="maxContourRemovalSize" data-type="float" data-error="Has to be float (. sep)">Float value</label>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Illustration type</p></td>
                                        <td>
                                            <div class="input-field">
                                                <select id="imageType" name="imageType">
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
                                    <h5>Original</h5>
                                    <img id="originalImg" width="100%" src="" />
                                </div>
                                <div class="col s2"></div>
                                <div class="col s4">
                                    <h5>Despeckled</h5>
                                    <img id="despeckledImg" width="100%" src="" />
                                </div>
                                <div class="col s1"></div>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </t:body>
</t:html>
