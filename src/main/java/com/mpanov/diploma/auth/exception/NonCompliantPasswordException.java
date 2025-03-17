package com.mpanov.diploma.auth.exception;


import lombok.Data;
import org.passay.RuleResultDetail;

import java.util.List;

@Data
public class NonCompliantPasswordException extends RuntimeException {

    private List<RuleResultDetail> details;

    public NonCompliantPasswordException(List<RuleResultDetail> details) {
        this.details = details;
    }
}
