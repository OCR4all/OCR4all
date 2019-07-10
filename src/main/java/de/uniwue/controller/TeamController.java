package de.uniwue.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * Controller class for pages of evaluation module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class TeamController {

    /**
     * Response to the request to send the content of the /Team page
     * @return Returns the content of the /Evaluation page
     */
    @RequestMapping("/Team")
    public ModelAndView show(HttpSession session, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("team");
        return mv;
    }
}