<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<t:html>
    <t:head>
        <!-- jQuery DataTables -->
        <link rel="stylesheet" type="text/css" href="resources/css/datatables.min.css">
        <script type="text/javascript" charset="utf8" src="resources/js/datatables.min.js"></script>

        <title>OCR4all_Web - Overview</title>
    </t:head>
    <t:body>
        <div class="container">
            <h3 class="header">Overview</h3>
            <form action="#">
                <div class="row">
                    <div class="input-field col s8">
                        <i class="material-icons prefix">folder</i>
                        <input id="icon_prefix" type="text" class="validate">
                        <label for="icon_prefix">Please insert the path to the project directory on the filesystem</label>
                    </div>
                    <div class="input-field col s4">
                        <button class="btn waves-effect waves-light" type="submit" name="action">Go
                            <i class="material-icons right">send</i>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </t:body>
</t:html>
