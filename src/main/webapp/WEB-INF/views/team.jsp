<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="s" tagdir="/WEB-INF/tags/settings" %>
<t:html>
    <t:head></t:head>
<t:body heading="Team">
    <script type="text/javascript">
        $(document).ready(function()
        {
            var OCR4ALL_VERSION = "UNKNOWN";
            var LAREX_VERSION = "UNKNOWN"
            $.get("ajax/team/sysenv")
                .done(function getEnv(data) {
                    OCR4ALL_VERSION = data;
                    $("#version_number").html(OCR4ALL_VERSION);
                })

        })
    </script>
    <div class="personList center-align grey-text text-darken-1">
        <div class="row">
            <div class="col l12 s12">
                <h5><b>Contact</b></h5>
            </div>
            <div class="col l12 s12">
                <ul class="collection">
                    <li class="collection-item">
                        <h6>Christian Reul</h6>
                        <h6><i>Project lead</i></h6>
                        <a href="mailto:christian.reul@uni-wuerzburg.de">christian.reul@uni-wuerzburg.de</a>
                    </li>
                    <li class="collection-item">
                        <h6>Maximilian Wehner</h6>
                        <h6><i>User support and guides</i></h6>
                        <a href="mailto:maximilian.wehner@uni-wuerzburg.de">maximilian.wehner@uni-wuerzburg.de</a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="col l12 s12">
                <h5><b>Developer</b></h5>
            </div>
            <div class="col l12 s12">
                <ul class="collection">
                    <li class="collection-item">Dr. Herbert Baier-Saip (lead)</li>
                    <li class="collection-item">Christoph Wick (Calamari)</li>
                    <li class="collection-item">Björn Eyselein (Distribution via Docker)</li>
                    <li class="collection-item">Kevin Chadbourne</li>
                    <li class="collection-item">Yannik Herbst</li>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="col l12 s12">
                <h5><b>Ideas and feedback</b></h5>
            </div>
            <div class="col l12 s12">
                <ul class="collection">
                    <li class="collection-item">Prof. Dr. Frank Puppe</li>
                    <li class="collection-item">Jonathan Gaede (OCR4all Wiki)</li>
                    <li class="collection-item">Raphaelle Jung (guides and artwork)</li>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="col l12 s12">
                <h5><b>Former Project Members</b></h5>
            </div>
            <div class="col l12 s12">
                <ul class="collection">
                    <li class="collection-item">Nico Balbach (LAREX and OCR4all)</li>
                    <li class="collection-item">Dennis Christ (OCR4all)</li>
                    <li class="collection-item">Alexander Hartelt (OCR4all)</li>
                    <li class="collection-item">Andreas Büttner (nashi)</li>
                    <li class="collection-item">Dr. Uwe Springmann (ideas and feedback)</li>
                    <li class="collection-item">Christine Grundig (ideas and feedback)</li>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="col l12 s12">
                <h5><b>Funding</b></h5>
            </div>
            <div class="col l12 s12">
                Developed during the BMPF project <b>"Kallimachos"</b> at the <b>Chair of Artificial Intelligence and Applied Computer Science (Prof. Dr. Frank Puppe)</b> in collaboration with the <b>Center for Philology and Digitality "Kallimachos"</b> at the University of Würzburg
            </div>
        </div>
        <div class="row">
            <div class="col l12 s12">
                <h6>Version = <span id="version_number"></span></h6>
            </div>
        </div>
    </div>

</t:body>
</t:html>
