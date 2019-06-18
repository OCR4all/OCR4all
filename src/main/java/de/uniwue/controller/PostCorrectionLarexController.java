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
public class PostCorrectionLarexController {
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
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("segmentationLarexHelper", segmentationLarexHelper);
        }
        return segmentationLarexHelper;
    }

    /**
     * Response to the request to send the content of the /SegmentationLarex page
     *
     * @param session Session of the user
     * @return Returns the content of the /PostCorrectionLarex page
     */
    @RequestMapping("/PostCorrectionLarex")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("postCorrectionLarex");

        SegmentationLarexHelper segmentationLarexHelper = provideHelper(session, response);
        if (segmentationLarexHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }
}
