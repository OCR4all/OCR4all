package de.uniwue.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.LineSegmentationDirectoryHelper;
import de.uniwue.helper.LineSegmentationHelper;
import de.uniwue.helper.LineSegmentationPageXMLHelper;

/**
 * Controller class for line segmentation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class LineSegmentationController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public LineSegmentationHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        String processingMode = session.getAttribute("processingMode").toString();
        
        // Keep a single helper object in session
        LineSegmentationHelper lineSegmentationHelper = (LineSegmentationHelper) session.getAttribute("lineSegmentationHelper");
        if (lineSegmentationHelper == null || 
        		(processingMode.equals("Directory") && lineSegmentationHelper instanceof LineSegmentationPageXMLHelper) ||
        		(processingMode.equals("Pagexml") && lineSegmentationHelper instanceof LineSegmentationDirectoryHelper)) {

        	// Select correct lineSegmentHelper for processingMode
        	if(processingMode.equals("Directory")){
				lineSegmentationHelper = new LineSegmentationDirectoryHelper(
					session.getAttribute("projectDir").toString(),
					session.getAttribute("imageType").toString(),
					processingMode
				);
        	} else if(processingMode.equals("Pagexml")) {
				lineSegmentationHelper = new LineSegmentationPageXMLHelper(
					session.getAttribute("projectDir").toString(),
					session.getAttribute("imageType").toString(),
					processingMode
				);
        	} else {
        		throw new IllegalArgumentException(String.format("Unknown processingMode %s", processingMode));
        	}
            session.setAttribute("lineSegmentationHelper", lineSegmentationHelper);
        }
        return lineSegmentationHelper;
    }

    /**
     * Response to the request to send the content of the /LineSegmentation page
     *
     * @param session Session of the user
     * @return Returns the content of the /LineSegmentation page
     */
    @RequestMapping("/LineSegmentation")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("lineSegmentation");

        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to execute the lineSegmentation script
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for the line segmentation process
     * @param session Session of the user
     * @param response Response to the request
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/lineSegmentation/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
           ) {

        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null)
            return;

        int conflictType = lineSegmentationHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "lineSegmentation");
        try {
            List<String> cmdArgList = new ArrayList<String>();
            if (cmdArgs != null)
                cmdArgList = Arrays.asList(cmdArgs);

            lineSegmentationHelper.execute(Arrays.asList(pageIds), cmdArgList);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            lineSegmentationHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "lineSegmentation");
    }

    /**
     * Response to the request to return the output of the lineSegmentation process
     * 
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/lineSegmentation/console" , method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null)
            return "";

        if (streamType.equals("err"))
            return lineSegmentationHelper.getProcessHandler().getConsoleErr();
        return lineSegmentationHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to cancel the lineSegmentation process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/lineSegmentation/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null)
            return;

        lineSegmentationHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the lineSegmentation service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/lineSegmentation/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null)
            return -1;

        try {
            return lineSegmentationHelper.getProgress();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Response to the request to return all pageIds that can be used for line segmentation
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return List of valid pageIds
     */
    @RequestMapping(value = "/ajax/lineSegmentation/getValidPageIds" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getValidPageIds(HttpSession session, HttpServletResponse response) {
        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null)
            return null;

        try {
            return lineSegmentationHelper.getValidPageIds();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/lineSegmentation/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response 
            ) {
        LineSegmentationHelper lineSegmentationHelper = provideHelper(session, response);
        if (lineSegmentationHelper == null)
            return false;

        return lineSegmentationHelper.doOldFilesExist(pageIds);
    }
}
