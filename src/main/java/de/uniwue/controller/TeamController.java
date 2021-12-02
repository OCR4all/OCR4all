package de.uniwue.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
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
        return new ModelAndView("team");
    }
    /**
     * Gets OCR4all from System Environment
     * @return ocr4all version
     */
    @RequestMapping(value ="ajax/team/sysenv" , method = RequestMethod.GET)
    public @ResponseBody String getSys(
            HttpSession session, HttpServletResponse response
    ) {
        String ocr4all_version  = System.getenv("OCR4ALL_VERSION");
        String larex_version = System.getenv("LAREX_VERSION");
        if(ocr4all_version.equals("")) { ocr4all_version = "UNKNOWN"; }
        if(larex_version.equals("")) { larex_version = "UNKNOWN"; }
        return ocr4all_version + "\n" + larex_version;
    }
}