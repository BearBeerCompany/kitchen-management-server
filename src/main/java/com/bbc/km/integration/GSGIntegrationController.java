package com.bbc.km.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/gsg"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class GSGIntegrationController {

    private GSGIntegrationService service;

    public GSGIntegrationController(GSGIntegrationService service) {
        this.service = service;
    }

    @PostMapping("/init")
    public ResponseEntity<GSGIntegrationResult> gsgInit() {
        return ResponseEntity.ok(service.init());
    }

}
