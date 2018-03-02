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

import de.uniwue.helper.LineSegmentationHelper;

/**
 * Controller class for line segmentation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class LineSegmentationController {
	@RequestMapping("/lineSegmentation")
	public ModelAndView showPreprocessing(HttpSession session) {
		ModelAndView mv = new ModelAndView("lineSegmentation");
	    String projectDir = (String)session.getAttribute("projectDir");
	    if (projectDir == null) {
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
 */
@RequestMapping(value = "/ajax/lineSegmentation/execute", method = RequestMethod.POST)
public @ResponseBody void executelineSegmentation(
           @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
           @RequestParam("pageIds[]") String[] pageIds,
           HttpSession session, HttpServletResponse response
       ) {
    String projectDir = (String) session.getAttribute("projectDir");
    if (projectDir == null || projectDir.isEmpty()) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }

    List<String> cmdArgList = new ArrayList<String>();
    if (cmdArgs != null)
        cmdArgList = Arrays.asList(cmdArgs);

    LineSegmentationHelper lineSegmentationHelper = new LineSegmentationHelper(projectDir);
    session.setAttribute("lineSegmentationHelper", lineSegmentationHelper);
    try {
    	lineSegmentationHelper.LineSegmentPages(Arrays.asList(pageIds), cmdArgList);

    } catch (IOException e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
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
public @ResponseBody String jsonConsole(
            @RequestParam("streamType") String streamType,
            HttpSession session, HttpServletResponse response
        ) {
	LineSegmentationHelper lineSegmentationHelper = (LineSegmentationHelper) session.getAttribute("lineSegmentationHelper");
    if (lineSegmentationHelper == null) {
        return "";
    }
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
public @ResponseBody void cancelPreprocessing(HttpSession session, HttpServletResponse response) {
    String projectDir = (String) session.getAttribute("projectDir");
    if (projectDir == null || projectDir.isEmpty()) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }

    LineSegmentationHelper lineSegmentation = (LineSegmentationHelper) session.getAttribute("lineSegmentationHelper");
    if (lineSegmentation == null) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }

    lineSegmentation.getProcessHandler().stopProcess();
}

/**
 * Response to the request to return the progress status of the lineSegmentation service
 *
 * @param session Session of the user
 * @param response Response to the request
 * @return Current progress (range: 0 - 100)
 */
@RequestMapping(value = "/ajax/lineSegmentation/progress" , method = RequestMethod.GET)
public @ResponseBody int jsonProgress(HttpSession session, HttpServletResponse response) {
	LineSegmentationHelper lineSegmentation = (LineSegmentationHelper) session.getAttribute("lineSegmentationHelper");
    if (lineSegmentation == null) {
        return -1;
    }
    int progress = -1;
    try {
    	progress = 	lineSegmentation.getProgress();
	} catch (IOException e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
	return progress;

 }
}
