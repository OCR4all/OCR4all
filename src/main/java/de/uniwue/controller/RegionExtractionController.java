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
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public RegionExtractionHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        RegionExtractionHelper regionExtractionHelper = (RegionExtractionHelper) session.getAttribute("regionExtractionHelper");
        if (regionExtractionHelper == null) {
            regionExtractionHelper = new RegionExtractionHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("regionExtractionHelper", regionExtractionHelper);
        }
        return regionExtractionHelper;
    }

    /**
     * Response to the request to send the content of the /RegionExtraction page
     *
     * @param session Session of the user
     * @return Returns the content of the /RegionExtraction page
     */
    @RequestMapping("/RegionExtraction")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("regionExtraction");

        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
        if (regionExtractionHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
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
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/regionExtraction/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
                @RequestParam("pageIds[]") String[] pageIds,
                @RequestParam("spacing") int spacing,
                @RequestParam("maxskew") int maxskew,
                @RequestParam("skewsteps") int skewsteps,
                @RequestParam("parallel") int parallel,
                HttpSession session, HttpServletResponse response,
                @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
            ) {
        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
        if (regionExtractionHelper == null)
            return;

        int conflictType = regionExtractionHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "regionExtraction");
        try {
            regionExtractionHelper.execute(Arrays.asList(pageIds), spacing, maxskew, skewsteps, parallel);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            regionExtractionHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "regionExtraction");
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
        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
        if (regionExtractionHelper == null)
            return "";

        if (streamType.equals("err"))
            return regionExtractionHelper.getProcessHandler().getConsoleErr();
        return regionExtractionHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to return the progress status of the region extraction service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/regionExtraction/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
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
        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
        if (regionExtractionHelper == null)
            return;

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
    public @ResponseBody ArrayList<String> getValidPageIds(HttpSession session, HttpServletResponse response) {
        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
        if (regionExtractionHelper == null)
            return null;

        try {
            return regionExtractionHelper.getValidPageIds();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
    @RequestMapping(value = "/ajax/regionExtraction/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        RegionExtractionHelper regionExtractionHelper = provideHelper(session, response);
        if (regionExtractionHelper == null)
            return false;

        return regionExtractionHelper.doOldFilesExist(pageIds);
    }
}
