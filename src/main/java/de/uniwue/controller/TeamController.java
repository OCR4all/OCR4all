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
    /**
     * Gets OCR4all from System Environment
     * @return
     */
    @RequestMapping(value ="ajax/team/sysenv" , method = RequestMethod.GET)
    public @ResponseBody String getSys(
            HttpSession session, HttpServletResponse response
    ) {
        String sysEnvStr = System.getenv("OCR4ALL_VERSION");
        if(sysEnvStr.equals("")) {
            return "UNKNOWN";
        } else {
            return sysEnvStr;
        }
    }
}