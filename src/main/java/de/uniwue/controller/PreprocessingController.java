package de.uniwue.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @RequestMapping(value = "/ajax/preprocessing/execute", method = RequestMethod.GET)
    public @ResponseBody void executePreprocessing(
           @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
           HttpSession session, HttpServletResponse response
           ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");
        
        List<String> args;
        if (cmdArgs == null)
           args = new ArrayList<String>();
        else
            args = Arrays.asList(cmdArgs);
        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        PreprocessingHelper preproHelper = new PreprocessingHelper(projectDir);
        session.setAttribute("preproHelper", preproHelper);
        preproHelper.preprocessAllPages(args);

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
        PreprocessingHelper preproHelper = (PreprocessingHelper) session.getAttribute("preproHelper");
        InputStream input = new SequenceInputStream(Collections.enumeration(preproHelper.getStreams()));
        Reader reader = new InputStreamReader(input);
        BufferedReader r = new BufferedReader(reader);
        String cmdOutput = "";
        while (r.readLine() != null) {
            cmdOutput = cmdOutput +r.readLine();
        }
        r.close();
        return cmdOutput;
    }

}
