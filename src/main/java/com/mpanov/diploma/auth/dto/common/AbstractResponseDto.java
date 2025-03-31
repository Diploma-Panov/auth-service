package com.mpanov.diploma.auth.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AbstractResponseDto<T> {

    private String payloadType;

    private T payload;

    public AbstractResponseDto(T payload) {
        this.payloadType = payload.getClass().getSimpleName();
        this.payload = payload;
    }
}
