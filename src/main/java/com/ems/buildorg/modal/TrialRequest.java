package com.ems.buildorg.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TrialRequest {
    @JsonProperty("TrailRequestId")
    Long trailRequestId;

    @JsonProperty("FullName")
    String fullName;

    @JsonProperty("Email")
    String email;

    @JsonProperty("CompanyName")
    String companyName;

    @JsonProperty("OrganizationName")
    String OrganizationName;

    @JsonProperty("PhoneNumber")
    String phoneNumber;

    @JsonProperty("HeadCount")
    Long headCount;

    @JsonProperty("Country")
    String country;

    @JsonProperty("State")
    String state;

    @JsonProperty("City")
    String city;

    @JsonProperty("FullAddress")
    String fullAddress;

    @JsonProperty("Index")
    int index;

    @JsonProperty("Total")
    int total;

}
