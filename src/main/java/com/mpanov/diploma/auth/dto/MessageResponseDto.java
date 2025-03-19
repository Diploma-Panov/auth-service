package com.mpanov.diploma.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponseDto {

    private String message;

    public static MessageResponseDto success() {
        return new MessageResponseDto("SUCCESS");
    }

    public static MessageResponseDto failure() {
        return new MessageResponseDto("FAILURE");
    }

}
