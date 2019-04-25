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

import de.uniwue.helper.EvaluationHelper;

/**
 * Controller class for pages of evaluation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class EvaluationController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public EvaluationHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        EvaluationHelper evaluationHelper = (EvaluationHelper) session.getAttribute("evaluationHelper");
        if (evaluationHelper == null) {
            evaluationHelper = new EvaluationHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("evaluationHelper", evaluationHelper);
        }
        return evaluationHelper;
    }

    /**
     * Response to the request to send the content of the /Evaluation page
     *
     * @param session Session of the user
     * @return Returns the content of the /Evaluation page
     */
    @RequestMapping("/Evaluation")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("evaluation");

        EvaluationHelper evaluationHelper = provideHelper(session, response);
        if (evaluationHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the evaluation status and output information
     *
     * @param pageIds[] Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs[] Command line arguments for evaluation process
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/evaluation/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam("pageIds[]") String[] pageIds,
               @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
               HttpSession session, HttpServletResponse response
           ) {
        EvaluationHelper evaluationHelper = provideHelper(session, response);
        if (evaluationHelper == null)
            return;

        List<String> cmdArgList = new ArrayList<String>();
        if (cmdArgs != null)
            cmdArgList = Arrays.asList(cmdArgs);

        int conflictType = evaluationHelper.getConflictType(GenericController.getProcessList(session));
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "evaluation");
        try {
            evaluationHelper.execute(Arrays.asList(pageIds), cmdArgList);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            evaluationHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "evaluation");
    }

    /**
     * Response to the request to cancel the evaluation process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/evaluation/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        EvaluationHelper evaluationHelper = provideHelper(session, response);
        if (evaluationHelper == null)
            return;

        evaluationHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the evaluation service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/evaluation/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        EvaluationHelper evaluationHelper = provideHelper(session, response);
        if (evaluationHelper == null)
            return -1;

        return evaluationHelper.getProgress();
    }
    /**
     * Response to the request to return the output of the evaluation process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/evaluation/console" , produces = "text/plain;charset=UTF-8" , method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
        EvaluationHelper evaluationHelper = provideHelper(session, response);
        if (evaluationHelper == null)
            return "";

        if (streamType.equals("err"))
            return evaluationHelper.getProcessHandler().getConsoleErr();
        return evaluationHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to return all pageIds that can be used for evaluation
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return List of valid pageIds
     */
    @RequestMapping(value = "/ajax/evaluation/getValidPageIds" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getValidPageIds(HttpSession session, HttpServletResponse response) {
        EvaluationHelper evaluationHelper = provideHelper(session, response);
        if (evaluationHelper == null)
            return null;

        try {
            return evaluationHelper.getValidPageIds();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return null;
        }
    }
}
