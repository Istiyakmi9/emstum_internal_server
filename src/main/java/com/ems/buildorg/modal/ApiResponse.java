package com.ems.buildorg.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiResponse {
    @JsonProperty("ResponseBody")
    String responseBody;

    @JsonProperty("HttpStatusCode")
    int httpStatusCode;

    @JsonProperty("HttpStatusMessage")
    String httpStatusMessage;

    @JsonProperty("AuthenticationToken")
    String authenticationToken;
}
