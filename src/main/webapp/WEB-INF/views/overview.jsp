<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<t:html>
    <t:head>
        <!-- jQuery DataTables -->
        <link rel="stylesheet" type="text/css" href="resources/css/datatables.min.css">
        <script type="text/javascript" charset="utf8" src="resources/js/datatables.min.js"></script>

        <script type="text/javascript">
            $(document).ready(function() {
                $("button").click(function() {
                    if( $.trim($('#projectDir').val()).length === 0 ) {
                        $('#projectDir').addClass('invalid').focus();
                    }
                    else {
                        $.get( "ajax/overview/list", { "projectDir": $('#projectDir').val(), "imageType": $('#imageType').val() } )
                        .done(function( data ) {
                            // Allow reinitializing DataTable with new data
                            if( $.fn.DataTable.isDataTable("#overviewTable") ) {
                                $('#overviewTable').DataTable().clear().destroy();
                            }

                            $('#overviewTable').DataTable( {
                                data: data,
                                columns: [
                                    { title: "Page Identifier", data: "pageId" },
                                    { title: "Preprocessed", data: "preprocessed" },
                                    { title: "Segmented", data: "segmented" },
                                    { title: "Segments Extracted", data: "segmentsExtracted" },
                                    { title: "Lines Extracted", data: "linesExtracted" },
                                    { title: "Has GT", data: "hasGT" },
                                ],
                                createdRow: function( row, data, index ){
                                    $('td:first-child', row).html('<a href="pageOverview?pageId=' + data.pageId + '">' + data.pageId + '</a>');
                                    $.each( $('td:not(:first-child)', row), function( idx, td ) {
                                        if( $(td).html() === 'true' ) {
                                            $(td).html('<i class="material-icons green-text">check</i>');
                                        }
                                        else {
                                            $(td).html('<i class="material-icons red-text">clear</i>');
                                        }
                                    });
                                }
                            });
                        })
                        .fail(function( data ) {
                            $('#projectDir').addClass('invalid');
                        })
                    }
                });

                // Trigger overview table fetching on pageload
                if( $.trim($('#projectDir').val()).length !== 0 ) {
                    $("button").trigger( "click" );
                }

                // Initialize select form
                $('select').material_select();
            });
        </script>

        <title>OCR4all_Web - Overview</title>
    </t:head>
    <t:body heading="Project Overview">
        <div class="container">
            <div class="section">
                <div class="row">
                    <div class="input-field col s6">
                        <i class="material-icons prefix">folder</i>
                        <input id="projectDir" name="projectDir" type="text" class="validate" value="${projectDir}">
                        <label for="projectDir" data-error="Directory path is empty or could not be accessed on the filesystem">Project directory path</label>
                    </div>
                    <div class="input-field col s3">
                        <c:choose>
                            <c:when test='${imageType == "Gray"}'><c:set value='selected="selected"' var="graySel"></c:set></c:when>
                            <c:otherwise><c:set value='selected="selected"' var="binarySel"></c:set></c:otherwise>
                        </c:choose>
                        <i class="material-icons prefix">image</i>
                        <select id="imageType" name="imageType">
                            <option value="Binary" ${binarySel}>Binary</option>
                            <option value="Gray" ${graySel}>Gray</option>
                        </select>
                        <label>Project image type</label>
                    </div>
                    <div class="input-field col s3">
                        <button class="btn waves-effect waves-light right" type="submit" name="action">
                            Load
                            <i class="material-icons right">send</i>
                        </button>
                    </div>
                </div>
            </div>
            <div class="section">
                <table id="overviewTable" class="display centered" width="100%"></table>
            </div>
        </div>
    </t:body>
</t:html>
