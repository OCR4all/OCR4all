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

import de.uniwue.helper.ResultGenerationHelper;

/**
 * Controller class for pages of result generation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class ResultGenerationController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public ResultGenerationHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        ResultGenerationHelper resultGenerationHelper = (ResultGenerationHelper) session.getAttribute("resultGenerationHelper");
        if (resultGenerationHelper == null) {
            resultGenerationHelper = new ResultGenerationHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("resultGenerationHelper", resultGenerationHelper);
        }
        return resultGenerationHelper;
    }

    /**
     * Response to the request to send the content of the /ResultGeneration page
     *
     * @param session Session of the user
     * @return Returns the content of the /ResultGeneration page
     */
    @RequestMapping("/ResultGeneration")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("resultGeneration");

        ResultGenerationHelper resultGenerationHelper = provideHelper(session, response);
        if (resultGenerationHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the result generation status and output information
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for result generation process
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/resultGeneration/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "resultType", required = true) String resultType,
               HttpSession session, HttpServletResponse response
           ) {
        ResultGenerationHelper resultGenerationHelper = provideHelper(session, response);
        if (resultGenerationHelper == null)
            return;

        int conflictType = resultGenerationHelper.getConflictType(GenericController.getProcessList(session));
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "result");
        try {
            resultGenerationHelper.executeProcess(Arrays.asList(pageIds), resultType);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resultGenerationHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "result");
    }

    /**
     * Response to the request to cancel the result generation process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/resultGeneration/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        ResultGenerationHelper resultGenerationHelper = provideHelper(session, response);
        if (resultGenerationHelper == null)
            return;

        resultGenerationHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the result generation service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/resultGeneration/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        ResultGenerationHelper resultGenerationHelper = provideHelper(session, response);
        if (resultGenerationHelper == null)
            return -1;

        return resultGenerationHelper.getProgress();
    }

    /**
     * Response to the request to return all pageIds that can be used for result generation process
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return List of valid pageIds
     */
    @RequestMapping(value = "/ajax/resultGeneration/getValidPageIds" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getValidPageIds(HttpSession session, HttpServletResponse response) {
        ResultGenerationHelper resultGenerationHelper = provideHelper(session, response);
        if (resultGenerationHelper == null)
            return null;

        try {
            return resultGenerationHelper.getValidPageIds();
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
     * @param resultType Type of the result, which should be checked (xml, txt) 
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/resultGeneration/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
                @RequestParam("pageIds[]") String[] pageIds,
                @RequestParam(value = "resultType", required = true) String resultType,
                HttpSession session, HttpServletResponse response
            ) {
        ResultGenerationHelper resultGenerationHelper = provideHelper(session, response);
        if (resultGenerationHelper == null)
            return false;

        return resultGenerationHelper.doOldFilesExist(pageIds, resultType);
    }
}
