package de.uniwue.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

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
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public RecognitionHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        RecognitionHelper recognitionHelper = (RecognitionHelper) session.getAttribute("recognitionHelper");
        if (recognitionHelper == null) {
            recognitionHelper = new RecognitionHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("recognitionHelper", recognitionHelper);
        }
        return recognitionHelper;
    }

    /**
     * Response to the request to send the content of the /Recognition page
     *
     * @param session Session of the user
     * @return Returns the content of the /Recognition page
     */
    @RequestMapping("/Recognition")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("recognition");

        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
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
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/recognition/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
           ) {
        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null)
            return;
        List<String> cmdArgList = new ArrayList<String>();
        if (cmdArgs != null)
            cmdArgList = Arrays.asList(cmdArgs);

        int conflictType = recognitionHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "recognition");
        try {
            recognitionHelper.execute(Arrays.asList(pageIds), cmdArgList);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            recognitionHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "recognition");
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
        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null)
            return "";

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
        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null)
            return;

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
        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null)
            return -1;

        try {
            return recognitionHelper.getProgress();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
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
        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null)
            return null;

        try {
            return recognitionHelper.getValidPageIds();
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
    @RequestMapping(value = "/ajax/recognition/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        RecognitionHelper recognitionHelper = provideHelper(session, response);
        if (recognitionHelper == null)
            return false;

        return recognitionHelper.doOldFilesExist(pageIds);
    }

    /**
     * Response to list the models
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/recognition/listModels" , method = RequestMethod.GET)
    public @ResponseBody TreeMap<String, String> listModels(HttpSession session, HttpServletResponse response) {
        try {
            return RecognitionHelper.listModels();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return null;
        }
    }
}
