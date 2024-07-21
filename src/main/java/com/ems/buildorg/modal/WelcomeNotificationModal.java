package com.ems.buildorg.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WelcomeNotificationModal {
    @JsonProperty("RecipientName")
    String RecipientName;

    @JsonProperty("Email")
    String Email;

    @JsonProperty("Password")
    String Password;

    @JsonProperty("CompanyName")
    String CompanyName;

    @JsonProperty("OrgCode")
    String OrgCode;

    @JsonProperty("Code")
    String Code;
}
