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

import de.uniwue.helper.TrainingHelper;

/**
 * Controller class for pages of training module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class TrainingController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public TrainingHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        TrainingHelper trainingHelper = (TrainingHelper) session.getAttribute("trainingHelper");
        if (trainingHelper == null) {
            trainingHelper = new TrainingHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("trainingHelper", trainingHelper);
        }
        return trainingHelper;
    }

    /**
     * Response to the request to send the content of the /Training page
     *
     * @param session Session of the user
     * @return Returns the content of the /Training page
     */
    @RequestMapping("/Training")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("training");

        TrainingHelper trainingHelper = provideHelper(session, response);
        if (trainingHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }

    /**
     * Response to the request to return the training status and output information
     *
     * @param cmdArgs[] Command line arguments for training process
     * @param trainingId Custom identifier to name the training directory
     * @param session Session of the user
     * @param response Response to the request
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     */
    @RequestMapping(value = "/ajax/training/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
               @RequestParam(value = "cmdArgs[]", required = false) String[] cmdArgs,
               @RequestParam(value = "trainingId") String trainingId,
               HttpSession session, HttpServletResponse response,
               @RequestParam(value = "inProcessFlow", required = false, defaultValue = "false") boolean inProcessFlow
           ) {
        TrainingHelper trainingHelper = provideHelper(session, response);
        if (trainingHelper == null)
            return;

        List<String> cmdArgList = new ArrayList<String>();
        if (cmdArgs != null)
            cmdArgList = Arrays.asList(cmdArgs);

        int conflictType = trainingHelper.getConflictType(GenericController.getProcessList(session), inProcessFlow);
        if (GenericController.hasProcessConflict(session, response, conflictType))
            return;

        GenericController.addToProcessList(session, "training");
        try {
            trainingHelper.execute(cmdArgList, session.getAttribute("projectName").toString(), trainingId);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            trainingHelper.resetProgress();
            e.printStackTrace();
        }
        GenericController.removeFromProcessList(session, "training");
    }

    /**
     * Response to the request to cancel the training
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/training/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        TrainingHelper trainingHelper = provideHelper(session, response);
        if (trainingHelper == null)
            return;

        trainingHelper.cancelProcess();
    }

    /**
     * Response to the request to return the progress status of the training service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/training/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        TrainingHelper trainingHelper = provideHelper(session, response);
        if (trainingHelper == null)
            return -1;

        return trainingHelper.getProgress();
    }

    /**
     * Response to the request to return the output of the training process
     *
     * @param streamType Type of the console output (out | err)
     * @param session Session of the user
     * @param response Response to the request
     * @return Console output
     */
    @RequestMapping(value = "/ajax/training/console" , produces = "text/plain;charset=UTF-8", method = RequestMethod.GET)
    public @ResponseBody String console(
                @RequestParam("streamType") String streamType,
                HttpSession session, HttpServletResponse response
            ) {
        TrainingHelper trainingHelper = provideHelper(session, response);
        if (trainingHelper == null)
            return "";

        if (streamType.equals("err"))
            return trainingHelper.getProcessHandler().getConsoleErr();
        return trainingHelper.getProcessHandler().getConsoleOut();
    }

    /**
     * Response to the request to check if old process related files exist
     *
     * @param trainingId TrainingId of the model
     * @param session Session of the user
     * @param response Response to the request
     * @return Information if files exist
     */
    @RequestMapping(value = "/ajax/training/exists" , method = RequestMethod.POST)
    public @ResponseBody boolean filesExists(
               @RequestParam(value = "trainingId") String trainingId,
               HttpSession session, HttpServletResponse response
            ) {
        TrainingHelper trainingHelper = provideHelper(session, response);
        if (trainingHelper == null)
            return false;

        return trainingHelper.doOldFilesExist(session.getAttribute("projectName").toString(), trainingId);
    }
}
