package de.uniwue.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.ResultHelper;

/**
 * Controller class for pages of result module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class ResultController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public ResultHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        ResultHelper resultHelper = (ResultHelper) session.getAttribute("resultHelper");
        if (resultHelper == null) {
        	resultHelper = new ResultHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString()
            );
            session.setAttribute("resultHelper", resultHelper);
        }
        return resultHelper;
    }

    /**
     * Response to the request to send the content of the /Result page
     *
     * @param session Session of the user
     * @return Returns the content of the /Result page
     */
    @RequestMapping("/Result")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("result");

        ResultHelper resultHelper = provideHelper(session, response);
        if (resultHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the result status and output information
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for result process
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/result/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "resultType", required = true) String resultType,
               HttpSession session, HttpServletResponse response
           ) {
    	ResultHelper resultHelper = provideHelper(session, response);
        if (resultHelper == null)
            return;


        if (resultHelper.isResultRunning() == true) {
            response.setStatus(530); //530 = Custom: Process still running
            return;
        }

        try {
        	resultHelper.executeProcess(Arrays.asList(pageIds), resultType);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resultHelper.resetProgress();
        }
    }

    /**
     * Response to the request to cancel the result process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/result/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        ResultHelper resultHelper = provideHelper(session, response);
        if (resultHelper == null)
            return;

        resultHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the result service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/result/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        ResultHelper resultHelper = provideHelper(session, response);
        if (resultHelper == null)
            return -1;

        return resultHelper.getProgress();
    }

    /**
     * Response to the request to return all pageIds that can be used for result process
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return List of valid pageIds
     */
    @RequestMapping(value = "/ajax/result/getValidPageIds" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getValidPageIdsforResult(HttpSession session, HttpServletResponse response) {
        ResultHelper resultHelper = provideHelper(session, response);
        if (resultHelper == null)
            return null;

        try {
            return resultHelper.getValidPageIdsforResult();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

}
