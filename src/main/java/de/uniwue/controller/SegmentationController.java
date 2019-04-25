package de.uniwue.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uniwue.helper.SegmentationHelper;

/**
 * Controller class for pages of segmentation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public SegmentationHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        SegmentationHelper segmentationHelper = (SegmentationHelper) session.getAttribute("segmentationHelper");
        if (segmentationHelper == null) {
            segmentationHelper = new SegmentationHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("segmentationHelper", segmentationHelper);
        }
        return segmentationHelper;
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/segmentation/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        SegmentationHelper segmentationHelper = provideHelper(session, response);
        if (segmentationHelper == null)
            return false;

        return segmentationHelper.doOldFilesExist(pageIds);
    }
}
