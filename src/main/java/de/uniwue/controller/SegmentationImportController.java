package de.uniwue.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.uniwue.helper.SegmentationImportHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.SegmentationImportHelper;

/**
 * Controller class for pages of segmentation import module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationImportController {

    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public SegmentationImportHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        SegmentationImportHelper segmentationImportHelper = (SegmentationImportHelper) session.getAttribute("segmentationImportHelper");
        if (segmentationImportHelper == null) {
            segmentationImportHelper = new SegmentationImportHelper(
                    session.getAttribute("projectDir").toString()
            );
            session.setAttribute("segmentationImportHelper", segmentationImportHelper);
        }
        return segmentationImportHelper;
    }

    /**
     * Response to the request to send the content of the /SegmentationImport page
     *
     * @param session Session of the user
     * @return Returns the content of the /SegmentationImport page
     */
    @RequestMapping("/SegmentationImport")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("segmentationImport");

        SegmentationImportHelper segmentationImportHelper = provideHelper(session, response);
        if(segmentationImportHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to execute the process
     *
     * @param sourcePath path to source file
     * @param outputPath path to output file
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationImport/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
            @RequestParam("sourcePath") String sourcePath,
            @RequestParam("outputPath") String outputPath,
            HttpSession session, HttpServletResponse response
    ) {
        SegmentationImportHelper segmentationImportHelper = provideHelper(session, response);
        if (segmentationImportHelper == null)
            return;
        try {
            segmentationImportHelper.execute(sourcePath,outputPath);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            segmentationImportHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "segmentationImport");
    }

    /**
     * Response to the request to return the progress status of the segmentation import service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentationImport/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        SegmentationImportHelper segmentationImportHelper = provideHelper(session, response);
        if (segmentationImportHelper == null)
            return -1;

        return segmentationImportHelper.getProgress();
    }

    /**
     * Response to the request to cancel the segmentation import process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationImport/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        SegmentationImportHelper segmentationImportHelper = provideHelper(session, response);
        if (segmentationImportHelper == null)
            return;

        segmentationImportHelper.cancelProcess();
    }

}
