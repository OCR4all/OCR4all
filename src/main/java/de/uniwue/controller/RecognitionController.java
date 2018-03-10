package de.uniwue.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
@Controller
public class RecognitionController {
    /**
     * Response to the request to send the content of the /Recognition page
     *
     * @param session Session of the user
     * @return Returns the content of the /Recognition page
     */
    @RequestMapping("/Recognition")
    public ModelAndView show(HttpSession session) {
        ModelAndView mv = new ModelAndView("recognition");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }
}
