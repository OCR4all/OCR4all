package de.uniwue.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public SegmentationDummyHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        SegmentationDummyHelper segmentationDummyHelper = (SegmentationDummyHelper) session.getAttribute("segmentationDummyHelper");
        if (segmentationDummyHelper == null) {
            segmentationDummyHelper = new SegmentationDummyHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("segmentationDummyHelper", segmentationDummyHelper);
        }
        return segmentationDummyHelper;
    }

    /**
     * Response to the request to send the content of the /SegmentationDummy page
     *
     * @param session Session of the user
     * @return Returns the content of the /SegmentationDummy page
     */
    @RequestMapping("/SegmentationDummy")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("segmentationDummy");

        SegmentationDummyHelper segmentationDummyHelper = provideHelper(session, response);
        if(segmentationDummyHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
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
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/segmentationDummy/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam("imageType") String segmentationImageType,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
           ) {
        SegmentationDummyHelper segmentationDummyHelper = provideHelper(session, response);
        if (segmentationDummyHelper == null)
            return;

        int conflictType = segmentationDummyHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "segmentationDummy");
        try {
            segmentationDummyHelper.execute(Arrays.asList(pageIds), segmentationImageType);
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            segmentationDummyHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "segmentationDummy");
    }

    /**
     * Response to the request to return the progress status of the segmentation dummy service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentationDummy/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        SegmentationDummyHelper segmentationDummyHelper = provideHelper(session, response);
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
        SegmentationDummyHelper segmentationDummyHelper = provideHelper(session, response);
        if (segmentationDummyHelper == null)
            return;

        segmentationDummyHelper.cancelProcess();
    }

}
