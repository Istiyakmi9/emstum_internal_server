package com.ems.buildorg.modal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseModel {
    String message;
    String statusCode;
    Object responseBody;
}
