package com.dev.ghassan.taskmanager.controller;

import com.dev.ghassan.taskmanager.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/404")
    public String test404() {
        throw new ResourceNotFoundException("This is a test 404 error");
    }

    @GetMapping("/500")
    public String test500() {
        throw new RuntimeException("This is a test 500 error");
    }
}