package de.uniwue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Overview {
    @RequestMapping("/")
    public ModelAndView showOverview() {
        ModelAndView mv = new ModelAndView("overview");
        return mv;
    }
}
