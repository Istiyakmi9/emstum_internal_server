package com.ems.buildorg.controller;

import com.ems.buildorg.modal.RegistrationDetail;
import com.ems.buildorg.modal.ResponseModel;
import com.ems.buildorg.service.BuildSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/create")
public class BuildSystemController {
    @Autowired
    BuildSystemService buildSystemService;

    @PostMapping("/new_organization")
    @CrossOrigin
    public ResponseEntity<?> buildNewOrganization(@RequestBody RegistrationDetail registrationDetail) throws IOException, URISyntaxException {
        String msg = buildSystemService.buildNewOrganizationService(registrationDetail);
        return ResponseEntity.ok(ResponseModel.builder().message(msg).build());
    }
}
