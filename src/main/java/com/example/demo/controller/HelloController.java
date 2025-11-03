package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "🚀 Hello from Maven CI/CD app deployed on AWS!";
    }

    @GetMapping("/health")
    public String health() {
        return "✅ Application is running fine!";
    }
}
