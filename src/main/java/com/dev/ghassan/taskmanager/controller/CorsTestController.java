package com.dev.ghassan.taskmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cors-test")
public class CorsTestController {

    @GetMapping("/simple")
    public ResponseEntity<Map<String, String>> simpleRequest() {
        return ResponseEntity.ok(Map.of(
            "message", "Simple CORS request successful",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @PostMapping("/preflight")
    public ResponseEntity<Map<String, String>> preflightRequest(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(Map.of(
            "message", "Preflight CORS request successful",
            "received", data.toString(),
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @PutMapping("/credentials")
    public ResponseEntity<Map<String, String>> credentialsRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(Map.of(
            "message", "CORS request with credentials successful",
            "hasAuth", String.valueOf(authHeader != null),
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}