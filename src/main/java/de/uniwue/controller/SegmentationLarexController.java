package de.uniwue.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.SegmentationLarexHelper;

/**
 * Controller class for pages of segmentation larex module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationLarexController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public SegmentationLarexHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        SegmentationLarexHelper segmentationLarexHelper = (SegmentationLarexHelper) session.getAttribute("segmentationLarexHelper");
        if (segmentationLarexHelper == null) {
            segmentationLarexHelper = new SegmentationLarexHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString()
            );
            session.setAttribute("segmentationLarexHelper", segmentationLarexHelper);
        }
        return segmentationLarexHelper;
    }

    /**
     * Response to the request to send the content of the /SegmentationLarex page
     *
     * @param session Session of the user
     * @return Returns the content of the /SegmentationLarex page
     */
    @RequestMapping("/SegmentationLarex")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("segmentationLarex");

        SegmentationLarexHelper segmentationLarexHelper = provideHelper(session, response);
        if (segmentationLarexHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to copy the XML files
     *
     * @param pageIds Ids of specified pages
     * @param imageType Type of the images (binary,despeckled)
     * @param replace If true, replaces the existing image files
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationLarex/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam("imageType") String segmentationImageType,
               HttpSession session, HttpServletResponse response
           ) {
        SegmentationLarexHelper segmentationLarexHelper = provideHelper(session, response);
        if (segmentationLarexHelper == null)
            return;

        if (segmentationLarexHelper.isSegmentationRunning() == true) {
            response.setStatus(530); //530 = Custom: Process still running
            return;
        }

        try {
            segmentationLarexHelper.moveExtractedSegments(Arrays.asList(pageIds), segmentationImageType);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            segmentationLarexHelper.resetProgress();
        }
    }

    /**
     * Response to the request to return the progress status of the segmentation larex service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentationLarex/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        SegmentationLarexHelper segmentationLarexHelper = provideHelper(session, response);
        if (segmentationLarexHelper == null)
            return -1;

        return segmentationLarexHelper.getProgress();
    }

    /**
     * Response to the request to cancel the segmentation larex copy process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationLarex/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        SegmentationLarexHelper segmentationLarexHelper = provideHelper(session, response);
        if (segmentationLarexHelper == null)
            return;

        segmentationLarexHelper.cancelProcess();
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/segmentationLarex/exists" , method = RequestMethod.GET)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        SegmentationLarexHelper segmentationLarexHelper = provideHelper(session, response);
        if (segmentationLarexHelper == null)
            return false;

        return segmentationLarexHelper.doOldFilesExist(pageIds);
    }
}
