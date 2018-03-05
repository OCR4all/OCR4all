package de.uniwue.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.SegmentationHelper;

/**
 * Controller class for pages of segmentation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationController {
    @RequestMapping("/Segmentation")
    public ModelAndView showPreprocessing(HttpSession session) {
        ModelAndView mv = new ModelAndView("segmentation");
        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to copy the xml files
     *
     * @param imageType Type of the images (binary,despeckled)
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentation/execute", method = RequestMethod.POST)
    public @ResponseBody void executePreprocessing(
               @RequestParam("imageType") String imageType,
               HttpSession session, HttpServletResponse response
               
           ) {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        SegmentationHelper segHelper = new SegmentationHelper(projectDir);
        session.setAttribute("segHelper", segHelper);

        try {
            segHelper.MoveExtractedSegments(imageType);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
    }   

    /**
     * Response to the request to return the progress status of the segmentation service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentation/progress" , method = RequestMethod.GET)
    public @ResponseBody int jsonProgress(HttpSession session) {
        if (session.getAttribute("segHelper") == null)
            return -1;
        SegmentationHelper segHelper = (SegmentationHelper) session.getAttribute("segHelper");
        return segHelper.getProgress();
    }
}
