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

import de.uniwue.helper.PreprocessingHelper;

/**
 * Controller class for pages of preprocessing module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class PreprocessingController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public PreprocessingHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null) {
            preprocessingHelper = new PreprocessingHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("preprocessingHelper", preprocessingHelper);
        }
        return preprocessingHelper;
    }

    /**
     * Response to the request to send the content of the /Preprocessing page
     *
     * @param session Session of the user
     * @return Returns the content of the /Preprocessing page
     */
    @RequestMapping("/Preprocessing")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("preprocessing");

        PreprocessingHelper preprocessingHelper = provideHelper(session, response);
        if (preprocessingHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the preprocessing status and output information
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for preprocessing process
     * @param session Session of the user
     * @param response Response to the request
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/preprocessing/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
           ) {
        PreprocessingHelper preprocessingHelper = provideHelper(session, response);
        if (preprocessingHelper == null)
            return;

        List<String> cmdArgList = new ArrayList<String>();
        if (cmdArgs != null)
            cmdArgList = Arrays.asList(cmdArgs);

        int conflictType = preprocessingHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "preprocessing");
        try {
            preprocessingHelper.execute(Arrays.asList(pageIds), cmdArgList);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            preprocessingHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "preprocessing");
    }

    /**
     * Response to the request to cancel the preprocessing
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/preprocessing/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        PreprocessingHelper preprocessingHelper = provideHelper(session, response);
        if (preprocessingHelper == null)
            return;

        preprocessingHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the preprocess service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/preprocessing/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        PreprocessingHelper preprocessingHelper = provideHelper(session, response);
        if (preprocessingHelper == null)
            return -1;

        return preprocessingHelper.getProgress();
    }

    /**
     * Response to the request to return the output of the preprocessing process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/preprocessing/console" , method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
        PreprocessingHelper preprocessingHelper = provideHelper(session, response);
        if (preprocessingHelper == null)
            return "";

        if (streamType.equals("err"))
            return preprocessingHelper.getProcessHandler().getConsoleErr();
        return preprocessingHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/preprocessing/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        PreprocessingHelper preprocessingHelper = provideHelper(session, response);
        if (preprocessingHelper == null)
            return false;

        return preprocessingHelper.doOldFilesExist(pageIds);
    }
}
