package de.uniwue.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RegionExtractorController {
    @RequestMapping("/regionExtraction")
    public ModelAndView showPreprocessing(HttpSession session) {
        ModelAndView mv = new ModelAndView("regionExtraction");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }

    /** Response to the request to execute the region extraction of the specified images
     * 
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/regionExtraction/execute", method = RequestMethod.POST)
    public @ResponseBody void executeDispeckling(
                @RequestParam("pageIds[]") String[] pageIds,
                @RequestParam("spacing") int spacing,
                @RequestParam("usespacing") boolean usespacing,
                @RequestParam("avgbackground") boolean avgbackground,
                HttpSession session, HttpServletResponse response
            ) {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty())
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
