package com.hemovia.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BloodBagExpiredException extends RuntimeException {

    public BloodBagExpiredException() {
        super("Blood bag expiration date must be in the future");
    }
}