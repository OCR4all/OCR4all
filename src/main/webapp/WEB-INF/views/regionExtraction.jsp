<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head imageList="true">
        <title>Region Extractor</title>

        <script type="text/javascript">
            $(document).ready(function() {
                initializeImageList("OCR");
                // Todo Content of Region Extractor page
            });
        </script>
    </t:head>
    <t:body heading="Region Extraction" imageList="true" processModals="true">
            <div class="container includes-list">
            <div class="section">
                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute
                    <i class="material-icons right">chevron_right</i>
                </button>
                <button data-id="cancel" class="btn waves-effect waves-light">
                    Cancel
                    <i class="material-icons right">cancel</i>
                </button>

                <ul class="collapsible" data-collapsible="expandable">
                    <li>
                        <div class="collapsible-header"><i class="material-icons">settings</i>Settings (General)</div>
                        <div class="collapsible-body">
                            <table class="compact">
                                <tbody>
                                    <tr>
                                        <td><p> Use average background</p></td>
                                        <td>
                                             <p>
                                                <input type="checkbox" class="filled-in" id="--nocheck" />
                                                <label for="--nocheck"></label>
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><p>Use spacing</p></td>
                                        <td>
                                            <div class="input-field">
                                                <input id="--parallel" type="number" />
                                                <label for="--parallel" data-type="int" data-error="Has to be integer">Default: 1 </label>
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </li>
                    <li>
                        <div class="collapsible-header"><i class="material-icons">info_outline</i>Status</div>
                        <div class="collapsible-body">
                            <div class="status"><p>Status: <span>No Preprocessing process running</span></p></div>
                            <div class="progress">
                                <div class="determinate"></div>
                            </div>
                        </div>
                    </li>
                </ul>

                <button data-id="execute" class="btn waves-effect waves-light">
                    Execute
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
