package com.narendra.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")  // Removed trailing slash for clarity
public class UserController {

    @GetMapping("/info")  // Corrected annotation with closing quote
    public String hostIsUp() {
        return "Host is up";
    }


}
