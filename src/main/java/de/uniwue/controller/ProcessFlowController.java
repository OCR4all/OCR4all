package de.uniwue.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.controller.PreprocessingController;
import de.uniwue.helper.ProcessFlowHelper;

/**
 * Controller class for pages of process flow module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class ProcessFlowController {
    /**
     * Response to the request to send the content of the /ProcessFlow page
     *
     * @param session Session of the user
     * @return Returns the content of the /ProcessFlow page
     */
    @RequestMapping("/ProcessFlow")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("processFlow");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }

    /**
     * Helper function to execute the Preprocessing process via its Controller
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for the process
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doPreprocessing(String[] pageIds, String[] cmdArgs, HttpSession session, HttpServletResponse response) {
        if (pageIds.length == 0)
            return;

        PreprocessingController pc = new PreprocessingController();
        pc.execute(pageIds, cmdArgs, session, response);
    }

    /**
     * Helper function to execute the Despeckling process via its Controller
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doDespeckling(String[] pageIds, double maxContourRemovalSize, HttpSession session, HttpServletResponse response) {
        if (pageIds.length == 0)
            return;

        DespecklingController dc = new DespecklingController();
        dc.execute(pageIds, maxContourRemovalSize, session, response);
    }

    /**
     * Helper function to execute the Segmentation process via its Controller
     *
     * @param segmentationImageType Type of the images (binary,despeckled)
     * @param replace If true, replaces the existing image files
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doSegmentation(String[] pageIds, String segmentationImageType, boolean replace, HttpSession session, HttpServletResponse response) {
        SegmentationController sc = new SegmentationController();
        sc.execute(pageIds, segmentationImageType, replace, session, response);
    }

    /**
     * Helper function to execute the RegionExtraction process via its Controller
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param spacing
     * @param useSpacing
     * @param avgBackground
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doRegionExtraction(
                String[] pageIds, int spacing, boolean useSpacing, boolean avgBackground,
                HttpSession session, HttpServletResponse response
            ) {
        if (pageIds.length == 0)
            return;

        RegionExtractionController rec = new RegionExtractionController();
        rec.execute(pageIds, spacing, useSpacing, avgBackground, session, response);
    }

    /**
     * Helper function to execute the LineSegmentation process via its Controller
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for the process
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doLineSegmentation(String[] pageIds, String[] cmdArgs, HttpSession session, HttpServletResponse response) {
        if (pageIds.length == 0)
            return;

        LineSegmentationController lsc = new LineSegmentationController();
        lsc.execute(pageIds, cmdArgs, session, response);
    }

    /**
     * Helper function to execute the Recognition process via its Controller
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for the process
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doRecognition(String[] pageIds, String[] cmdArgs, HttpSession session, HttpServletResponse response) {
        if (pageIds.length == 0)
            return;

        RecognitionController rc = new RecognitionController();
        rc.execute(pageIds, cmdArgs, session, response);
    }

    /**
     * Response to the request to execute the processflow
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param processesToExecute[] Holds the names of the processes that should be executed
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/processFlow/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam("processesToExecute[]") String[] processesToExecute,
               HttpSession session, HttpServletResponse response
           ) {
        // Check that necessary session variables are set
        String projectDir = (String) session.getAttribute("projectDir");
        String imageType  = (String) session.getAttribute("imageType");
        if (projectDir == null || projectDir.isEmpty() || imageType == null || imageType.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        /*
         * Execute all processes consecutively
         * Check via response status if a process fails and exit
         * Store current executing process in session as point of reference for JSP
         * Determine the results after each process and use them in the next one
         */
        List<String> processes = Arrays.asList(processesToExecute);
        ProcessFlowHelper processFlowHelper = new ProcessFlowHelper(projectDir, imageType);

        if (processes.contains("preprocessing")) {
            session.setAttribute("currentProcess", "preprocessing");
            doPreprocessing(pageIds, new String[0], session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("despeckling")) {
            session.setAttribute("currentProcess", "despeckling");
            pageIds = processFlowHelper.getValidPageIds(pageIds, "preprocessing");
            doDespeckling(pageIds, 100.0, session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("segmentation")) {
            session.setAttribute("currentProcess", "segmentation");
            pageIds = processFlowHelper.getValidPageIds(pageIds, "despeckling");
            doSegmentation(pageIds, "Despeckled", true, session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("regionExtraction")) {
            session.setAttribute("currentProcess", "regionExtraction");
            pageIds = processFlowHelper.getValidPageIds(pageIds, "segmentation");
            doRegionExtraction(pageIds, 10, true, false, session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("lineSegmentation")) {
            session.setAttribute("currentProcess", "lineSegmentation");
            pageIds = processFlowHelper.getValidPageIds(pageIds, "regionExtraction");
            String[] lsCmdArgs = new String[]{ "--nocheck", "--usegauss", "--csminheight", "100000" };
            doLineSegmentation(pageIds, lsCmdArgs, session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("recognition")) {
            session.setAttribute("currentProcess", "recognition");
            pageIds = processFlowHelper.getValidPageIds(pageIds, "lineSegmentation");
            String[] rCmdArgs = new String[]{ "--nocheck" };
            doRecognition(pageIds, rCmdArgs, session, response);
            if (response.getStatus() != 200)
                return;
        }

        session.setAttribute("currentProcess", "");
    }

    /**
     * Get the process that is currently executed in the process flow
     *
     * @param session Session of the user
     * @return Currently executed process name
     */
    @RequestMapping(value = "/ajax/processFlow/current", method = RequestMethod.GET)
    public @ResponseBody String currentProcess(HttpSession session) {
        String currentProcess = (String) session.getAttribute("currentProcess");
        if (currentProcess == null) {
            return "";
        }

        return currentProcess;
    }
}
