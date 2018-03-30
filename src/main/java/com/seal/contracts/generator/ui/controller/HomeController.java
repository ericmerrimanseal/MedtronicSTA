package com.seal.contracts.generator.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by root on 17.08.15..
 */
@Controller
public class HomeController {
    @RequestMapping("/home")
    public String index(Model model) {
        model.addAttribute("title", "Welcome | Contract Bundles Generator");
        return "index";
    }
}
