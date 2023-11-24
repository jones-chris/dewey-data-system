package com.deweydatasystem.controller.health_check;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class HealthCheckController {

    public final static String MESSAGE = "I am healthy";

    @GetMapping("/api/v1/health")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok(MESSAGE);
    }

}
