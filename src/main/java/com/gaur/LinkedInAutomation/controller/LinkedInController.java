package com.gaur.LinkedInAutomation.controller;


import org.springframework.web.bind.annotation.*;
import com.gaur.LinkedInAutomation.service.LinkedInScraper;

import java.util.List;

@RestController
@RequestMapping("/linkedin")
public class LinkedInController {

    private final LinkedInScraper linkedInScraper;

    public LinkedInController(LinkedInScraper linkedInScraper) {
        this.linkedInScraper = linkedInScraper;
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        linkedInScraper.loginToLinkedIn(email, password);
        return "Login successful!";
    }

    @GetMapping("/recruiters")
    public List<String> getRecruiters() {
        return linkedInScraper.getRecruitersFromConnections();
    }

    @PostMapping("/shutdown")
    public String closeDriver() {
        linkedInScraper.closeDriver();
        return "Selenium driver closed!";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}

