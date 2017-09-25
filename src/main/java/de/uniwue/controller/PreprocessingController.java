package de.uniwue.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return;
    }
}
