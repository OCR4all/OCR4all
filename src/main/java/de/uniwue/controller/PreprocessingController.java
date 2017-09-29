package de.uniwue.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
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
     * @param response Response to the request
     * @return Returns the content of the /Preprocessing page with the specific pageId
     */
    @RequestMapping("/Preprocessing")
    public ModelAndView showPreprocessing(
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        ModelAndView mv = new ModelAndView("preprocessing");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }

    /**
     * Response to the request to return the preprocessing status and output information
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return
     */
    @RequestMapping(value = "/ajax/preprocessing/execute", method = RequestMethod.POST)
    public @ResponseBody void executePreprocessing(
           @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
           HttpSession session, HttpServletResponse response
           ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");

        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        List<String> args;
        if (cmdArgs == null) {
           args = new ArrayList<String>();
        }
        else {
            args = Arrays.asList(cmdArgs);
        }

        PreprocessingHelper preproHelper = new PreprocessingHelper(projectDir);
        session.setAttribute("preproHelper", preproHelper);
        try {
            preproHelper.preprocessAllPages(args);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Response to the request to cancel the preprocessing
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return
     */
    @RequestMapping(value = "/ajax/preprocessing/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancelPreprocessing(
           HttpSession session, HttpServletResponse response
           ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");

        if (projectDir == null || projectDir.isEmpty())
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        PreprocessingHelper preproHelper = (PreprocessingHelper) session.getAttribute("preproHelper");
        preproHelper.cancelPreprocessAllPages();
    }

    /**
     * Response to the request to return the progress status of the preprocess service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return
     */
    @RequestMapping(value = "/ajax/preprocessing/progress" , method = RequestMethod.GET)
    public @ResponseBody int jsonProgress( 
                HttpSession session, HttpServletResponse response
            ) throws IOException {

        if (session.getAttribute("preproHelper") == null)
            return -1;

        PreprocessingHelper preproHelper = (PreprocessingHelper) session.getAttribute("preproHelper");
        return preproHelper.getProgress();
    }

    /**
     * Response to the request to return the commandline output of the preprocess service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return
     */
    @RequestMapping(value = "/ajax/preprocessing/console" , method = RequestMethod.GET)
    public @ResponseBody String jsonConsole( 
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String cmdOutput = "";
        if (session.getAttribute("preproHelper") != null) {
            PreprocessingHelper preproHelper = (PreprocessingHelper) session.getAttribute("preproHelper");
            InputStream input = new SequenceInputStream(Collections.enumeration(preproHelper.getStreams()));
            cmdOutput = IOUtils.toString(input, "UTF-8");
        }
        return cmdOutput;
    }

}
