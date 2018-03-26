package de.uniwue.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import de.uniwue.helper.RegionExtractionHelper;

/**
 * Controller class for pages of region extraction module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class RegionExtractionController {
    /**
     * Response to the request to send the content of the /RegionExtraction page
     *
     * @param session Session of the user
     * @return Returns the content of the /RegionExtraction page
     */
    @RequestMapping("/RegionExtraction")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("regionExtraction");

        if(!GenericController.checkSession(session, response)) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        setHelperSession(session);
        return mv;
    }

    /**
     * Response to the request to execute the region extraction of the specified images
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @param spacing
     * @param usespacing
     * @param avgbackground
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/regionExtraction/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
                @RequestParam("pageIds[]") String[] pageIds,
                @RequestParam("spacing") int spacing,
                @RequestParam("avgbackground") boolean avgbackground,
                @RequestParam("parallel") int parallel,
                HttpSession session, HttpServletResponse response
            ) {
        if(!GenericController.checkSession(session, response)) 
            return;
        RegionExtractionHelper regionExtractionHelper = setHelperSession(session);

        if (regionExtractionHelper.isRegionExtractionRunning() == true) {
            response.setStatus(530); //530 = Custom: Process still running
            return;
        }

        try {
            regionExtractionHelper.executeRegionExtraction(Arrays.asList(pageIds), spacing, avgbackground, parallel);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            regionExtractionHelper.resetProgress();
        }
    }

    /**
     * Response to the request to return the output of the region extraction process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/regionExtraction/console" , method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
    	RegionExtractionHelper regionExtractionHelper = (RegionExtractionHelper) session.getAttribute("regionExtractionHelper");
        if (regionExtractionHelper == null) {
            return "";
        }

        if (streamType.equals("err"))
            return regionExtractionHelper.getProcessHandler().getConsoleErr();
        return regionExtractionHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to return the progress status of the region extraction service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/regionExtraction/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session) {
        RegionExtractionHelper regionExtractionHelper = (RegionExtractionHelper) session.getAttribute("regionExtractionHelper");
        if (regionExtractionHelper == null)
            return -1;

        return regionExtractionHelper.getProgress();
    }

    /**
     * Response to the request to cancel the regionExtraction process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/regionExtraction/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        RegionExtractionHelper regionExtractionHelper = (RegionExtractionHelper) session.getAttribute("regionExtractionHelper");
        if (regionExtractionHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        regionExtractionHelper.cancelProcess();
    }

    /**
     * Response to the request to return all pageIds that can be used for region extraction
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return List of valid pageIds
     */
    @RequestMapping(value = "/ajax/regionExtraction/getValidPageIds" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getValidPageIdsforRegionExtractinon(HttpSession session, HttpServletResponse response) {
        RegionExtractionHelper regionExtractionHelper = (RegionExtractionHelper) session.getAttribute("regionExtractionHelper");
        if (regionExtractionHelper == null)
            return null;

        try {
            return regionExtractionHelper.getValidPageIdsforRegionExtraction();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/regionExtraction/exists" , method = RequestMethod.GET)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        if(!GenericController.checkSession(session, response)) 
            return false;
        RegionExtractionHelper regionExtractionHelper = setHelperSession(session);

        return regionExtractionHelper.doOldFilesExist(pageIds);
    }

    /**
     * Creates the helper object and puts it in the session of the user
     * @param session Session of the user
     * @return Returns the helper object of the process
     */
    public RegionExtractionHelper setHelperSession(HttpSession session) {
        RegionExtractionHelper regionExtractionHelper = (RegionExtractionHelper) session.getAttribute("regionExtractionHelper");
    if (regionExtractionHelper == null) {
        regionExtractionHelper = new RegionExtractionHelper(session.getAttribute("projectDir").toString(), session.getAttribute("imageType").toString());
        session.setAttribute("regionExtractionHelper", regionExtractionHelper);
        }
    return regionExtractionHelper;
    }
}
