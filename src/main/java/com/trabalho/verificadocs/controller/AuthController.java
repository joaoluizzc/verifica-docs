package com.trabalho.verificadocs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String home() {
        return "redirect:/documentos";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
