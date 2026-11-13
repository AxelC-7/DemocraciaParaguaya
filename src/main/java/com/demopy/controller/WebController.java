package com.demopy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // Redirige cualquier acceso a la raíz (/) directamente al index.html estático
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}