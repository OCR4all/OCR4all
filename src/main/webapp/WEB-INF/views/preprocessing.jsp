<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function() {
                var inProgress = false;

                // Fetch all modified parameters and return them appropriately
                function getParams() {
                    var params = { 'cmdArgs': [] };
                    $.each($('input[type="checkbox"]'), function() {
                        if( $(this).prop('checked') === true )
                            params['cmdArgs'].push($(this).attr('id'));
                    });
                    $.each($('input[type="text"]'), function() {
                        if( $(this).val() !== "" )
                            params['cmdArgs'].push($(this).attr('id'), $(this).val());
                    });
                    return params;
                }

                $("button").click(function() {
                    if( inProgress === true ) {
                        //TODO: Error handling
                    }
                    else {
                        var xhr = new XMLHttpRequest();

                        xhr.seenBytes = 0;
                        xhr.onreadystatechange = function() {
                            //  loading                 finished
                            if( xhr.readyState === 3 || xhr.readyState === 4 ) {
                                var newData = xhr.response.substr(xhr.seenBytes);
                                //TODO: Update HTML content
                                console.log(newData);
                                xhr.seenBytes = xhr.responseText.length;
                            }
                        };
                        xhr.addEventListener("error", function(e) {
                            //TODO: Error handling
                        });

                        xhr.open('GET', 'ajax/preprocessing/execute?' + jQuery.param(getParams()));
                        xhr.send();
                    }
                });
            });
        </script>
    </t:head>
    <t:body heading="Preprocessing">
        <div class="container">
            <div class="section">
                <div class="row">
                    <div class="col s9">
                        <table class="compact">
                            <tbody>
                                <tr>
                                    <td><p>Disable error checking on inputs</p></td>
                                    <td>
                                        <p>
                                            <input type="checkbox" class="filled-in" id="--nocheck" />
                                            <label for="--nocheck"></label>
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Threshold, determines lightness</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--threshold" type="text" />
                                            <label for="--threshold">Default: 0.5</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Zoom for page background estimation</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--zoom" type="text" />
                                            <label for="--zoom">Default: 0.5</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Scale for estimating a mask over the text region</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--escale" type="text" />
                                            <label for="--escale">Default: 1.0</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Scale for estimating a mask over the text region</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--escale" type="text" />
                                            <label for="--escale">Default: 1.0</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Ignore this much of the border for threshold estimation</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--bignore" type="text" />
                                            <label for="--bignore">Default: 0.1</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Percentage for filters</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--perc" type="text" />
                                            <label for="--perc">Default: 80</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Range for filters</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--range" type="text" />
                                            <label for="--range">Default: 20</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Skew angle estimation parameters (degrees)</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--maxskew" type="text" />
                                            <label for="--maxskew">Default: 2</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Force grayscale processing even if image seems binary</p></td>
                                    <td>
                                        <p>
                                            <input type="checkbox" class="filled-in" id="--gray" />
                                            <label for="--gray"></label>
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Percentile for black estimation</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--lo" type="text" />
                                            <label for="--lo">Default: 5</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Percentile for white estimation</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--hi" type="text" />
                                            <label for="--hi">Default: 90</label>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><p>Steps for skew angle estimation (per degree)</p></td>
                                    <td>
                                        <div class="input-field">
                                            <input id="--skewsteps" type="text" />
                                            <label for="--skewsteps">Default: 8</label>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <button class="btn waves-effect waves-light" type="submit" name="action">
                    Start
                    <i class="material-icons right">send</i>
                </button>
            </div>
        </div>
    </t:body>
</t:html>
