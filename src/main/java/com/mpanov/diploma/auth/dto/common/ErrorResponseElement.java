package com.mpanov.diploma.auth.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponseElement {
    private String errorMessage;
    private String errorType;
    private String errorClass;
}
