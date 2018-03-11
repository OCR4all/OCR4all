package de.uniwue.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.controller.PreprocessingController;
import de.uniwue.helper.OverviewHelper;
import de.uniwue.model.PageOverview;

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
     * Helper function to execute the Preprocessing process via PreprocessingController
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for preprocessing process
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doPreprocessing(String[] pageIds, String[] cmdArgs, HttpSession session, HttpServletResponse response) {
        PreprocessingController pc = new PreprocessingController();
        pc.execute(new String[0], new String[0], session, response);
    }

    /**
     * Helper function to execute the Despeckling process via DespecklingController
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @param projectDir Absolute path to the project
     * @param imageType Project type (Binary or Gray)
     * @param session Session of the user
     * @param response Response to the request
     */
    public void doDespeckling(
                String[] pageIds, double maxContourRemovalSize,
                String projectDir, String imageType, HttpSession session, HttpServletResponse response
            ) {
        // Needed to determine process results
        OverviewHelper overviewHelper = new OverviewHelper(projectDir, imageType);

        // Get overview of pages including their preprocessing status
        try {
            for (String pageId : pageIds)
                overviewHelper.initialize(pageId, false);
            overviewHelper.checkPreprocessed();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // Find correctly preprocessed pages (only these can be despeckled)
        Set<String> preprocessedPages = new TreeSet<String>();
        Map<String, PageOverview> overview = overviewHelper.getOverview();
        for(Entry<String, PageOverview> pageInfo: overview.entrySet()) {
            if (pageInfo.getValue().isPreprocessed())
                preprocessedPages.add(pageInfo.getValue().getPageId());
        }
        String[] preprocessedPagesArr = preprocessedPages.toArray(new String[preprocessedPages.size()]);

        DespecklingController dc = new DespecklingController();
        dc.execute(preprocessedPagesArr, maxContourRemovalSize, session, response);
    }

    /**
     * Response to the request to execute the processflow
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param processesToExecute[] Holds the names of the processes that should be executed
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/processflow/execute", method = RequestMethod.POST)
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
         * Determine the results after each process and use them in the next one
         */
        List<String> processes = Arrays.asList(processesToExecute);

        if (processes.contains("preprocessing")) {
            doPreprocessing(pageIds, new String[0], session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("despeckling")) {
            doDespeckling(pageIds, 100.0, projectDir, imageType, session, response);
            if (response.getStatus() != 200)
                return;
        }

        //TODO: Create helper functions for remaining processes
        /*
        if (processes.contains("segmentation")) {
            SegmentationController sc = new SegmentationController();
            sc.execute("", true, session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("regionextraction")) {
            RegionExtractionController rec = new RegionExtractionController();
            rec.execute(new String[0], 0, true, true, session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("linesegmentation")) {
            LineSegmentationController lsc = new LineSegmentationController();
            lsc.execute(new String[0], new String[0], session, response);
            if (response.getStatus() != 200)
                return;
        }

        if (processes.contains("recognition")) {
            RecognitionController rc = new RecognitionController();
            rc.execute(new String[0], new String[0], session, response);
            if (response.getStatus() != 200)
                return;
        }
        */
    }
}
