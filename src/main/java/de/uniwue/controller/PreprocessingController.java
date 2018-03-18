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
     * Response to the request to send the content of the /Preprocessing page
     *
     * @param session Session of the user
     * @return Returns the content of the /Preprocessing page
     */
    @RequestMapping("/Preprocessing")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("preprocessing");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        // Keep a single helper object in session
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null) {
            preprocessingHelper = new PreprocessingHelper(projectDir);
            session.setAttribute("preprocessingHelper", preprocessingHelper);
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
     */
    @RequestMapping(value = "/ajax/preprocessing/execute", method = RequestMethod.POST)
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
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null) {
            preprocessingHelper = new PreprocessingHelper(projectDir);
            session.setAttribute("preprocessingHelper", preprocessingHelper);
        }

        if (preprocessingHelper.isPreprocessingRunning() == true) {
            response.setStatus(530); //530 = Custom: Process still running
            return;
        }

        try {
            preprocessingHelper.preprocessPages(Arrays.asList(pageIds), cmdArgList);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            preprocessingHelper.resetProgress();
        }
    }

    /**
     * Response to the request to cancel the preprocessing
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/preprocessing/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

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
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null) {
            return -1;
        }

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
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null) {
            return "";
        }

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
    @RequestMapping(value = "/ajax/preprocessing/exists" , method = RequestMethod.GET)
    public @ResponseBody boolean check(
                @RequestParam("pageIds[]") String[] pageIds,
                HttpSession session, HttpServletResponse response
            ) {
        PreprocessingHelper preprocessingHelper = (PreprocessingHelper) session.getAttribute("preprocessingHelper");
        if (preprocessingHelper == null)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        return preprocessingHelper.checkIfExisting(pageIds);
    }
}
