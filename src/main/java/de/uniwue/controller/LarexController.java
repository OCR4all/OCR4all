package de.uniwue.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.LarexHelper;

/**
 * Controller class for pages of larex module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class LarexController {
    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public LarexHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        LarexHelper larexHelper = (LarexHelper) session.getAttribute("larexHelper");
        if (larexHelper == null) {
            larexHelper = new LarexHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString(),
                session.getAttribute("processingMode").toString()
            );
            session.setAttribute("larexHelper", larexHelper);
        }
        return larexHelper;
    }

    /**
     * Response to the request to send the content of the /{mode}Larex page
     *
     * @param session Session of the user
     * @param mode Module page to display larex for
     * @return Returns the content of the /{mode}Larex page
     */
    @RequestMapping("/{mode}Larex")
    public ModelAndView show(HttpSession session, HttpServletResponse response, @PathVariable String mode) {
        ModelAndView mv = new ModelAndView("larex");
        switch(mode) {
        case "Segmentation":
        	mv.addObject("title", "Segmentation");
        	mv.addObject("modes", new String[] {"segment"});
        	mv.addObject("start_mode", "segment");
        	break;
        case "PostCorrection":
        	mv.addObject("title", "Post Correction");
        	mv.addObject("modes", new String[] {"segment","lines","text"});
        	mv.addObject("start_mode", "segment");
        	break;
        case "GroundTruthProduction":
        	mv.addObject("title", "Ground Truth Production");
        	mv.addObject("modes", new String[] {"segment","lines","text"});
        	mv.addObject("start_mode", "text");
        	break;
        	default:
        }

        LarexHelper larexHelper = provideHelper(session, response);
        if (larexHelper == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }
        return mv;
    }


    /**
     * Response to the request to return the progress status of the segmentation larex service
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/{mode}Larex/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response, @PathVariable String mode) {
        LarexHelper larexHelper = provideHelper(session, response);
        if (larexHelper == null)
            return -1;

        return larexHelper.getProgress();
    }

    /**
     * Response to the request to cancel the segmentation larex copy process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/{mode}Larex/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response, @PathVariable String mode) {
        LarexHelper larexHelper = provideHelper(session, response);
        if (larexHelper == null)
            return;

        larexHelper.cancelProcess();
    }
}
