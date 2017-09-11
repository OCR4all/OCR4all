package de.uniwue.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.OverviewHelper;
import de.uniwue.model.PageOverview;

@Controller
public class OverviewController {
    @RequestMapping("/")
    public ModelAndView showOverview() throws IOException {
        ModelAndView mv = new ModelAndView("overview");
        return mv;
    }

    @RequestMapping("/pageOverview")
    public ModelAndView showPageOverview() throws IOException {
        ModelAndView mv = new ModelAndView("pageOverview");
        return mv;
    } 

    @RequestMapping(value = "/ajax/overview/list" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<PageOverview> jsonOverview(@RequestParam("projectDir") String projectDir, HttpSession session, HttpServletResponse response) throws IOException{
        // Store project directory in session (serves as entry point)
        session.setAttribute("projectDir", projectDir);

        OverviewHelper view = new OverviewHelper(projectDir);
        try {
            view.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(view.getOverview().values());
    }
}
