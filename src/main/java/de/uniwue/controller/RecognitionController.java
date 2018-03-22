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

import de.uniwue.helper.RecognitionHelper;

/**
 * Controller class for pages of recognition module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class RecognitionController {
    /**
     * Response to the request to send the content of the /Recognition page
     *
     * @param session Session of the user
     * @return Returns the content of the /Recognition page
     */
    @RequestMapping("/Recognition")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("recognition");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        // Keep a single helper object in session
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null) {
            recognitionHelper = new RecognitionHelper(projectDir);
            session.setAttribute("recognitionHelper", recognitionHelper);
        }

        return mv;
    }

    /**
     * Response to the request to execute the recognition script
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for the line segmentation process
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/recognition/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
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

        // Keep a single helper object in session
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null) {
            recognitionHelper = new RecognitionHelper(projectDir);
            session.setAttribute("recognitionHelper", recognitionHelper);
        }

        if (recognitionHelper.isRecongitionRunning() == true) {
            response.setStatus(530); //530 = Custom: Process still running
            return;
        }

        try {
            recognitionHelper.RecognizeImages(Arrays.asList(pageIds), cmdArgList);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            recognitionHelper.resetProgress();
        }
    }

    /**
     * Response to the request to return the output of the recognition process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/recognition/console" , produces = "text/plain;charset=UTF-8", method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null) {
            return "";
        }

        if (streamType.equals("err"))
            return recognitionHelper.getProcessHandler().getConsoleErr();
        return recognitionHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to cancel the recognition process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/recognition/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        recognitionHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the recognition service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/recognition/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null) {
            return -1;
        }

        try {
            return recognitionHelper.getProgress();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return -1;
        }
    }

    /**
     * Response to the request to return all pageIds that can be used for recognition
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return List of valid pageIds
     */
    @RequestMapping(value = "/ajax/recognition/getValidPageIds" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getIdsforRecognition(HttpSession session, HttpServletResponse response) {
    	RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null)
            return null;

        return recognitionHelper.getValidPageIdsforRecognition();
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/recognition/exists" , method = RequestMethod.GET)
    public @ResponseBody boolean check(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        return recognitionHelper.doOldFilesExist(pageIds);
    }
}
