package de.uniwue.controller;

import de.uniwue.helper.SegmentationKrakenHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Controller class for pages of segmentation kraken module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class SegmentationKrakenController {

    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public SegmentationKrakenHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (!GenericController.isSessionValid(session, response))
            return null;

        // Keep a single helper object in session
        SegmentationKrakenHelper segmentationKrakenHelper = (SegmentationKrakenHelper) session.getAttribute("segmentationKrakenHelper");
        if (segmentationKrakenHelper == null) {
            segmentationKrakenHelper = new SegmentationKrakenHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString()
            );
            session.setAttribute("segmentationKrakenHelper", segmentationKrakenHelper);
        }
        return segmentationKrakenHelper;
    }

    /**
     * Response to the request to send the content of the /SegmentationKraken page
     *
     * @param session Session of the user
     * @return Returns the content of the /SegmentationKraken page
     */
    @RequestMapping("/SegmentationKraken")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("segmentationKraken");

        SegmentationKrakenHelper segmentationKrakenHelper = provideHelper(session, response);
        if(segmentationKrakenHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the output of the lineSegmentation process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/segmentationKraken/console" , method = RequestMethod.GET)
    public @ResponseBody String console(
            @RequestParam("streamType") String streamType,
            HttpSession session, HttpServletResponse response
    ) {
        SegmentationKrakenHelper segmentationKrakenHelper = provideHelper(session, response);
        if (segmentationKrakenHelper == null)
            return "";

        if (streamType.equals("err"))
            return segmentationKrakenHelper.getProcessHandler().getConsoleErr();
        return segmentationKrakenHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to execute the process
     *
     * @param pageIds Ids of specified pages
     * @param segmentationImageType Type of the images (binary,despeckled)
     * @param session Session of the user
     * @param response Response to the request
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/segmentationKraken/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam("imageType") String segmentationImageType,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
           ) {
        SegmentationKrakenHelper segmentationKrakenHelper = provideHelper(session, response);
        if (segmentationKrakenHelper == null)
            return;

        int conflictType = segmentationKrakenHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "segmentationKraken");
        try {
            segmentationKrakenHelper.execute(Arrays.asList(pageIds), segmentationImageType);
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            segmentationKrakenHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "segmentationKraken");
    }

    /**
     * Response to the request to return the progress status of the kraken segmentation service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/segmentationKraken/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) throws IOException {
        SegmentationKrakenHelper segmentationKrakenHelper = provideHelper(session, response);
        if (segmentationKrakenHelper == null)
            return -1;

        return segmentationKrakenHelper.getProgress();
    }

    /**
     * Response to the request to cancel the kraken segmentation process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/segmentationKraken/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        SegmentationKrakenHelper segmentationKrakenHelper = provideHelper(session, response);
        if (segmentationKrakenHelper == null)
            return;

        segmentationKrakenHelper.cancelProcess();
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/segmentationKraken/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
            @RequestParam("pageIds[]") String[] pageIds,
            HttpSession session, HttpServletResponse response
    ) {
        SegmentationKrakenHelper segmentationKrakenHelper = provideHelper(session, response);
        if (segmentationKrakenHelper == null)
            return false;

        return segmentationKrakenHelper.doOldFilesExist(pageIds);
    }

}
