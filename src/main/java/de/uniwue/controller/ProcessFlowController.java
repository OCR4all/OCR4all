package de.uniwue.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class for pages of process flow module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class ProcessFlowController {
    /**
     * Response to the request to send the content of the /ProcessFlow page
     *
     * @param session Session of the user
     * @return Returns the content of the /ProcessFlow page
     */
    @RequestMapping("/ProcessFlow")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("processFlow");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }
}
