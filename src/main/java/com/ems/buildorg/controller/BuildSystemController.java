package com.ems.buildorg.controller;

import com.ems.buildorg.modal.OrganizationDetail;
import com.ems.buildorg.modal.ResponseModel;
import com.ems.buildorg.service.BuildSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/create")
public class BuildSystemController {
    @Autowired
    BuildSystemService buildSystemService;

    @PostMapping("/new_organization")
    public ResponseEntity<?> buildNewOrganization(@RequestBody OrganizationDetail organizationDetail) throws IOException {
        String msg = buildSystemService.buildNewOrganizationService(organizationDetail);
        return ResponseEntity.ok(ResponseModel.builder().message(msg).build());
    }
}
