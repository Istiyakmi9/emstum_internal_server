package com.ems.buildorg.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationDetail {
    @JsonProperty("OrganizationName")
    String OrganizationName;
}
