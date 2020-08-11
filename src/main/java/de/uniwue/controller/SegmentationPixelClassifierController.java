package de.uniwue.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.uniwue.helper.RecognitionHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.SegmentationPixelClassifierHelper;

/**
 * Controller class for pages of segmentation Pixel classifier module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationPixelClassifierController {

    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public SegmentationPixelClassifierHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        SegmentationPixelClassifierHelper segmentationPixelClassifierHelper = (SegmentationPixelClassifierHelper) session.getAttribute("segmentationPixelClassifier");
        if (segmentationPixelClassifierHelper == null) {
            segmentationPixelClassifierHelper = new SegmentationPixelClassifierHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString()
            );
            session.setAttribute("segmentationPixelClassifier", segmentationPixelClassifierHelper);
        }
        return segmentationPixelClassifierHelper;
    }

    /**
     * Response to the request to send the content of the /SegmentationPixelClassifier page
     *
     * @param session Session of the user
     * @return Returns the content of the /SegmentationPixelClassifier page
     */
    @RequestMapping("SegmentationPixelClassifier")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("segmentationPixelClassifier");

        SegmentationPixelClassifierHelper segmentationPixelClassifierHelper = provideHelper(session, response);
        if(segmentationPixelClassifierHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to execute the process
     *
     * @param pageIds Ids of specified pages
     * @param cmdArgs Additional parameters for the pixel classifier
     * @param segmentationImageType Type of the images (binary,despeckled)
     * @param session Session of the user
     * @param response Response to the request
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @param modelId Model for the pixelclassifier to use. See {@link SegmentationPixelClassifierHelper#listModels()} for format.
     */
    @RequestMapping(value = "/ajax/segmentationPixelClassifier/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "cmdArgs[]") Optional<String[]> cmdArgs,
               @RequestParam("imageType") String segmentationImageType,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow,
               @RequestParam(value = "model") Optional<String> modelId
           ) {
        SegmentationPixelClassifierHelper segmentationPixelClassifierHelper = provideHelper(session, response);
        if (segmentationPixelClassifierHelper == null)
            return;

        List<String> cmdArgList = cmdArgs.map(Arrays::asList).orElseGet(ArrayList::new);

        int conflictType = segmentationPixelClassifierHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "segmentationPixelClassifier");
        try {
            segmentationPixelClassifierHelper.execute(
                    Arrays.asList(pageIds), cmdArgList, segmentationImageType, modelId.orElse("default")
            );
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            segmentationPixelClassifierHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "segmentationPixelClassifier");
    }

    /**
     * Response to the request to return the progress status of the segmentation Pixel Classifier service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentationPixelClassifier/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        SegmentationPixelClassifierHelper segmentationPixelClassifierHelper = provideHelper(session, response);
        if (segmentationPixelClassifierHelper == null)
            return -1;

        return segmentationPixelClassifierHelper.getProgress();
    }

    /**
     * Response to the request to cancel the segmentation Pixel Classifier process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationPixelClassifier/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        SegmentationPixelClassifierHelper segmentationPixelClassifierHelper = provideHelper(session, response);
        if (segmentationPixelClassifierHelper == null)
            return;

        segmentationPixelClassifierHelper.cancelProcess();
    }

    /**
     * Response to the request to return the output of the recognition process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/segmentationPixelClassifier/console" , produces = "text/plain;charset=UTF-8", method = RequestMethod.GET)
    public @ResponseBody String console(
            @RequestParam("streamType") String streamType,
            HttpSession session, HttpServletResponse response
    ) {
        SegmentationPixelClassifierHelper helper = provideHelper(session, response);
        if (helper == null)
            return "";

        if (streamType.equals("err"))
            return helper.getProcessHandler().getConsoleErr();
        return helper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to list the models
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/segmentationPixelClassifier/listModels" , method = RequestMethod.GET)
    public @ResponseBody
    TreeMap<String, String> listModels(HttpSession session, HttpServletResponse response) {
        try {
            return SegmentationPixelClassifierHelper.listModels();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return null;
        }
    }

}
