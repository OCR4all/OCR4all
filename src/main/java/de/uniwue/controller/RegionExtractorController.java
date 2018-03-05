package de.uniwue.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.RegionExtractorHelper;

/**
 * Controller class for pages of region extraction module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class RegionExtractorController {
    /**
     * Response to the request to send the content of the /RegionExtraction page
     *
     * @param session Session of the user
     * @return Returns the content of the /RegionExtraction page
     */
    @RequestMapping("/RegionExtraction")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("regionExtraction");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }

    /**
     * Response to the request to execute the region extraction of the specified images
     * 
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/regionExtraction/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
                @RequestParam("pageIds[]") String[] pageIds,
                @RequestParam("spacing") int spacing,
                @RequestParam("usespacing") boolean usespacing,
                @RequestParam("avgbackground") boolean avgbackground,
                HttpSession session, HttpServletResponse response
            ) {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Keep a single helper object in session
        RegionExtractorHelper regionExtractorHelper = (RegionExtractorHelper) session.getAttribute("regionExtractorHelper");
        if (regionExtractorHelper == null) {
            regionExtractorHelper = new RegionExtractorHelper(projectDir);
            session.setAttribute("regionExtractorHelper", regionExtractorHelper);
        }

        if (regionExtractorHelper.isRegionExtractionRunning() == true) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        regionExtractorHelper.executeRegionExtraction(Arrays.asList(pageIds), spacing, usespacing, avgbackground);
    }

    /**
     * Response to the request to return the progress status of the region extraction service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/regionExtraction/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session) {
        RegionExtractorHelper regionExtractorHelper = (RegionExtractorHelper) session.getAttribute("regionExtractorHelper");
        if (regionExtractorHelper == null)
            return -1;

        regionExtractorHelper = (RegionExtractorHelper) session.getAttribute("regionExtractorHelper");
        return regionExtractorHelper.getProgress();
    }

    /**
     * Response to the request to cancel the regionExtraction process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/regionExtraction/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        RegionExtractorHelper regionExtractorHelper = (RegionExtractorHelper) session.getAttribute("regionExtractorHelper");
        if (regionExtractorHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        regionExtractorHelper.cancelProcess();
    }
}
