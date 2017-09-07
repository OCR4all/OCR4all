package de.uniwue.controller;

import java.io.IOException;
import java.util.Map;

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
    @RequestMapping(value = "/ajax/overview/list" , method = RequestMethod.GET)
    public @ResponseBody Map<String, PageOverview> jsonOverview(@RequestParam("projectDir") String projectDir) throws IOException{
        OverviewHelper view = new OverviewHelper(projectDir);
        view.initialize();
        //@RequestMapping automatically transforms object to json format
        return view.getOverview();
    }
}
