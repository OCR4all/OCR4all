package de.uniwue.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.PostCorrectionHelper;

/**
 * Controller class for pages of Post Correction module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class PostCorrectionController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public PostCorrectionHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        PostCorrectionHelper postCorrectionHelper = (PostCorrectionHelper) session.getAttribute("postCorrectionHelper");
        if (postCorrectionHelper == null) {
            postCorrectionHelper = new PostCorrectionHelper(
                session.getAttribute("projectDir").toString()
            );
            session.setAttribute("postCorrectionHelper", postCorrectionHelper);
        }
        return postCorrectionHelper;
    }

    /**
     * Response to the request to send the content of the /PostCorrection page
     *
     * @param session Session of the user
     * @return Returns the content of the /PostCorrection page
     */
    @RequestMapping("/PostCorrection")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("postCorrection");

        PostCorrectionHelper postCorrectionHelper = provideHelper(session, response);
        if (postCorrectionHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the postcorrection status and output information
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/postCorrection/execute", method = RequestMethod.POST)
    public @ResponseBody void execute( HttpSession session, HttpServletResponse response) {
        PostCorrectionHelper postCorrectionHelper = provideHelper(session, response);
        if (postCorrectionHelper == null)
            return;

        try {
            postCorrectionHelper.execute(session.getAttribute("projectName").toString());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            postCorrectionHelper.resetProgress();
            e.printStackTrace();
        }
    }

    /**
     * Response to the request to cancel the postcorrection
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/postCorrection/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        PostCorrectionHelper postCorrectionHelper = provideHelper(session, response);
        if (postCorrectionHelper == null)
            return;

        postCorrectionHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the postcorrection service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/postCorrection/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        PostCorrectionHelper postCorrectionHelper = provideHelper(session, response);
        if (postCorrectionHelper == null)
            return -1;

        return postCorrectionHelper.getProgress();
    }

    /**
     * Response to the request to return the output of the postcorrection process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/postCorrection/console" , method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
        PostCorrectionHelper postCorrectionHelper = provideHelper(session, response);
        if (postCorrectionHelper == null)
            return "";

        if (streamType.equals("err"))
            return postCorrectionHelper.getProcessHandler().getConsoleErr();
        return postCorrectionHelper.getProcessHandler().getConsoleOut();
    }
}
