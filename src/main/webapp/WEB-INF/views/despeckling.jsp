<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function() {
                // Checkbox handling (select all functioanlity)
                $('#selectAll').on('change', function() {
                    var checked = false;
                    if( $(this).is(':checked') )
                        checked = true;

                    $('input[type="checkbox"]').prop('checked', checked);
                });
                $('input[type="checkbox"]').not('#selectAll').on('change', function() {
                    var checked = true;
                    $.each($('input[type="checkbox"]').not('#selectAll'), function(index, el) {
                        if( !$(el).is(':checked') )
                            checked = false;
                    });

                    $('#selectAll').prop('checked', checked);
                });

                // Fetch all page images and add them to the list
                $.get( "ajax/image/list", { "imageType" : "Binary" } )
                .done(function( data ) {
                    $.each(data, function(pageId, pageImage) {
                        var li = '<li>';
                        li    += 'Page ' + pageId;
                        li    += '<a href="#!"><img width="100" src="data:image/jpeg;base64, ' + pageImage + '" /></a>';
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
            </div>
        </div>
    </t:body>
</t:html>
