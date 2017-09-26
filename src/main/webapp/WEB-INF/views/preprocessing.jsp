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

                        xhr.open('GET', 'ajax/preprocessing/execute');
                        xhr.send();
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
