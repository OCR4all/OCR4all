package de.uniwue.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.DespecklingHelper;

/**
 * Controller class for pages of despeckling module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class DespecklingController {
    /**
     * Response to the request to send the content of the /Despeckling page
     *
     * @param session Session of the user
     * @return Returns the content of the /Despeckling page
     */
    @RequestMapping("/Despeckling")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("despeckling");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }

    /** Response to the request to return the despeckling status and output information
     * 
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @param illustrationType Standard: the result image shows the resulting binary image
     *                         Marked: the result image shows the resulting binary image and additionally represents the removed contours
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/despeckling/execute", method = RequestMethod.POST)
    public @ResponseBody void execute(
                @RequestParam("pageIds[]") String[] pageIds,
                @RequestParam("maxContourRemovalSize") double maxContourRemovalSize,
                HttpSession session, HttpServletResponse response
            ) {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Keep a single helper object in session
        DespecklingHelper despecklingHelper = (DespecklingHelper) session.getAttribute("despecklingHelper");
        if (despecklingHelper == null) {
            despecklingHelper = new DespecklingHelper(projectDir);
            session.setAttribute("despecklingHelper", despecklingHelper);
        }

        if (despecklingHelper.isDespecklingRunning() == true) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        despecklingHelper.despeckleGivenPages(Arrays.asList(pageIds), maxContourRemovalSize);
    }

    /**
     * Response to the request to cancel the despeckling process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/despeckling/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        DespecklingHelper despecklingHelper = (DespecklingHelper) session.getAttribute("despecklingHelper");
        if (despecklingHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        despecklingHelper.cancelDespecklingProcess();
    }

    /**
     * Response to the request to return the progress status of the preprocess service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/despeckling/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session) {
        DespecklingHelper despecklingHelper = (DespecklingHelper) session.getAttribute("despecklingHelper");
        if (despecklingHelper == null)
            return -1;

        return despecklingHelper.getProgress();
    }
}
