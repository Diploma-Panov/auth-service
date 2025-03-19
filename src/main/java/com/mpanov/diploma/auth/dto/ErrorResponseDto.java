package com.mpanov.diploma.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponseDto {
    private List<ErrorResponseElement> errors = new ArrayList<>();

    public ErrorResponseDto(Exception exception, ServiceErrorType errorType, Boolean hideMessage) {
        this.errors = List.of(
                new ErrorResponseElement(
                        exception.getMessage(),
                        errorType.toString(),
                        exception.getClass().getSimpleName()
                )
        );
    }
}
