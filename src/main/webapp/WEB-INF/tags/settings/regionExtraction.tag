<%@ tag description="Region Extraction settings" pageEncoding="UTF-8" %>
<table class="compact">
    <tbody>
        <tr>
            <td><p>Spacing</p></td>
            <td>
                <div class="input-field">
                    <input id="spacing" type="number" value="10" />
                    <label for="spacing" data-type="int" data-error="Has to be integer" >Default: 10 </label>
                </div>
            </td>
        </tr>
        <tr>
            <td><p>Number of parallel threads for program execution</p></td>
            <td>
                <div class="input-field">
                    <input id="regionExtraction--parallel" data-setting="--parallel" type="number"/>
                    <label for="regionExtraction--parallel" data-type="int" data-error="Has to be integer">Default: 1 | Current: Available threats (Int value)</label>
                </div>
            </td>
        </tr>
        <tr>
            <td><p>Max angle for skew estimation (degrees)</p></td>
            <td>
                <div class="input-field">
                    <input id="regionExtraction--maxskew" data-setting="--maxskew" type="number" value="2"/>
                    <label for="regionExtraction--maxskew" data-type="int" data-error="Has to be integer">Default: 2 | Current: Max angle (Int value)</label>
                </div>
            </td>
        </tr>
        <tr>
            <td><p>Steps per angle in skew estimation (degrees)</p></td>
            <td>
                <div class="input-field">
                    <input id="regionExtraction--skewsteps" data-setting="--skewsteps" type="number"  value="8"/>
                    <label for="regionExtraction--skewsteps" data-type="int" data-error="Has to be integer">Default: 8 | Current: Steps per angle (Int value)</label>
                </div>
            </td>
        </tr>
    </tbody>
</table>
