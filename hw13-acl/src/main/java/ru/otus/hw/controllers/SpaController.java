package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = { "/", "/login", "/{path:[^\\.]*}", "/{path:^(?!api).*$}/**" })
    public String forward() {
        return "forward:/index.html";
    }
}
