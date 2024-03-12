package com.ems.buildorg.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistrationDetail {
    @JsonProperty("OrganizationName")
    String organizationName;
    @JsonProperty("CompanyName")
    String companyName;
    @JsonProperty("Mobile")
    String mobile;
    @JsonProperty("EmailId")
    String emailId;
    @JsonProperty("FirstName")
    String firstName;
    @JsonProperty("LastName")
    String lastName;
    @JsonProperty("Password")
    String password;
    @JsonProperty("Country")
    String country;
    @JsonProperty("State")
    String state;
    @JsonProperty("City")
    String city;
    @JsonProperty("FirstAddress")
    String firstAddress;
    @JsonProperty("SecondAddress")
    String secondAddress;
    @JsonProperty("ThirdAddress")
    String thirdAddress;
    @JsonProperty("ForthAddress")
    String forthAddress;
    @JsonProperty("gSTNo")
    String gSTNo;
    @JsonProperty("DeclarationStartMonth")
    int declarationStartMonth;
    @JsonProperty("DeclarationEndMonth")
    int declarationEndMonth;
    @JsonProperty("FinancialYear")
    int financialYear;
    @JsonProperty("AttendanceSubmissionLimit")
    int attendanceSubmissionLimit;
}
