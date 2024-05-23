package com.ems.buildorg.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.stereotype.Component;
@Data
@Component
public class FilterModel {
    @JsonProperty("SearchString")
    String SearchString ;

    @JsonProperty("PageIndex")
    int PageIndex;

    @JsonProperty("PageSize")
    int PageSize;

    @JsonProperty("sortBy")
    String sortBy;
}
