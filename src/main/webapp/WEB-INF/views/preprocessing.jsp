<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:html>
    <t:head>
        <script type="text/javascript">
            $(document).ready(function() {
                var inProgress = false;
                $("button").click(function() {
                    if( inProgress === true ) {
                        //TODO: Error handling
                    }
                    else {
                        inProgress = true;

                        $.get( "ajax/preprocessing/execute", { } )
                        .done(function( data ) {
                            //TODO: Output handling
                            inProgress = false;
                        })
                        .fail(function( data ) {
                            //TODO: Error handling
                            inProgress = false;
                        })
                    }
                });
            });
        </script>
    </t:head>
    <t:body heading="Preprocessing">
        <div class="container">
            <div class="section">
                <button class="btn waves-effect waves-light" type="submit" name="action">
                    Start
                    <i class="material-icons right">send</i>
                </button>
            </div>
        </div>
    </t:body>
</t:html>
