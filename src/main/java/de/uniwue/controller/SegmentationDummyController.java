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

import de.uniwue.helper.SegmentationDummyHelper;

/**
 * Controller class for pages of segmentation dummy module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationDummyController {
    /**
     * Response to the request to send the content of the /SegmentationDummy page
     *
     * @param session Session of the user
     * @return Returns the content of the /SegmentationDummy page
     */
    @RequestMapping("/SegmentationDummy")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("segmentationDummy");

        if(!GenericController.checkSession(session, response)) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        setHelperSession(session);
        return mv;
    }

    /**
     * Response to the request to execute the process
     *
     * @param pageIds Ids of specified pages
     * @param imageType Type of the images (binary,despeckled)
     * @param replace If true, replaces the existing image files
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationDummy/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam("imageType") String segmentationImageType,
               HttpSession session, HttpServletResponse response
           ) {
        if(!GenericController.checkSession(session, response)) 
            return;
        SegmentationDummyHelper segmentationDummyHelper = setHelperSession(session);

        if (segmentationDummyHelper.isSegmentationRunning() == true) {
            response.setStatus(530); //530 = Custom: Process still running
            return;
        }

        try {
        	segmentationDummyHelper.extractXmlFiles(Arrays.asList(pageIds), segmentationImageType);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            segmentationDummyHelper.resetProgress();
        }
    }

    /**
     * Response to the request to return the progress status of the segmentation dummy service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentationDummy/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session) {
        SegmentationDummyHelper segmentationDummyHelper = (SegmentationDummyHelper) session.getAttribute("segmentationDummyHelper");
        if (segmentationDummyHelper == null)
            return -1;

        return segmentationDummyHelper.getProgress();
    }

    /**
     * Response to the request to cancel the segmentation dummy process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationDummy/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        SegmentationDummyHelper segmentationDummyHelper = (SegmentationDummyHelper) session.getAttribute("segmentationDummyHelper");
        if (segmentationDummyHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        segmentationDummyHelper.cancelProcess();
    }

    /**
     * Creates the helper object and puts it in the session of the user
     * @param session Session of the user
     * @return Returns the helper object of the process
     */
    public SegmentationDummyHelper setHelperSession(HttpSession session) {
        SegmentationDummyHelper segmentationDummyHelper = (SegmentationDummyHelper) session.getAttribute("segmentationDummyHelper");
        if (segmentationDummyHelper == null) {
            segmentationDummyHelper = new SegmentationDummyHelper(session.getAttribute("projectDir").toString(), session.getAttribute("imageType").toString());
            session.setAttribute("segmentationDummyHelper", segmentationDummyHelper);
            }
        return segmentationDummyHelper;
    }
}
