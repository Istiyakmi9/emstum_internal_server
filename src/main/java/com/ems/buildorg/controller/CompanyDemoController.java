package com.ems.buildorg.controller;

import com.ems.buildorg.modal.FilterModel;
import com.ems.buildorg.modal.ResponseModel;
import com.ems.buildorg.service.CompanyDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trial")
public class CompanyDemoController {
    @Autowired
    CompanyDemoService companyDemoService;

    @PostMapping("/companyTrial")
    @CrossOrigin
    public ResponseEntity<?> companyTrial(@RequestBody FilterModel filterModel) throws Exception {
        var data = companyDemoService.getCompanyTrialService(filterModel);
        return ResponseEntity.ok(ResponseModel.builder().responseBody(data).build());
    }

    @GetMapping("/getCompanyTrial/{trialRequestId}")
    @CrossOrigin
    public ResponseEntity<?> companyTrial(@PathVariable("trialRequestId") Long trialRequestId) throws Exception {
        var data = companyDemoService.getCompanyTrialByIdService(trialRequestId);
        return ResponseEntity.ok(ResponseModel.builder().responseBody(data).build());
    }
}
